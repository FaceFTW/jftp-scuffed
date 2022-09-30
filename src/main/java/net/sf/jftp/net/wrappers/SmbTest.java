package net.sf.jftp.net.wrappers;

import jcifs.smb.NtlmAuthenticator;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;

public class SmbTest extends NtlmAuthenticator {

	public SmbTest(String[] argv) throws Exception {
		NtlmAuthenticator.setDefault(this);

		SmbFile file = new SmbFile(argv[0]);

		SmbFile[] files = file.listFiles();

		for (jcifs.smb.SmbFile smbFile : files) {
			System.out.print("FILE: " + smbFile.getName());
		}
		System.out.println("EOL");
	}

	public static String readLine() throws Exception {
		int c;
		StringBuilder sb = new StringBuilder();
		while ((c = System.in.read()) != '\n') {
			if (c == -1) return "";
			sb.append((char) c);
		}
		return sb.toString().trim();
	}

	public static void main(String[] argv) throws Exception {
		new SmbTest(new String[]{"smb://Cyberdemon/tv/"});
	}

	protected NtlmPasswordAuthentication getNtlmPasswordAuthentication() {
		System.out.println(this.getRequestingException().getMessage() + " for " + this.getRequestingURL());

		try {
			String username = "guest";
			String password = "";

			if (password.length() == 0) {
				return null;
			}
			return new NtlmPasswordAuthentication(null, username, password);
		} catch (Exception e) {
		}
		return null;
	}
}
