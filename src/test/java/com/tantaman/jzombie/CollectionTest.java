package com.tantaman.jzombie;

import org.junit.Test;

import com.google.gson.annotations.Expose;
import com.tantaman.commons.concurrent.executors.SwingEDTAsExecutor;

public class CollectionTest {

	@Test
	public void test() {
		MyCollection coll = new MyCollection();
		
		coll.add(new MyModel("James", 1));
		coll.add(new MyModel("Jimmy", 2));
		coll.add(new MyModel("Jack", 3));
		
		String json = coll.serialize();
		
		System.out.println(json);
		
		MyCollection coll2 = coll.deserialize(json);
		
		System.out.println(coll2);
	}

	public static class MyCollection extends Collection<MyCollection, MyModel> {		
		public MyCollection() {
			super(SwingEDTAsExecutor.instance);
		}
	}
	
	public static class MyModel extends Model<MyModel> {
		@Expose
		public String name;
		@Expose
		public int age;
		
		private MyModel() {
			this("", 0);
		}
		
		public MyModel(String name, int age) {
			super(SwingEDTAsExecutor.instance);
			this.name = name;
			this.age = age;
		}
	}
}
