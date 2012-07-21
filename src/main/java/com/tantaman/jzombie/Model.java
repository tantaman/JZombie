package com.tantaman.jzombie;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicLong;

import com.google.gson.annotations.Expose;
import com.tantaman.commons.concurrent.NamedThreadFactory;
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
public class Model<T extends Model<T>> extends ModelCollectionCommon<T> {	
	@Expose
	protected volatile long id = -1;
	protected volatile long cid = -1;
	
	protected static AtomicLong nextCid = new AtomicLong(-1);
	
	// TODO: on a post we can expect to get an id back...  how should we handle that?
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
		
		cid = nextCid.incrementAndGet();
	}
	
	public void subscribe() {
		// Subscribes to cometd / bayeux at url url()
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public static interface Listener {
		public void change();
	}

	@Override
	protected long id() {
		return id;
	}
	
	// TODO: should probably make a model wrapper that implements hash code and equals in this
	// method such that nothing will break when someone overrides hashcode and equals on a model.
	@Override
	public int hashCode() {
		int hashCode;
		if (id < 0)
			hashCode = (int)cid;
		else
			hashCode = (int)id;
		
		return hashCode;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Model) {
			if (id < 0) {
				return ((Model)obj).cid == cid;
			} else {
				return ((Model)obj).id == id;
			}
		} else {
			return false;
		}
	}
}
