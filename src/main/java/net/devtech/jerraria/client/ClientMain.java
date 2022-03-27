package net.devtech.jerraria.client;

import java.io.File;

import com.beust.jcommander.JCommander;
import net.devtech.jerraria.resource.IndexVirtualFile;
import net.devtech.jerraria.resource.VirtualFile;

public class ClientMain {
	public static void main(String[] argv) {
		ClientArgs args = new ClientArgs();
		JCommander.newBuilder()
		          .addObject(args)
		          .build()
		          .parse(argv);

		VirtualFile.Directory client = IndexVirtualFile.from(ClientMain.class);
		// resource reloading or no?

	}
}
