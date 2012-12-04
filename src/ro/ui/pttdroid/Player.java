package ro.ui.pttdroid;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import ro.ui.pttdroid.channels.Channel;
import ro.ui.pttdroid.channels.ListenOnlyChannel;
import ro.ui.pttdroid.channels.NullChannel;
import ro.ui.pttdroid.codecs.Speex;
import ro.ui.pttdroid.settings.AudioSettings;
import ro.ui.pttdroid.util.AudioParams;
import ro.ui.pttdroid.util.PhoneIPs;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class Player extends Thread {
		
	private static final int SOCKET_TIMEOUT_MILLISEC = 1000;
	
	private AudioTrack track;
	private boolean isRunning = true;	
	private boolean isFinishing = false;
	
	private DatagramSocket socket;
	private MulticastSocket multicastSocket;
	private DatagramPacket packet;	
	
	private short[] pcmFrame = new short[AudioParams.FRAME_SIZE];
	private byte[] encodedFrame;
	
	private int progress = 0;
	
	private Channel channel = null;
	
	public Player(Channel channel) {
		this.channel = channel;
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
					// prevent buffer underrun, stop streaming when there's no data
					try {
						socket.setSoTimeout(SOCKET_TIMEOUT_MILLISEC);
						socket.receive(packet);	// packet contains encodedFrame
					} catch(SocketTimeoutException e) {
						track.stop();
						socket.setSoTimeout(0);
						socket.receive(packet); // block forever
					}
					
					track.play();
					
					Log.d("Player", "Packet sender: " + packet.getAddress().getHostAddress() + 
							" Packet port: " + packet.getPort() + " Packet length: " + packet.getLength()); // DEBUG: STOKER
					// Log.d("Player", "AudioTrack.getPlaybackHeadPosition(): " + track.getPlaybackHeadPosition()); // DEBUG: STOKER
					
					// If echo is turned off and I was the packet sender then skip playing
					if(AudioSettings.getEchoState() == AudioSettings.ECHO_OFF && PhoneIPs.contains(packet.getAddress())) // TODO
						continue;
					
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

			switch(channel.getCastType()) {
				case Channel.BROADCAST:
					socket = new DatagramSocket(channel.rxPort);
					socket.setBroadcast(true);
					break;
				case Channel.MULTICAST:
					multicastSocket = new MulticastSocket(channel.rxPort);
					multicastSocket.joinGroup(channel.addr);
					socket = multicastSocket;
					break;
				case Channel.UNICAST:
					socket = new DatagramSocket(channel.rxPort);
					
					// TODO: filter out packets from everyone but peer
					if (!(channel instanceof NullChannel) && !(channel instanceof ListenOnlyChannel)) {
						socket.connect(channel.addr, Channel.DEFAULT_TX_PORT);
					}
					break;
			}							
			
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
		if(track != null) {
			track.stop();		
			track.release();
		}
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
		leaveGroup();
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
	
	private void leaveGroup() {
		try {
			if (multicastSocket != null) {
				multicastSocket.leaveGroup(channel.addr);
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		catch(NullPointerException e) {
			e.printStackTrace();
		}		
	}
		
}
