package com.tantaman.jzombie;

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
public class Model<T extends Model<T>> extends ModelCollectionCommon<T> {	
	@Expose
	protected volatile long id = -1;
	protected volatile long cid = -1;
	
	protected static AtomicLong nextCid = new AtomicLong(-1);
	
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
}
