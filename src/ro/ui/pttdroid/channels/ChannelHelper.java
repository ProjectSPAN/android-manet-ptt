package ro.ui.pttdroid.channels;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import android.adhoc.manet.routing.Node;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import ro.ui.pttdroid.groups.Group;
import ro.ui.pttdroid.groups.GroupHelper;

public class ChannelHelper {
	
	public static final String CHANNEL_PREFS = "channels";
	
	public static final String CHANNEL_NAME  = "channel_name";
	public static final String CHANNEL_TYPE  = "channel_type";
	public static final String CHANNEL_ADDR  = "channel_addr";
	public static final String CHANNEL_ID    = "channel_id"; // for group channels only
	
	public static String CHANNEL_NULL = "Silence Mode";
	public static String CHANNEL_LISTEN_ONLY = "Listen Only Mode";
	
	private static Channel nullChannel = new NullChannel();
	private static Channel listenOnlyChannel = new ListenOnlyChannel();
	
	private static List<Channel> channels = null;
	private static Channel channel = null;
	
	private static SharedPreferences prefs = null;
	
	public static void getSettings(Context context) {
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		loadChannel();
	}
	
	private static void loadChannel() {
		if (prefs.contains(CHANNEL_NAME)) {
			String name = prefs.getString(CHANNEL_NAME, "");
			String type = prefs.getString(CHANNEL_TYPE, "");

			try {
				if (type.equals(NullChannel.class.getName())) {
					channel = nullChannel;
				} else if (type.equals(ListenOnlyChannel.class.getName())) {
					channel = listenOnlyChannel;
				} else if (type.equals(PeerChannel.class.getName())) {
					String addr = prefs.getString(CHANNEL_ADDR, "");
					InetAddress inetAddr = InetAddress.getByName(addr);
					channel = new PeerChannel(name, inetAddr); // TODO
				} else if (type.equals(GroupChannel.class.getName())) {
					int id = prefs.getInt(CHANNEL_ID, -1);
					Group group = GroupHelper.getGroup(id);
					channel = new GroupChannel(group); // TODO
				}
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		} else {
			channel = getDefaultChannel();
		}
	}
	
	public static void setChannel(Channel _channel) {
		channel = _channel;
		
		SharedPreferences.Editor prefEditor = prefs.edit();
		String type = channel.getClass().getName();
		
		prefEditor.putString(CHANNEL_NAME, channel.name);
		prefEditor.putString(CHANNEL_TYPE, type);
		
		if (type.equals(NullChannel.class.getName()) ||
				type.equals(ListenOnlyChannel.class.getName())) {
			prefEditor.putString(CHANNEL_ADDR, "");
		} else {
			prefEditor.putString(CHANNEL_ADDR, channel.addr.getHostAddress());
		}

		if (type.equals(GroupChannel.class.getName())) {
			GroupChannel groupChannel = (GroupChannel) channel;
			prefEditor.putInt(CHANNEL_ID, groupChannel.group.id);
		} else {
			prefEditor.putInt(CHANNEL_ID, -1);
		}
		
		prefEditor.commit();
	}
	
	public static Channel getChannel() {
		return channel;
	}
	
	public static Channel getDefaultChannel() {
		return listenOnlyChannel;
	}
	
	public static Channel getNullChannel() {
		return nullChannel;
	}
	
	public static Channel getListenOnlyChannel() {
		return listenOnlyChannel;
	}
	
	public static List<Channel> getChannels() {
		if (channels == null) {
			updateChannels(null);
		}
		return channels;
	}
	
	public static void updateChannels(HashSet<Node> peers) {
		Channel c = null;
		channels = new ArrayList<Channel>(); // TODO: sort alphabetically, groups before individuals
		
		// special channels
		channels.add(nullChannel);
		channels.add(listenOnlyChannel);
		
		// groups
		List<Group> groups = GroupHelper.getGroups();
		for (Group group : groups) {
			c = new GroupChannel(group);
			if (c.equals(channel)) {
				// use current channel instance
				channels.add(channel);
			} else {
				channels.add(c);
			}
		}
		
		// peers
		if (peers != null) {
			// NOTE: don't show individual peers that aren't in the mesh right now
			for (Node peer : peers) {
				c = new PeerChannel(peer);
				if (c.equals(channel)) {
					// use current channel instance
					channel.setStatus(Channel.GOOD_STATUS);
					channels.add(channel);
				} else {
					c.setStatus(Channel.GOOD_STATUS);
					channels.add(c);
				}
			}
		}
		
		// current channel
		if (!channels.contains(channel)) {
 			channels.add(channel); // add invalid channel back in for user awareness
 			channel.setStatus(Channel.BAD_STATUS);
 		}
		
		updateGroupChannelStatus();
	}
	
	private static void updateGroupChannelStatus() {
 		for (Channel c : channels) {
 			if (c instanceof GroupChannel) {
 	 			// check if all, some, or none of the peers are available
 	 			GroupChannel groupChannel = (GroupChannel) c;
 	 			int partCount = 0;
 	 			int fullCount = groupChannel.channels.size();
 	 			for (Channel gc : groupChannel.channels) {
 	 				if (channels.contains(gc)) {
 	 					partCount++;
 	 	 	 		} 
 	 			}
 	 			if (partCount == fullCount) { // all
 	 				c.setStatus(Channel.GOOD_STATUS);
 	 			} else if (partCount == 0) { // none
 	 				c.setStatus(Channel.BAD_STATUS);
 	 			} else { // partial
 	 				c.setStatus(Channel.PARTIAL_STATUS);
 	 			}
 	 		}
 		}
    }
}