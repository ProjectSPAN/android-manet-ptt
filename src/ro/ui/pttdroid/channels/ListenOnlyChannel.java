package ro.ui.pttdroid.channels;

public class ListenOnlyChannel extends Channel {
	
	public ListenOnlyChannel () {
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
	public int getCastType() {
		return Channel.UNICAST;
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