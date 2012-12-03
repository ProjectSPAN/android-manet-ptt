package ro.ui.pttdroid.channels;

import java.net.InetAddress;
import java.net.UnknownHostException;

import android.adhoc.manet.routing.Node;

public class PeerChannel extends Channel {
	
	private Node peer = null;
	
	public PeerChannel(Node peer) {
		super(peer.userId); // default
		
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
	public int getCastType() {
		return Channel.UNICAST;
	}

	@Override
	public boolean usesMic() {
		return true;
	}
}