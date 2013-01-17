package org.span.ptt.channels;

import java.net.InetAddress;

public class PeerChannel extends Channel {
	
	// NOTE: should only be invoked by ChannelHelper
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