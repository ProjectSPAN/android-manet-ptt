package ro.ui.pttdroid.channels;

import ro.ui.pttdroid.groups.Group;

public class GroupChannel extends Channel {
	
	private Group group = null;
	
	public GroupChannel (Group group) {
		super(group.name);
		this.group = group;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof GroupChannel) {
			GroupChannel channel = (GroupChannel) other;
			if (channel.group.id == group.id) {
				return true;
			}
		}
		return false;
	}
}