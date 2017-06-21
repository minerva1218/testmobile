package org.join.wfs.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;

public class WebServer extends Thread {

	static final String SUFFIX_ZIP = "..zip";

	private int port;
	private String webRoot;
	private boolean isLoop = false;

	public WebServer(int port, final String webRoot) {
		super();
		this.port = port;
		this.webRoot = webRoot;
	}

	@Override
	public void run() {
		ServerSocket serverSocket = null;
		try {
			// Create ServerSocket Service
			serverSocket = new ServerSocket(port);
			// Create HTTP Handle
			BasicHttpProcessor httpproc = new BasicHttpProcessor();
			// Add HTTP Interceptor
			httpproc.addInterceptor(new ResponseDate());
			httpproc.addInterceptor(new ResponseServer());
			httpproc.addInterceptor(new ResponseContent());
			httpproc.addInterceptor(new ResponseConnControl());
			// Create HTTP Service
			HttpService httpService = new HttpService(httpproc,
					new DefaultConnectionReuseStrategy(),
					new DefaultHttpResponseFactory());
			// Create HTTP HttpParams
			HttpParams params = new BasicHttpParams();
			params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 5000)
					.setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE,
							8 * 1024)
					.setBooleanParameter(
							CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
					.setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
					.setParameter(CoreProtocolPNames.ORIGIN_SERVER,
							"WebServer/1.1");
			// Set Http Params
			httpService.setParams(params);
			// Create Request Handler Registry
			HttpRequestHandlerRegistry reqistry = new HttpRequestHandlerRegistry();
			// Add Request Handle
			reqistry.register("*" + SUFFIX_ZIP, new HttpZipHandler(webRoot));
			reqistry.register("*", new HttpFileHandler(webRoot));
			// Set Request Handle
			httpService.setHandlerResolver(reqistry);

			/** Circle receive client request*/

			isLoop = true;
			while (isLoop && !Thread.interrupted()) {
				// Receive client request
				Socket socket = serverSocket.accept();

				DefaultHttpServerConnection conn = new DefaultHttpServerConnection();
				conn.bind(socket, params);
				Thread t = new WorkerThread(httpService, conn);
				t.setDaemon(true); // DaemonThread
				t.start();
			}
		} catch (IOException e) {
			isLoop = false;
			e.printStackTrace();
		} finally {
			try {
				if (serverSocket != null) {
					serverSocket.close();
				}
			} catch (IOException e) {
			}
		}
	}

	public void close() {
		isLoop = false;
	}

}