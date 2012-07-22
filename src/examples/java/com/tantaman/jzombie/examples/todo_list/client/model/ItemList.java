package com.tantaman.jzombie.examples.todo_list.client.model;

import com.tantaman.commons.concurrent.executors.SwingEDTAsExecutor;
import com.tantaman.jzombie.Collection;

public class ItemList extends Collection<ItemList, Item> {	
	public ItemList() {
		super(SwingEDTAsExecutor.instance);
	}
	
	@Override
	protected void resetFromServer() {
		super.resetFromServer();
		for (Item item : this) {
			item.subscribe();
		}
	}
}
