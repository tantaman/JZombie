package com.tantaman.jzombie.examples.todo_list.client.view;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.TextAttribute;
import java.util.Date;
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
				if (completed.isSelected()) {
					model.setCompleted(true);
				} else {
					model.setCompleted(false);
				}
				model.save();
			}
		});
		
		model.addListener(listener);
	}
	
	public Component render() {
		name.setText(model.getName());
		completed.setSelected(model.getCompleted());
		
		
		if (model.getCompleted()) {
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
			render();
		}
	}
}
