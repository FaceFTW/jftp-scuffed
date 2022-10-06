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

import chrriis.dj.sweet.components.JWebBrowser;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

public class NativeHttpBrowser extends Composite {

	private NativeHttpBrowser(Composite parent, String url) throws Exception {
		super(parent, SWT.NONE);
		this.setLayout(new GridLayout());
		Group webBrowserPanel = new Group(this, SWT.NONE);
		webBrowserPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		webBrowserPanel.setLayout(new FillLayout());

		JWebBrowser webBrowser = new JWebBrowser(webBrowserPanel);
		webBrowser.navigate(url);

		webBrowserPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	}

	public static void main(String[] args) throws Exception {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new NativeHttpBrowser(shell, args[0]);
		shell.setSize(800, 600);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}

}


