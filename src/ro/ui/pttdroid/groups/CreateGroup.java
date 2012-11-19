package ro.ui.pttdroid.groups;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import ro.ui.pttdroid.R;

import android.adhoc.manet.ManetHelper;
import android.adhoc.manet.ManetObserver;
import android.adhoc.manet.routing.Node;
import android.adhoc.manet.service.ManetService.AdhocStateEnum;
import android.adhoc.manet.system.ManetConfig;
import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class CreateGroup extends ListActivity implements ManetObserver {
	
	private ManetHelper manet = null;
	
	private ListView mainListView = null;

	private ArrayList<String> selectedItems = new ArrayList<String>();
	
    private Button btnCommit = null;
    private Button btnCancel = null;
    
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
	  			SaveSelections();
	  		}
		});
	    
	    btnCancel = (Button) findViewById(R.id.btnCancel);
	  	btnCancel.setOnClickListener(new View.OnClickListener() {
	  		public void onClick(View v) {
	  			ClearSelections();
				finish();
	  		}
		});
	  	
 		this.mainListView = getListView();
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
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public void onStop() {
		super.onStop();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		manet.unregisterObserver(this);
		
        if (manet.isConnectedToService()) {
			manet.disconnectFromService();
        }
	}
	
	private void ClearSelections() {
		// user has clicked clear button so uncheck all the items
		int count = this.mainListView.getAdapter().getCount();

		for (int i = 0; i < count; i++) {
			this.mainListView.setItemChecked(i, false);
		}

		// also clear the saved selections
		SaveSelections();
	}

	private void LoadSelections() {
		/*
		// if the selections were previously saved load them

		SharedPreferences settingsActivity = getPreferences(MODE_PRIVATE);

		if (settingsActivity.contains(SETTING_TODOLIST)) {
			String savedItems = settingsActivity
					.getString(SETTING_TODOLIST, "");

			this.selectedItems.addAll(Arrays.asList(savedItems.split(",")));
			int count = this.mainListView.getAdapter().getCount();

			for (int i = 0; i < count; i++) {
				String currentItem = (String) this.mainListView.getAdapter()
						.getItem(i);
				if (this.selectedItems.contains(currentItem)) {
					this.mainListView.setItemChecked(i, true);
				}
			}
		}
		*/
	}

	private void SaveSelections() {
		/*
		// save the selections in the shared preference in private mode for the user

		SharedPreferences settingsActivity = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor prefEditor = settingsActivity.edit();

		String savedItems = getSavedItems();

		prefEditor.putString(SETTING_TODOLIST, savedItems);

		prefEditor.commit();
		*/
	}

	private String getSavedItems() {
		String savedItems = "";

		int count = this.mainListView.getAdapter().getCount();

		for (int i = 0; i < count; i++) {

			if (this.mainListView.isItemChecked(i)) {
				if (savedItems.length() > 0) {
					savedItems += "," + this.mainListView.getItemAtPosition(i);
				} else {
					savedItems += this.mainListView.getItemAtPosition(i);
				}
			}

		}
		return savedItems;
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
		
 		// Bind the data with the list
		String[] lv_array = new String[options.size()]; 
		options.toArray(lv_array);
		
 		mainListView.setAdapter(new ArrayAdapter<String>(this,
 				android.R.layout.simple_list_item_multiple_choice, lv_array));

 		mainListView.setItemsCanFocus(false);
 		mainListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

 		LoadSelections();
	}

	public void onError(String error) {
		// TODO Auto-generated method stub
		
	}
}