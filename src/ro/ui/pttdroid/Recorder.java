package ro.ui.pttdroid;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import ro.ui.pttdroid.channels.Channel;
import ro.ui.pttdroid.codecs.Speex;
import ro.ui.pttdroid.settings.AudioSettings;
import ro.ui.pttdroid.util.AudioParams;
import ro.ui.pttdroid.util.PhoneIPs;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;

public class Recorder extends Thread {
	
	private final int SO_TIMEOUT = 0;
	
	private AudioRecord recorder;
	/*
	 * True if thread is running, false otherwise.
	 * This boolean is used for internal synchronization.
	 */
	private boolean isRunning = false;	
	/*
	 * True if thread is safely stopped.
	 * This boolean must be false in order to be able to start the thread.
	 * After changing it to true the thread is finished, without the ability to start it again.
	 */
	private boolean isFinishing = false;
	
	private DatagramSocket socket;
	private DatagramPacket packet;
	
	private short[] pcmFrame = new short[AudioParams.FRAME_SIZE];
	private byte[] encodedFrame;
	
	private Channel channel = null;
			
	public Recorder(Channel channel) {
		this.channel = channel;
	}
	
	public void run() {
		// Set audio specific thread priority
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
		
		while(!isFinishing()) {		
			init();
			while(isRunning()) {
				
				try {		
					// Read PCM from the microphone buffer & encode it
					if(AudioSettings.useSpeex() == AudioSettings.USE_SPEEX) {
						recorder.read(pcmFrame, 0, AudioParams.FRAME_SIZE);
						Speex.encode(pcmFrame, encodedFrame);						
					}
					else {
						recorder.read(encodedFrame, 0, AudioParams.FRAME_SIZE_IN_BYTES);						
					}
																		
					// Send encoded frame packed within an UDP datagram
					if (packet.getAddress() != null) { // TODO: special channel
						socket.send(packet);
					}
				}
				catch(IOException e) {
					e.printStackTrace();
				}	
			}		
		
			release();	
			/*
			 * While is not running block the thread.
			 * By doing it, CPU time is saved.
			 */
			synchronized(this) {
				try {	
					if(!isFinishing())
						this.wait();
				}
				catch(InterruptedException e) {
					e.printStackTrace();
				}
			}					
		}							
	}
	
	private void init() {				
		try {	    	
			PhoneIPs.load();
			
			InetAddress addr = channel.addr;
			socket = new DatagramSocket();
			socket.setSoTimeout(SO_TIMEOUT);
			
			switch(channel.getCastType()) {
				case Channel.BROADCAST:
					socket.setBroadcast(true);
					break;
				case Channel.MULTICAST:
					// nothing				
					break;
				case Channel.UNICAST:
					// nothing					
					break;
			}							
			
			if(AudioSettings.useSpeex() == AudioSettings.USE_SPEEX) {
				encodedFrame = new byte[Speex.getEncodedSize(AudioSettings.getSpeexQuality())];
			} else { 
				encodedFrame = new byte[AudioParams.FRAME_SIZE_IN_BYTES];
			}
			
			packet = new DatagramPacket(
					encodedFrame, 
					encodedFrame.length, 
					addr, 
					channel.port);

	    	recorder = new AudioRecord(
	    			AudioSource.MIC, 
	    			AudioParams.SAMPLE_RATE, 
	    			AudioFormat.CHANNEL_CONFIGURATION_MONO, 
	    			AudioParams.ENCODING_PCM_NUM_BITS, 
	    			AudioParams.RECORD_BUFFER_SIZE);
	    	
			recorder.startRecording();				
		}
		catch(SocketException e) {
			e.printStackTrace();
		}	
	}
	
	private void release() {
		if(socket != null) {
			socket.close();
			socket = null;
		}
		if(recorder != null) {
			recorder.stop();
			recorder.release();
			recorder = null;
		}
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
