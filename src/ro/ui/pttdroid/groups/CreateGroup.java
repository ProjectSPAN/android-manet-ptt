package ro.ui.pttdroid.groups;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import ro.ui.pttdroid.R;

import android.adhoc.manet.ManetHelper;
import android.adhoc.manet.ManetObserver;
import android.adhoc.manet.routing.Node;
import android.adhoc.manet.service.ManetService.AdhocStateEnum;
import android.adhoc.manet.system.ManetConfig;
import android.app.ListActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class CreateGroup extends ListActivity implements ManetObserver {
	
	private ManetHelper manet = null;
	
	private ListView mainListView = null;
	
    private Button btnCommit = null;
    private Button btnCancel = null;
	
    private GroupHelper groupHelper = null;
    
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);	
		
		setContentView(R.layout.creategroup);
		
		manet = new ManetHelper(this);
	    manet.registerObserver(this);
	    		
	    btnCommit = (Button) findViewById(R.id.btnCommit);
	    btnCommit.setOnClickListener(new View.OnClickListener() {
	  		public void onClick(View v) {
	  			createGroup();
	  			finish();
	  		}
		});
	    
	    btnCancel = (Button) findViewById(R.id.btnCancel);
	  	btnCancel.setOnClickListener(new View.OnClickListener() {
	  		public void onClick(View v) {
				finish();
	  		}
		});
	  	
 		mainListView = getListView();
 		
 		SharedPreferences prefs = getSharedPreferences(GroupHelper.GROUP_PREFS, MODE_PRIVATE);
 		groupHelper = new GroupHelper(prefs);
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
	
	private List<String> getSelectedItems() {
		List<String> peers = new ArrayList<String>(); // TODO: sort alphabetically

		int count = mainListView.getAdapter().getCount();

		for (int i = 0; i < count; i++) {
			if (mainListView.isItemChecked(i)) {
				peers.add((String)mainListView.getItemAtPosition(i));
			}
		}
		return peers;
	}

	private void createGroup() {
		List<String> peers = getSelectedItems();
		
		if (peers.size() == 0) {
			// show empty group dialog // TODO
			return;
		}
		
		// prompt for user name // TODO
		String name = "grouper";
		
		groupHelper.createGroup(name, peers);
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
		
		Set<String> options = new TreeSet<String>();
		String option = null;
		for (Node peer : peers) {
			if (peer.userId != null) {
				option = peer.addr + " (" + peer.userId + ")";
			} else {
				option = peer.addr;	
			}
			options.add(option);
		}
		
		// DEBUG: TEST
		options.add("moo");
		options.add("bark");
		options.add("meow");
		
 		// Bind the data with the list
		String[] lv_array = new String[options.size()]; 
		options.toArray(lv_array);
		
 		mainListView.setAdapter(new ArrayAdapter<String>(this,
 				android.R.layout.simple_list_item_multiple_choice, lv_array));

 		mainListView.setItemsCanFocus(false);
 		mainListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
	}

	public void onError(String error) {
		// TODO Auto-generated method stub
		
	}
}