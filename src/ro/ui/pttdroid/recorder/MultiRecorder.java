package ro.ui.pttdroid.recorder;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import ro.ui.pttdroid.channels.Channel;
import ro.ui.pttdroid.channels.GroupChannel;
import ro.ui.pttdroid.channels.PeerChannel;

public class MultiRecorder extends Recorder {
	
	private Set<Recorder> recorders = null;
		
	public MultiRecorder(Channel channel) {
		super(channel);
	}
	
	protected void init() {	
		super.init();
		
		GroupChannel groupChannel = (GroupChannel) channel;
		
		recorders = new HashSet<Recorder>();
		
		try {
			InetAddress addr = null;
			Channel channel = null;
			Recorder recorder = null;
			for(String peer : groupChannel.group.peers) {
				addr = InetAddress.getByName(peer);
				channel = new PeerChannel(peer, addr); // TODO
				recorder = new SingleRecorder(channel);
				recorder.init();
				
				recorders.add(recorder);
			}
		} catch(UnknownHostException e) {
			e.printStackTrace();
		}	
	}
	
	protected void release() {
		super.release();
		
		if(recorders != null) {
			for (Recorder recorder : recorders) {
				recorder.release();
			}
		}
	}

	@Override
	protected void send() {
		for (Recorder recorder : recorders) {
			recorder.send();
		}
	}
}
