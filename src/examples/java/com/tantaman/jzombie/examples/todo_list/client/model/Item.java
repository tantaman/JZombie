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
		super(SwingEDTAsExecutor.instance);
		
		this.completed = completed;
		this.name = name;
	}
	
	public boolean isCompleted() {
		return completed;
	}
	
	public String getName() {
		return name;
	}
}
