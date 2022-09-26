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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package net.sf.jftp.net;


/**
 * This class provides some constant used by FtpConnection.
 */
public interface FtpConstants {
	/* List */

	//public static final String LIST = "LIST -laL";

	/**
	 * Possible upload() / download() return code
	 */
	int TRANSFER_FAILED = -1;

	/**
	 * Possible upload() / download() return code
	 */
	int TRANSFER_STOPPED = -2;

	/**
	 * Possible upload() / download() / remove() return code
	 */
	int MKDIR_FAILED = -3;

	/**
	 * Possible upload() / download() return code
	 */
	int PERMISSION_DENIED = -4;

	/**
	 * Possible upload() / download() / exists() return code
	 */
	int CHDIR_FAILED = -5;

	/**
	 * Possible login() return code
	 */
	int WRONG_LOGIN_DATA = -6;

	/**
	 * Possible login() return code
	 */
	int OFFLINE = -7;

	/**
	 * Possible login() / remove() return code
	 */
	int GENERIC_FAILED = -8;

	/**
	 * Possible remove() return code
	 */
	int REMOVE_FAILED = -9;

	/**
	 * Possible upload() return code
	 */
	int RENAME_FAILED = -10;

	/**
	 * Possible exists() return code
	 */
	int FILE_NOT_FOUND = -11;

	/**
	 * Possible up/download() return code
	 */
	int DATACONNECTION_FAILED = -12;

	/**
	 * Possible upload() / download() return code
	 */
	int NEW_TRANSFER_SPAWNED = 0;

	/**
	 * Possible upload() / download() return code
	 */
	int TRANSFER_SUCCESSFUL = 1;

	/**
	 * Possible login() return code
	 */
	int LOGIN_OK = 2;

	/**
	 * Possible remove() return code
	 */
	int REMOVE_SUCCESSFUL = 3;

	/**
	 * Possible getPermissions() / exists() return code
	 */
	int R = 23;

	/**
	 * Possible getPermissions() / exists() return code
	 */
	int W = 42;

	/**
	 * Possible getPermissions() / exists() return code
	 */
	int DENIED = -666;

	// Access Control Commands

	/**
	 * User Name
	 */
	String USER = "USER";

	/**
	 * Password
	 */
	String PASS = "PASS";

	/**
	 * Account
	 */
	String ACCT = "ACCT";

	/**
	 * Change Working Directory
	 */
	String CWD = "CWD";

	/**
	 * Change to Parent Directory
	 */
	String CDUP = "CDUP";

	/**
	 * Structure Mount
	 */
	String SMNT = "SMNT";

	/**
	 * Reinitialize
	 */
	String REIN = "REIN";

	/**
	 * Logout
	 */
	String QUIT = "QUIT";

	// Transfer Parameter Commands

	/**
	 * Data Port
	 */
	String PORT = "PORT";

	/**
	 * Passive
	 */
	String PASV = "PASV";

	/**
	 * Representation Type (ASCII, EBCDIC, Image, Non-print, Telnet format effectors, Carriage Control, Local byte Byte size)
	 */
	String TYPE = "TYPE";

	/**
	 * File Structure (File, Record Structure, Page Structure)
	 */
	String STRU = "STRU";

	/**
	 * Transfer Mode (Stream, Block, Compressed)
	 */
	String MODE = "MODE";

	// FTP Service Commands

	/**
	 * Retrieve
	 */
	String RETR = "RETR";

	/**
	 * Store
	 */
	String STOR = "STOR";

	/**
	 * Store Unique
	 */
	String STOU = "STOU";

	/**
	 * Append (with create)
	 */
	String APPE = "APPE";

	/**
	 * Allocate
	 */
	String ALLO = "ALLO";

	/**
	 * Restart
	 */
	String REST = "REST";

	/**
	 * Rename From
	 */
	String RNFR = "RNFR";

	/**
	 * Rename To
	 */
	String RNTO = "RNTO";

	/**
	 * Abort
	 */
	String ABOR = "ABOR";

	/**
	 * Delete
	 */
	String DELE = "DELE";

	/**
	 * Remove Directory
	 */
	String RMD = "RMD";

	/**
	 * Make Directory
	 */
	String MKD = "MKD";

	/**
	 * Print Working Directory
	 */
	String PWD = "PWD";

	/**
	 * Name List
	 */
	String NLST = "NLST";

	/**
	 * Site Parameters
	 */
	String SITE = "SITE";

	/**
	 * System
	 */
	String SYST = "SYST";

	/**
	 * Status
	 */
	String STAT = "STAT";

	/**
	 * Help
	 */
	String HELP = "HELP";

	/**
	 * NOOP
	 */
	String NOOP = "NOOP";

	// The following commands were introduced in rfc 2228 FTP Security Extensions

	/**
	 * Authentication/Security Mechanism
	 */
	String AUTH = "AUTH";

	/**
	 * Authentication/Security Data
	 */
	String ADAT = "ADAT";

	/**
	 * Data Channel Protection Level
	 */
	String PROT = "PROT";

	/**
	 * Protection Buffer Size
	 */
	String PBSZ = "PBSZ";

	/**
	 * Clear Command Channel
	 */
	String CCC = "CCC";

	/**
	 * Integrity Protected Command
	 */
	String MIC = "MIC";

	/**
	 * Confidentiality Protected Command
	 */
	String CONF = "CONF";

	/**
	 * Privacy Protected Command
	 */
	String ENC = "ENC";

	// end of commands from rfc 2228
	// ftp reply codes
	// x0z Syntax
	// x1z Information
	// x2z Connections
	// x3z Authentication and accounting
	// x4z Unspecified
	// x5z File system
	// 1yz Positive Preliminary reply

	/**
	 * Restart marker reply
	 */
	String RC110 = "110";

	/**
	 * Service ready in nnn minutes
	 */
	String RC120 = "120";

	/**
	 * Data connection already open
	 */
	String RC125 = "125";

	/**
	 * File status okay; about to open data connection
	 */
	String RC150 = "150";

	// 2yz Positive Completion reply

	/**
	 * Command okay
	 */
	String FTP200_OK = "2";

	/**
	 * Command not implemented, superfluous at this site
	 */
	String RC202 = "202";

	/**
	 * System status, or system help reply
	 */
	String RC211 = "211";

	/**
	 * Directory status
	 */
	String RC212 = "212";

	/**
	 * File status
	 */
	String RC213 = "213";

	/**
	 * Help message
	 */
	String RC214 = "214";

	/**
	 * System type
	 */
	String FTP215_SYSTEM_TYPE = "215";

	/**
	 * Service ready for new user
	 */
	String FTP220_SERVICE_READY = "220";

	/**
	 * Service closing control connection
	 */
	String FTP221_SERVICE_CLOSING = "221";

	/**
	 * Data connection open; no transfer in progress
	 */
	String RC225 = "225";

	/**
	 * Closing data connection; requested file action successful
	 */
	String FTP226_CLOSING_DATA_REQUEST_SUCCESSFUL = "226";

	/**
	 * Entering Passive Mode
	 */
	String FTP227_ENTERING_PASSIVE_MODE = "227";

	/**
	 * User logged in, proceed
	 */
	String FTP230_LOGGED_IN = "2"; // 230

	/**
	 * Requested file action okay, completed
	 */
	String FTP250_COMPLETED = "250";

	/**
	 * "PATHNAME" created
	 */
	String FTP257_PATH_CREATED = "257";

	// 3yz Positive Intermediate reply

	/**
	 * User name okay, need password
	 */
	String FTP331_USER_OK_NEED_PASSWORD = "331";

	/**
	 * Need account for login
	 */
	String RC332 = "332";

	/**
	 * Requested file action pending further information
	 */
	String RC350 = "350";

	// 4yz Transient Negative Completion reply

	/**
	 * Service not available
	 */
	String RC421 = "421";

	/**
	 * Can't open data connection
	 */
	String RC425 = "425";

	/**
	 * Connection closed; transfer aborted
	 */
	String RC426 = "426";

	/**
	 * Requested file action not taken, file unavailable (file busy)
	 */
	String RC450 = "450";

	/**
	 * Requested action aborted: local error in processing
	 */
	String RC451 = "451";

	/**
	 * Requested action not taken, insufficient storage space in system
	 */
	String RC452 = "452";

	// 5yz Permanent Negative Completion reply

	/**
	 * Syntax error, command unrecognized
	 */
	String RC500 = "500";

	/**
	 * Syntax error in parameters or arguments
	 */
	String RC501 = "501";

	/**
	 * Command not implemented
	 */
	String RC502 = "502";

	/**
	 * Bad sequence of commands
	 */
	String RC503 = "503";

	/**
	 * Command not implemented for that parameter
	 */
	String RC504 = "504";

	/**
	 * Not logged in
	 */
	String RC530 = "530";

	/**
	 * Need account for storing files
	 */
	String RC532 = "532";

	/**
	 * Requested action not taken, file unavailable (file not found, no access)
	 */
	String RC550 = "550";

	/**
	 * Requested action aborted: page type unknown
	 */
	String RC551 = "551";

	/**
	 * Requested file action aborted, exceeded storage allocation
	 */
	String RC552 = "552";

	/**
	 * Requested action not taken, file name not allowed
	 */
	String RC553 = "553";
}
