package ro.ui.pttdroid.settings;

import java.net.InetAddress;
import java.net.UnknownHostException;

import ro.ui.pttdroid.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

public class CommSettings extends PreferenceActivity {
	
	private static InetAddress broadcastAddr;
	private static InetAddress multicastAddr;
	private static InetAddress unicastAddr;	
	
	public static final int BROADCAST = 0;
	public static final int MULTICAST = 1;
	public static final int UNICAST = 2;
		
	private static int castType;	
	private static int port;	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings_comm);		
	}		
	
	/**
	 * Update cache settings
	 * @param context
	 */
	public static void getSettings(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Resources res = context.getResources();
		
		try {
    		castType = Integer.parseInt(prefs.getString(
    				"cast_type", 
    				res.getStringArray(R.array.cast_types_values)[0]));			
    		broadcastAddr = InetAddress.getByName(prefs.getString(
    				"broadcast_addr", 
    				res.getString(R.string.broadcast_addr_default)));			
    		multicastAddr = InetAddress.getByName(prefs.getString(
    				"multicast_addr", 
    				res.getString(R.string.multicast_addr_default)));
    		unicastAddr = InetAddress.getByName(prefs.getString(
    				"unicast_addr", 
    				res.getString(R.string.unicast_addr_default)));
    		port = Integer.parseInt(prefs.getString(
    				"port", 
    				res.getString(R.string.port_default)));
		}
		catch(UnknownHostException e) {
			Log.d("CommSettings", e.getMessage());
		}
	}
	
	public static int getCastType() {
		return castType;
	}
		
	public static InetAddress getBroadcastAddr() {
		return broadcastAddr;
	}	
	
	public static InetAddress getMulticastAddr() {
		return multicastAddr;
	}
	
	public static InetAddress getUnicastAddr() {
		return unicastAddr;
	}	
	
	public static int getPort() {
		return port;
	}		

}
