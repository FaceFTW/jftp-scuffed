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
	public Socket sock;
	public boolean finished;
	private BufferedInputStream in;
	private BufferedOutputStream out;
	private Thread reciever;
	private int port = 7000;
	private ServerSocket ssock;
	private String type;
	private boolean resume;
	private boolean isThere;
	private long start;
	private int skiplen;
	private boolean justStream;
	private boolean ok = true;
	private String localfile;
	//private String outputCharset = "CP037";
	private String newLine;

	public DataConnection(FtpConnection con, int port, String host, String file, String type) {
		super();
		this.con = con;
		this.file = file;
		this.host = host;
		this.port = port;
		this.type = type;
		this.reciever = new Thread(this);
		this.reciever.start();
	}

	public DataConnection(FtpConnection con, int port, String host, String file, String type, boolean resume) {
		super();
		this.con = con;
		this.file = file;
		this.host = host;
		this.port = port;
		this.type = type;
		this.resume = resume;

		//resume = false;
		this.reciever = new Thread(this);
		this.reciever.start();
	}

	public DataConnection(FtpConnection con, int port, String host, String file, String type, boolean resume, boolean justStream) {
		super();
		this.con = con;
		this.file = file;
		this.host = host;
		this.port = port;
		this.type = type;
		this.resume = resume;
		this.justStream = justStream;

		//resume = false;
		this.reciever = new Thread(this);
		this.reciever.start();
	}

	public DataConnection(FtpConnection con, int port, String host, String file, String type, boolean resume, String localfile) {
		super();
		this.con = con;
		this.file = file;
		this.host = host;
		this.port = port;
		this.type = type;
		this.resume = resume;
		this.localfile = localfile;

		//resume = false;
		this.reciever = new Thread(this);
		this.reciever.start();
	}

	public DataConnection(FtpConnection con, int port, String host, String file, String type, boolean resume, int skiplen) {
		super();
		this.con = con;
		this.file = file;
		this.host = host;
		this.port = port;
		this.type = type;
		this.resume = resume;
		this.skiplen = skiplen;

		//resume = false;
		this.reciever = new Thread(this);
		this.reciever.start();
	}

	public DataConnection(FtpConnection con, int port, String host, String file, String type, boolean resume, int skiplen, InputStream i) {
		super();
		this.con = con;
		this.file = file;
		this.host = host;
		this.port = port;
		this.type = type;
		this.resume = resume;
		this.skiplen = skiplen;

		if (null != i) {
			this.in = new BufferedInputStream(i);
		}

		//resume = false;
		this.reciever = new Thread(this);
		this.reciever.start();
	}

	public void run() {
		try {
			this.newLine = this.con.getCRLF();
			//Log.debug("NL: "+newLine+"\n\n\n\n\n");

			if (Settings.getFtpPasvMode()) {
				try {
					this.sock = new Socket(this.host, this.port);
					this.sock.setSoTimeout(Settings.getSocketTimeout());
				} catch (Exception ex) {
					this.ok = false;
					ex.printStackTrace();
					this.debug("Can't open Socket on " + this.host + ":" + this.port);
				}
			} else {
				//Log.debug("trying new server socket: "+port);
				try {
					this.ssock = new ServerSocket(this.port);
				} catch (Exception ex) {
					this.ok = false;
					ex.printStackTrace();
					Log.debug("Can't open ServerSocket on port " + this.port);
				}
			}
		} catch (Exception ex) {
			this.debug(ex.toString());
		}

		this.isThere = true;

		boolean ok = true;

		RandomAccessFile fOut = null;
		BufferedOutputStream bOut = null;
		RandomAccessFile fIn = null;

		try {
			if (!Settings.getFtpPasvMode()) {
				int retry = 0;

				while ((5 > retry++) && (null == this.sock)) {
					try {
						this.ssock.setSoTimeout(Settings.connectionTimeout);
						this.sock = this.ssock.accept();
					} catch (IOException e) {
						this.sock = null;
						this.debug("Got IOException while trying to open a socket!");

						if (5 == retry) {
							this.debug("Connection failed, tried 5 times - maybe try a higher timeout in Settings.java...");
						}

						this.finished = true;

						throw e;
					} finally {
						this.ssock.close();
					}

					this.debug("Attempt timed out, retrying...");
				}
			}

			if (ok) {
				byte[] buf = new byte[Settings.bufferSize];
				this.start = System.currentTimeMillis();

				long buflen = 0;

				//---------------download----------------------
				if (this.type.equals(GET) || this.type.equals(GETDIR)) {
					if (!this.justStream) {
						try {
							if (this.resume) {
								File f = new File(this.file);
								fOut = new RandomAccessFile(this.file, "rw");
								fOut.seek(f.length());
								buflen = f.length();
							} else {
								if (null == this.localfile) {
									this.localfile = this.file;
								}

								File f2 = new File(Settings.appHomeDir);
								f2.mkdirs();

								File f = new File(this.localfile);

								if (f.exists()) {
									f.delete();
								}

								bOut = new BufferedOutputStream(new FileOutputStream(this.localfile), Settings.bufferSize);
							}
						} catch (Exception ex) {
							this.debug("Can't create outputfile: " + this.file);
							ok = false;
							ex.printStackTrace();
						}
					}

					if (ok) {
						try {
							this.in = new BufferedInputStream(this.sock.getInputStream(), Settings.bufferSize);

							if (this.justStream) {
								return;
							}
						} catch (Exception ex) {
							ok = false;
							this.debug("Can't get InputStream");
						}

						if (ok) {
							try {
								long len = buflen;

								if (null != fOut) {
									while (true) {
										// resuming
										int read = -2;

										try {
											read = this.in.read(buf);
										} catch (IOException es) {
											Log.out("got a IOException");
											ok = false;
											fOut.close();
											this.finished = true;
											this.con.fireProgressUpdate(this.file, FAILED, -1);

											Log.out("last read: " + read + ", len: " + (len + read));
											es.printStackTrace();

											return;
										}

										len += read;

										if (-1 == read) {
											break;
										}

										if (null != this.newLine) {
											byte[] buf2 = this.modifyGet(buf, read);
											fOut.write(buf2, 0, buf2.length);
										} else {
											fOut.write(buf, 0, read);
										}

										this.con.fireProgressUpdate(this.file, this.type, len);

										if (this.time()) {
											// Log.debugSize(len, true, false, file);
										}

										if (java.io.StreamTokenizer.TT_EOF == read) {
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
											read = this.in.read(buf);
										} catch (IOException es) {
											Log.out("got a IOException");
											ok = false;
											bOut.close();
											this.finished = true;
											this.con.fireProgressUpdate(this.file, FAILED, -1);

											Log.out("last read: " + read + ", len: " + (len + read));
											es.printStackTrace();

											return;
										}

										len += read;

										if (-1 == read) {
											break;
										}

										if (null != this.newLine) {
											byte[] buf2 = this.modifyGet(buf, read);
											bOut.write(buf2, 0, buf2.length);
										} else {
											bOut.write(buf, 0, read);
										}

										this.con.fireProgressUpdate(this.file, this.type, len);

										if (this.time()) {
											//Log.debugSize(len, true, false, file);
										}

										if (java.io.StreamTokenizer.TT_EOF == read) {
											break;
										}
									}

									bOut.flush();

									//Log.debugSize(len, true, true, file);
								}
							} catch (IOException ex) {
								ok = false;
								this.debug("Old connection removed");
								this.con.fireProgressUpdate(this.file, FAILED, -1);

								//debug(ex + ": " + ex.getMessage());
								ex.printStackTrace();
							}
						}
					}
				}

				//---------------upload----------------------
				if (this.type.equals(PUT) || this.type.equals(PUTDIR)) {
					if (null == this.in) {
						try {
							fIn = new RandomAccessFile(this.file, "r");

							if (this.resume) {
								fIn.seek(this.skiplen);
							}

							//fIn = new BufferedInputStream(new FileInputStream(file));
						} catch (Exception ex) {
							this.debug("Can't open inputfile: " + " (" + ex + ")");
							ok = false;
						}
					}

					if (ok) {
						try {
							this.out = new BufferedOutputStream(this.sock.getOutputStream());
						} catch (Exception ex) {
							ok = false;
							this.debug("Can't get OutputStream");
						}

						if (ok) {
							try {
								int len = this.skiplen;

								while (true) {
									int read;

									if (null != this.in) {
										read = this.in.read(buf);
									} else {
										read = fIn.read(buf);
									}

									len += read;

									//System.out.println(file + " " + type+ " " + len + " " + read);
									if (-1 == read) {
										break;
									}

									if (null != this.newLine) {
										byte[] buf2 = this.modifyPut(buf, read);
										this.out.write(buf2, 0, buf2.length);
									} else {
										this.out.write(buf, 0, read);
									}

									this.con.fireProgressUpdate(this.file, this.type, len);

									if (this.time()) {
										//   Log.debugSize(len, false, false, file);
									}

									if (java.io.StreamTokenizer.TT_EOF == read) {
										break;
									}
								}

								this.out.flush();

								//Log.debugSize(len, false, true, file);
							} catch (IOException ex) {
								ok = false;
								this.debug("Error: Data connection closed.");
								this.con.fireProgressUpdate(this.file, FAILED, -1);
								ex.printStackTrace();
							}
						}
					}
				}
			}
		} catch (IOException ex) {
			Log.debug("Can't connect socket to ServerSocket");
			ex.printStackTrace();
		} finally {
			try {
				if (null != this.out) {
					this.out.flush();
					this.out.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			try {
				if (null != bOut) {
					bOut.flush();
					bOut.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			try {
				if (null != fOut) {
					fOut.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			try {
				if (null != this.in && !this.justStream) {
					this.in.close();
				}

				if (null != fIn) {
					fIn.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		try {
			this.sock.close();
		} catch (Exception ex) {
			this.debug(ex.toString());
		}

		if (!Settings.getFtpPasvMode()) {
			try {
				this.ssock.close();
			} catch (Exception ex) {
				this.debug(ex.toString());
			}
		}

		this.finished = true;

		if (ok) {
			this.con.fireProgressUpdate(this.file, FINISHED, -1);
		} else {
			this.con.fireProgressUpdate(this.file, FAILED, -1);
		}
	}

	public InputStream getInputStream() {
		return this.in;
	}

	public FtpConnection getCon() {
		return this.con;
	}

	private void debug(String msg) {
		Log.debug(msg);
	}

	public void reset() {
		//reciever.destroy();
		this.reciever = new Thread(this);
		this.reciever.start();
	}

	private boolean time() {
		long now = System.currentTimeMillis();
		long offset = now - this.start;

		if (Settings.statusMessageAfterMillis < offset) {
			this.start = now;

			return true;
		}

		return false;
	}

	public boolean isThere() {
		if (this.finished) {
			return true;
		}

		return this.isThere;
	}

	public void setType(String tmp) {
		this.type = tmp;
	}

	public boolean isOK() {
		return this.ok;
	}

	public void interrupt() {
		if (Settings.getFtpPasvMode() && (this.type.equals(GET) || this.type.equals(GETDIR))) {
			try {
				this.reciever.join();
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}

	private byte[] modifyPut(byte[] buf, int len) {
		//Log.debug("\n\n\n\nNewline: "+newLine);
		if (null == this.newLine) return buf;

		String s = (new String(buf)).substring(0, len);
		s = s.replaceAll(this.LINEEND, this.newLine);
		//Log.debug("Newline_own: "+LINEEND+", s:"+s);

		return s.getBytes();
	}

	private byte[] modifyGet(byte[] buf, int len) {
		//Log.debug("\n\n\n\nNewline: "+newLine);
		if (null == this.newLine) return buf;

		String s = (new String(buf)).substring(0, len);
		s = s.replaceAll(this.newLine, this.LINEEND);
		//Log.debug("Newline_own: "+LINEEND+", s:"+s);

		return s.getBytes();
	}
}
