package org.span.ptt.channels;

public class ListenOnlyChannel extends Channel {
	
	// NOTE: should only be invoked by ChannelHelper
	protected ListenOnlyChannel () {
		super(ChannelHelper.CHANNEL_LISTEN_ONLY);
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof ListenOnlyChannel) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isRecorderEnabled() {
		return false;
	}

	@Override
	public boolean isPlayerEnabled() {
		return true;
	}
}