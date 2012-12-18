package ro.ui.pttdroid;

import java.util.HashSet;
import java.util.List;

import ro.ui.pttdroid.channels.Channel;
import ro.ui.pttdroid.channels.ChannelHelper;
import ro.ui.pttdroid.channels.GroupChannel;
import ro.ui.pttdroid.channels.PeerChannel;
import ro.ui.pttdroid.channels.ViewChannel;
import ro.ui.pttdroid.codecs.Speex;
import ro.ui.pttdroid.groups.GroupHelper;
import ro.ui.pttdroid.groups.ViewGroups;
import ro.ui.pttdroid.player.Player;
import ro.ui.pttdroid.recorder.MultiRecorder;
import ro.ui.pttdroid.recorder.Recorder;
import ro.ui.pttdroid.recorder.SingleRecorder;
import ro.ui.pttdroid.service.ConnectionService;
import ro.ui.pttdroid.settings.AudioSettings;
import ro.ui.pttdroid.settings.CommSettings;
import android.adhoc.manet.ManetHelper;
import android.adhoc.manet.ManetObserver;
import android.adhoc.manet.routing.Node;
import android.adhoc.manet.service.ManetService.AdhocStateEnum;
import android.adhoc.manet.system.ManetConfig;
import android.app.Activity;
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
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

public class Main extends Activity implements ManetObserver {
	
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
	private ImageButton btnInfo = null;
	
	private List<Channel> channels = null;
	private Channel channel = null;
	
	private ManetHelper manet = null;
	
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
	
	private static Runnable channelRunnable;
	private static final int CHANNEL_CHECK_PERIOD_MILLISEC = 5000;
		
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
					ChannelHelper.setChannel(channel);
					checkChannels();
					pttReset();
				}
			}

			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
			}
    	});
    	
    	spnChannel.setOnTouchListener(new OnTouchListener() {
    		public boolean onTouch(View view, MotionEvent event) {	
	    		switch(event.getAction()) {
		    		case MotionEvent.ACTION_DOWN:
		    			// update spinner contents here so as not to confuse the user
		    			// by removing the current channel from the list mid-use
		    			updateChannelList();
		    			break;
	    		}
		    	return false; // don't consume event so that spinner behaves normally 
			}
    	});
    	
    	btnInfo = (ImageButton) findViewById(R.id.btnInfo);
    	btnInfo.setOnClickListener(new View.OnClickListener() {
	  		public void onClick(View v) {
	    		Intent i = new Intent(Main.this, ViewChannel.class);
				// i.putExtra(GroupHelper.CHANNEL_INDEX, (int)index); // TODO
	    		startActivityForResult(i, 0);
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
    	
		manet = new ManetHelper(this);
	    manet.registerObserver(this);
    	
   		// start service so that it runs even if no active activities are bound to it
   		startService(new Intent(this, ConnectionService.class));
		
        pttInit();
    }
    
    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        
        if (!manet.isConnectedToService()) {
			manet.connectToService();
        }
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
    	
		manet.unregisterObserver(this);
		
        if (manet.isConnectedToService()) {
			manet.disconnectFromService();
        }
    	
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
	    	default:
	    		return super.onOptionsItemSelected(item);
    	}
    }
    
    private void checkChannels() {
 		channels = ChannelHelper.getChannels();
 		
 		if (channel instanceof PeerChannel) {
 			if (!channels.contains(channel)) {
 	 			channels.add(channel); // add invalid channel back in for user awareness
 	 			channel.setValid(false);
 				btnInfo.setImageResource(R.drawable.red_orb_icon);
 	 		} else {
 	 			channel.setValid(true);
 	 			btnInfo.setImageResource(R.drawable.green_orb_icon);
 	 		}
 		} else if (channel instanceof GroupChannel) {
 			// check if all, some, or none of the peers are available
 			GroupChannel groupChannel = (GroupChannel) channel;
 			int partCount = 0;
 			int fullCount = groupChannel.channels.size();
 			for (Channel channel : groupChannel.channels) {
 				if (channels.contains(channel)) {
 					partCount++;
 	 	 		} 
 			}
 			if (partCount == fullCount) { // all
 				channel.setValid(true);
 				btnInfo.setImageResource(R.drawable.green_orb_icon);
 			} else if (partCount == 0) { // none
 				channel.setValid(false);
 				btnInfo.setImageResource(R.drawable.red_orb_icon);
 			} else { // partial
 				channel.setValid(true);
 				btnInfo.setImageResource(R.drawable.orange_orb_icon);
 			}
 		} else {
 			btnInfo.setImageResource(R.drawable.green_orb_icon);
 		}
    }
    
    private void updateChannelList() {
    	checkChannels();
 		
		ArrayAdapter<Channel> adapter = 
				new ArrayAdapter<Channel>(this, android.R.layout.simple_spinner_item, channels);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnChannel.setAdapter(adapter);
		
		adapter.notifyDataSetChanged();
		
		// maintain channel selection
		if (channel == null) {
			channel = ChannelHelper.getDefaultChannel();
		}
		int index = adapter.getPosition(channel);
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
    

	public void onServiceConnected() {
		// Update channel list
		channelRunnable = new Runnable() {
			
			public void run() {					
				manet.sendPeersQuery();
				handler.postDelayed(this, CHANNEL_CHECK_PERIOD_MILLISEC);
			}
		};
		
		handler.removeCallbacks(channelRunnable);
		handler.postDelayed(channelRunnable, STATE_CHECK_PERIOD_MILLISEC);
	}

	public void onServiceDisconnected() {
		handler.removeCallbacks(channelRunnable);
	}

	public void onServiceStarted() {
		// TODO Auto-generated method stub
	}

	public void onServiceStopped() {
		// TODO Auto-generated method stub
	}

	public void onAdhocStateUpdated(AdhocStateEnum state, String info) {
		// TODO Auto-generated method stub
	}

	public void onConfigUpdated(ManetConfig manetcfg) {
		// TODO Auto-generated method stub
	}
	
	public void onRoutingInfoUpdated(String info) {
		// TODO Auto-generated method stub
	}

	public void onPeersUpdated(HashSet<Node> peers) {
		ChannelHelper.updatePeers(peers);
		checkChannels();
	}

	public void onError(String error) {
		// TODO Auto-generated method stub
	}
}