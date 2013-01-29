package org.span.ptt.recorder;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import org.span.ptt.channels.Channel;

import android.util.Log;


public class SingleRecorder extends Recorder {
	
	protected DatagramSocket socket;
	protected DatagramPacket packet;
	
	public SingleRecorder(Channel channel) {
		super(channel);
	}
	
	@Override
	protected void send() {
		try {
			packet = new DatagramPacket(
					encodedFrame, 
					encodedFrame.length, 
					channel.addr, 
					channel.port);
			
			// Send encoded frame packed within an UDP datagram
			if (packet.getAddress() != null) { // TODO: special channel
				// Log.d("PTT", "Sending to " + packet.getAddress().getHostAddress() + " : " + bytesToHex(packet.getData())); // DEBUG
								
				socket.send(packet);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String bytesToHex(byte[] bytes) {
	    final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
	    char[] hexChars = new char[bytes.length * 2];
	    int v;
	    for ( int j = 0; j < bytes.length; j++ ) {
	        v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
	protected void init() {		
		super.init();
		
		try {	    	
			Log.d("PTT", "SingleRecorder, txPort: " + channel.port + ", remote addr: " + channel.addr.getHostAddress() + "::" + channel.port); // DEBUG
			
			// bind to any available local port; don't care since we're not rxing, only txing
			// receiver addr and port specified in packet; don't connect to remote host
			socket = new DatagramSocket();
			socket.setSoTimeout(SO_TIMEOUT_MILLISEC);	
		}
		catch(SocketException e) {
			e.printStackTrace();
		}	
	}
	
	protected void release() {
		super.release();
		
		if(socket != null) {
			socket.close();
			socket = null;
		}
	}
}
