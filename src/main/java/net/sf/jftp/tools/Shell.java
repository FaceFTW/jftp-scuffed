package net.sf.jftp.tools;

import net.sf.jftp.gui.framework.HFrame;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StreamTokenizer;
import java.util.Vector;

public class Shell extends HFrame implements Runnable {
	BufferedOutputStream out;
	BufferedReader in;
	BufferedOutputStream err;
	final JTextArea text = new JTextArea(25, 101);

	long off;
	Thread runner;
	JScrollPane textP;
	String input = "";
	final Vector commands = new Vector();
	int currCmd = 0;

	public Shell(final InputStream in, final OutputStream out) {
		try {
			this.in = new BufferedReader(new InputStreamReader(in));
			this.out = new BufferedOutputStream(out);
			this.init();
		} catch (final Exception e) {
			e.printStackTrace();
			net.sf.jftp.system.logging.Log.debug("ERROR: " + e.getMessage());
		}
	}


	public Shell(final BufferedReader in, final OutputStream out) {
		try {
			this.in = in;
			this.out = new BufferedOutputStream(out);
			this.init();
		} catch (final Exception e) {
			e.printStackTrace();
			net.sf.jftp.system.logging.Log.debug("ERROR: " + e.getMessage());
		}
	}

	public static void main(final String[] argv) {
		try {
			final Process p = Runtime.getRuntime().exec(argv.length > 0 ? argv[0] : "/bin/bash");
			new Shell(p.getInputStream(), p.getOutputStream());
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

	public void init() throws Exception {
		this.setTitle("Shell");

		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		HFrame.fixLocation(this);

		this.textP = new JScrollPane(this.text);
		this.text.setFont(new Font("Monospaced", java.awt.Font.PLAIN, 10));

		this.getContentPane().setLayout(new BorderLayout(5, 5));
		this.getContentPane().add("Center", this.textP);
		this.text.setEditable(false);
		this.setBackground(this.text.getBackground());

		final DefaultCaret c = new DefaultCaret();
		c.setBlinkRate(1000);

		this.text.setCaret(c);
		this.text.setCaretColor(Color.BLACK);
		c.setVisible(true);

		this.text.setLineWrap(true);

		this.text.addKeyListener(new KeyAdapter() {
			public void keyPressed(final KeyEvent e) {
				if ((e.getKeyCode() == KeyEvent.VK_BACK_SPACE) && (net.sf.jftp.tools.Shell.this.input.length() > 0)) {
					net.sf.jftp.tools.Shell.this.input = net.sf.jftp.tools.Shell.this.input.substring(0, net.sf.jftp.tools.Shell.this.input.length() - 1);

					String t = net.sf.jftp.tools.Shell.this.text.getText();
					t = t.substring(0, t.length() - 1);
					net.sf.jftp.tools.Shell.this.text.setText(t);
				} else if (e.getKeyCode() == KeyEvent.VK_UP) {
					String t = net.sf.jftp.tools.Shell.this.text.getText();
					t = t.substring(0, t.length() - net.sf.jftp.tools.Shell.this.input.length());

					if ((net.sf.jftp.tools.Shell.this.currCmd <= net.sf.jftp.tools.Shell.this.commands.size()) && (net.sf.jftp.tools.Shell.this.currCmd > 0)) {
						net.sf.jftp.tools.Shell.this.currCmd--;

						final String cmd = (String) net.sf.jftp.tools.Shell.this.commands.get(net.sf.jftp.tools.Shell.this.currCmd);
						net.sf.jftp.tools.Shell.this.input = cmd.substring(0, cmd.length() - 1);
						net.sf.jftp.tools.Shell.this.text.setText(t + net.sf.jftp.tools.Shell.this.input);
					}
				} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
					String t = net.sf.jftp.tools.Shell.this.text.getText();
					t = t.substring(0, t.length() - net.sf.jftp.tools.Shell.this.input.length());

					if (((net.sf.jftp.tools.Shell.this.currCmd + 1) < net.sf.jftp.tools.Shell.this.commands.size()) && (net.sf.jftp.tools.Shell.this.currCmd >= 0)) {
						net.sf.jftp.tools.Shell.this.currCmd++;

						final String cmd = (String) net.sf.jftp.tools.Shell.this.commands.get(net.sf.jftp.tools.Shell.this.currCmd);
						net.sf.jftp.tools.Shell.this.input = cmd.substring(0, cmd.length() - 1);
						net.sf.jftp.tools.Shell.this.text.setText(t + net.sf.jftp.tools.Shell.this.input);
					}
				} else if (e.getKeyCode() != KeyEvent.VK_SHIFT) {
					if (!e.isActionKey()) {
						net.sf.jftp.tools.Shell.this.input += e.getKeyChar();
						net.sf.jftp.tools.Shell.this.text.append("" + e.getKeyChar());
					}
				}

				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					net.sf.jftp.tools.Shell.this.send();
				}
			}
		});

		this.pack();
		HFrame.fixLocation(this);
		this.setVisible(true);

		this.runner = new Thread(this);
		this.runner.start();

		this.toFront();
		this.text.requestFocus();
	}

	public void run() {
		try {
			final char[] b = new char[4096];
			int i;

			while ((i = this.in.read(b, 0, b.length)) != StreamTokenizer.TT_EOF) {
				this.text.append(new String(b, 0, i));

				while (this.text.getRows() > 500) {
					String t = this.text.getText();
					t = t.substring(250);

					this.text.setText(t);
				}

				try {
					Thread.sleep(100);
				} catch (final Exception ex) {
					ex.printStackTrace();
				}

				final JScrollBar bar = this.textP.getVerticalScrollBar();
				bar.setValue(bar.getMaximum());
				this.text.setCaretPosition(this.text.getText().length());
			}

			this.text.setEnabled(false);
		} catch (final Exception ex) {
			ex.printStackTrace();
			net.sf.jftp.system.logging.Log.debug("ERROR: " + ex.getMessage());
			this.dispose();
		}
	}

	private void send() {
		try {
			final String msg = this.input;
			this.input = "";

			this.out.write(msg.getBytes());
			this.out.flush();

			this.commands.add(msg);
			this.currCmd = this.commands.size();

		} catch (final IOException ex) {
			ex.printStackTrace();
			net.sf.jftp.system.logging.Log.debug("ERROR: " + ex.getMessage());
			this.dispose();
		}
	}
}
