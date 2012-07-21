package com.tantaman.jzombie;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.ExecutorService;

import com.google.gson.annotations.Expose;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import com.tantaman.commons.Fn;
import com.tantaman.commons.lang.ObjectUtils;
import com.tantaman.jzombie.serializers.GSonSerializer;
import com.tantaman.jzombie.serializers.ISerializer;

/*
 * When using this class, please ensure that you understand
 * the actions that cause a `Happens Before` relationship.
 * 
 * This is necessary since you will likely be passing model data
 * between threads.
 * http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/package-summary.html#MemoryVisibility
 * 
 * The main point to know is: Actions in a thread prior to the submission of a Runnable to an Executor happen-before its execution begins. Similarly for Callables submitted to an ExecutorService.
 */
public class Model<T extends Model<T>> {
	private final ISerializer<String, T> serializer;
	// TODO: eventaully we should generalize to not be HTTP only
	// could take an IClient
	private final AsyncHttpClient asyncHttpClient;
	private static String testString;
	
	@Expose
	protected volatile long id = -1;
	
	/**
	 * 
	 * @param safeThreads Thread(s) to use when modifying model data after it has been returned by the server and deserialized.
	 */
	// Does submitting to the EDT create a happens-before like submitting to a normal executor...?
	
	public Model(ExecutorService safeThreads) {
		this(safeThreads, null);
	}
	
	public Model(ExecutorService safeThreads, ISerializer<String, T> s) {
		if (s == null) {
			serializer = new GSonSerializer();
		} else {
			serializer = s;
		}
		
		asyncHttpClient = new AsyncHttpClient();
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	protected String rootUrl() {
		return "";
	}
	
	private String url() {
		return host() + rootUrl() + "/" + getIdString();
	}
	
	protected String host() {
		return "http://localhost";
	}
	
	protected String getIdString() {
		if (id < 0)
			return "";
		else
			return Long.toString(id);
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
		
		if (this.id < 0) {
			asyncHttpClient.preparePost(url()).execute(handler);
		} else {
			asyncHttpClient.preparePut(url()).execute(handler);
		}
	}
	
	public void fetch(final Fn<Void, T> success, final Fn<Void, Throwable> err) throws IOException {
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
	
	private void handleServerDataReturn(Response response, Fn<Void, T> success) throws IOException {
		String body = response.getResponseBody();
		T result = serializer.deserialize(body, (Class<T>)this.getClass());
		setUpdatedData(result);
		if (success != null)
			success.fn(result);
	}
	
	protected void setUpdatedData(Object data) {
		Field [] fields = ObjectUtils.setFields(this, data, new Fn<Boolean, Field>() {
			@Override
			public Boolean fn(Field param) {
				return param.getAnnotation(Expose.class) != null;
			}
		});
		
		changed();
	}
	
	protected void fieldSet(Field f) {
		
	}
	
	protected void changed() {}
	
	public String serialize() {
		return serializer.serialize((T) this);
	}
}
