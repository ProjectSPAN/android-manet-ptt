package ro.ui.pttdroid.channels;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import android.adhoc.manet.routing.Node;

import ro.ui.pttdroid.groups.Group;
import ro.ui.pttdroid.groups.GroupHelper;

public class ChannelHelper {
	
	public static String CHANNEL_NONE = "None";
	
	private GroupHelper groupHelper = null;
	private HashSet<Node> peers = null;
	
	public ChannelHelper(GroupHelper groupHelper) {
		this.groupHelper = groupHelper;
	}
	
	public void updatePeers(HashSet<Node> peers) {
		this.peers = peers;
	}
	
	public List<Channel> getChannels() {
		List<Channel> channels = new ArrayList<Channel>(); // TODO: sort alphabetically, groups before individuals
		
		List<Group> groups = groupHelper.getGroups();
		for (Group group : groups) {
			Channel channel = new GroupChannel(group);
			channels.add(channel);
		}
		
		if (peers != null) {
			for (Node peer : peers) {
				Channel channel = new PeerChannel(peer);
				channels.add(channel);
			}
		}
		
		return channels;
	}
	
	public String[] getNames(List<Channel> channels) {
		String[] names = new String[channels.size()+1];
		names[0] = CHANNEL_NONE;
		for (int i = 0; i < channels.size(); i ++) {
			names[i+1] = channels.get(i).name;
		}
		return names;
	}
}