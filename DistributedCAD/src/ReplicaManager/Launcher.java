package ReplicaManager;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;

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
		// Sets process arguments and redirects output
		final ProcessBuilder builder = new ProcessBuilder("java", "-jar",
				System.getProperty("user.dir") + "\\frontend.jar");
		builder.redirectOutput(Redirect.INHERIT);
		builder.redirectError(Redirect.INHERIT);
		new Thread(new Runnable() {
			public void run() {
				while (true) {
					restartProcess(builder);
				}
			}
		}).start();

		for (int i = 0; i < 4; i++) {
			final ProcessBuilder replicaBuilder = new ProcessBuilder("java", "-jar",
					System.getProperty("user.dir") + "\\replica.jar", i + "");
			replicaBuilder.redirectOutput(Redirect.INHERIT);
			replicaBuilder.redirectError(Redirect.INHERIT);
			new Thread(new Runnable() {
				public void run() {
					while (true) {
						restartProcess(replicaBuilder);
					}
				}
			}).start();
		}
	}
}
