package ro.ui.pttdroid.channels;

import ro.ui.pttdroid.groups.Group;

public class GroupChannel extends Channel {
	
	private Group group = null;
	
	public GroupChannel (Group group) {
		super(group.name);
		this.group = group;
	}
}