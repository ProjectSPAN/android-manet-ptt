package ro.ui.pttdroid.channels;

import java.util.ArrayList;
import java.util.List;

import ro.ui.pttdroid.groups.Group;
import ro.ui.pttdroid.groups.GroupHelper;

public class ChannelHelper {
	
	private GroupHelper groupHelper = null;
	
	public ChannelHelper(GroupHelper groupHelper) {
		this.groupHelper = groupHelper;
	}
	
	public List<Channel> getChannels() {
		List<Channel> channels = new ArrayList<Channel>(); // TODO: sort alphabetically, groups before individuals
		
		List<Group> groups = groupHelper.getGroups();
		for (Group group : groups) {
			Channel channel = new GroupChannel(group);
			channels.add(channel);
		}
		
		// TODO: add all peer channels
		
		return channels;
	}
	
	public String[] getNames(List<Channel> channels) {
		String[] names = new String[channels.size()];
		for (int i = 0; i < channels.size(); i ++) {
			names[i] = channels.get(i).name;
		}
		return names;
	}
}