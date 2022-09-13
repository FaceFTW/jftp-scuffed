package poc;

import java.util.Scanner;

import com.github.fracpete.processoutput4j.output.ConsoleOutputProcessOutput;
import com.github.fracpete.rsync4j.RSync;
import com.github.fracpete.rsync4j.SshPass;
import com.github.fracpete.rsync4j.core.Binaries;

public final class App {
	public static void main(String[] args) throws Exception {
		Scanner sc = new Scanner(System.in);

		System.out.println("Enter source path:  ");
		String sourcePath = sc.nextLine();
		System.out.println("Enter destination user+hostname:  ");
		String destinationUserHost = sc.nextLine();
		System.out.println("Enter destination path:  ");
		String destinationPath = sc.nextLine();
		System.out.println("Enter the password of the destination user:  ");
		String password = sc.nextLine();

		SshPass pass = new SshPass().password(password);

		String source = sourcePath;
		String destination = destinationUserHost + ":" + destinationPath;

		RSync rsync = new RSync().sshPass(pass).source(source).destination(destination).verbose(true)
				.rsh(Binaries.sshBinary() + "-o StrictHostKeyChecking=no");

		ConsoleOutputProcessOutput output = new ConsoleOutputProcessOutput();
		output.monitor(rsync.builder());

	}
}
