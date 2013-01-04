package android.adhoc.manet.ptt.groups;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import android.adhoc.manet.ptt.R;

import android.adhoc.manet.ManetHelper;
import android.adhoc.manet.ManetObserver;
import android.adhoc.manet.routing.Node;
import android.adhoc.manet.service.ManetService.AdhocStateEnum;
import android.adhoc.manet.system.ManetConfig;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class CreateGroup extends ListActivity implements ManetObserver {
	
	private ManetHelper manet = null;
	private ManetConfig manetcfg = null;
	
	private ListView mainListView = null;
	
    private Button btnCommit = null;
    private Button btnCancel = null;
    
    private List<String> peers = null;
    private InetAddress addr = null;
    
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
	  		}
		});
	    
	    btnCancel = (Button) findViewById(R.id.btnCancel);
	  	btnCancel.setOnClickListener(new View.OnClickListener() {
	  		public void onClick(View v) {
				finish();
	  		}
		});
	  	
 		mainListView = getListView();
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
		List<String> items = new ArrayList<String>(); // TODO: sort alphabetically

		int count = mainListView.getAdapter().getCount();

		for (int i = 0; i < count; i++) {
			if (mainListView.isItemChecked(i)) {
				items.add((String)mainListView.getItemAtPosition(i));
			}
		}
		return items;
	}

	private void createGroup() {
		peers = getSelectedItems();
		
		if (peers.size() == 0) {
			openEmptyGroupDialog();
			return;
		}

		openGroupNameDialog();
	}
	
	private void openEmptyGroupDialog() {
		new AlertDialog.Builder(this)
        	.setTitle("Empty Group")
        	.setMessage("Cannot create empty group. Please add at least one peer.")
        	.setNeutralButton("Okay", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	// nothing to do
                }
        	})
        	.show();  		
   	}
	
	private void openBlankNameDialog() {
		new AlertDialog.Builder(this)
        	.setTitle("Blank Name")
        	.setMessage("Please specify a group name.")
        	.setNeutralButton("Okay", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	openGroupNameDialog(); // try again
                }
        	})
        	.show();  		
   	}
	
	private void openGroupNameDialog() {
        LayoutInflater factory = LayoutInflater.from(this);
        final View view = factory.inflate(R.layout.groupname, null);
        final EditText etName = (EditText) view.findViewById(R.id.etName);
		
		new AlertDialog.Builder(this)
        	.setTitle("Group Name")
        	.setMessage("Please enter a name for this group.")
        	.setView(view)
        	.setPositiveButton("Commit", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
            		String name = etName.getText().toString();
            		if (name.length() == 0) {
            			openBlankNameDialog();
            		} else {
            			GroupHelper.createGroup(name, peers, addr);
            			finish();
            		}
                }
        	})
        	.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	// nothing to do
                }
        	})
        	.show();  		
   	}

	public void onServiceConnected() {
		manet.sendPeersQuery();
		manet.sendManetConfigQuery();
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
		this.manetcfg = manetcfg;
		
		try {
			addr = InetAddress.getByName(manetcfg.getIpBroadcast()); // TODO
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
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