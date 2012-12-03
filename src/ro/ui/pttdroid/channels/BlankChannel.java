package ro.ui.pttdroid.channels;

public class BlankChannel extends Channel {
	
	public BlankChannel () {
		super(ChannelHelper.CHANNEL_NONE);
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof BlankChannel) {
			return true;
		}
		return false;
	}
	
	@Override
	public int getCastType() {
		return Channel.UNICAST;
	}
}