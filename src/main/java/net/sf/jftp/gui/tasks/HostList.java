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
package net.sf.jftp.gui.tasks;

import javax.swing.*;
import java.awt.*;
import java.io.File;


public class HostList extends JDialog {

	// has an ok, cancel button, and a save
	// needs to load itself too.
	private JPanel jpHostInfo;
	private JTextField jtfHost;
	private JTextField jtfUser;
	private JPasswordField jtfPass;
	private JTextField jtfName;
	private JTextField jtfPort;
	private JPanel jpbuttons;
	private JButton jbsave;
	private JButton jbcancel;
	private JButton jbok;
	private JButton jbnew;
	private JButton jbdelete;
	private JList hostList;
	private DefaultListModel hostListModel;

	/**
	 * The currently selected FtpHost instance
	 */
	private net.sf.jftp.gui.base.FtpHost selectedHostInfo = null;

	/**
	 * Constructs an instance of the HostList with the
	 * given parent and initializes the UI for the host list.
	 * calling getFtpHost() will show the dialog and wait until
	 * the user clicks ok() or cancel().
	 *
	 * @param parent The parent JDialog
	 */
	public HostList(final JDialog parent) {
		super(parent);
		final String promptDialogTitle = " J-FTP Host Selection ";
		this.setTitle(promptDialogTitle);
		this.init();
		this.setSize(600, 300);
	}

	/**
	 * Adds listeners to any components that need them
	 */
	protected void initListeners() {
		this.hostList.addListSelectionListener(lse -> this.onSelectHost());
		this.jbsave.addActionListener(ae -> this.onSave());
		this.jbok.addActionListener(ae -> this.onOk());
		this.jbcancel.addActionListener(ae -> this.onCancel());
		this.jbnew.addActionListener(ae -> this.onNew());
		this.jbdelete.addActionListener(ae -> this.onDelete());
	}

	/**
	 * This method makes the dialog popup
	 * and the user must select ok or cancel
	 * upon clicking ok, the selected FtpHost will be returned
	 * upon cancel, a null will be returned.
	 */
	public net.sf.jftp.gui.base.FtpHost getFtpHost() {
		this.selectedHostInfo = null;
		this.setVisible(true);

		return this.selectedHostInfo;
	}

	/**
	 * overall initialization routine called from the ctor
	 */
	protected void init() {
		this.initPrompts();
		this.initHostInfoPanel();
		this.initButtonPanel();
		this.initHostListFrame();
		this.loadHostList();
		this.initListeners();

		if (this.hostListModel.size() > 0) {
			this.hostList.setSelectedIndex(0);
		} else {
			this.updateHostInfoPanel();
		}

		this.selectedHostInfo = this.getSelected();
		this.setModal(true);
	}

	/**
	 * This is where your internationalization can
	 * take hold, you can change the values of the prompt
	 * strings to whatever
	 */
	protected void initPrompts() {
		// do nothing
	}

	/**
	 * initialize the button panel
	 */
	protected void initButtonPanel() {
		this.jpbuttons = new JPanel();
		this.jpbuttons.setLayout(new FlowLayout(FlowLayout.RIGHT));
		final String promptButtonCancel = "Cancel";
		this.jbcancel = new JButton(promptButtonCancel);
		final String promptButtonOk = "  Ok  ";
		this.jbok = new JButton(promptButtonOk);
		final String promptButtonSave = " Apply ";
		this.jbsave = new JButton(promptButtonSave);
		final String promptButtonNew = " New  ";
		this.jbnew = new JButton(promptButtonNew);
		final String promptButtonDelete = "Delete";
		this.jbdelete = new JButton(promptButtonDelete);
		this.jpbuttons.add(this.jbsave);
		this.jpbuttons.add(this.jbok);
		this.jpbuttons.add(this.jbcancel);
	}

	/**
	 * Build the host info panel
	 */
	protected void initHostInfoPanel() {
		this.jtfHost = new JTextField(20);
		this.jtfUser = new JTextField(20);
		this.jtfPass = new JPasswordField(20);
		this.jtfName = new JTextField(20);
		this.jtfPort = new JTextField(20);
		final String promptHost = " Host : ";
		final javax.swing.JLabel jlHost = new javax.swing.JLabel(promptHost);
		final String promptUser = " User : ";
		final javax.swing.JLabel jlUser = new javax.swing.JLabel(promptUser);
		final String promptPass = " Password : ";
		final javax.swing.JLabel jlPass = new javax.swing.JLabel(promptPass);
		final String promptName = " Name : ";
		final javax.swing.JLabel jlName = new javax.swing.JLabel(promptName);
		final String promptPort = " Port : ";
		final javax.swing.JLabel jlPort = new javax.swing.JLabel(promptPort);

		this.jpHostInfo = new JPanel();

		final GridBagLayout gbl = new GridBagLayout();
		this.jpHostInfo.setLayout(gbl);

		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		gbl.setConstraints(jlName, gbc);

		gbc.gridy = 1;
		gbl.setConstraints(jlHost, gbc);

		gbc.gridy = 2;
		gbl.setConstraints(jlUser, gbc);

		gbc.gridy = 3;
		gbl.setConstraints(jlPass, gbc);

		gbc.gridy = 4;
		gbl.setConstraints(jlPort, gbc);

		gbc.gridy = 0;
		gbc.gridx = 1;
		gbc.weightx = 1.0;
		gbl.setConstraints(this.jtfName, gbc);

		gbc.gridy = 1;
		gbl.setConstraints(this.jtfHost, gbc);

		gbc.gridy = 2;
		gbl.setConstraints(this.jtfUser, gbc);

		gbc.gridy = 3;
		gbl.setConstraints(this.jtfPass, gbc);

		gbc.gridy = 4;
		gbl.setConstraints(this.jtfPort, gbc);

		this.jpHostInfo.add(jlName);
		this.jpHostInfo.add(jlHost);
		this.jpHostInfo.add(jlUser);
		this.jpHostInfo.add(jlPass);
		this.jpHostInfo.add(jlPort);
		this.jpHostInfo.add(this.jtfName);
		this.jpHostInfo.add(this.jtfHost);
		this.jpHostInfo.add(this.jtfUser);
		this.jpHostInfo.add(this.jtfPass);
		this.jpHostInfo.add(this.jtfPort);
	}

	/**
	 * Initializes the overall dialog/frame
	 */
	protected void initHostListFrame() {
		this.hostListModel = new DefaultListModel();
		this.hostList = new JList(this.hostListModel);
		final javax.swing.JScrollPane jscrollpane = new javax.swing.JScrollPane(this.hostList);

		final JPanel jptempleft = new JPanel(new BorderLayout());
		jptempleft.add(jscrollpane, BorderLayout.CENTER);

		final JPanel jptempbutt = new JPanel(new FlowLayout());
		jptempbutt.add(this.jbnew);
		jptempbutt.add(this.jbdelete);
		jptempleft.add(jptempbutt, BorderLayout.SOUTH);

		final JPanel jptemp = new JPanel(new BorderLayout());
		jptemp.add(this.jpbuttons, BorderLayout.SOUTH);
		jptemp.add(this.jpHostInfo, BorderLayout.CENTER);

		final javax.swing.JSplitPane jsplitpane = new javax.swing.JSplitPane(javax.swing.JSplitPane.HORIZONTAL_SPLIT, jptempleft, jptemp);
		this.getContentPane().add(jsplitpane);
	}

	/**
	 * Loads the host list from the hard drive
	 */
	protected void loadHostList() {
		//Log.out("x");
		// current host number
		int i = 0;

		while (i >= 0) {
			final String filename = net.sf.jftp.config.Settings.login.concat(String.valueOf(i));
			final String[] host_info = net.sf.jftp.config.LoadSet.loadSet(filename);

			if ((host_info == null) || (host_info.length == 1)) {
				// no file was loaded, break out.
				i = -1;

				continue;
			}

			final net.sf.jftp.gui.base.FtpHost ftpHost = new net.sf.jftp.gui.base.FtpHost();

			try {
				ftpHost.hostname = host_info[0];
				ftpHost.username = host_info[1];
				ftpHost.password = host_info[2];
				ftpHost.name = host_info[3];
				ftpHost.port = host_info[4];
			} catch (final ArrayIndexOutOfBoundsException aioobe) {
				// do nothing, this can happen
			}

			this.hostListModel.addElement(ftpHost);
			i++;
		}
	}

	public void onSelectHost() {
		// update the old one, then show the new one
		this.updateHostInfoObject();
		this.selectedHostInfo = this.getSelected();
		this.updateHostInfoPanel();
	}

	/**
	 * Delete button handler
	 */
	public void onDelete() {
		final Object selected = this.hostList.getSelectedValue();
		this.hostListModel.removeElement(selected);
		this.selectedHostInfo = null;

		if (this.hostListModel.size() > 0) {
			this.hostList.setSelectedIndex(0);
		} else {
			this.updateHostInfoPanel();
		}

		this.onSave();
		this.hostList.repaint();
	}

	/**
	 * Save button handler
	 */
	public void onSave() {
		this.updateHostInfoObject();

		// remove all previously saved hosts
		int i = 0;

		while (true) {
			final File f = new File(net.sf.jftp.config.Settings.login.concat(String.valueOf(i)));

			if (f.exists()) {
				f.delete();
				i++;
			} else {
				break;
			}
		}

		final int len = this.hostListModel.size();

		for (i = 0; i < len; i++) {
			final net.sf.jftp.gui.base.FtpHost ftphost = (net.sf.jftp.gui.base.FtpHost) this.hostListModel.elementAt(i);
			final String htmp = net.sf.jftp.system.StringUtils.cut(ftphost.hostname, " ");
			final String utmp = net.sf.jftp.system.StringUtils.cut(ftphost.username, " ");
			final String ptmp = net.sf.jftp.system.StringUtils.cut(ftphost.password, " ");
			final String ntmp = net.sf.jftp.system.StringUtils.cut(ftphost.name, " ");
			final String ttmp = net.sf.jftp.system.StringUtils.cut(ftphost.port, " ");
			final net.sf.jftp.config.SaveSet s = new net.sf.jftp.config.SaveSet(net.sf.jftp.config.Settings.login.concat(String.valueOf(i)), htmp, utmp, ptmp, ntmp, ttmp);
		}

		this.hostList.repaint();
	}

	/**
	 * OK Button handler
	 */
	public void onOk() {
		this.selectedHostInfo = this.getSelected();
		this.onSave();
		this.dispose();
	}

	/**
	 * Cancel button handler
	 */
	public void onCancel() {
		this.selectedHostInfo = null;
		this.dispose();
	}

	/**
	 * Create a default one and stuff itin the list
	 */
	public void onNew() {
		final net.sf.jftp.gui.base.FtpHost ftpHost = new net.sf.jftp.gui.base.FtpHost();
		ftpHost.name = "undefined";
		ftpHost.username = "undefined";
		ftpHost.hostname = "undefined";
		ftpHost.password = "undefined";
		ftpHost.port = "21";
		this.hostListModel.addElement(ftpHost);
		this.hostList.setSelectedValue(ftpHost, true);
		this.selectedHostInfo = ftpHost;
	}

	/**
	 * Returns the selected FtpHost from the hostList
	 */
	private net.sf.jftp.gui.base.FtpHost getSelected() {
		final int sel = this.hostList.getSelectedIndex();

		if ((sel < 0) || (sel > (this.hostListModel.size() - 1))) {
			return null;
		} else {
			return (net.sf.jftp.gui.base.FtpHost) this.hostListModel.elementAt(this.hostList.getSelectedIndex());
		}
	}

	/**
	 * Updates the screen to reflect the values from the currently
	 * selected FtpHost object.  If none is selected, then
	 * it clears the panel
	 */
	private void updateHostInfoPanel() {
		if (this.selectedHostInfo == null) {
			this.jtfName.setText("");
			this.jtfUser.setText("");
			this.jtfPass.setText("");
			this.jtfHost.setText("");
			this.jtfPort.setText("");
			this.jtfName.setEnabled(false);
			this.jtfUser.setEnabled(false);
			this.jtfHost.setEnabled(false);
			this.jtfPass.setEnabled(false);
			this.jtfPort.setEnabled(false);
		} else {
			this.jtfName.setEnabled(true);
			this.jtfUser.setEnabled(true);
			this.jtfHost.setEnabled(true);
			this.jtfPass.setEnabled(true);
			this.jtfPort.setEnabled(true);
			this.jtfName.setText(this.selectedHostInfo.name);
			this.jtfUser.setText(this.selectedHostInfo.username);
			this.jtfPass.setText(this.selectedHostInfo.password);
			this.jtfHost.setText(this.selectedHostInfo.hostname);
			this.jtfPort.setText(this.selectedHostInfo.port);
		}
	}

	/**
	 * Updates the currently selected FtpHost object called
	 * "selectedHostInfo" from the contents of the screen
	 */
	private void updateHostInfoObject() {
		if (this.selectedHostInfo == null) {
			return;
		}

		this.selectedHostInfo.hostname = this.jtfHost.getText();
		this.selectedHostInfo.name = this.jtfName.getText();
		this.selectedHostInfo.username = this.jtfUser.getText();
		this.selectedHostInfo.password = new String(this.jtfPass.getPassword());
		this.selectedHostInfo.port = this.jtfPort.getText();
	}
}
