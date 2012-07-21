package com.tantaman.jzombie.examples.todo_list.model;

import com.google.gson.annotations.Expose;
import com.tantaman.commons.concurrent.executors.SwingEDTAsExecutor;
import com.tantaman.jzombie.Model;

public class Item extends Model<Item> {
	@Expose
	private boolean completed;
	@Expose
	private String task;
	
	public Item(boolean completed, String task) {
		super(SwingEDTAsExecutor.instance);
		
		this.completed = completed;
		this.task = task;
	}
	
	public boolean isCompleted() {
		return completed;
	}
	
	public String getTask() {
		return task;
	}
}
