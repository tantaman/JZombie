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
		super(SwingEDTAsExecutor.instance);
		
		this.completed = completed;
		this.name = name;
	}
	
	private Item() {
		super(SwingEDTAsExecutor.instance);
	}
	
	public String getName() {
		return name;
	}
	
	// TODO: we really should do some byte code manipulation to generate the emit, check if new val != old val and if we are in a change event.
	public void setName(String newName) {
		name = newName;
		((Model.Listener)emitter.emit).change(this);
	}
	
	public boolean getCompleted() {
		return completed;
	}
	
	public void setCompleted(boolean newCompleted) {
		completed = newCompleted;
		((Model.Listener)emitter.emit).change(this);
	}
	
	@Override
	protected void setCollection(Collection<?, Item> collection) {
		super.setCollection(collection);
		subscribe();
	}
	
	@Override
	protected void endServerReset() {
		((Model.Listener)emitter.emit).change(this);
	}
	
	@Override
	public String toString() {
		return name + " " + completed;
	}
}
