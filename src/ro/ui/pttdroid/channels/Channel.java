package ro.ui.pttdroid.channels;

public abstract class Channel {
	
	public String name = null;
	
	public Channel (String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
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
}