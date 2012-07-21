package com.tantaman.jzombie;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;

import com.google.gson.annotations.Expose;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import com.tantaman.commons.Fn;
import com.tantaman.commons.lang.ObjectUtils;
import com.tantaman.commons.listeners.AbstractMultiEventSource;
import com.tantaman.jzombie.serializers.GSonSerializer;
import com.tantaman.jzombie.serializers.ISerializer;

public abstract class ModelCollectionCommon<T> extends AbstractMultiEventSource {
	private final ISerializer<String, T> serializer;
	
	// TODO: eventaully we should generalize to not be HTTP only
	// could take an IClient
	private final AsyncHttpClient asyncHttpClient;
	private final ExecutorService safeThreads;
	
	public ModelCollectionCommon(ExecutorService safeThreads, ISerializer<String, T> s, Class[] listenerClasses) {
		super(true, listenerClasses);
		
		if (s == null) {
			serializer = createDefaultSerializer();
		} else {
			serializer = s;
		}
		
		asyncHttpClient = new AsyncHttpClient();
		this.safeThreads = safeThreads;
	}
	
	protected ISerializer<String, T> createDefaultSerializer() {
		return new GSonSerializer();
	}
	
	protected abstract long id();
		
	protected String rootUrl() {
		return "";
	}
	
	protected String url() {
		return host() + rootUrl() + "/" + getIdString();
	}
	
	protected String host() {
		return "http://localhost";
	}
	
	protected String getIdString() {
		if (id() < 0)
			return "";
		else
			return Long.toString(id());
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
	
	public void save(Fn<Void, T> success, Fn<Void, Throwable> err) throws IOException {
		save(serialize(), success, err);
	}
	
	public void save(String data, final Fn<Void, T> success, final Fn<Void, Throwable> err) throws IOException {
		String rootUrl = rootUrl();
		
		if (rootUrl == "")
			return;
		
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
		
		if (this.id() < 0) {
			asyncHttpClient.preparePost(url()).execute(handler);
		} else {
			asyncHttpClient.preparePut(url()).execute(handler);
		}
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
	
	public abstract void subscribe();
	
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
				throw new IllegalArgumentException("Unexpected return code"); // TODO: make a custom exception for this
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
				throw new IllegalArgumentException("Unexpected return code");
			}
		} catch (Exception e) {
			if (err != null)
				err.fn(e);
			else
				e.printStackTrace();
		}
	}
	
	private void handleServerDataReturn(Response response, final Fn<Void, T> success) throws IOException {
		String body = response.getResponseBody();
		final T result = serializer.deserialize(body, (Class<T>)this.getClass());
		
		safeThreads.execute(new Runnable() {
			@Override
			public void run() {
				setUpdatedData(result);
				if (success != null)
					success.fn(result);
			}
		});
	}
	
	protected void setUpdatedData(Object data) {
		Field [] fields = ObjectUtils.setFields(this, data, new Fn<Boolean, Field>() {
			@Override
			public Boolean fn(Field param) {
				return param.getAnnotation(Expose.class) != null;
			}
		}, ModelCollectionCommon.class);
		
		dataSetFromServer();
		changed();
	}
	
	protected void fieldSet(Field f) {}
	protected void changed() {}
	protected void dataSetFromServer() {}
	
	public String serialize() {
		return serializer.serialize((T) this);
	}
	
	public T deserialize(String data) {
		return serializer.deserialize(data, (Class<T>)this.getClass());
	}
}
