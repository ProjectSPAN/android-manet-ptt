package ro.ui.pttdroid.channels;

import java.net.InetAddress;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

public abstract class Channel {
	
	public static final int BROADCAST = 0;
	public static final int MULTICAST = 1;
	public static final int UNICAST   = 2;
	
	public String name = null;
	
	public int port = 2010; // TODO: DEFAULT
	public InetAddress addr = null;
	
	public Channel(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public abstract boolean equals(Object other);
	
	public abstract int getCastType();
	
	public abstract boolean isRecorderEnabled();
	
	public abstract boolean isPlayerEnabled();
	
	public void getSettings(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Resources res = context.getResources();
		
		/*
    	useSpeex = prefs.getBoolean(
    			"use_speex",
    			USE_SPEEX);    		    		
    	speexQuality = Integer.parseInt(prefs.getString(
    			"speex_quality", 
    			res.getStringArray(R.array.speex_quality_values)[0]));
    	echoState = prefs.getBoolean(
    			"echo",
    			ECHO_OFF);    		
    	speakerState = prefs.getBoolean(
    			"speaker",
    			SPEAKER_OFF);
    	*/
	}
}