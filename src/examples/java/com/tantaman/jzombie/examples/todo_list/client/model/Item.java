package com.tantaman.jzombie.examples.todo_list.client.model;

import com.google.gson.annotations.Expose;
import com.tantaman.commons.concurrent.executors.SwingEDTAsExecutor;
import com.tantaman.jzombie.Model;

public class Item extends Model<Item> {
	@Expose
	private boolean completed;
	@Expose
	private String name;
	
	public Item(boolean completed, String name) {
		super(SwingEDTAsExecutor.instance, ItemListener.class);
		
		this.completed = completed;
		this.name = name;
	}
	
	private Item() {
		super(SwingEDTAsExecutor.instance, ItemListener.class);
	}
	
	public boolean completed() {
		return completed;
	}
	
	public String name() {
		return name;
	}
	
	// TODO: we really should do some byte code manipulation to generate the emit, check if new val != old val and if we are in a change event.
	public void name(String newName) {
		name = newName;
		((ItemListener)emitter.emit).nameChanged(newName);
	}
	
	public void completed(boolean newCompleted) {
		completed = newCompleted;
		((ItemListener)emitter.emit).completedChanged(completed);
	}
	
	public static interface ItemListener {
		public void nameChanged(String name);
		public void completedChanged(boolean completed);
	}
}
