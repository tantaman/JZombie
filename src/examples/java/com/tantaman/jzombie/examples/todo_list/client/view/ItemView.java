package com.tantaman.jzombie.examples.todo_list.client.view;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.font.TextAttribute;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.tantaman.jzombie.Model;
import com.tantaman.jzombie.examples.todo_list.client.model.Item;

/**
 * The view for one item, or line, in the To Do List.
 * @author tantaman
 *
 */
public class ItemView extends JPanel {
	private Item model;
	private final ItemListener listener = new ItemListener();
	
	private JLabel name;
	private JCheckBox completed;
	
	private Map fontAttrs;
	
	public ItemView(Item model) {
		// This setting up of the components would be moved to a template class
		this.model = model;
		this.model.addListener(listener);
		
		name = new JLabel();
		completed = new JCheckBox();
		
		fontAttrs = new Font("Serif", Font.PLAIN, 12).getAttributes();
		
		add(completed);
		add(name);
		
		bind();
	}
	
	private void bind() {
		completed.addItemListener(new java.awt.event.ItemListener() {			
			@Override
			public void itemStateChanged(ItemEvent e) {
				System.out.println("ITEM EVENT");
				if (e.getStateChange() == ItemEvent.SELECTED) {
					model.completed(true);
				} else {
					model.completed(false);
				}
			}
		});
	}
	
	public Component render() {
		name.setText(model.name());
		completed.setSelected(model.completed());
		
		
		if (model.completed()) {
			fontAttrs.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
		} else {
			fontAttrs.remove(TextAttribute.STRIKETHROUGH);
		}
		name.setFont(new Font(fontAttrs));
		return this;
	}
	
	private class ItemListener implements Model.Listener<Item>, Item.ItemListener {
		@Override
		public void sync(Item item) {
			render();
		}

		@Override
		public void nameChanged(String name) {
			render();
		}

		@Override
		public void completedChanged(boolean completed) {
			render();
		}
	}
}
