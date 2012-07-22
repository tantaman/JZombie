package com.tantaman.jzombie.examples.todo_list.client.model;

import com.tantaman.commons.concurrent.executors.SwingEDTAsExecutor;
import com.tantaman.jzombie.Collection;

public class ItemList extends Collection<ItemList, Item> {	
	public ItemList() {
		super(SwingEDTAsExecutor.instance, Item.class);
		subscribe();
	}
	
	@Override
	protected void endServerReset() {
		super.endServerReset();
		System.out.println("Get reset...");
	}
}
