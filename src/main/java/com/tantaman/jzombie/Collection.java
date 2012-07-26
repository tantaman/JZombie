package com.tantaman.jzombie;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.annotations.Expose;

public class Collection<T, ModelType extends Model> extends ModelCollectionCommon<T> implements Iterable<ModelType> {
	// TODO: will this get serialized correctly?  Doubtful... so how do we do it?
	// I don't think a type adapter will do it for us...
	@Expose
	private final List<ModelType> models;
	
	private final Set<String> addedIds;
	private final Type modelType;
	
	public Collection(ExecutorService safeThreads, Type m) {
		this(safeThreads, m, null);
	}
	
	// TODO: need to know when fetch completes.
	
	public Collection(ExecutorService safeThreads, Type m, Class<?> ... listenerClasses) {
		super(safeThreads, addListenerInterface(listenerClasses, Listener.class));
		models = new ArrayList<ModelType>();
		modelType = m;
		addedIds = new HashSet<String>();
	}
	
	protected T setUpdatedData(String data) {
		beginServerReset();
		T result = deserialize("{'models':" + data + "}");
		endServerReset();
		
		return result;
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
	
	public void push(ModelType model) {
		add(model);
	}
	
	public ModelType pop() {
		return doRemove(models.size() - 1);
	}
	
	public void unshift(ModelType model) {
		add(model, 0);
	}
	
	public ModelType shift() {
		return doRemove(0);
	}
	
	public void add(ModelType model) {
		add(model, -1);
	}
	
	public void add(ModelType model, int index) {
		if (addedIds.contains(model.id()))
			return;
		
		model.setCollection(this);
		
		if (index < 0) {
			index = models.size();
			models.add(model);
		} else
			models.add(index, model);
		
		addedIds.add(model.id());
		
		((Listener)emitter.emit).add(model, this, index);
	}
	
	public boolean remove(ModelType item) {
		return doRemove(item);
	}
	
	public boolean removeById(String id) {
		FakeModel m = new FakeModel(id);
		return doRemove(m);
	}
	
	private ModelType doRemove(int index) {
		ModelType model = models.remove(index);
		model.setCollection(null);
		addedIds.remove(model.id());
		((Listener)emitter.emit).remove(model, this, index);
		return model;
	}
	
	private boolean doRemove(IModelComaprable model) {
		// TODO: don't use index of, instead use our own method with our own comparator
		// so people can freely override hashCode and equals of their models.
		int idx = models.indexOf(model);
		
		if (idx >= 0) {
			ModelType removed = models.remove(idx);
			addedIds.remove(removed.id());
			((Listener)emitter.emit).remove(removed, this, idx);
			return true;
		}
		
		return false;
	}
	
	@Override
	protected void beginServerReset() {
		for (ModelType model : models) {
			model.dispose();
		}
	}
	
	@Override
	protected void endServerReset() {
		for (ModelType model : models) {
			model.setCollection(this);
		}
		((Listener)emitter.emit).reset(this);
	}
	
	@Override
	protected String id() {
		return "";
	}
	
	@Override
	protected void handleUnkownUpdateMessage(JSONObject dataObject) {
		try {
			String verb = dataObject.getString("verb");
			switch (verb) {
			case "add":
				break;
			case "remove":
				break;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public static interface Listener<CollectionType, ItemType extends Model> {
		public void add(ItemType item, Collection<CollectionType, ItemType> collection, int index);
		public void remove(ItemType item, Collection<CollectionType, ItemType> collection, int index);
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
