package org.span.ptt;

import java.util.HashSet;


import org.span.ptt.channels.ChannelHelper;
import org.span.ptt.service.ConnectionService;
import org.span.service.ManetHelper;
import org.span.service.ManetObserver;
import org.span.service.legal.EulaHelper;
import org.span.service.legal.EulaObserver;
import org.span.service.routing.Node;
import org.span.service.core.ManetService.AdhocStateEnum;
import org.span.service.system.ManetConfig;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public abstract class PeersQueryActivity extends Activity implements EulaObserver, ManetObserver {
	
	private ManetHelper manet = null;
	
	private static Handler handler = new Handler();

	private static Runnable channelRunnable;
	private static final int CHANNEL_CHECK_PERIOD_MILLISEC = 5000;
		
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		manet = new ManetHelper(this);
	    manet.registerObserver(this);
	    
        EulaHelper eula = new EulaHelper(this, this);
        eula.showDialog();
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
       
	public void onEulaAccepted() {
		// used to be part of onPostCreate()
        if (!manet.isConnectedToService()) {
			manet.connectToService();
        }
        
   		// start service so that it runs even if no active activities are bound to it
        // do nothing if the service is already running
   		startService(new Intent(this, ConnectionService.class));
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