package org.span.ptt.recorder;

import java.util.HashSet;
import java.util.Set;

import org.span.ptt.channels.Channel;
import org.span.ptt.channels.GroupChannel;


public class MultiRecorder extends Recorder {
	
	private Set<Recorder> recorders = null;
		
	public MultiRecorder(Channel channel) {
		super(channel);
	}
	
	protected void init() {	
		super.init();
		
		GroupChannel groupChannel = (GroupChannel) channel;
		
		recorders = new HashSet<Recorder>();
		
		Recorder recorder = null;
		for(Channel channel : groupChannel.channels) {
			recorder = new SingleRecorder(channel);
			recorder.init();
			recorders.add(recorder);
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
