package ro.ui.pttdroid.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.LinkedList;

import android.util.Log;

public class PhoneIPs {
	
	static private LinkedList<InetAddress> inetAddresses = new LinkedList<InetAddress>(); 

	public static void load() {
		inetAddresses.clear();
		try {
			Enumeration<NetworkInterface> networkInterfaceEnum = NetworkInterface.getNetworkInterfaces();
			
			while(networkInterfaceEnum.hasMoreElements()) {								
				Enumeration<InetAddress> inetAddresseEnum = networkInterfaceEnum.nextElement().getInetAddresses();
				
				while(inetAddresseEnum.hasMoreElements()) {
					inetAddresses.add(inetAddresseEnum.nextElement());
				}
			}
		}
		catch(IOException e) {
			Log.d("MyNetworkInterfaces", e.toString());
		}
	}
	
	public static boolean contains(InetAddress addr) {
		return inetAddresses.contains(addr);
	}
	
}
