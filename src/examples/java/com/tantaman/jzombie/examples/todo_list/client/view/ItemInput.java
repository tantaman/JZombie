package com.tantaman.jzombie.examples.todo_list.client.view;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.tantaman.jzombie.examples.todo_list.client.model.Item;
import com.tantaman.jzombie.examples.todo_list.client.model.ItemList;

public class ItemInput extends JPanel {
	private JTextField textInput;
	private JButton okButton;
	private ItemList model;
	
	public ItemInput(ItemList model) {
		this.model = model;
		setLayout(new FlowLayout());
		
		textInput = new JTextField();
		textInput.setColumns(20);
		okButton = new JButton();
		okButton.setText("Add");
		
		add(textInput);
		add(okButton);
		
		bind();
	}
	
	private void bind() {
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String name = textInput.getText();
				if (name != null && name != "") {
					Item item = new Item(false, name);
					model.add(item);
					item.save();
					
					textInput.setText("");
				}
			}
		});
	}
	
	public Component render() {
		return this;
	}
}
