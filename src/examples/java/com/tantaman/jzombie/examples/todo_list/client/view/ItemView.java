package com.tantaman.jzombie.examples.todo_list.client.view;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
	private final ModelListener listener = new ModelListener();
	
	private JLabel name;
	private JCheckBox completed;
	
	private Map fontAttrs;
	
	public ItemView(Item model) {
		// This setting up of the components would be moved to a template class
		this.model = model;
		this.model.addListener(listener);
		
		name = new JLabel();
		completed = new JCheckBox();
		
		fontAttrs = new Font("Arial", Font.BOLD, 12).getAttributes();
		
		add(completed);
		add(name);
		
		bind();
	}
	
	private void bind() {
		completed.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("ACTION EVENT");
				if (completed.isSelected()) {
					model.completed(true);
				} else {
					model.completed(false);
				}
				model.save();
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
	
	private class ModelListener implements Model.Listener<Item> {
		@Override
		public void sync(Item item) {}
		
		@Override
		public void change(Item model) {
			System.out.println("RE RENDERING");
			System.out.println(model);
			render();
		}
	}
}
