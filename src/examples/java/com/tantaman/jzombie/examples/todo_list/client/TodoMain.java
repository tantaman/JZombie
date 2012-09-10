package com.tantaman.jzombie.examples.todo_list.client;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.tantaman.jzombie.BayeuxConfiguration;
import com.tantaman.jzombie.examples.todo_list.client.view.MainFrame;

public class TodoMain {
	public static void main(String[] args) throws Exception {
		BayeuxConfiguration.configureDefaultInstance("http://localhost:8080/bayeux", null, null);
		SwingUtilities.invokeAndWait(new Runnable() {
			
			@Override
			public void run() {
				JFrame frame = new MainFrame();
				frame.setVisible(true);
			}
		});
	}
}
