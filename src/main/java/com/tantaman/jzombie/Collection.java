package com.tantaman.jzombie;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import com.google.gson.annotations.Expose;
import com.tantaman.jzombie.serializers.ISerializer;

public class Collection<T, ItemType extends Model> extends ModelCollectionCommon<T> {
	// TODO: will this get serialized correctly?  Doubtful... so how do we do it?
	// I don't think a type adapter will do it for us...
	@Expose
	private final Set<ItemType> items;
	
	public Collection(ExecutorService safeThreads) {
		this(safeThreads, null, null);
	}
	
	// TODO: need to know when fetch completes.
	
	public Collection(ExecutorService safeThreads, ISerializer<String, T> s, Class<?> ... listenerClasses) {
		super(safeThreads, s, addListenerInterface(listenerClasses, Listener.class));
		items = new HashSet<ItemType>();
	}
	
	public void add(ItemType item) {
		items.add(item);
	}
	
	public void remove(ItemType item) {
		boolean removed = items.remove(item);
		if (removed) {
			((Listener)emitter.emit).remove(item, this);
		}
	}
	
	public void removeById(long id) {
		// find by id. . . 
	}
	
	public void removeByCid(long id) {
		
	}

	public void subscribe() {
		// Subscribes to cometd / bayeux at url url()
	}
	
	@Override
	protected long id() {
		return -1;
	}

	public static interface Listener<CollectionType, ItemType extends Model> {
		public void add(ItemType item, Collection<CollectionType, ItemType> collection);
		public void remove(ItemType item, Collection<CollectionType, ItemType> collection);
		public void reset(Collection<CollectionType, ItemType> newModels);
	}
}
