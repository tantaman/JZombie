package com.tantaman.jzombie.examples.todo_list.model;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.tantaman.commons.concurrent.executors.SwingEDTAsExecutor;
import com.tantaman.jzombie.Model;

public class ItemList extends Model<ItemList> {
	@Expose
	private final List<Item> items;
	
	public ItemList() {
		super(SwingEDTAsExecutor.instance, Listener.class);
		
		items = new ArrayList<Item>();
	}
	
	@Override
	protected String rootUrl() {
		return "/todo";
	}
	
	public List<Item> getItems() {
		return items;
	}
	
	public static interface Listener {
		public void itemAdded(Item item);
		public void reset();
	}
}
