package ro.ui.pttdroid.channels;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import ro.ui.pttdroid.R;
import android.adhoc.manet.ManetHelper;
import android.adhoc.manet.ManetObserver;
import android.adhoc.manet.routing.Node;
import android.adhoc.manet.service.ManetService.AdhocStateEnum;
import android.adhoc.manet.system.ManetConfig;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class ViewChannel extends ListActivity implements ManetObserver {
	
	private ListView mainListView = null;
	
	private ManetHelper manet = null;
	
	private Channel channel = null;
	private List<Channel> channels = null;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);	
		
		setContentView(R.layout.viewchannel);
		
 		mainListView = getListView();
 		mainListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long index) {				
	    		// TODO
			}
		});
		
		manet = new ManetHelper(this);
	    manet.registerObserver(this);
	    
 		channel = ChannelHelper.getChannel();
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    	
        if (!manet.isConnectedToService()) {
			manet.connectToService();
        }
    }
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		manet.unregisterObserver(this);
		
        if (manet.isConnectedToService()) {
			manet.disconnectFromService();
        }
	}
	
	private void showChannels() {
		List<Channel> myChannels = new ArrayList<Channel>();
		if (channel instanceof PeerChannel) {
 			myChannels.add(channel);
 		} else if (channel instanceof GroupChannel) {
 			GroupChannel groupChannel = (GroupChannel) channel;
 			myChannels.addAll(groupChannel.channels);
 		}
		
		ArrayAdapter<Channel> adapter = 
				new ArrayAdapter<Channel>(this, android.R.layout.simple_list_item_1, myChannels);
		mainListView.setAdapter(adapter);
 		mainListView.setItemsCanFocus(false);
 		mainListView.setChoiceMode(ListView.CHOICE_MODE_NONE); // TODO
	}
	
	private void checkChannels() {
		channels = ChannelHelper.getChannels();
		showChannels();
		
		/*
		if (channel instanceof PeerChannel) {
 			if (!channels.contains(channel)) {
 				// btnInfo.setImageResource(R.drawable.red_orb_icon);
 	 		} else {
 	 			// btnInfo.setImageResource(R.drawable.green_orb_icon);
 	 		}
 		} else if (channel instanceof GroupChannel) {
 			// check if all, some, or none of the peers are available
 			GroupChannel groupChannel = (GroupChannel) channel;
 			for (Channel channel : groupChannel.channels) {
 				if (channels.contains(channel)) {
 					// TODO
 	 	 		} 
 			}
 		} else {
 			// TODO
 		}
 		*/
	}

	public void onServiceConnected() {
		manet.sendPeersQuery();
	}

	public void onServiceDisconnected() {
		// TODO Auto-generated method stub
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