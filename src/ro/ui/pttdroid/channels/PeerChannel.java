package ro.ui.pttdroid.channels;

import android.adhoc.manet.routing.Node;

public class PeerChannel extends Channel {
	
	private Node peer = null;
	
	public PeerChannel (Node peer) {
		super(peer.userId); // default
		
		if (peer.userId != null) {
			name = peer.addr + " (" + peer.userId + ")";
		} else {
			name = peer.addr;	
		}

		this.peer = peer;
	}
}