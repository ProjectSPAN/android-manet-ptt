package org.span.ptt.recorder;

import org.span.ptt.channels.Channel;
import org.span.ptt.settings.AudioSettings;
import org.span.ptt.util.AudioParams;
import org.span.ptt.util.PhoneIPs;

import ro.ui.pttdroid.codecs.Speex;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;

public abstract class Recorder implements Runnable {
	
	protected final int SO_TIMEOUT = 0;

	protected Channel channel = null;
	
	protected AudioRecord recorder;
	/*
	 * True if thread is running, false otherwise.
	 * This boolean is used for internal synchronization.
	 */
	protected boolean isRunning = false;	
	/*
	 * True if thread is safely stopped.
	 * This boolean must be false in order to be able to start the thread.
	 * After changing it to true the thread is finished, without the ability to start it again.
	 */
	protected boolean isFinishing = false;
	
	protected static short[] pcmFrame = new short[AudioParams.FRAME_SIZE];
	protected static byte[] encodedFrame;
	
	public Recorder(Channel channel) {
		this.channel = channel;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof Recorder) {
			Recorder recorder = (Recorder) other;
			if (recorder.channel.equals(recorder.channel)) {
				return true;
			}
		}
		return false;
	}
	
	public void run() {
		if (!channel.isRecorderEnabled()) {
			return;
		}
		
		// Set audio specific thread priority
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
		
		while(!isFinishing()) {		
			init();
			while(isRunning()) {
					
				// Read PCM from the microphone buffer & encode it
				if(AudioSettings.useSpeex() == AudioSettings.USE_SPEEX) {
					recorder.read(pcmFrame, 0, AudioParams.FRAME_SIZE);
					Speex.encode(pcmFrame, encodedFrame);						
				}
				else {
					recorder.read(encodedFrame, 0, AudioParams.FRAME_SIZE_IN_BYTES);						
				}
																	
				send();
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
	
	protected void init() {				 	
		PhoneIPs.load();
		
		if(AudioSettings.useSpeex() == AudioSettings.USE_SPEEX) {
			encodedFrame = new byte[Speex.getEncodedSize(AudioSettings.getSpeexQuality())];
		} else { 
			encodedFrame = new byte[AudioParams.FRAME_SIZE_IN_BYTES];
		}

    	recorder = new AudioRecord(
    			AudioSource.MIC, 
    			AudioParams.SAMPLE_RATE, 
    			AudioFormat.CHANNEL_CONFIGURATION_MONO, 
    			AudioParams.ENCODING_PCM_NUM_BITS, 
    			AudioParams.RECORD_BUFFER_SIZE);
    	
		recorder.startRecording();				
	}
	
	protected void release() {
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
	
	protected abstract void send();
}
