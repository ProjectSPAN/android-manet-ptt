package ro.ui.pttdroid.channels;

public class NullChannel extends Channel {
	
	// NOTE: should only be invoked by ChannelHelper
	protected NullChannel() {
		super(ChannelHelper.CHANNEL_NULL);
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof NullChannel) {
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
		return false;
	}
}