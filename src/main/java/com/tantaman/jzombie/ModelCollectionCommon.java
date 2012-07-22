package com.tantaman.jzombie;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import org.cometd.bayeux.Message;
import org.cometd.bayeux.client.ClientSession;
import org.cometd.bayeux.client.ClientSessionChannel;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import com.tantaman.commons.Fn;
import com.tantaman.commons.listeners.AbstractMultiEventSource;
import com.tantaman.jzombie.serializers.GSonSerializer;
import com.tantaman.jzombie.serializers.ISerializer;

/**
 * This class is in a very early exploratory phase.  The 
 * pub/sub update feature is currently just hacked in to explore
 * different types of interfaces.
 * 
 * Since this is the case, custom serializers can be written for all things EXCEPT the pub/sub interface.
 * The pub/sub interface requires specifically formatted json data at the moment.
 * 
 * @author tantaman
 *
 * @param <T>
 */
// TODO: need to sync publishes with fetches!!!  How shall we know which comes first : the fetch or publish?  Maybe fetches should return on the publish channel if we find ourselves subscribed....
// I assume we can't do publishes on the same socket as fetches or can we???
// Other: the server can fill in a seq number for us and we can discard anything less than the latest seq we have received
// We could use the ETag field of the HTTP response headers!!
@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class ModelCollectionCommon<T> extends AbstractMultiEventSource {
	private final ClientSession bayeuxClient;
	protected final ISerializer<String, T> serializer;
	
	// TODO: eventaully we should generalize to not be HTTP only.
	// Could take an IClient.
	private final AsyncHttpClient asyncHttpClient;
	private final ExecutorService safeThreads;
	private final Object receiveLock = new Object();
	
	private ClientSessionChannel updateChannel;
	/** non volatile since access to etag is controlled by receiveLock **/
	private volatile String etag;
	
	// TODO: fetch returns need to be synchronized?
	// Synch them with publish receptions at least so synch em anyhow...
	public ModelCollectionCommon(ExecutorService safeThreads, Class[] listenerClasses) {
		super(true, listenerClasses);
		
		// TODO: allow this to be configurable
		bayeuxClient = BayeuxConfiguration.getDefaultInstance().bayeuxClient();
		serializer = createSerializer();
		//updateMessageSerializer = createUpdateMessageSerializer();
		
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
	
	protected abstract String id();
	protected abstract String rootUrl();
	
	protected String url() {
		return host() + rootUrl() + "/" + id();
	}
	
	protected String channel() {
		String id = id();
		if (id == "" || id == null) {
			return rootUrl();
		} else {
			return rootUrl() + "/" + id();
		}
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
	    	etag = etag();
			asyncHttpClient.preparePut(url())
				.addHeader("Content-Type", "application/json")
				.addQueryParameter("etag", etag)
				.setBody(data)
				.execute(handler);
	    } catch (IOException e) {
	    	if (err != null)
	    		err.fn(e);
	    }
	}
	
	protected String etag() {
		return UUID.randomUUID().toString();
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
		System.out.println("DISPOSING");
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
			if (strEtag != null && strEtag != "") {
				if (strEtag.equals(etag))
					return;
				etag = strEtag;
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
	
	private T setUpdatedData(String data) {
		beginServerReset();
		T result = deserialize(data);
		endServerReset();
		
		return result;
	}
	
	protected void fieldSet(Field f) {}
	protected void beginServerReset() {}
	protected void endServerReset() {}
	
	public String serialize() {
		return serializer.serialize((T) this);
	}
	
	public T deserialize(String data) {
		return serializer.deserialize(data, (Class<T>)this.getClass());
	}
	
	// TODO: ugh.. the serializer should take care of this.
	// The ability to pass in custom serializers is now effectively useless.
	protected void handleUnkownUpdateMessage(JSONObject dataObject) {}
	private void updateMessageReceived(String json) {
		synchronized (receiveLock) {
			try {
				JSONObject messageObject = new JSONObject(new JSONTokener(json));

				JSONObject dataObject = (JSONObject) messageObject.get("data");
				String verb = dataObject.getString("verb");
				if (dataObject.has("etag")) {
					String strEtag = dataObject.getString("etag");
					if (strEtag.equals(etag)) {// equivalent objects
						return;
					} else {
						// Update our etag to what we received.
						// This resolves the race condition of
						// 1. updating our model locally
						// 2. receiving a model update from the server
						// 3. publishing our local model update to the server (server and local state are now out of sync)
						// Since the etag has been updated then we won't ignore the publish we sent out
						// when it comes back to us.
						etag = strEtag;
					}
				}
				switch (verb) {
				case "update":
				case "reset":
					final String modelObject = dataObject.get("model").toString();
					safeThreads.execute(new Runnable() {
						@Override
						public void run() {
							setUpdatedData(modelObject);
						}
					});
					break;
				default:
					handleUnkownUpdateMessage(dataObject);
					break;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	private class BayeuxMessageListener implements ClientSessionChannel.MessageListener {
		@Override
		public void onMessage(ClientSessionChannel channel, Message message) {
			updateMessageReceived(message.getJSON());
		}
	}
}
