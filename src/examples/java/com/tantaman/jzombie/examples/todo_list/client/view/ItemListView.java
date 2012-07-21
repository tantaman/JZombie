package com.tantaman.jzombie.examples.todo_list.client.view;

import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import com.tantaman.jzombie.Collection;
import com.tantaman.jzombie.examples.todo_list.client.model.Item;
import com.tantaman.jzombie.examples.todo_list.client.model.ItemList;

/**
 * The list of items in the To Do List.
 * @author tantaman
 *
 */
public class ItemListView extends JPanel {
	private ItemList model;
	private ModelListener listener;
	public ItemListView(ItemList model) {
		this.model = model;
		listener = new ModelListener();
		this.model.addListener(listener);
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	}
	
	public Component render() {
		removeAll();
		for (Item item : model) {
			ItemView itemView = new ItemView(item);
			add(itemView.render());
		}
		
		revalidate();
		
		return this;
	}
	
	private class ModelListener implements Collection.Listener<ItemList, Item> {
		@Override
		public void add(Item item, Collection<ItemList, Item> collection) {
			ItemView itemView = new ItemView(item);
			ItemListView.this.add(itemView.render(), 0);
			revalidate();
		}

		@Override
		public void remove(Item item, Collection<ItemList, Item> collection) {
		}

		@Override
		public void reset(Collection<ItemList, Item> newModels) {
			render();
		}
	}
}
