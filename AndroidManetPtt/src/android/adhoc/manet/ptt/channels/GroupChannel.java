package android.adhoc.manet.ptt.channels;

import java.util.List;

import android.adhoc.manet.ptt.groups.Group;

public class GroupChannel extends Channel {
	
	public Group group = null;
	public List<Channel> channels = null;
	
	// NOTE: should only be invoked by ChannelHelper
	protected GroupChannel (Group group, List<Channel> channels) {
		super(group.name);
		this.group = group;
		this.channels = channels;
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