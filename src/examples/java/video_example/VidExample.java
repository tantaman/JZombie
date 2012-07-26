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
		
		SyncedObject o = new SyncedObject("Hello", "");
		o.subscribe();
		
		String newName;
		while (!(newName = scanner.nextLine()).equals("q")) {
			if (newName.equals("create")) {
				System.out.print("Id: ");
				String id = scanner.nextLine();
				System.out.print("\nName: ");
				String name = scanner.nextLine();
				new SyncedObject(name, id).save();
			} else {
				o.setName(newName);
				o.save();
			}
		}
	}
	
	public static class SyncedObject extends Model<SyncedObject> {
		@Expose
		private volatile String name;
		
		public SyncedObject(String n, String id) {
			super(exec, id);
			name = n;
		}
		
		public void setName(String newName) {
			name = newName;
		}
		
		@Override
		protected String rootUrl() {
			return "/SyncedObject";
		}
		
		@Override
		protected void endServerReset() {
			super.endServerReset();
			System.out.println("Updated to: " + this);
		}
		
		public SyncedObject() {
			super(exec);
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
}
