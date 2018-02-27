package ReplicaManager;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;

public class Launcher {
	
	public static void main (String[] args) {
		new Launcher();
	}
	
	public Launcher(){
		try {
			Process[] processes = new Process[4];
			for(int i = 0; i < processes.length; i++){
				ProcessBuilder builder = new ProcessBuilder("java", "-jar", System.getProperty("user.dir") + "\\replica.jar", i + "");
				builder.redirectOutput(Redirect.INHERIT);
				builder.redirectError(Redirect.INHERIT);
				processes[i] = builder.start();
			}
			//System.out.println(System.getProperty("user.dir"));
			//ProcessBuilder builder = new ProcessBuilder("java", "-jar", System.getProperty("user.dir") + "\\frontend.jar");
			//Process pro = builder.start();
			/*try {
				pro.waitFor();
				System.out.println(pro.exitValue());
				pro = builder.start();
				pro.waitFor();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			for(int i = 0; i < processes.length; i++){
				try {
					processes[i].waitFor();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			System.out.println("done");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
