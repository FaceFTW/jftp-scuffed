package yourTestsHere;

import static org.junit.Assert.*;

import net.sf.jftp.JFtp;
import net.sf.jftp.gui.base.LocalDir;
import net.sf.jftp.gui.base.RemoteDir;

import org.junit.Before;
import org.junit.Test;

/**
 * Put your unit tests in this class.
 * Put any Testing-only subclasses in this file or in the yourTestsHere package
 * 
 * @author hewner
 *
 */
public class CSSE375Test {

	@Before
	public void setUp() throws Exception {
	}
	
/*
 Requirements for tests (from the assignment):
 
+ You *must not* have your tests rely on either a particular webserver
  (e.g. ftp.csse.rose-hulman.edu) OR the local filesystem.  Instead,
  you need to create a fake connection with non-real files and test
  your remote and local dirs with that.

+ You must create both LocalDir and RemoteDir in your tests (or maybe
  a test specific subclass or either of them).

+ The code you test should be the actual code in those classes, not
  some copy of the code in your test-only classes.

+ Avoid changing the codebase to allow for tests (the occasional
  extract method or extract interface is fine though)

+ Your tests should test with several different directory sizes.

+ Your tests should not bring up a visible GUI

+ You should not use any additional testing library (e.g. mocking)
  beyond JUnit
 
+ To test your feature, you can feel free to add a function like
  getDirLength() and test against that.  Obviously, in the GUI you
  must use java to bring up a dialog but you don't need to test the
  dialog creation code.

*/
	@Test
	public void test() {
		
		// JFtp jftp = new JFtp(); //this line is problematical because it opens the GUI
		
		LocalDir local = new LocalDir();
		RemoteDir bar = new RemoteDir();
		
		fail("Not yet implemented");
	}

}
