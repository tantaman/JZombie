package com.tantaman.jzombie;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import com.google.gson.annotations.Expose;
import com.tantaman.jzombie.serializers.ISerializer;

public class Collection<T, ItemType extends Model> extends ModelCollectionCommon<T> {
	// TODO: will this get serialized correctly?  Doubtful... so how do we do it?
	// I don't think a type adapter will do it for us...
	@Expose
	private final List<ItemType> items;
	
	private final Map<Long, ItemType> itemsById;
	private final Map<Long, ItemType> itemsByCid;
	
	public Collection(ExecutorService safeThreads) {
		this(safeThreads, null, null);
	}
	
	// TODO: need to know when fetch completes.
	
	public Collection(ExecutorService safeThreads, ISerializer<String, T> s, Class<?> ... listenerClasses) {
		super(safeThreads, s, addListenerInterface(listenerClasses, Listener.class));
		items = new ArrayList<ItemType>();
		itemsById = new ConcurrentHashMap<Long, ItemType>();
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
	
	public void remove(long id) {
		// find by id. . . 
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
