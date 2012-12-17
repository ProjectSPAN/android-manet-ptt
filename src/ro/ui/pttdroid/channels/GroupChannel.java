package ro.ui.pttdroid.channels;

import ro.ui.pttdroid.groups.Group;

public class GroupChannel extends Channel {
	
	public Group group = null;
	
	public GroupChannel (Group group) {
		super(group.name);
		this.group = group;
		addr = group.addr;
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

	@Override
	public boolean isRecorderEnabled() {
		return true;
	}

	@Override
	public boolean isPlayerEnabled() {
		return true;
	}
}