package ReplicaManager;

import java.io.IOException;

public class Manager {
	
	public static void main (String[] args) {
		new Manager();
	}
	
	public Manager(){
		try {
			System.out.println(System.getProperty("user.dir"));
			ProcessBuilder builder = new ProcessBuilder("java", "-jar", System.getProperty("user.dir") + "\\frontend.jar");
			Process pro = builder.start();
			try {
				pro.waitFor();
				System.out.println(pro.exitValue());
				pro = builder.start();
				pro.waitFor();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("done");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
