package com.tantaman.jzombie;

import static org.junit.Assert.*;
//import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import com.google.gson.annotations.Expose;
import com.tantaman.commons.concurrent.executors.SwingEDTAsExecutor;

public class ModelTest {
	private static final SwingEDTAsExecutor swingExec = new SwingEDTAsExecutor();
	@Test
	public void test() throws IOException {
		TestClass t = new TestClass("omg", "wee");
		
		//t.save(null, null);
		t.setId(0);
		t.fetch(null, null, null);
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static class TestClass extends Model<TestClass> {
		@Expose
		public final String itemOne;
		@Expose
		public final String itemTwo;
		@Expose
		private TestClassTwo tc = new TestClassTwo();
		
		public TestClass(String wtf, String woah) {
			super(swingExec);
			this.itemOne = wtf;
			this.itemTwo = woah;
		}
		
		private TestClass() {super(swingExec); this.itemOne =""; this.itemTwo = "";}
		
		@Override
		public String toString() {
			return itemOne + itemTwo + tc;
		}
		
//		@Override
//		protected void changed() {
//			System.out.println("Model changed");
//		}
	}
	
	public static class TestClassTwo extends Model<TestClass> {
		@Expose
		public int one = 1;
		
		public TestClassTwo() {
			super(swingExec);
		}
	}
}
