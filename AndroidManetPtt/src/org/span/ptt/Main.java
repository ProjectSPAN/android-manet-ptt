package org.span.ptt;

import java.util.List;

import org.span.ptt.channels.Channel;
import org.span.ptt.channels.ChannelAdapter;
import org.span.ptt.channels.ChannelHelper;
import org.span.ptt.channels.GroupChannel;
import org.span.ptt.channels.ListenOnlyChannel;
import org.span.ptt.channels.NullChannel;
import org.span.ptt.channels.ViewChannel;
import org.span.ptt.groups.GroupHelper;
import org.span.ptt.groups.ViewGroups;
import org.span.ptt.player.Player;
import org.span.ptt.recorder.MultiRecorder;
import org.span.ptt.recorder.Recorder;
import org.span.ptt.recorder.SingleRecorder;
import org.span.ptt.service.ConnectionService;
import org.span.ptt.settings.AudioSettings;
import org.span.ptt.settings.CommSettings;

import ro.ui.pttdroid.codecs.Speex;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

public class Main extends PeersQueryActivity {
	
	public static final int MIC_STATE_NORMAL   = 0;
	public static final int MIC_STATE_PRESSED  = 1;
	public static final int MIC_STATE_PLAYBACK = 2;
	public static final int MIC_STATE_DISABLED = 3;
	
	private static int microphoneState = MIC_STATE_NORMAL;
	
	/*
	 * True if the activity is really starting for the first time.
	 * False if the activity starts after it was previously closed by a configuration change, like screen orientation. 
	 */
	private static boolean isStarting = true;
	
	private ImageView microphoneImage = null;
	private Spinner spnChannel = null;
	
	private List<Channel> channels = null;
	private Channel channel = null;
	
	/*
	 * Threads for recording and playing audio data.
	 * This threads are stopped only if isFinishing() returns true on onDestroy(), meaning the back button was pressed.
	 * With other words, recorder and player threads will still be running if an screen orientation event occurs.
	 */	
	private static Player player = null;	
	private static Recorder recorder = null;
	
	private static Thread playerThread = null;
	private static Thread recorderThread = null;
	
	// Block recording when playing  something.
	private static Handler handler = new Handler();
	private static Runnable stateRunnable;
	private static int storedProgress = 0;	
	private static final int STATE_CHECK_PERIOD_MILLISEC = 100;
		
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
            	
    	spnChannel = (Spinner) findViewById(R.id.spnChannel);
    	spnChannel.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int position, long index) {
				Channel selection = (Channel) spnChannel.getAdapter().getItem(position);
				if (!selection.equals(channel)) {
					channel = selection;
					ChannelHelper.setCurrentChannel(channel);
					// updateChannelStatus(); // TODO?
					pttReset();
				}
			}

			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
			}
    	});
    	
    	microphoneImage = (ImageView) findViewById(R.id.microphone_image);
    	microphoneImage.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View view, MotionEvent event) {
				boolean canTalk = false;
				if (getMicrophoneState() != MIC_STATE_DISABLED) {
					if (AudioSettings.getTalkOverState() || getMicrophoneState() != MIC_STATE_PLAYBACK) { 	
						canTalk = true;
					}
				}
				if (canTalk) {
		    		switch(event.getAction()) {
			    		case MotionEvent.ACTION_DOWN: 
			    			// player.pauseAudio(); // TODO
			    			recorder.resumeAudio();
			    			setMicrophoneState(MIC_STATE_PRESSED);
			    			break;
			    		case MotionEvent.ACTION_UP:
			    			setMicrophoneState(MIC_STATE_NORMAL);
			    			// player.resumeAudio(); // TODO
			    			recorder.pauseAudio();    			
			    			break;
		    		}
		    	}
		    	return true;
			}
    	});
    	
    	GroupHelper.getSettings(this); 
    	ChannelHelper.getSettings(this);
    	
    	channel = ChannelHelper.getChannel(); // TODO
    	
   		// start service so that it runs even if no active activities are bound to it
   		startService(new Intent(this, ConnectionService.class));
		
        pttInit();
    }
    
    @Override
    public void onStart() {
    	super.onStart();
    	pttResume();
    }
    
    // activity comes to the foreground
    @Override
    public void onResume() {
    	super.onResume();
		updateChannelList();
    }
    
    @Override
    public void onStop() {
    	super.onStop();
    	pttPause();
    }
                
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	pttRelease();    	
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	getMenuInflater().inflate(R.menu.menu, menu);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	Intent i; 
    	
    	switch(item.getItemId()) {
	    	case R.id.settings_comm:
	    		i = new Intent(this, CommSettings.class);
	    		startActivityForResult(i, 0);    		
	    		return true;
	    	case R.id.settings_audio:
	    		i = new Intent(this, AudioSettings.class);
	    		startActivityForResult(i, 0);    		
	    		return true;    
	    	case R.id.settings_reset_all:
	    		return resetAllSettings();
	    	case R.id.groups:
	    		i = new Intent(this, ViewGroups.class);
	    		startActivityForResult(i, 0);
	    		return true;
	    	case R.id.channel:
	    		viewChannel();
	    	default:
	    		return super.onOptionsItemSelected(item);
    	}
    }
        
    @Override
    protected void updateChannelList() {
    	channels = ChannelHelper.getChannels();
 		
    	/*
		ArrayAdapter<Channel> adapter = 
				new ArrayAdapter<Channel>(this, android.R.layout.simple_spinner_item, channels);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		*/
    	
    	ChannelAdapter adapter = new ChannelAdapter(this, R.layout.spinnerchannelrow, channels);
		spnChannel.setAdapter(adapter);
		
		adapter.notifyDataSetChanged(); // force redraw
		
		// maintain channel selection
		if (channel == null) {
			channel = ChannelHelper.getDefaultChannel();
		}
		int index = channels.indexOf(channel);
		spnChannel.setSelection(index);
    }
    
    /**
     * Reset all settings to their default value
     * @return
     */
    private boolean resetAllSettings() {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	
    	Editor editor = prefs.edit();
    	editor.clear();
    	editor.commit();   
    	
    	Toast toast = Toast.makeText(this, getString(R.string.setting_reset_all_confirm), Toast.LENGTH_SHORT);
    	toast.setGravity(Gravity.CENTER, 0, 0);
    	toast.show();

    	return true;
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	CommSettings.getSettings(this);     	    	
    	AudioSettings.getSettings(this);    	
    }
    
    public synchronized void setMicrophoneState(int state) {
    	switch(state) {
    	case MIC_STATE_NORMAL:
    		microphoneState = MIC_STATE_NORMAL;
    		microphoneImage.setImageResource(R.drawable.microphone_normal_image);
    		break;
    	case MIC_STATE_PRESSED:
    		microphoneState = MIC_STATE_PRESSED;
    		microphoneImage.setImageResource(R.drawable.microphone_pressed_image);
    		break;
    	case MIC_STATE_PLAYBACK:
    		microphoneState = MIC_STATE_PLAYBACK;
    		microphoneImage.setImageResource(R.drawable.microphone_playback_image);
    		break; 
    	case MIC_STATE_DISABLED:
    		microphoneState = MIC_STATE_DISABLED;
    		microphoneImage.setImageResource(R.drawable.microphone_disabled_image);
    		break; 
    	}
    }
    
    public synchronized int getMicrophoneState() {
    	return microphoneState;
    }
    
    private void pttReset() {
		storedProgress = 0;
		pttPause();
		pttRelease();		
		pttInit();
		pttResume();
    }
    
    private void pttResume() {
    	// Initialize codec 
    	Speex.open(AudioSettings.getSpeexQuality());
    	
    	player.resumeAudio();
    }
    
    private void pttPause() {
    	// player.pauseAudio(); // TODO
    	recorder.pauseAudio();    	
    	
    	// Release codec resources
    	Speex.close();
    }
    
    private void pttInit() {    	
    	// When the volume keys will be pressed the audio stream volume will be changed. 
    	setVolumeControlStream(AudioManager.STREAM_MUSIC);
    	    	    	    	
    	/*
    	 * If the activity is first time created and not destroyed and created again like on an orientation screen change event.
    	 * This will be executed only once.
    	 */    	    	    	    	
    	if(isStarting) {
    		AudioSettings.getSettings(this); // TODO  	
        	
    		player = new Player(channel);    		    		     		    	
    		
    		if (channel instanceof GroupChannel) {
    			recorder = new MultiRecorder(channel);
    		} else {
    			recorder = new SingleRecorder(channel);    
    		}
    		
    		// initial mic state
    		if (channel.isRecorderEnabled()) {
    			setMicrophoneState(MIC_STATE_NORMAL);
    		} else {
    			setMicrophoneState(MIC_STATE_DISABLED);
    		}
    		
    		// Disable microphone when receiving data.
    		stateRunnable = new Runnable() {
				
				public void run() {					
					int currentProgress = player.getProgress();
					
					if(currentProgress != storedProgress) {
						if(getMicrophoneState() != MIC_STATE_PLAYBACK) {
							if (AudioSettings.getTalkOverState()) {
								if (getMicrophoneState() != MIC_STATE_PRESSED) {
									setMicrophoneState(MIC_STATE_PLAYBACK);	
								}
							} else {
								recorder.pauseAudio();
								setMicrophoneState(MIC_STATE_PLAYBACK);	
							}
						}						 							
					}
					else {
						if(getMicrophoneState() == MIC_STATE_PLAYBACK) {
							if(channel.isRecorderEnabled()) {
								setMicrophoneState(MIC_STATE_NORMAL);
							} else {
								setMicrophoneState(MIC_STATE_DISABLED);
							}
						}
					}
					
					storedProgress = currentProgress;
					handler.postDelayed(this, STATE_CHECK_PERIOD_MILLISEC);
				}
			};
			
    		handler.removeCallbacks(stateRunnable);
    		handler.postDelayed(stateRunnable, STATE_CHECK_PERIOD_MILLISEC);
    		
    		// player.start();
    		// recorder.start(); 
    		
    		playerThread = new Thread(player);
    		playerThread.start();
    		
    		recorderThread = new Thread(recorder);
    		recorderThread.start();
    		
    		isStarting = false;    		
    	}
    }
    
    private void pttRelease() {
    	if (isStarting == false) {
			handler.removeCallbacks(stateRunnable);
	
			// Force threads to finish.
			player.finish();    		    		
			recorder.finish();
			
			try {
				// player.join();
				// recorder.join();
				
				playerThread.join();
				recorderThread.join();
			}
			catch(InterruptedException e) {
				Log.d("PTT", e.toString());
			}
			player = null;
			recorder = null;
		    		
			// Resetting isStarting.
			isStarting = true;
    	}
    }
    
    private void viewChannel() {
    	Channel channel = ChannelHelper.getChannel();
		if (channel instanceof NullChannel) {
			openDialog("Silence Mode", "Will not play or transmit audio.");
		} else if (channel instanceof ListenOnlyChannel) {
			openDialog("Listen Only Mode", "Will play audio received by any peer. Will not transmit audio.");
		} else {
			Intent i = new Intent(Main.this, ViewChannel.class);
			startActivityForResult(i, 0);
		}
    }
    
	private void openDialog(String title, String message) {
		new AlertDialog.Builder(this)
        	.setTitle(title)
        	.setMessage(message)
        	.setNeutralButton("Okay", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	// nothing to do
                }
        	})
        	.show();  		
   	}
}