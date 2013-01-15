package android.adhoc.manet.ptt.player;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import ro.ui.pttdroid.codecs.Speex;

import android.adhoc.manet.ptt.channels.Channel;
import android.adhoc.manet.ptt.channels.GroupChannel;
import android.adhoc.manet.ptt.channels.PeerChannel;
import android.adhoc.manet.ptt.settings.AudioSettings;
import android.adhoc.manet.ptt.util.AudioParams;
import android.adhoc.manet.ptt.util.PhoneIPs;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class Player implements Runnable {
	
	private static final int SOCKET_TIMEOUT_MILLISEC = 1000;
	
	private Map<InetAddress,AudioTrack> trackMap;
	private AudioTrack track = null; // current track
	
	private boolean isRunning = true;	
	private boolean isFinishing = false;
	
	private DatagramSocket socket;
	private DatagramPacket packet;	
	
	private short[] pcmFrame = new short[AudioParams.FRAME_SIZE];
	private byte[] encodedFrame;
	
	private int progress = 0;
	
	private Channel channel = null;
	
	public Player(Channel channel) {
		this.channel = channel;
		trackMap = new HashMap<InetAddress,AudioTrack>();
	}
				
	public void run() {
		if (!channel.isPlayerEnabled()) {
			return;
		}
			
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);				 
		
		while(!isFinishing()) {			
			init();
			while(isRunning()) {
								
				try {	
					// TODO: prevent double playback on "hangup"
					if (track != null) {
						track.stop();
					}
					socket.receive(packet); // block forever
					
					/*
					// prevent buffer underrun, stop streaming when there's no data
					try {
						socket.setSoTimeout(SOCKET_TIMEOUT_MILLISEC);
						socket.receive(packet);	// packet contains encodedFrame
					} catch(SocketTimeoutException e) {
						track.stop();
						socket.setSoTimeout(0);
						socket.receive(packet); // block forever
					}
					*/
					
					// TODO: check if this is a probe request
					if (packet.getLength() == 0) {
						Log.d("Player", "PROBE REQUEST!");
						continue; // drop packet on floor
					}
					
					// filter audio data to separate tracks based on sender address
					InetAddress sender = packet.getAddress();
					if (trackMap.containsKey(sender)) {
						track = trackMap.get(sender);
					} else {
						
						// filter out packets from everyone but peer(s)
						if (channel instanceof PeerChannel) {
							if (!channel.addr.getHostAddress().equals(sender.getHostAddress())) {
								continue; // drop packet on floor
							}
						} else if (channel instanceof GroupChannel) {
							GroupChannel groupChannel = (GroupChannel) channel;
							if (!groupChannel.group.containsHostAddress(sender.getHostAddress())) {
								continue; // drop packet on floor
							}
						}
						// ListenOnlyChannel doesn't filter
						
						int streamType;
						if(AudioSettings.getSpeakerState() == AudioSettings.SPEAKER_OFF) {
							streamType = AudioManager.STREAM_VOICE_CALL;
						} else {
							streamType = AudioManager.STREAM_MUSIC;
						}
						
						track = new AudioTrack(
								streamType,
								AudioParams.SAMPLE_RATE, 
								AudioFormat.CHANNEL_CONFIGURATION_MONO, 
								AudioParams.ENCODING_PCM_NUM_BITS, 
								AudioParams.TRACK_BUFFER_SIZE, 
								AudioTrack.MODE_STREAM);	
						
						trackMap.put(sender, track);
					}
					
					track.play();
					
					Log.d("Player", "Packet sender: " + packet.getAddress().getHostAddress() + 
							" Packet port: " + packet.getPort() + " Packet length: " + packet.getLength()); // DEBUG: STOKER
					// Log.d("Player", "AudioTrack.getPlaybackHeadPosition(): " + track.getPlaybackHeadPosition()); // DEBUG: STOKER
					
					// If echo is turned off and I was the packet sender then skip playing
					if(AudioSettings.getEchoState() == AudioSettings.ECHO_OFF && PhoneIPs.contains(packet.getAddress())) // TODO
						continue; // drop packet on floor
					
					// Decode audio
					if(AudioSettings.useSpeex() == AudioSettings.USE_SPEEX) {
						Speex.decode(encodedFrame, encodedFrame.length, pcmFrame);
						track.write(pcmFrame, 0, AudioParams.FRAME_SIZE);
					}
					else {			
						track.write(encodedFrame, 0, AudioParams.FRAME_SIZE_IN_BYTES);
					}	
										
					// Make some progress
					makeProgress();
				}
				catch(SocketException e) {
					// this may be expected 
					Log.w("PTT", e.getMessage());
				}
				catch(IOException e) {
					e.printStackTrace();
				}	
			}		
		
			release();	
			synchronized(this) {
				try {	
					if(!isFinishing()) {
						this.wait();
					}
				}
				catch(InterruptedException e) {
					e.printStackTrace();
				}
			}			
		}				
	}
	
	private void init() {	
		try {			
			socket = new DatagramSocket(channel.port);
						
			if(AudioSettings.useSpeex() == AudioSettings.USE_SPEEX) {
				encodedFrame = new byte[Speex.getEncodedSize(AudioSettings.getSpeexQuality())];
			} else { 
				encodedFrame = new byte[AudioParams.FRAME_SIZE_IN_BYTES];
			}
			
			packet = new DatagramPacket(encodedFrame, encodedFrame.length);			
			
			// track.play(); // STOKER			
		}
		catch(IOException e) {
			e.printStackTrace();
		}		
	}
	
	private void release() {
		for (AudioTrack track : trackMap.values()) {
			track.stop();		
			track.release();
		}
		trackMap.clear();
		track = null;
	}
	
	private synchronized void makeProgress() {
		progress++;
	}
	
	public synchronized int getProgress() {
		return progress;
	}
	
	public synchronized boolean isRunning() {
		return isRunning;
	}
	
	public synchronized void resumeAudio() {
		isRunning = true;
		this.notify();
	}
		
	public synchronized void pauseAudio() {
		isRunning = false;
		if (socket != null) {
			socket.close();
		}
	}
		
	public synchronized boolean isFinishing() {
		return isFinishing;
	}
	
	public synchronized void finish() {
		pauseAudio();
		isFinishing = true;
		this.notify();
	}	
}
