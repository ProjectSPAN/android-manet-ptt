package ro.ui.pttdroid.channels;

import java.net.InetAddress;
import java.net.UnknownHostException;

import ro.ui.pttdroid.settings.CommSettings;

import android.adhoc.manet.routing.Node;

public class PeerChannel extends Channel {
	
	private Node peer = null;
	
	public PeerChannel(Node peer) {
		super(peer.userId); // default
		
		// TODO: DEBUG VPN
		if (CommSettings.getVpnState()) {
			String octets[] = peer.addr.split("\\.");
			peer.addr = "2.2.2." + octets[3];
		}
		
		if (peer.userId != null) {
			name = peer.addr + " (" + peer.userId + ")";
		} else {
			name = peer.addr;	
		}
		
		try {
			addr = InetAddress.getByName(peer.addr);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		this.peer = peer;
	}
	
	public PeerChannel(String name, InetAddress addr) { // TODO
		super(name);
		this.addr = addr;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof PeerChannel) {
			PeerChannel channel = (PeerChannel) other;
			if (channel.name.equals(name)) {
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