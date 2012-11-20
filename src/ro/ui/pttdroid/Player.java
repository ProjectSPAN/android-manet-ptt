package ro.ui.pttdroid;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MulticastSocket;

import ro.ui.pttdroid.codecs.Speex;
import ro.ui.pttdroid.settings.AudioSettings;
import ro.ui.pttdroid.settings.CommSettings;
import ro.ui.pttdroid.util.AudioParams;
import ro.ui.pttdroid.util.PhoneIPs;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class Player extends Thread {
		
	private AudioTrack player;
	private boolean isRunning = true;	
	private boolean isFinishing = false;	
	
	private DatagramSocket socket;
	private MulticastSocket multicastSocket;
	private DatagramPacket packet;	
	
	private short[] pcmFrame = new short[AudioParams.FRAME_SIZE];
	private byte[] encodedFrame;
	
	private int progress = 0;
				
	public void run() {
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);				 
		
		while(!isFinishing()) {			
			init();
			while(isRunning()) {
								
				try {		
					// player.stop(); // STOKER
					
					socket.receive(packet);	// packet contains encodedFrame
					
					// player.play(); // STOKER
					
					Log.d("Player", "Player packet length: " + packet.getLength()); // DEBUG: STOKER
					
					// If echo is turned off and I was the packet sender then skip playing
					if(AudioSettings.getEchoState()==AudioSettings.ECHO_OFF && PhoneIPs.contains(packet.getAddress()))
						continue;
					
					// Decode audio
					if(AudioSettings.useSpeex()==AudioSettings.USE_SPEEX) {
						Speex.decode(encodedFrame, encodedFrame.length, pcmFrame);
						player.write(pcmFrame, 0, AudioParams.FRAME_SIZE);
					}
					else {			
						player.write(encodedFrame, 0, AudioParams.FRAME_SIZE_IN_BYTES);
					}	
										
					// Make some progress
					makeProgress();
				}
				catch(IOException e) {
					Log.d("Player", e.toString());
				}	
			}		
		
			release();	
			synchronized(this) {
				try {	
					if(!isFinishing())
						this.wait();
				}
				catch(InterruptedException e) {
					Log.d("Player", e.toString());
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
			
			player = new AudioTrack(
					streamType,
					AudioParams.SAMPLE_RATE, 
					AudioFormat.CHANNEL_CONFIGURATION_MONO, 
					AudioParams.ENCODING_PCM_NUM_BITS, 
					AudioParams.TRACK_BUFFER_SIZE, 
					AudioTrack.MODE_STREAM);	

			switch(CommSettings.getCastType()) {
			case CommSettings.BROADCAST:
				socket = new DatagramSocket(CommSettings.getPort());
				socket.setBroadcast(true);
				break;
			case CommSettings.MULTICAST:
				multicastSocket = new MulticastSocket(CommSettings.getPort());
				multicastSocket.joinGroup(CommSettings.getMulticastAddr());
				socket = multicastSocket;				
				break;
			case CommSettings.UNICAST:
				socket = new DatagramSocket(CommSettings.getPort());
				break;
			}							
			
			if(AudioSettings.useSpeex()==AudioSettings.USE_SPEEX) 
				encodedFrame = new byte[Speex.getEncodedSize(AudioSettings.getSpeexQuality())];
			else 
				encodedFrame = new byte[AudioParams.FRAME_SIZE_IN_BYTES];
			
			packet = new DatagramPacket(encodedFrame, encodedFrame.length);			
			
			player.play();				
		}
		catch(IOException e) {
			Log.d("Player", e.toString());
		}		
	}
	
	private void release() {
		if(player!=null) {
			player.stop();		
			player.release();
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
		socket.close();
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
			multicastSocket.leaveGroup(CommSettings.getMulticastAddr());
		}
		catch(IOException e) {
			Log.d("Player", e.toString());
		}
		catch(NullPointerException e) {
			Log.d("Player", e.toString());
		}		
	}
		
}
