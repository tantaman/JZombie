package com.tantaman.jzombie;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import com.google.gson.annotations.Expose;
import com.tantaman.jzombie.serializers.ISerializer;

public class Collection<T, ItemType extends Model> extends ModelCollectionCommon<T> implements Iterable<ItemType> {
	// TODO: will this get serialized correctly?  Doubtful... so how do we do it?
	// I don't think a type adapter will do it for us...
	@Expose
	private final List<ItemType> items;
	private final Set<Long> addedIds;
	private final Set<Long> addedCids;
	
	public Collection(ExecutorService safeThreads) {
		this(safeThreads, null, null);
	}
	
	// TODO: need to know when fetch completes.
	
	public Collection(ExecutorService safeThreads, ISerializer<String, T> s, Class<?> ... listenerClasses) {
		super(safeThreads, s, addListenerInterface(listenerClasses, Listener.class));
		items = new ArrayList<ItemType>();
		
		addedIds = new HashSet<Long>();
		addedCids = new HashSet<Long>();
	}
	
	// TODO: kinda weird the we expect JSON responses for collection to be wrapped... we need to fix that
	// by implementing our own custom serialize, deserialze and setItemData
	
	@Override
	protected String rootUrl() {
		return "/" + this.getClass().getSimpleName();
	}
	
	@Override
	public Iterator<ItemType> iterator() {
		return items.iterator();
	}
	
	public ItemType at(long id) {
		FakeModel m = new FakeModel(id, -1);
		int idx = items.indexOf(m);
		return items.get(idx);
	}
	
	public ItemType get(int idx) {
		return items.get(idx);
	}
	
	public void add(ItemType item) {
		if (item.id() > 0) {
			if (addedIds.contains(item.id()))
				return;
		} else if (addedCids.contains(item.cid())) {
			return;
		}
		
		items.add(item);
		if (item.id() > 0)
			addedIds.add(item.id());
		else
			addedCids.add(item.cid());
		
		((Listener)emitter.emit).add(item, this);
	}
	
	public boolean remove(ItemType item) {
		return doRemove(item);
	}
	
	public boolean removeById(long id) {
		FakeModel m = new FakeModel(id, -1);
		return doRemove(m);
	}
	
	public boolean removeByCid(long cid) {
		// TODO: we don't really need cid...  we can do identityHashCode...
		FakeModel m = new FakeModel(-1, cid);
		return doRemove(m);
	}
	
	private boolean doRemove(IModelComaprable item) {
		// TODO: don't use index of, instead use our own method with our own comparator
		// so people can freely override hashCode and equals of their models.
		int idx = items.indexOf(item);
		
		if (idx >= 0) {
			ItemType removed = items.remove(idx);
			addedIds.remove(removed.id());
			addedCids.remove(removed.cid());
			((Listener)emitter.emit).remove(removed, this);
			return true;
		}
		
		return false;
	}
	
	@Override
	protected void sync() {
		((Listener)emitter.emit).reset(this);
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
	
	private static class FakeModel implements IModelComaprable {
		private final long id;
		private final long cid;
		
		public FakeModel(long id, long cid) {
			this.id = id;
			this.cid = cid;
		}
		
		@Override
		public long cid() {
			return cid;
		}
		
		@Override
		public long id() {
			return id;
		}
		
		// TODO move these two methods to a shared class
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
			if (obj instanceof IModelComaprable) {
				if (id < 0) {
					return ((IModelComaprable)obj).cid() == cid;
				} else {
					return ((IModelComaprable)obj).id() == id;
				}
			} else {
				return false;
			}
		}
	}
}
