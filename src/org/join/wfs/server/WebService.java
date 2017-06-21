package org.join.wfs.server;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;

public class WebService extends Service {

	public static final int PORT = 5555;
	public static final String WEBROOT = Environment.getExternalStoragePublicDirectory
			(Environment.DIRECTORY_DCIM)+"/100ANDRO".toString();

	private WebServer webServer;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		webServer = new WebServer(PORT, WEBROOT);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		webServer.setDaemon(true);
		webServer.start();
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		webServer.close();
		super.onDestroy();
	}

}
