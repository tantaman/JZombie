package com.tantaman.jzombie;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.cometd.bayeux.Message;
import org.cometd.bayeux.client.ClientSession;
import org.cometd.bayeux.client.ClientSessionChannel;
import org.cometd.client.BayeuxClient;
import org.cometd.client.transport.ClientTransport;
import org.cometd.websocket.client.WebSocketTransport;
import org.eclipse.jetty.websocket.WebSocketClientFactory;

import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import com.tantaman.commons.Fn;
import com.tantaman.commons.concurrent.NamedThreadFactory;
import com.tantaman.commons.listeners.AbstractMultiEventSource;
import com.tantaman.jzombie.serializers.GSonSerializer;
import com.tantaman.jzombie.serializers.ISerializer;

// TODO: need to sync publishes with fetches!!!
// I assume we can't do publishes on the same socket as fetches or can we???
// Other: the server can fill in a seq number for us and we can discard anything less than the latest seq we have received
// We could use the ETag field of the HTTP response headers!!
@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class ModelCollectionCommon<T> extends AbstractMultiEventSource {
	private static final ScheduledExecutorService bayeuxService = Executors.newScheduledThreadPool(4, new NamedThreadFactory("JZombie-Client-BayeuxService"));
	private static final WebSocketClientFactory webSocketClientFactory = new WebSocketClientFactory();
	private static final ClientSession bayeuxClient;
	static {
		try {
			webSocketClientFactory.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		ClientTransport transport = WebSocketTransport.create(null, webSocketClientFactory, bayeuxService);
		// TODO: THIS NEEDS TO BE CONFIGURABLE!  And even configurable by object if the user so desires!
		// Is the bayeux client light weight enough to make one per object? . . . .
    	bayeuxClient = new BayeuxClient("http://localhost/bayeux", transport);
    	bayeuxClient.handshake();
	}
	
	protected final ISerializer<String, T> serializer;
	
	/**
	 * Type erasure forces us to to ModelUpdateMessage instead of ModelUpdateMessage<T>
	 */
	private final ISerializer<String, ModelUpdateMessage> updateMessageSerializer;
	
	// TODO: eventaully we should generalize to not be HTTP only.
	// Could take an IClient.
	private final AsyncHttpClient asyncHttpClient;
	private final ExecutorService safeThreads;
	private final Object receiveLock = new Object();
	
	private ClientSessionChannel updateChannel;
	/** non volatile since access to etag is controlled by receiveLock **/
	private long etag;
	
	// TODO: fetch returns need to be synchronized?
	// Synch them with publish receptions at least so synch em anyhow...
	public ModelCollectionCommon(ExecutorService safeThreads, Class[] listenerClasses) {
		super(true, listenerClasses);
		
		serializer = createSerializer();
		updateMessageSerializer = createUpdateMessageSerializer();
		
		asyncHttpClient = new AsyncHttpClient();
		this.safeThreads = safeThreads;
	}
	
	protected ISerializer<String, T> createSerializer() {
		GsonBuilder builder = createBuilder();
		return new GSonSerializer(builder.create());
	}
	
	private GsonBuilder createBuilder() {
		GsonBuilder builder = new GsonBuilder();
		final T self = (T)this;
		
		builder.registerTypeAdapter(this.getClass(), new InstanceCreator<T>() {
			@Override
			public T createInstance(Type arg0) {
				return self;
			}
		}).excludeFieldsWithoutExposeAnnotation();
		
		return builder;
	}
	
	protected ISerializer<String, ModelUpdateMessage> createUpdateMessageSerializer() {
		GsonBuilder builder = createBuilder();
		
		builder.registerTypeAdapter(ModelCollectionCommon.class, new InstanceCreator<ModelCollectionCommon>() {
			@Override
			public ModelCollectionCommon createInstance(Type type) {
				return ModelCollectionCommon.this;
			}
		});
		
		return new GSonSerializer(builder.create());
	}
	
	protected abstract String id();
	protected abstract String rootUrl();
	
	protected String url() {
		return host() + rootUrl() + "/" + id();
	}
	
	protected String channel() {
		return rootUrl() + "/" + id();
	}
	
	protected String host() {
		return "http://localhost";
	}

	protected static Class<?> [] addListenerInterface(Class<?> [] listenerInterfaces, Class<?> interf) {
		if (listenerInterfaces != null) {
			listenerInterfaces = Arrays.copyOf(listenerInterfaces, listenerInterfaces.length + 1);
			listenerInterfaces[listenerInterfaces.length - 1] = interf;
		} else {
			listenerInterfaces = new Class<?> [] {interf};
		}
		
		return listenerInterfaces;
	}
	
	public void save() {
		System.out.println("Saving...");
		save(null, null);
	}
	
	public void save(Fn<Void, T> success, Fn<Void, Throwable> err) {
		save(serialize(), success, err);
	}
	
	public void save(String data, final Fn<Void, T> success, final Fn<Void, Throwable> err) {
		AsyncCompletionHandler<Response> handler = new AsyncCompletionHandler<Response>(){
	        @Override
	        public Response onCompleted(Response response) throws Exception {
	        	saveCompleted(response, success, err);
	            return response;
	        }

	        @Override
	        public void onThrowable(Throwable t) {
	        	if (err != null)
	        		err.fn(t);
	        	else
	        		t.printStackTrace();
	        }
	    };
		
	    try {
				System.out.println("Putting to url: " + url());
				asyncHttpClient.preparePut(url())
					.addHeader("Content-Type", "application/json")
					.setBody(serialize())
					.execute(handler);
	    } catch (IOException e) {
	    	if (err != null)
	    		err.fn(e);
	    }
	}
	
	public void fetch() throws IOException {
		fetch(null, null, null);
	}
	
	public void fetch(final Fn<Void, T> success, final Fn<Void, Throwable> err, String [] options) throws IOException {
		AsyncCompletionHandler<Response> handler = new AsyncCompletionHandler<Response>(){
	        @Override
	        public Response onCompleted(Response response) throws Exception {
	        	fetchCompleted(response, success, err);
	            return response;
	        }

	        @Override
	        public void onThrowable(Throwable t){
	        	if (err != null)
	        		err.fn(t);
	        	else
	        		t.printStackTrace();
	        }
	    };
	    
	    asyncHttpClient.prepareGet(url()).execute(handler);
	}
	
	public boolean isSubscribed() {
		return updateChannel != null;
	}
	
	// TODO: shouldn't synchronize on this!
	public synchronized void unsubscribe() {
		if (updateChannel != null) {
			updateChannel.unsubscribe();
			updateChannel.release();
			updateChannel = null;
		}
	}
	
	/**
	 * Will either subscribe for updates from the Bayeux server or
	 * set a flag indicating that the object wants a subscription.
	 * 
	 * An object is flagged as wanting a subscription
	 * if it tries to subscribe while its update channel url is still
	 * malformed.  Once the update channel url is well formed
	 * subscription will take place.
	 */
	public synchronized void subscribe() {
		if (updateChannel == null) {
	        // Can I start subscribing or does handshake need to actually complete first?
	        // TODO: we need to clean up the Bayeux client correctly when the model is no longer used.
	        updateChannel = bayeuxClient.getChannel(channel());
	        System.out.println("Trying to subscribe: " + channel());
	        updateChannel.subscribe(new BayeuxMessageListener());
		}
	}
	
    // TODO: use GCNotifier to handle clean up
	// so client code doesn't need to worry about disposing models.
	public synchronized void dispose() {
		unsubscribe();
	}
	
	private void fetchCompleted(Response response, Fn<Void, T> success, Fn<Void, Throwable> err) {
		try {
			int statusCode = response.getStatusCode();
			switch (statusCode) {
			case 200:
				handleServerDataReturn(response, success);
				break;
			case 201:
			case 202:
			case 203:
			case 204:
			case 205:
			case 206:
			default:
				throw new IllegalArgumentException("Unexpected return code: " + statusCode + " url: " + url()); // TODO: make a custom exception for this
			}
		} catch (Exception e) {
			if (err != null)
				err.fn(e);
			else
				e.printStackTrace();
		}
	}
	
	private void saveCompleted(Response response, Fn<Void, T> success, Fn<Void, Throwable> err) {
		try {
			int statusCode = response.getStatusCode();
			switch (statusCode) {
			case 200:
				handleServerDataReturn(response, success);
				break;
			case 204:
				if (success != null)
					success.fn(null);
				break;
			default:
				throw new IllegalArgumentException("Unexpected return code: " + statusCode + " url: " + url()); // TODO: make a custom exception for this
			}
		} catch (Exception e) {
			if (err != null)
				err.fn(e);
			else
				e.printStackTrace();
		}
	}
	
	private void handleServerDataReturn(Response response, final Fn<Void, T> success) throws IOException {
		final String body;
		synchronized (receiveLock) {
			String strEtag = response.getHeader("ETag");
			if (strEtag != null) {
				long tag = Long.parseLong(strEtag);
				if (tag < etag)
					return;
				etag = tag;
			} else {
				System.out.println("No etag");
			}
			
			body = response.getResponseBody();
		}
		
		safeThreads.execute(new Runnable() {
			@Override
			public void run() {
				//setUpdatedData(result);
				T result = setUpdatedData(body);
				if (success != null)
					success.fn(result);
			}
		});
	}
	
	private T setUpdatedData(String json) {
		T result = deserialize(json);
		
		resetFromServer();
		
		return result;
	}
	
//	protected void setUpdatedData(T data) {
//		boolean idChanged = false;
//		if (((ModelCollectionCommon)data).id() != id()) {
//			idChanged = true;
//		}
//		
//		Field [] fields = ObjectUtils.setFields(this, data, new Fn<Boolean, Field>() {
//			@Override
//			public Boolean fn(Field param) {
//				return param.getAnnotation(Expose.class) != null;
//			}
//		}, ModelCollectionCommon.class);
//		
//		if (idChanged) {
//			idChanged();
//		}
//		
//		resetFromServer();
//		changed();
//	}
	
	protected void fieldSet(Field f) {}
	protected void resetFromServer() {}
	
	public String serialize() {
		return serializer.serialize((T) this);
	}
	
	public T deserialize(String data) {
		return serializer.deserialize(data, (Class<T>)this.getClass());
	}
	
	private class BayeuxMessageListener implements ClientSessionChannel.MessageListener {
		@Override
		public void onMessage(ClientSessionChannel channel, Message message) {
			System.out.println("GOT A MESSAGE! WOO!!");
			String json = message.getJSON();
			
			// TODO: we need to peek at the message first and pull its sequence number and discard it if it is old!
			// TODO: we can also keep a digest of the last serialization of our model.
			// if the digest of the returned serialization of the same as our last digest
			// then discard the message as well.
			ModelUpdateMessage msg = updateMessageSerializer.deserialize(json, ModelUpdateMessage.class);
			System.out.println(msg.data);
			System.out.println(ModelCollectionCommon.this);
			System.out.println("Emitting");
			resetFromServer();
			//System.out.println(msg);
			//System.out.println(json);
		}
	}
}
