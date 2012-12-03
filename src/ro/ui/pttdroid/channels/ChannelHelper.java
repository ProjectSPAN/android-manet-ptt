package ro.ui.pttdroid.channels;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import android.adhoc.manet.routing.Node;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import ro.ui.pttdroid.groups.Group;
import ro.ui.pttdroid.groups.GroupHelper;

public class ChannelHelper {
	
	public static final String CHANNEL_PREFS = "channels";
	
	public static final String CHANNEL_NAME  = "channel_name";
	public static final String CHANNEL_TYPE  = "channel_type";
	public static final String CHANNEL_ADDR  = "channel_addr";
	public static final String CHANNEL_ID    = "channel_id"; // for group channels only
	
	public static String CHANNEL_NULL = "Silence";
	public static String CHANNEL_LISTEN_ONLY = "Listen only";
	
	private static HashSet<Node> peers = null;
	
	private static Channel nullChannel = new NullChannel();
	private static Channel listenOnlyChannel = new ListenOnlyChannel();
	
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
	
	public static void updatePeers(HashSet<Node> _peers) {
		peers = _peers;
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
		List<Channel> channels = new ArrayList<Channel>(); // TODO: sort alphabetically, groups before individuals
		Channel channel = null;
		
		// special channels
		channels.add(nullChannel);
		channels.add(listenOnlyChannel);
		
		// groups
		List<Group> groups = GroupHelper.getGroups();
		for (Group group : groups) {
			channel = new GroupChannel(group);
			channels.add(channel);
		}
		
		// peers
		if (peers != null) {
			for (Node peer : peers) {
				channel = new PeerChannel(peer);
				channels.add(channel);
			}
		}
		
		return channels;
	}
}