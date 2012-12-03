package ro.ui.pttdroid.groups;

import ro.ui.pttdroid.R;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class ViewGroup extends ListActivity {
	
	private ListView mainListView = null;
	
    private Button btnDelete = null;
	
	private Group group = null;
    
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);	
		
		setContentView(R.layout.viewgroup);
		
	    btnDelete = (Button) findViewById(R.id.btnDelete);
	    btnDelete.setOnClickListener(new View.OnClickListener() {
	  		public void onClick(View v) {
	    		GroupHelper.deleteGroup(group.id);
	    		finish();
	  		}
		});
	  	
 		mainListView = getListView();
 		
 		int index = getIntent().getExtras().getInt(GroupHelper.GROUP_INDEX);
 		
 		group = GroupHelper.getGroup(index);
 		
 		showGroup();
    }
	
	private void showGroup() {
		setTitle("View Group: " + group.name);
		
 		String[] peers = new String[group.peers.size()];
 		group.peers.toArray(peers);
 		
 		mainListView.setAdapter(new ArrayAdapter<String>(this,
 				android.R.layout.simple_list_item_1, peers));

 		mainListView.setItemsCanFocus(false);
 		mainListView.setChoiceMode(ListView.CHOICE_MODE_NONE);
 		mainListView.setEnabled(false); // prevent clicking
	}
}