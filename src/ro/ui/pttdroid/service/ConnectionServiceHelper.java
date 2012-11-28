package ro.ui.pttdroid.service;

import android.widget.Toast;

public class ConnectionServiceHelper {
				
	// singleton
	private static ConnectionServiceHelper instance = null;
	
	private ConnectionService service = null;
	
	private ConnectionServiceHelper() {}
	
	public static ConnectionServiceHelper getInstance() {
		if (instance == null) {
			instance = new ConnectionServiceHelper();
		}
		return instance;
	}
	
	public void setService(ConnectionService service) {
		this.service = service;
	}
	
	private void displayToastMessage(String message) {
		Toast.makeText(service, message, Toast.LENGTH_LONG).show();
	}
	
	public void setup() {
		service.showNotification("pttdroid is running"); // TODO: use strings.xml
	}
}
