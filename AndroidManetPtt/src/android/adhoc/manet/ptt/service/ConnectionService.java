package android.adhoc.manet.ptt.service;

import android.adhoc.manet.ptt.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

public class ConnectionService extends Service {

	private final static String TAG = "ConnectionService";
    
	// unique id for the notification
	private static final int NOTIFICATION_ID = 0;
    
	private final Messenger receiveMessenger = new Messenger(new IncomingHandler());
	
	// notification management
	private NotificationManager notificationManager = null;
	
	private Notification notification = null;
	
	private PendingIntent pendingIntent = null;
	
	private ConnectionServiceHelper helper = null;

	@Override
	public IBinder onBind(Intent intent) {
		return receiveMessenger.getBinder();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate()"); // DEBUG
				
		// android.os.Debug.waitForDebugger(); // DEBUG
		
		// notification management
		notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		notificationManager.cancelAll(); // in case service was force-killed
				
		helper = ConnectionServiceHelper.getInstance();
		helper.setService(this);
		helper.setup();
		
		// TODO: After "No longer want ..." eventually kills the service,
		// this method will be called. Gracefully resume operations.
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy()"); // DEBUG
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Log.d(TAG, "onStart"); // DEBUG
	}
	
	// called by the system every time a client explicitly starts the service by calling startService(Intent)
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand"); // DEBUG
		return START_STICKY; // run service until explicitly stopped   
	}
	
	public void showNotification(String content) {
		
		int icon = R.drawable.icon;

    	if (notification == null || notification.icon != icon) {    		
	    	// set the icon, ticker text, and timestamp        
	    	notification = new Notification(icon, content, System.currentTimeMillis());
	    	
	    	// try to prevent service from being killed with "no longer want";
	    	// this only prolongs the inevitable
	    	startForeground(NOTIFICATION_ID, notification);
	
	    	// pending intent to launch main activity if the user selects notification        
	    	// pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, DummyActivity.class), 0);
	    	
	    	Intent launchIntent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
	    	launchIntent.setComponent(new ComponentName("android.adhoc.manet.ptt", "android.adhoc.manet.ptt.Main"));
	    	pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
	    	
	    	// don't allow user to clear notification
	    	notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT;
    	} else {
    		// set the ticker text
    		notification.tickerText = content;
    	}

    	// set the info for the views that show in the notification panel    
    	notification.setLatestEventInfo(this, "pttdroid", content, pendingIntent);
    	
    	// send the notification        
    	notificationManager.notify(NOTIFICATION_ID, notification);
    }
	
	private class IncomingHandler extends Handler {    
		
		@Override        
		public void handleMessage(Message rxmessage) {    
			switch (rxmessage.what) {
			
				// TODO
			
				default:                    
					super.handleMessage(rxmessage);
			}
		}
	}
}