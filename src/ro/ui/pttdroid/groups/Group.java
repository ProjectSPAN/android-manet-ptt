package ro.ui.pttdroid.groups;

import java.util.List;

public class Group {
	
	public int id = -1;
	public String name = null;
	public List<String> peers = null;
	
	public Group (int id, String name, List<String> peers) {
		this.id = id;
		this.name = name;
		this.peers = peers;
	}
}