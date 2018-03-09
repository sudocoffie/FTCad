package ReplicaManager;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;

public class Launcher {

	public static void main(String[] args) {
		new Launcher();
	}

	public void restartProcess(ProcessBuilder builder) {
		while (true) {
			try {
				Process process = builder.start();
				process.waitFor();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	public Launcher() {

		for (int i = 0; i < 4; i++) {
			final ProcessBuilder builder = new ProcessBuilder("java", "-jar",System.getProperty("user.dir") + "\\replica.jar", i + "");
			builder.redirectOutput(Redirect.INHERIT);
			builder.redirectError(Redirect.INHERIT);
			new Thread(new Runnable() {
				public void run() {
					restartProcess(builder);
				}
			}).start();
		}
		// System.out.println(System.getProperty("user.dir"));
		final ProcessBuilder builder = new ProcessBuilder("java", "-jar", System.getProperty("user.dir") + "\\frontend.jar");
		builder.redirectOutput(Redirect.INHERIT);
		builder.redirectError(Redirect.INHERIT);
		new Thread(new Runnable() {
			public void run() {
				restartProcess(builder);
			}
		}).start();

	}

}
