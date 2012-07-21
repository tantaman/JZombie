package com.tantaman.jzombie;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import com.google.gson.annotations.Expose;
import com.tantaman.jzombie.serializers.ISerializer;

public class Collection<T, ItemType extends Model> extends ModelCollectionCommon<T> {
	// TODO: will this get serialized correctly?  Doubtful... so how do we do it?
	// I don't think a type adapter will do it for us...
	@Expose
	private final List<ItemType> items;
	
	public Collection(ExecutorService safeThreads, ISerializer<String, T> s, Class<?> ... listenerClasses) {
		super(safeThreads, s, addListenerInterface(listenerClasses, Listener.class));
		items = new ArrayList<ItemType>();
	}
	
	public void add() {
		
	}
	
	public void remove() {
		
	}

	public void subscribe() {
		// Subscribes to cometd / bayeux at url url()
	}
	
	@Override
	protected long id() {
		return -1;
	}

	public static interface Listener {
		public void add();
		public void remove();
		public void reset();
	}
}
