package net.sf.jftp.net.wrappers;

import jcifs.smb.NtlmAuthenticator;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;

class SmbTest extends NtlmAuthenticator {

	private SmbTest(String[] argv) throws Exception {
		super();
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
		while ('\n' != (c = System.in.read())) {
			if (-1 == c) return "";
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
			final String username = "guest";
			final String password = "";

			if (password.isEmpty()) {
				return null;
			}
			return new NtlmPasswordAuthentication(null, username, password);
		} catch (Exception e) {
		}
		return null;
	}
}
