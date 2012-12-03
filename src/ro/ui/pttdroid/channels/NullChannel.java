package ro.ui.pttdroid.channels;

public class NullChannel extends Channel {
	
	public NullChannel() {
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
	public int getCastType() {
		return Channel.UNICAST;
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