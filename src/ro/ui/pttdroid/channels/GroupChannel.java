package ro.ui.pttdroid.channels;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import ro.ui.pttdroid.groups.Group;

public class GroupChannel extends Channel {
	
	public Group group = null;
	public List<Channel> channels = null;
	
	public GroupChannel (Group group) {
		super(group.name);
		this.group = group;
		addr = group.addr;
		
		// create peer channels
		channels = new ArrayList<Channel>();
		InetAddress addr = null;
		Channel channel = null;
		for(String peer : group.peers) {
			try {
				addr = InetAddress.getByName(peer);
				channel = new PeerChannel(peer, addr);
				channels.add(channel);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
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