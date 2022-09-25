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
	//BufferedInputStream in;
	BufferedReader in;
	BufferedOutputStream err;
	final JTextArea text = new JTextArea(25, 101);

	//JTextField input = new JTextField();
	long off;
	Thread runner;
	JScrollPane textP;
	String input = "";
	final Vector commands = new Vector();
	int currCmd = 0;

	public Shell(InputStream in, OutputStream out) {
		try {
			this.in = new BufferedReader(new InputStreamReader(in));
			this.out = new BufferedOutputStream(out);
			//in = new BufferedInputStream(System.in);
			//out = new BufferedOutputStream(System.out);
			//err = new BufferedOutputStream(System.err);
			init();
		} catch (Exception e) {
			e.printStackTrace();
			net.sf.jftp.system.logging.Log.debug("ERROR: " + e.getMessage());
		}
	}


	public Shell(BufferedReader in, OutputStream out) {
		try {
			this.in = in;
			this.out = new BufferedOutputStream(out);
			//in = new BufferedInputStream(System.in);
			//out = new BufferedOutputStream(System.out);
			//err = new BufferedOutputStream(System.err);
			init();
		} catch (Exception e) {
			e.printStackTrace();
			net.sf.jftp.system.logging.Log.debug("ERROR: " + e.getMessage());
		}
	}

	public static void main(String[] argv) {
		try {
			Process p = Runtime.getRuntime().exec(argv.length > 0 ? argv[0] : "/bin/bash");
			new Shell(p.getInputStream(), p.getOutputStream());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void init() throws Exception {
		setTitle("Shell");

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		//setLocation(150, 150);
		HFrame.fixLocation(this);

		textP = new JScrollPane(text);
		text.setFont(new Font("Monospaced", Font.TRUETYPE_FONT, 10));

		getContentPane().setLayout(new BorderLayout(5, 5));
		getContentPane().add("Center", textP);

		//getContentPane().add("South", input);
		text.setEditable(false);
		setBackground(text.getBackground());

		DefaultCaret c = new DefaultCaret();
		c.setBlinkRate(1000);

		text.setCaret(c);
		text.setCaretColor(Color.BLACK);
		c.setVisible(true);

		text.setLineWrap(true);

		text.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if ((e.getKeyCode() == KeyEvent.VK_BACK_SPACE) && (input.length() > 0)) {
					input = input.substring(0, input.length() - 1);

					String t = text.getText();
					t = t.substring(0, t.length() - 1);
					text.setText(t);
				} else if (e.getKeyCode() == KeyEvent.VK_UP) {
					String t = text.getText();
					t = t.substring(0, t.length() - input.length());

					if ((currCmd <= commands.size()) && (currCmd > 0)) {
						currCmd--;

						String cmd = (String) commands.get(currCmd);
						input = cmd.substring(0, cmd.length() - 1);
						text.setText(t + input);
					}
				} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
					String t = text.getText();
					t = t.substring(0, t.length() - input.length());

					if (((currCmd + 1) < commands.size()) && (currCmd >= 0)) {
						currCmd++;

						String cmd = (String) commands.get(currCmd);
						input = cmd.substring(0, cmd.length() - 1);
						text.setText(t + input);
					}
				} else if (e.getKeyCode() != KeyEvent.VK_SHIFT) {
					//Char c = new Char(e.getKeyChar());
					if (!e.isActionKey()) {
						input += e.getKeyChar();
						text.append("" + e.getKeyChar());
					}
				}

				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					send();
				}
			}
		});

		pack();
		HFrame.fixLocation(this);
		setVisible(true);

		runner = new Thread(this);
		runner.start();

		toFront();
		text.requestFocus();
	}

	public void run() {
		try {
			char[] b = new char[4096];
			int i;

			while ((i = in.read(b, 0, b.length)) != StreamTokenizer.TT_EOF) {
				text.append(new String(b, 0, i));

				//Log.out("recv: "+i+" -> "+new String(b));
				//while(err.available() > 0)
				//{
				//    err.read(b);
				//    text.append(new String(b, 0, i));
				//}

				while (text.getRows() > 500) {
					String t = text.getText();
					t = t.substring(250);

					text.setText(t);
					// text.setCaretPosition(-250);
				}

				try {
					Thread.sleep(100);
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				JScrollBar bar = textP.getVerticalScrollBar();
				bar.setValue(bar.getMaximum());

				// text.setCaretPosition(i);
				text.setCaretPosition(text.getText().length());
				//text.getCaret().setDot(1);
			}

			text.setEnabled(false);
		} catch (Exception ex) {
			ex.printStackTrace();
			net.sf.jftp.system.logging.Log.debug("ERROR: " + ex.getMessage());
			this.dispose();
		}
	}

	private void send() {
		try {
			String msg = input;
			input = "";

			out.write(msg.getBytes());
			out.flush();

			commands.add(msg);
			currCmd = commands.size();

			//Log.out("send: "+msg);
		} catch (IOException ex) {
			ex.printStackTrace();
			net.sf.jftp.system.logging.Log.debug("ERROR: " + ex.getMessage());
			this.dispose();
		}
	}
}
