package ro.ui.pttdroid.channels;

import java.util.ArrayList;
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
	
	public static String CHANNEL_NONE = "None";
	
	private static GroupHelper groupHelper = null;
	private static HashSet<Node> peers = null;
	
	private static Channel blankChannel = new BlankChannel();
	
	public static void updatePeers(HashSet<Node> _peers) {
		peers = _peers;
	}
	
	public static Channel getBlankChannel() {
		return blankChannel;
	}
	
	public static List<Channel> getChannels() {
		List<Channel> channels = new ArrayList<Channel>(); // TODO: sort alphabetically, groups before individuals
		Channel channel = null;
		
		// blank channel
		channels.add(blankChannel);
		
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
	
	public static void getSettings(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Resources res = context.getResources();
		
		/*
		if (prefs.contains("channel")) {
			String channelStr = prefs.getString("channel", "");
		}
		*/
	}
}