package com.tantaman.jzombie.examples.todo_list.client.model;

import com.google.gson.annotations.Expose;
import com.tantaman.commons.concurrent.executors.SwingEDTAsExecutor;
import com.tantaman.jzombie.Collection;
import com.tantaman.jzombie.Model;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class Item extends Model<Item> {
	@Expose
	private boolean completed;
	@Expose
	private String name;
	
	public Item(boolean completed, String name) {
		super(SwingEDTAsExecutor.instance); //, ItemListener.class
		
		this.completed = completed;
		this.name = name;
	}
	
	private Item() {
		super(SwingEDTAsExecutor.instance); //, ItemListener.class
	}
	
	@Override
	protected void setCollection(Collection<?, Item> collection) {
		super.setCollection(collection);
		subscribe();
	}
	
	public boolean completed() {
		return completed;
	}
	
	public String name() {
		return name;
	}
	
	@Override
	protected void endServerReset() {
		System.out.println("CHANGED ON SERVER");
		// TODO: base model class needs to tell us if anything actually changed within the model...
		((Model.Listener)emitter.emit).change(this);
	}
	
	// TODO: we really should do some byte code manipulation to generate the emit, check if new val != old val and if we are in a change event.
	public void name(String newName) {
		name = newName;
		((Model.Listener)emitter.emit).change(this);
	}
	
	public void completed(boolean newCompleted) {
		completed = newCompleted;
		((Model.Listener)emitter.emit).change(this);
	}
	
	@Override
	public String toString() {
		return name + " " + completed;
	}
	
//	public static interface ItemListener {
//		public void nameChanged(String name);
//		public void completedChanged(boolean completed);
//	}
}
