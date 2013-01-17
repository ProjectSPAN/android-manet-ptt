package org.span.ptt.groups;

import java.util.List;

import org.span.ptt.R;
import org.span.ptt.channels.Channel;


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
	    		Group group = (Group) mainListView.getAdapter().getItem(position);
				i.putExtra(GroupHelper.GROUP_ID, group.id);
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
		ArrayAdapter<Group> adapter = 
				new ArrayAdapter<Group>(this, R.layout.listtextitem, groups);
		mainListView.setAdapter(adapter);
 		mainListView.setItemsCanFocus(false);
 		mainListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	}
}