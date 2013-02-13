package org.span.ptt.channels;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.span.ptt.R;
import org.span.ptt.groups.Group;
import org.span.ptt.groups.GroupHelper;
import org.span.ptt.settings.CommSettings;
import org.span.service.routing.Node;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class ChannelHelper {
	
	public static final String CHANNEL_PREFS = "channels";
	
	public static final String CHANNEL_NAME  = "channel_name";
	public static final String CHANNEL_TYPE  = "channel_type";
	public static final String CHANNEL_ADDR  = "channel_addr";
	public static final String CHANNEL_ID    = "channel_id"; // for group channels only
	
	public static String CHANNEL_NULL = "Silence Mode";
	public static String CHANNEL_LISTEN_ONLY = "Listen Only Mode";
	
	private static Channel nullChannel = null;;
	private static Channel listenOnlyChannel = null;
	
	private static List<Channel> channels = new ArrayList<Channel>(); // maintain insertion order
	private static Channel channel = null;
	
	private static SharedPreferences prefs = null;
	
	private static Map<Node, ProbeThread> probeMap = new HashMap<Node, ProbeThread>();
	
	static {
		ProbeListenerThread probeListenerThread = new ProbeListenerThread();
		probeListenerThread.start();
	}
	
	public static void getSettings(Context context) {
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		loadChannel();
	}
	
	public static Channel getChannel() {
		return channel;
	}
	
	public static Channel getDefaultChannel() {
		return getNullChannel(); // in case user declines EULA
	}
	
	public static List<Channel> getChannels() {
		if (channels == null) {
			updateChannels(null);
		}
		
		// order channels
		List<Channel> ordered 	= new ArrayList<Channel>();
		List<Channel> peers 	= new ArrayList<Channel>();
		List<Channel> groups 	= new ArrayList<Channel>();
		
		for (Channel c : channels) {
			if (c instanceof PeerChannel) {
				peers.add(c);
			} else if (c instanceof GroupChannel) {
				groups.add(c);
			}
		}
		
		Collections.sort(groups);
		Collections.sort(peers);
		
		ordered.add(getNullChannel());
		ordered.add(getListenOnlyChannel());
		
		ordered.addAll(groups);
		ordered.addAll(peers);
		
		return ordered;
	}
	
	// TODO: check if channel exists before instantiating a new one
	private static Channel addChannel(Channel c) {
		Channel retval = null;
		if (channels.contains(c)) {
			// use existing channel instance
			retval = channels.get(channels.indexOf(c));
		} else {
			// use new channel instance
			channels.add(c);
			retval = c;
		}
		return retval;
	}
	
	public static Channel getNullChannel() {
		if (nullChannel == null) {
			nullChannel = new NullChannel();
		}
		return addChannel(nullChannel);
	}
	
	public static Channel getListenOnlyChannel() {
		if (listenOnlyChannel == null) {
			listenOnlyChannel = new ListenOnlyChannel();
		}
		return addChannel(listenOnlyChannel);
	}
	
	public static Channel getPeerChannel(Node peer) {
		Channel retval = null;
		
		try {
			// TODO: DEBUG VPN
			if (CommSettings.getVpnState()) {
				String octets[] = peer.addr.split("\\.");
				peer.addr = "2.2.2." + octets[3];
			}
			
			String name = null;
			if (peer.userId != null) {
				name = peer.addr + " (" + peer.userId + ")";
			} else {
				name = peer.addr;	
			}
			
			InetAddress addr = InetAddress.getByName(peer.addr);
			
			retval = getPeerChannel(name, addr);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		return retval;
	}
	
	public static Channel getPeerChannel(String peer) {
		Channel retval = null;
		
		try {
			InetAddress addr = InetAddress.getByName(peer);
			retval = getPeerChannel(peer, addr);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		return retval;
	}
	
	public static Channel getPeerChannel(String name, InetAddress addr) {
		Channel c = new PeerChannel(name, addr);
		return addChannel(c);
	}
	
	public static Channel getGroupChannel(Group group) {
		
		// peer channels
		List<Channel> l = new ArrayList<Channel>();
		Channel c = null;
		for(String peer : group.peers) {
			c = getPeerChannel(peer);
			l.add(c);
		}
		
		Channel gc = new GroupChannel(group, l);
		return addChannel(gc);
	}
	
	private static void loadChannel() {
		if (prefs.contains(CHANNEL_NAME)) {
			String name = prefs.getString(CHANNEL_NAME, "");
			String type = prefs.getString(CHANNEL_TYPE, "");

			try {
				if (type.equals(NullChannel.class.getName())) {
					channel = getNullChannel();
				} else if (type.equals(ListenOnlyChannel.class.getName())) {
					channel = getListenOnlyChannel();
				} else if (type.equals(PeerChannel.class.getName())) {
					String addr = prefs.getString(CHANNEL_ADDR, "");
					InetAddress inetAddr = InetAddress.getByName(addr);
					channel = getPeerChannel(name, inetAddr);
				} else if (type.equals(GroupChannel.class.getName())) {
					int id = prefs.getInt(CHANNEL_ID, -1);
					Group group = GroupHelper.getGroup(id);
					channel = getGroupChannel(group);
				}
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		} else {
			channel = getDefaultChannel();
		}
	}
	
	public static void setCurrentChannel(Channel _channel) {
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
	
	public static void updateChannels(HashSet<Node> peers) {
		Channel c = null;
		
		// special channels
		c = getNullChannel();
		c.setStatus(Channel.GOOD_STATUS);
		
		c = getListenOnlyChannel();
		c.setStatus(Channel.GOOD_STATUS);
		
		// peers
		// NOTE: always update peer channels before group channels
		if (peers != null) {
			ProbeThread probeThread = null;
			for (Node peer : peers) {
				// c = getPeerChannel(peer);
				// c.setStatus(Channel.GOOD_STATUS);
				if (!probeMap.containsKey(peer)) {
					probeThread = new ProbeThread(peer);
					probeMap.put(peer, probeThread);
					probeThread.start();
				}
			}
		}

		// groups
		List<Group> groups = GroupHelper.getGroups();
		List<Channel> groupChannels = new ArrayList<Channel>();
		for (Group group : groups) {
			c = getGroupChannel(group);
			groupChannels.add(c);
		}
		
		List<Channel> copy = new ArrayList<Channel>(channels);
		for (Channel gc : copy) {
			if (gc instanceof GroupChannel) {
				if (!groupChannels.contains(gc)) {
					channels.remove(gc); // group was removed
				}
			}
		}
				
		updateGroupChannelStatuses();
	}
	
	private static void updateGroupChannelStatuses() {
 		for (Channel c : channels) {
 			if (c instanceof GroupChannel) {
 	 			// check if all, some, or none of the peers are available
 	 			GroupChannel groupChannel = (GroupChannel) c;
 	 			int partCount = 0;
 	 			int fullCount = groupChannel.channels.size();
 	 			for (Channel gc : groupChannel.channels) {
 	 				if (gc.status == Channel.GOOD_STATUS) {
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
	
    public static int getChannelStatusResource(Channel _channel) {    	
 		int resource = -1;
 		if (_channel instanceof NullChannel) {
 			resource = R.drawable.stop_icon;
 		} 
 		else if (_channel instanceof ListenOnlyChannel) {
 			resource = R.drawable.eye_icon;
 		}
 		else if (_channel instanceof PeerChannel) {
 			switch (_channel.status) {
				case Channel.GOOD_STATUS:
					resource = R.drawable.peer_green_icon;
					break;
				case Channel.BAD_STATUS:
					// fall through
				default:
					resource = R.drawable.peer_red_icon;
					break;
 			}
		} 
 		else if (_channel instanceof GroupChannel) {
 			switch (_channel.status) {
				case Channel.GOOD_STATUS:
					resource = R.drawable.group_green_icon;
					break;
				case Channel.PARTIAL_STATUS:
					resource = R.drawable.group_orange_icon;
					break;
				case Channel.BAD_STATUS:
					// fall through
				default:
					resource = R.drawable.group_red_icon;
					break;
 			}
		}
		return resource;
    }
    
    // inner classes
    
    private static class ProbeThread extends Thread {
    	
    	private final int SO_TIMEOUT_MILLISEC = 5000;
    	
    	private Node peer = null;
    	
    	public ProbeThread(Node peer) {
    		this.peer = peer;
    	}
    	
    	@Override
    	public void run() {
			Channel c = getPeerChannel(peer);
    		DatagramSocket socket = null;
    		
    		try {    			
    			socket = new DatagramSocket();
    			socket.setSoTimeout(SO_TIMEOUT_MILLISEC);
    			
    			DatagramPacket txpacket = 
    				new DatagramPacket(new byte[0], 0, c.addr, Channel.PROBE_PORT);
    			socket.send(txpacket);
    			
    			byte rxdata[] = new byte[1];
    			DatagramPacket rxpacket = new DatagramPacket(rxdata, rxdata.length);	
    			socket.receive(rxpacket); // blocking
    			
    			if (rxdata[0] == 1) {
    				c.setStatus(Channel.GOOD_STATUS);
    			} else {
    				c.setStatus(Channel.BAD_STATUS);
    			}
    		} catch (SocketTimeoutException e) {
    			c.setStatus(Channel.BAD_STATUS);
    		} catch (Exception e) {
    			e.printStackTrace();
    		} finally {
    			if(socket != null) {
    				socket.close();
    			}
    			probeMap.remove(peer);
    		}
    	}
    }
    
    private static class ProbeListenerThread extends Thread {
    	
    	private final int SO_TIMEOUT_MILLISEC = 0;
    	
    	@Override
    	public void run() {
    		DatagramSocket socket = null;
    		
    		try {
    			socket = new DatagramSocket(Channel.PROBE_PORT);
    			socket.setSoTimeout(SO_TIMEOUT_MILLISEC); // wait forever
    			
    			DatagramPacket rxpacket = new DatagramPacket(new byte[0], 0);
    			
    			while (true) {
	    			socket.receive(rxpacket); // blocking
	    			
	    			byte txdata[] = {1};
	    			DatagramPacket txpacket = new DatagramPacket(txdata, txdata.length, rxpacket.getAddress(), rxpacket.getPort());
	    			
	    			socket.send(txpacket);
    			}
    		} catch (Exception e) {
    			e.printStackTrace();
    		} finally {
    			if(socket != null) {
    				socket.close();
    			}
    		}
    	}
    }
}