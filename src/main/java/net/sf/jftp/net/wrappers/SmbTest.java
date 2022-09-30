package net.sf.jftp.net.wrappers;

import jcifs.smb.NtlmAuthenticator;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;

public class SmbTest extends NtlmAuthenticator {

	public SmbTest(final String[] argv) throws Exception {
		NtlmAuthenticator.setDefault(this);

		final SmbFile file = new SmbFile(argv[0]);

		final SmbFile[] files = file.listFiles();

		for (final jcifs.smb.SmbFile smbFile : files) {
			System.out.print("FILE: " + smbFile.getName());
		}
		System.out.println("EOL");
	}

	public static String readLine() throws Exception {
		int c;
		final StringBuilder sb = new StringBuilder();
		while ('\n' != (c = System.in.read())) {
			if (-1 == c) return "";
			sb.append((char) c);
		}
		return sb.toString().trim();
	}

	public static void main(final String[] argv) throws Exception {
		new SmbTest(new String[]{"smb://Cyberdemon/tv/"});
	}

	protected NtlmPasswordAuthentication getNtlmPasswordAuthentication() {
		System.out.println(this.getRequestingException().getMessage() + " for " + this.getRequestingURL());

		try {
			final String username = "guest";
			final String password = "";

			if (0 == password.length()) {
				return null;
			}
			return new NtlmPasswordAuthentication(null, username, password);
		} catch (final Exception e) {
		}
		return null;
	}
}
