package video_example;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.annotations.Expose;
import com.tantaman.jzombie.BayeuxConfiguration;
import com.tantaman.jzombie.Model;

public class VidExample {
	private static final ExecutorService exec = Executors.newFixedThreadPool(1);
	public static void main(String[] args) throws Exception {
		Scanner scanner = new Scanner(System.in);
		
		BayeuxConfiguration.configureDefaultInstance("http://localhost/bayeux", null, null);
		
		SomeClass object = new SomeClass("1");
		
		String newName;
		while (!(newName = scanner.nextLine()).equals("q")) {
			object.setName(newName);
			String lastName = scanner.nextLine();
			object.setLastName(lastName);
			
			object.contained.setVar(newName + lastName);
			
			object.save();
			System.out.println(object);
		}
	}
	
	public static class SomeClass extends Model<SomeClass> {
		@Expose
		private String name;
		@Expose
		private String lastName;
		@Expose
		public Contained contained = new Contained();
		
		public SomeClass() {
			super(exec);
		}
		
		public SomeClass(String id) {
			super(exec, id);
			subscribe();
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public void setLastName(String lastName) {
			this.lastName = lastName;
		}
		
		@Override
		protected void endServerReset() {
			System.out.println("JZOMBIE SYNCHRONIZED!");
			System.out.println(this);
		}
		
		@Override
		public String toString() {
			return name + " " + lastName + contained.var;
		}
	}
	
	public static class Contained {
		@Expose
		private String var = "variable";
		
		public void setVar(String var) {
			this.var = var;
		}
	}
}
