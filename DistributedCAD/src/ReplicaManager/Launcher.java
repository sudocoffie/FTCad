package ReplicaManager;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.List;

public class Launcher {
	
	public static void main(String[] args) {
		new Launcher();
	}

	public void restartProcess(ProcessBuilder builder) {
		try {
			Process process = builder.start();
			process.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public Launcher() {

		final ProcessBuilder builder = new ProcessBuilder("java", "-jar", System.getProperty("user.dir") + "\\frontend.jar");
		builder.redirectOutput(Redirect.INHERIT);
		builder.redirectError(Redirect.INHERIT);
		new Thread(new Runnable() {
			public void run() {
				restartProcess(builder);
			}
		}).start();
		
		for (int i = 0; i < 4; i++) {
			final ProcessBuilder replicaBuilder = new ProcessBuilder("java", "-jar",System.getProperty("user.dir") + "\\replica.jar", i + "", "0");
			replicaBuilder.redirectOutput(Redirect.INHERIT);
			replicaBuilder.redirectError(Redirect.INHERIT);
			
			new Thread(new Runnable() {
				public void run() {
					List<String> commandList = replicaBuilder.command();
					while(true) {
						restartProcess(replicaBuilder);
						// Sets that the process should be restarted
						commandList.set(4, "1");
						replicaBuilder.command(commandList);
					}
				}
			}).start();
		}
	}
}
