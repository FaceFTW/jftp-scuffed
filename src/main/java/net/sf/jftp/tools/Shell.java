package net.sf.jftp.tools;

import net.sf.jftp.gui.framework.HFrame;
import net.sf.jftp.system.logging.Log;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class Shell extends HFrame implements Runnable {
	private final JTextArea text = new JTextArea(25, 101);
	private final List<String> commands = new ArrayList<>();
	private BufferedOutputStream out;
	private BufferedReader in;
	BufferedOutputStream err;
	long off;
	private Thread runner;
	private JScrollPane textP;
	private String input = "";
	private int currCmd;

	public Shell(InputStream in, OutputStream out) {
		super();
		try {
			this.in = new BufferedReader(new InputStreamReader(in));
			this.out = new BufferedOutputStream(out);
			this.init();
		} catch (Exception e) {
			e.printStackTrace();
			Log.debug("ERROR: " + e.getMessage());
		}
	}

	public Shell(BufferedReader in, OutputStream out) {
		super();
		try {
			this.in = in;
			this.out = new BufferedOutputStream(out);
			this.init();
		} catch (Exception e) {
			e.printStackTrace();
			Log.debug("ERROR: " + e.getMessage());
		}
	}

	public static void main(String[] argv) {
		try {
			Process p = Runtime.getRuntime().exec(0 < argv.length ? argv[0] : "/bin/bash");
			new Shell(p.getInputStream(), p.getOutputStream());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void init() throws Exception {
		this.setTitle("Shell");

		this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		HFrame.fixLocation(this);

		this.textP = new JScrollPane(this.text);
		this.text.setFont(new Font("Monospaced", java.awt.Font.PLAIN, 10));

		this.getContentPane().setLayout(new BorderLayout(5, 5));
		this.getContentPane().add("Center", this.textP);
		this.text.setEditable(false);
		this.setBackground(this.text.getBackground());

		DefaultCaret c = new DefaultCaret();
		c.setBlinkRate(1000);

		this.text.setCaret(c);
		this.text.setCaretColor(Color.BLACK);
		c.setVisible(true);

		this.text.setLineWrap(true);

		this.text.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if ((java.awt.event.KeyEvent.VK_BACK_SPACE == e.getKeyCode()) && (0 < Shell.this.input.length())) {
					Shell.this.input = Shell.this.input.substring(0, Shell.this.input.length() - 1);

					String t = Shell.this.text.getText();
					t = t.substring(0, t.length() - 1);
					Shell.this.text.setText(t);
				} else if (java.awt.event.KeyEvent.VK_UP == e.getKeyCode()) {
					String t = Shell.this.text.getText();
					t = t.substring(0, t.length() - Shell.this.input.length());

					if ((Shell.this.currCmd <= Shell.this.commands.size()) && (0 < Shell.this.currCmd)) {
						Shell.this.currCmd--;

						String cmd = (String) Shell.this.commands.get(Shell.this.currCmd);
						Shell.this.input = cmd.substring(0, cmd.length() - 1);
						Shell.this.text.setText(t + Shell.this.input);
					}
				} else if (java.awt.event.KeyEvent.VK_DOWN == e.getKeyCode()) {
					String t = Shell.this.text.getText();
					t = t.substring(0, t.length() - Shell.this.input.length());

					if (((Shell.this.currCmd + 1) < Shell.this.commands.size()) && (0 <= Shell.this.currCmd)) {
						Shell.this.currCmd++;

						String cmd = (String) Shell.this.commands.get(Shell.this.currCmd);
						Shell.this.input = cmd.substring(0, cmd.length() - 1);
						Shell.this.text.setText(t + Shell.this.input);
					}
				} else if (java.awt.event.KeyEvent.VK_SHIFT != e.getKeyCode()) {
					if (!e.isActionKey()) {
						Shell.this.input += e.getKeyChar();
						Shell.this.text.append("" + e.getKeyChar());
					}
				}

				if (java.awt.event.KeyEvent.VK_ENTER == e.getKeyCode()) {
					Shell.this.send();
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
			char[] b = new char[4096];
			int i;

			while (java.io.StreamTokenizer.TT_EOF != (i = this.in.read(b, 0, b.length))) {
				this.text.append(new String(b, 0, i));

				while (500 < this.text.getRows()) {
					String t = this.text.getText();
					t = t.substring(250);

					this.text.setText(t);
				}

				try {
					Thread.sleep(100);
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				JScrollBar bar = this.textP.getVerticalScrollBar();
				bar.setValue(bar.getMaximum());
				this.text.setCaretPosition(this.text.getText().length());
			}

			this.text.setEnabled(false);
		} catch (Exception ex) {
			ex.printStackTrace();
			Log.debug("ERROR: " + ex.getMessage());
			this.dispose();
		}
	}

	private void send() {
		try {
			String msg = this.input;
			this.input = "";

			this.out.write(msg.getBytes());
			this.out.flush();

			this.commands.add(msg);
			this.currCmd = this.commands.size();

		} catch (IOException ex) {
			ex.printStackTrace();
			Log.debug("ERROR: " + ex.getMessage());
			this.dispose();
		}
	}
}
