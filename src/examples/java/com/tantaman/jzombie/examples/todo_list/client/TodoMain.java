package com.tantaman.jzombie.examples.todo_list.client;

import java.lang.reflect.InvocationTargetException;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.tantaman.jzombie.examples.todo_list.client.view.MainFrame;

public class TodoMain {
	public static void main(String[] args) throws InvocationTargetException, InterruptedException {
		SwingUtilities.invokeAndWait(new Runnable() {
			
			@Override
			public void run() {
				JFrame frame = new MainFrame();
				frame.setVisible(true);
			}
		});
	}
}
