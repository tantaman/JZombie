package com.tantaman.jzombie;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.cometd.bayeux.client.ClientSession;
import org.cometd.client.BayeuxClient;
import org.cometd.client.transport.ClientTransport;
import org.cometd.websocket.client.WebSocketTransport;
import org.eclipse.jetty.websocket.WebSocketClientFactory;

import com.tantaman.commons.concurrent.NamedThreadFactory;

public class BayeuxConfiguration {	
	/**
	 * If the user does not supply a {@link BayeuxConfiguration} then the default instance
	 * will be created and gotten by {@link Model} and {@link Collection}.
	 */
	private static BayeuxConfiguration defaultInstance;
	public static synchronized BayeuxConfiguration getDefaultInstance() {
		return defaultInstance;
	}
	
	public static synchronized BayeuxConfiguration configureDefaultInstance(String serverLocation, ScheduledExecutorService threadPool, WebSocketClientFactory wsClientFactory) throws Exception {
		if (defaultInstance != null) {
			throw new IllegalStateException("The default instance has already been configured for your application!");
		}
		
		if (serverLocation == null)
			serverLocation = "http://localhost/bayeux";
		
		if (threadPool == null)
			threadPool = Executors.newScheduledThreadPool(4, new NamedThreadFactory("JZombie-Client-BayeuxService"));
		
		if (wsClientFactory == null)
			wsClientFactory = new WebSocketClientFactory();
		
		defaultInstance = new BayeuxConfiguration("http://localhost/bayeux", threadPool, wsClientFactory);
		
		return defaultInstance;
	}
	
	private final ScheduledExecutorService bayeuxService;
	private final WebSocketClientFactory webSocketClientFactory;
	private final ClientSession bayeuxClient;
	
	public BayeuxConfiguration(String serverLocation, ScheduledExecutorService threadPool, WebSocketClientFactory wsClientFactory) throws Exception {
		bayeuxService = threadPool;
		webSocketClientFactory = wsClientFactory;
		
		webSocketClientFactory.start();
		
		ClientTransport transport = WebSocketTransport.create(null, webSocketClientFactory, bayeuxService);
    	bayeuxClient = new BayeuxClient(serverLocation, transport);
    	bayeuxClient.handshake();
	}
	
	public ClientSession bayeuxClient() {
		return bayeuxClient;
	}
}
