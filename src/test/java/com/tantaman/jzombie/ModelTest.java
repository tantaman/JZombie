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
		TestClass t = new TestSubClass("omg", "wee");
		
		//t.save(null, null);
		t.setId(0);
		t.fetch(null, null);
		
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
		protected TestClassTwo tc = new TestClassTwo();
		
		public TestClass(String wtf) {
			super(swingExec);
			this.itemOne = wtf;
		}
	}
	
	public static class TestSubClass extends TestClass {
		@Expose
		public final String itemTwo;
		
		public TestSubClass(String wtf, String woah) {
			super(wtf);
			this.itemTwo = woah;
		}
		
		@Override
		public String toString() {
			return itemOne + " " + itemTwo + " " + tc;
		}
	}
	
	public static class TestClassTwo extends Model<TestClass> {
		@Expose
		public int one = 1;
		
		public TestClassTwo() {
			super(swingExec);
		}
		
		@Override
		public String toString() {
			return Integer.toString(one);
		}
	}
}
