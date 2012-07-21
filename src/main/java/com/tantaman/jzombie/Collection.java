package com.tantaman.jzombie;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import com.google.gson.annotations.Expose;
import com.tantaman.jzombie.serializers.ISerializer;

public class Collection<T, ModelType extends Model> extends ModelCollectionCommon<T> implements Iterable<ModelType> {
	// TODO: will this get serialized correctly?  Doubtful... so how do we do it?
	// I don't think a type adapter will do it for us...
	@Expose
	private final List<ModelType> models;
	private final Set<String> addedIds;
	
	public Collection(ExecutorService safeThreads) {
		this(safeThreads, null, null);
	}
	
	// TODO: need to know when fetch completes.
	
	public Collection(ExecutorService safeThreads, ISerializer<String, T> s, Class<?> ... listenerClasses) {
		super(safeThreads, s, addListenerInterface(listenerClasses, Listener.class));
		models = new ArrayList<ModelType>();
		
		addedIds = new HashSet<String>();
	}
	
	// TODO: kinda weird the we expect JSON responses for collection to be wrapped... we need to fix that
	// by implementing our own custom serialize, deserialze and setItemData
	
	protected boolean canSubscribe() {
		return true;
	}
	
	@Override
	protected String rootUrl() {
		return "/" + this.getClass().getSimpleName();
	}
	
	@Override
	public Iterator<ModelType> iterator() {
		return models.iterator();
	}
	
	public ModelType at(String id) {
		FakeModel m = new FakeModel(id);
		int idx = models.indexOf(m);
		return models.get(idx);
	}
	
	public ModelType get(int idx) {
		return models.get(idx);
	}
	
	public void add(ModelType item) {
		if (addedIds.contains(item.id()))
			return;
		
		item.setCollection(this);
		
		models.add(item);
		addedIds.add(item.id());
		
		((Listener)emitter.emit).add(item, this);
	}
	
	public boolean remove(ModelType item) {
		return doRemove(item);
	}
	
	public boolean removeById(String id) {
		FakeModel m = new FakeModel(id);
		return doRemove(m);
	}
	
	private boolean doRemove(IModelComaprable item) {
		// TODO: don't use index of, instead use our own method with our own comparator
		// so people can freely override hashCode and equals of their models.
		int idx = models.indexOf(item);
		
		if (idx >= 0) {
			ModelType removed = models.remove(idx);
			addedIds.remove(removed.id());
			((Listener)emitter.emit).remove(removed, this);
			return true;
		}
		
		return false;
	}
	
	@Override
	protected void resetFromServer() {
		for (ModelType model : models) {
			model.setCollection(this);
		}
		((Listener)emitter.emit).reset(this);
	}
	
	@Override
	protected String id() {
		return "";
	}

	public static interface Listener<CollectionType, ItemType extends Model> {
		public void add(ItemType item, Collection<CollectionType, ItemType> collection);
		public void remove(ItemType item, Collection<CollectionType, ItemType> collection);
		public void reset(Collection<CollectionType, ItemType> newModels);
	}
	
	private static class FakeModel implements IModelComaprable {
		private final String id;
		
		public FakeModel(String id) {
			this.id = id;
		}
		
		@Override
		public String id() {
			return id;
		}
		
		// TODO move these two methods to a shared class
		@Override
		public int hashCode() {
			return id.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof IModelComaprable) {
				return ((IModelComaprable)obj).id() == id;
			} else {
				return false;
			}
		}
	}
}
