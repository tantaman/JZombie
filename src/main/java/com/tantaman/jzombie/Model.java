package com.tantaman.jzombie;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

import com.google.gson.annotations.Expose;
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
public class Model<T extends Model<T>> extends ModelCollectionCommon<T> implements IModelComaprable {	
	@Expose
	protected final String id = UUID.randomUUID().toString();
	// TODO: base 64 and uri encode the uuid
	//Base64Utils.trim(new sun.misc.BASE64Encoder().encode(UUIDUtils.asByteArray(
	
	protected static AtomicLong nextCid = new AtomicLong(-1);
	private volatile Collection<?, T> collection;
	
	// TODO: on a post we can expect to get an id back...  how should we handle that?
	// TODO: we need to know if we are in a collection!!!
	/**
	 * @param safeThreads Thread(s) to use when modifying model data after it has been returned by the server and deserialized.
	 */
	// Does submitting to the EDT create a happens-before like submitting to a normal executor...?
	public Model(ExecutorService safeThreads) {
		this(safeThreads, null);
	}
	
	public Model(ExecutorService safeThreads, Class<?> ... listenerInterfaces) {
		this(safeThreads, null, listenerInterfaces);
	}
	
	public Model(ExecutorService safeThreads, ISerializer<String, T> s, Class<?> ... listenerInterfaces) {
		super(safeThreads, s, addListenerInterface(listenerInterfaces, Listener.class));
	}
	
	protected void setCollection(Collection<?, T> collection) {
		this.collection = collection;
	}
	
	// TODO: get out url based on our collection url!
	@Override
	protected String rootUrl() {
		if (this.collection != null) {
			return this.collection.rootUrl();
		} else {
			return "";
		}
	}

	@Override
	public String id() {
		return id;
	}
	
	// TODO: should probably make a model wrapper that implements hash code and equals in this
	// method such that nothing will break when someone overrides hashcode and equals on a model.
	@Override
	public int hashCode() {
		return id().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IModelComaprable) {
			return ((IModelComaprable)obj).id() == id;
		} else {
			return false;
		}
	}
	
	public static interface Listener<T> {
		public void sync(T model);
	}
}
