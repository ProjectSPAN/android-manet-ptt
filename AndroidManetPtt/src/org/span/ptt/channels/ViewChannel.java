package org.span.ptt.channels;

import java.util.ArrayList;
import java.util.List;

import org.span.ptt.PeersQueryActivity;
import org.span.ptt.R;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class ViewChannel extends PeersQueryActivity {
	
	private ListView mainListView = null;
	private ChannelAdapter adapter = null;
	
	private Channel channel = null;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);	
		
		setContentView(R.layout.viewchannel);
		
 		mainListView = (ListView) findViewById(R.id.list);
 		mainListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long index) {				
	    		// TODO
			}
		});
	    
 		channel = ChannelHelper.getChannel();
    }
	
	protected void updateChannelList() {		
		List<Channel> myChannels = new ArrayList<Channel>();
		if (channel instanceof PeerChannel) {
 			myChannels.add(channel);
 		} else if (channel instanceof GroupChannel) {
 			GroupChannel groupChannel = (GroupChannel) channel;
 			myChannels.addAll(groupChannel.channels);
 		}
		
		adapter = new ChannelAdapter(this, R.layout.listchannelrow, myChannels);
		mainListView.setAdapter(adapter);
 		mainListView.setItemsCanFocus(false);
 		mainListView.setChoiceMode(ListView.CHOICE_MODE_NONE); // TODO
 		
 		adapter.notifyDataSetChanged(); // force redraw
	}
}