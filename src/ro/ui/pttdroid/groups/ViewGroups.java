package ro.ui.pttdroid.groups;

import java.util.List;

import ro.ui.pttdroid.R;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class ViewGroups extends ListActivity {
	
	private ListView mainListView = null;
	
    private Button btnCreate = null;
	
	private List<Group> groups = null;
    
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);	
		
		setContentView(R.layout.viewgroups);
		
	    btnCreate = (Button) findViewById(R.id.btnCreate);
	    btnCreate.setOnClickListener(new View.OnClickListener() {
	  		public void onClick(View v) {
	    		Intent i = new Intent(ViewGroups.this, CreateGroup.class);
	    		startActivityForResult(i, 0);
	  		}
		});
	  	
 		mainListView = getListView();
 		mainListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long index) {				
	    		Intent i = new Intent(ViewGroups.this, ViewGroup.class);
				i.putExtra(GroupHelper.GROUP_INDEX, (int)index);
	    		startActivityForResult(i, 0);
			}
		});
    }
	
	@Override
	public void onStart() {
		super.onStart();
		
 		groups = GroupHelper.getGroups();
 		
 		showGroups();
	}
	
	private void showGroups() {
		String[] names = GroupHelper.getNames(groups);
		
 		mainListView.setAdapter(new ArrayAdapter<String>(this,
 				android.R.layout.simple_list_item_1, names));

 		mainListView.setItemsCanFocus(false);
 		mainListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	}
}