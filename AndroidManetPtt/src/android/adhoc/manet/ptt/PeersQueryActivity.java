package android.adhoc.manet.ptt;

import java.util.HashSet;

import android.adhoc.manet.ptt.channels.ChannelHelper;

import android.adhoc.manet.ManetHelper;
import android.adhoc.manet.ManetObserver;
import android.adhoc.manet.routing.Node;
import android.adhoc.manet.service.ManetService.AdhocStateEnum;
import android.adhoc.manet.system.ManetConfig;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

public abstract class PeersQueryActivity extends Activity implements ManetObserver {
	
	private ManetHelper manet = null;
	
	private static Handler handler = new Handler();

	private static Runnable channelRunnable;
	private static final int CHANNEL_CHECK_PERIOD_MILLISEC = 5000;
		
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		manet = new ManetHelper(this);
	    manet.registerObserver(this);
    }
    
    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        
        if (!manet.isConnectedToService()) {
			manet.connectToService();
        }
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	
    	if (manet.isConnectedToService()) {
    		handler.removeCallbacks(channelRunnable);
    		handler.post(channelRunnable); // run now
    	}
    }
    
    @Override
    public void onPause() {
    	super.onPause();
		handler.removeCallbacks(channelRunnable);
    }
                
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
		manet.unregisterObserver(this);
		
        if (manet.isConnectedToService()) {
			manet.disconnectFromService();
        }	
    }
    
    protected abstract void updateChannelList();
       
	public void onServiceConnected() {
		// Update channel list
		channelRunnable = new Runnable() {
			
			public void run() {					
				manet.sendPeersQuery();
				handler.postDelayed(this, CHANNEL_CHECK_PERIOD_MILLISEC);
			}
		};
		
		handler.removeCallbacks(channelRunnable);
		handler.post(channelRunnable); // run now
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
		ChannelHelper.updateChannels(peers);
		updateChannelList();
	}

	public void onError(String error) {
		// TODO Auto-generated method stub
	}
}