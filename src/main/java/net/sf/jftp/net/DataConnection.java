/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package net.sf.jftp.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.StreamTokenizer;
import java.net.ServerSocket;
import java.net.Socket;


/**
 * This class represents a ftp data connection.
 * It is used internally by FtpConnection, so you probably don't have to use it directly.
 */
public class DataConnection implements Runnable {
	public static final String GET = "GET";
	public static final String PUT = "PUT";
	public static final String FAILED = "FAILED";
	public static final String FINISHED = "FINISHED";
	public static final String DFINISHED = "DFINISHED";
	public static final String GETDIR = "DGET";
	public static final String PUTDIR = "DPUT";
	private final String file;
	private final String host;
	private final FtpConnection con;
	private final String LINEEND = System.getProperty("line.separator");
	public Socket sock = null;
	public boolean finished = false;
	private BufferedInputStream in = null;
	private BufferedOutputStream out = null;
	private Thread reciever;
	private int port = 7000;
	private ServerSocket ssock = null;
	private String type;
	private boolean resume = false;
	private boolean isThere = false;
	private long start;
	private int skiplen = 0;
	private boolean justStream = false;
	private boolean ok = true;
	private String localfile = null;
	//private String outputCharset = "CP037";
	private String newLine = null;

	public DataConnection(FtpConnection con, int port, String host, String file, String type) {
		this.con = con;
		this.file = file;
		this.host = host;
		this.port = port;
		this.type = type;
		reciever = new Thread(this);
		reciever.start();
	}

	public DataConnection(FtpConnection con, int port, String host, String file, String type, boolean resume) {
		this.con = con;
		this.file = file;
		this.host = host;
		this.port = port;
		this.type = type;
		this.resume = resume;

		//resume = false;
		reciever = new Thread(this);
		reciever.start();
	}

	public DataConnection(FtpConnection con, int port, String host, String file, String type, boolean resume, boolean justStream) {
		this.con = con;
		this.file = file;
		this.host = host;
		this.port = port;
		this.type = type;
		this.resume = resume;
		this.justStream = justStream;

		//resume = false;
		reciever = new Thread(this);
		reciever.start();
	}

	public DataConnection(FtpConnection con, int port, String host, String file, String type, boolean resume, String localfile) {
		this.con = con;
		this.file = file;
		this.host = host;
		this.port = port;
		this.type = type;
		this.resume = resume;
		this.localfile = localfile;

		//resume = false;
		reciever = new Thread(this);
		reciever.start();
	}

	public DataConnection(FtpConnection con, int port, String host, String file, String type, boolean resume, int skiplen) {
		this.con = con;
		this.file = file;
		this.host = host;
		this.port = port;
		this.type = type;
		this.resume = resume;
		this.skiplen = skiplen;

		//resume = false;
		reciever = new Thread(this);
		reciever.start();
	}

	public DataConnection(FtpConnection con, int port, String host, String file, String type, boolean resume, int skiplen, InputStream i) {
		this.con = con;
		this.file = file;
		this.host = host;
		this.port = port;
		this.type = type;
		this.resume = resume;
		this.skiplen = skiplen;

		if (i != null) {
			this.in = new BufferedInputStream(i);
		}

		//resume = false;
		reciever = new Thread(this);
		reciever.start();
	}

	public void run() {
		try {
			newLine = con.getCRLF();
			//Log.debug("NL: "+newLine+"\n\n\n\n\n");

			if (net.sf.jftp.config.Settings.getFtpPasvMode()) {
				try {
					sock = new Socket(host, port);
					sock.setSoTimeout(net.sf.jftp.config.Settings.getSocketTimeout());
				} catch (Exception ex) {
					ok = false;
					ex.printStackTrace();
					this.debug("Can't open Socket on " + host + ":" + port);
				}
			} else {
				//Log.debug("trying new server socket: "+port);
				try {
					ssock = new ServerSocket(port);
				} catch (Exception ex) {
					ok = false;
					ex.printStackTrace();
					net.sf.jftp.system.logging.Log.debug("Can't open ServerSocket on port " + port);
				}
			}
		} catch (Exception ex) {
			this.debug(ex.toString());
		}

		isThere = true;

		boolean ok = true;

		RandomAccessFile fOut = null;
		BufferedOutputStream bOut = null;
		RandomAccessFile fIn = null;

		try {
			if (!net.sf.jftp.config.Settings.getFtpPasvMode()) {
				int retry = 0;

				while ((retry++ < 5) && (sock == null)) {
					try {
						ssock.setSoTimeout(net.sf.jftp.config.Settings.connectionTimeout);
						sock = ssock.accept();
					} catch (IOException e) {
						sock = null;
						this.debug("Got IOException while trying to open a socket!");

						if (retry == 5) {
							this.debug("Connection failed, tried 5 times - maybe try a higher timeout in Settings.java...");
						}

						finished = true;

						throw e;
					} finally {
						ssock.close();
					}

					this.debug("Attempt timed out, retrying...");
				}
			}

			if (ok) {
				byte[] buf = new byte[net.sf.jftp.config.Settings.bufferSize];
				start = System.currentTimeMillis();

				long buflen = 0;

				//---------------download----------------------
				if (type.equals(GET) || type.equals(GETDIR)) {
					if (!justStream) {
						try {
							if (resume) {
								File f = new File(file);
								fOut = new RandomAccessFile(file, "rw");
								fOut.seek(f.length());
								buflen = f.length();
							} else {
								if (localfile == null) {
									localfile = file;
								}

								File f2 = new File(net.sf.jftp.config.Settings.appHomeDir);
								f2.mkdirs();

								File f = new File(localfile);

								if (f.exists()) {
									f.delete();
								}

								bOut = new BufferedOutputStream(new FileOutputStream(localfile), net.sf.jftp.config.Settings.bufferSize);
							}
						} catch (Exception ex) {
							this.debug("Can't create outputfile: " + file);
							ok = false;
							ex.printStackTrace();
						}
					}

					if (ok) {
						try {
							in = new BufferedInputStream(sock.getInputStream(), net.sf.jftp.config.Settings.bufferSize);

							if (justStream) {
								return;
							}
						} catch (Exception ex) {
							ok = false;
							this.debug("Can't get InputStream");
						}

						if (ok) {
							try {
								long len = buflen;

								if (fOut != null) {
									while (true) {
										// resuming
										int read = -2;

										try {
											read = in.read(buf);
										} catch (IOException es) {
											net.sf.jftp.system.logging.Log.out("got a IOException");
											ok = false;
											fOut.close();
											finished = true;
											con.fireProgressUpdate(file, FAILED, -1);

											net.sf.jftp.system.logging.Log.out("last read: " + read + ", len: " + (len + read));
											es.printStackTrace();

											return;
										}

										len += read;

										if (read == -1) {
											break;
										}

										if (newLine != null) {
											byte[] buf2 = this.modifyGet(buf, read);
											fOut.write(buf2, 0, buf2.length);
										} else {
											fOut.write(buf, 0, read);
										}

										con.fireProgressUpdate(file, type, len);

										if (this.time()) {
											// Log.debugSize(len, true, false, file);
										}

										if (read == StreamTokenizer.TT_EOF) {
											break;
										}
									}

									//Log.debugSize(len, true, true, file);
								} else {
									// no resuming
									while (true) {
										//System.out.println(".");
										int read = -2;

										try {
											read = in.read(buf);
										} catch (IOException es) {
											net.sf.jftp.system.logging.Log.out("got a IOException");
											ok = false;
											bOut.close();
											finished = true;
											con.fireProgressUpdate(file, FAILED, -1);

											net.sf.jftp.system.logging.Log.out("last read: " + read + ", len: " + (len + read));
											es.printStackTrace();

											return;
										}

										len += read;

										if (read == -1) {
											break;
										}

										if (newLine != null) {
											byte[] buf2 = this.modifyGet(buf, read);
											bOut.write(buf2, 0, buf2.length);
										} else {
											bOut.write(buf, 0, read);
										}

										con.fireProgressUpdate(file, type, len);

										if (this.time()) {
											//Log.debugSize(len, true, false, file);
										}

										if (read == StreamTokenizer.TT_EOF) {
											break;
										}
									}

									bOut.flush();

									//Log.debugSize(len, true, true, file);
								}
							} catch (IOException ex) {
								ok = false;
								this.debug("Old connection removed");
								con.fireProgressUpdate(file, FAILED, -1);

								//debug(ex + ": " + ex.getMessage());
								ex.printStackTrace();
							}
						}
					}
				}

				//---------------upload----------------------
				if (type.equals(PUT) || type.equals(PUTDIR)) {
					if (in == null) {
						try {
							fIn = new RandomAccessFile(file, "r");

							if (resume) {
								fIn.seek(skiplen);
							}

							//fIn = new BufferedInputStream(new FileInputStream(file));
						} catch (Exception ex) {
							this.debug("Can't open inputfile: " + " (" + ex + ")");
							ok = false;
						}
					}

					if (ok) {
						try {
							out = new BufferedOutputStream(sock.getOutputStream());
						} catch (Exception ex) {
							ok = false;
							this.debug("Can't get OutputStream");
						}

						if (ok) {
							try {
								int len = skiplen;

								while (true) {
									int read;

									if (in != null) {
										read = in.read(buf);
									} else {
										read = fIn.read(buf);
									}

									len += read;

									//System.out.println(file + " " + type+ " " + len + " " + read);
									if (read == -1) {
										break;
									}

									if (newLine != null) {
										byte[] buf2 = this.modifyPut(buf, read);
										out.write(buf2, 0, buf2.length);
									} else {
										out.write(buf, 0, read);
									}

									con.fireProgressUpdate(file, type, len);

									if (this.time()) {
										//   Log.debugSize(len, false, false, file);
									}

									if (read == StreamTokenizer.TT_EOF) {
										break;
									}
								}

								out.flush();

								//Log.debugSize(len, false, true, file);
							} catch (IOException ex) {
								ok = false;
								this.debug("Error: Data connection closed.");
								con.fireProgressUpdate(file, FAILED, -1);
								ex.printStackTrace();
							}
						}
					}
				}
			}
		} catch (IOException ex) {
			net.sf.jftp.system.logging.Log.debug("Can't connect socket to ServerSocket");
			ex.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.flush();
					out.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			try {
				if (bOut != null) {
					bOut.flush();
					bOut.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			try {
				if (fOut != null) {
					fOut.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			try {
				if (in != null && !justStream) {
					in.close();
				}

				if (fIn != null) {
					fIn.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		try {
			sock.close();
		} catch (Exception ex) {
			this.debug(ex.toString());
		}

		if (!net.sf.jftp.config.Settings.getFtpPasvMode()) {
			try {
				ssock.close();
			} catch (Exception ex) {
				this.debug(ex.toString());
			}
		}

		finished = true;

		if (ok) {
			con.fireProgressUpdate(file, FINISHED, -1);
		} else {
			con.fireProgressUpdate(file, FAILED, -1);
		}
	}

	public InputStream getInputStream() {
		return in;
	}

	public FtpConnection getCon() {
		return con;
	}

	private void debug(String msg) {
		net.sf.jftp.system.logging.Log.debug(msg);
	}

	public void reset() {
		//reciever.destroy();
		reciever = new Thread(this);
		reciever.start();
	}

	private boolean time() {
		long now = System.currentTimeMillis();
		long offset = now - start;

		if (offset > net.sf.jftp.config.Settings.statusMessageAfterMillis) {
			start = now;

			return true;
		}

		return false;
	}

	public boolean isThere() {
		if (finished) {
			return true;
		}

		return isThere;
	}

	public void setType(String tmp) {
		type = tmp;
	}

	public boolean isOK() {
		return ok;
	}

	public void interrupt() {
		if (net.sf.jftp.config.Settings.getFtpPasvMode() && (type.equals(GET) || type.equals(GETDIR))) {
			try {
				reciever.join();
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}

	private byte[] modifyPut(byte[] buf, int len) {
		//Log.debug("\n\n\n\nNewline: "+newLine);
		if (newLine == null) return buf;

		String s = (new String(buf)).substring(0, len);
		s = s.replaceAll(LINEEND, newLine);
		//Log.debug("Newline_own: "+LINEEND+", s:"+s);

		return s.getBytes();
	}

	private byte[] modifyGet(byte[] buf, int len) {
		//Log.debug("\n\n\n\nNewline: "+newLine);
		if (newLine == null) return buf;

		String s = (new String(buf)).substring(0, len);
		s = s.replaceAll(newLine, LINEEND);
		//Log.debug("Newline_own: "+LINEEND+", s:"+s);

		return s.getBytes();
	}
}
