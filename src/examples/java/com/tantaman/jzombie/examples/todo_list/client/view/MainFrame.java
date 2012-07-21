package com.tantaman.jzombie.examples.todo_list.client.view;

import java.awt.Container;
import java.awt.Dimension;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JFrame;

import com.tantaman.jzombie.examples.todo_list.client.model.ItemList;

public class MainFrame extends JFrame {
	public MainFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setPreferredSize(new Dimension(800,600));
		setSize(new Dimension(800, 600));
		
		ItemList todoListModel = new ItemList();
		ItemListView todoListView = new ItemListView(todoListModel);
		
		Container contentPane = getContentPane();
		contentPane.setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		contentPane.add(new ItemInput(todoListModel).render());
		contentPane.add(todoListView.render());
		
		try {
			// Fetches are asynchronous so they won't block the EDT
			todoListModel.fetch();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
