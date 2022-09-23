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

package net.sf.jftp.config;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

@SuppressWarnings("restriction")
public class Crypto {
	private static final char[] PASSWORD = "(l_[m^5][:]FQ8D* ;zoG,7".toCharArray();

	private static final byte[] SALT = {(byte) 0x56, (byte) 0x40, (byte) 0x77, (byte) 0x32, (byte) 0x10, (byte) 0x63, (byte) 0x25, (byte) 0x3C,};

	private static String base64Encode(byte[] bytes) {
		return new String(Base64.encodeBase64(bytes));
	}

	private static byte[] base64Decode(String str) throws IOException {
		return Base64.decodeBase64(str.getBytes());
	}

	public static String Encrypt(String str) {
		// create cryptography object
		SecretKeyFactory factory;
		try {
			factory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
		} catch (NoSuchAlgorithmException e) {
			// We could try another algorithm, but it is highly unlikely that this would be the case
			return "";
		}

		// init key
		SecretKey key;

		try {
			key = factory.generateSecret(new PBEKeySpec(PASSWORD));
		} catch (InvalidKeySpecException e) {
			// The password is hard coded - this exception can't be the case
			return "";
		}

		// init cipher
		Cipher pbeCipher;
		try {
			pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
		} catch (NoSuchPaddingException e) {
			// We could try another algorithm, but it is highly unlikely that this would be the case
			return "";
		} catch (NoSuchAlgorithmException e) {
			// We could try another algorithm, but it is highly unlikely that this would be the case
			return "";
		}

		try {
			pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
		} catch (InvalidKeyException e) {
			return "";
		} catch (InvalidAlgorithmParameterException e) {
			return "";
		}

		// encode & return encoded string
		try {
			return base64Encode(pbeCipher.doFinal(str.getBytes()));
		} catch (IllegalBlockSizeException e) {
			return "";
		} catch (BadPaddingException e) {
			return "";
		}
	}

	public static String Decrypt(String str) {
		// create cryptography object
		SecretKeyFactory keyFactory;
		try {
			keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
		} catch (NoSuchAlgorithmException e) {
			// We could try another algorithm, but it is highly unlikely that this would be the case
			return "";
		}

		// init key
		SecretKey key;
		try {
			key = keyFactory.generateSecret(new PBEKeySpec(PASSWORD));
		} catch (InvalidKeySpecException e) {
			return "";
		}

		// init cipher
		Cipher pbeCipher;
		try {
			pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
		} catch (NoSuchAlgorithmException e) {
			return "";
		} catch (NoSuchPaddingException e) {
			return "";
		}

		try {
			pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
		} catch (InvalidKeyException e) {
			return "";
		} catch (InvalidAlgorithmParameterException e) {
			return "";
		}

		// decode & return decoded string
		String dec;

		try {
			dec = new String(pbeCipher.doFinal(base64Decode(str)));
		} catch (IllegalBlockSizeException e) {
			return "";
		} catch (BadPaddingException e) {
			return "";
		} catch (IOException e) {
			return "";
		}

		return dec;
	}

}