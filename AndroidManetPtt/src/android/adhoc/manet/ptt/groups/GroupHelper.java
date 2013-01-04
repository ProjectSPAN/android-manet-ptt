package android.adhoc.manet.ptt.groups;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class GroupHelper {
	
	public static final String GROUP_PREFS  = "groups";
	public static final String GROUP_ID     = "group_id";
	
	public static final String GROUP_NAME   = "group_name";
	public static final String GROUP_LIST   = "group_list";
	public static final String GROUP_ADDR   = "group_addr";
	public static final String GROUP_MAX_ID = "group_max_id"; 
	
	private static Map<Integer, Group> groupMap = null;
	
	private static SharedPreferences prefs = null;
	
	private static int nextId = -1; // next group id
	
	public static void getSettings(Context context) {
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		loadGroups();
	}
	
	private static void loadGroups() {
		groupMap = new HashMap<Integer, Group>(); // TODO: sort alphabetically
		
		nextId = prefs.getInt(GROUP_MAX_ID, -1) + 1;
		
		for (int id = 0; id < nextId; id++) {
			if (prefs.contains(GROUP_NAME + "." + id)) {
				String name = prefs.getString(GROUP_NAME + "." + id, "");
				String list = prefs.getString(GROUP_LIST + "." + id, "");
				String addr = prefs.getString(GROUP_ADDR + "." + id, "");
	
				ArrayList<String> peers = new ArrayList<String>();
				peers.addAll(Arrays.asList(list.split(",")));
				
				try {
					InetAddress inetAddr = InetAddress.getByName(addr);
					Group group = new Group(id, name, peers, inetAddr);
					groupMap.put(id, group);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static List<Group> getGroups() {
		List<Group> groups = new ArrayList<Group>(groupMap.values());
		return groups;
	}
	
	public static Group getGroup(int id) {
		return groupMap.get(id);
	}
	
	/*
	public static String[] getNames(List<Group> groups) {
		String[] names = new String[groups.size()];
		for (int i = 0; i < groups.size(); i ++) {
			names[i] = groups.get(i).name;
		}
		return names;
	}
	*/
	
	public static Group createGroup(String name, List<String> peers, InetAddress addr) {
		String items = "";
		for (int i = 0; i < peers.size(); i++) {
			items += peers.get(i) + ",";
		}
		items = items.substring(0, items.length()-1);
		
		Group group = new Group(nextId, name, peers, addr);
				
		SharedPreferences.Editor prefEditor = prefs.edit();

		prefEditor.putString(GROUP_NAME + "." + nextId, name);
		prefEditor.putString(GROUP_LIST + "." + nextId, items);
		prefEditor.putString(GROUP_ADDR + "." + nextId, addr.getHostAddress());
		prefEditor.putInt(GROUP_MAX_ID, nextId);

		prefEditor.commit();
		
		groupMap.put(nextId, group);
		
		nextId++; // next group id
		
		return group;
	}
	
	public static void deleteGroup(int id) {
		SharedPreferences.Editor prefEditor = prefs.edit();
		
		prefEditor.remove(GROUP_NAME + "." + id);
		prefEditor.remove(GROUP_LIST + "." + id);
		prefEditor.remove(GROUP_ADDR + "." + id);

		prefEditor.commit();
		
		groupMap.remove(id);
	}
}