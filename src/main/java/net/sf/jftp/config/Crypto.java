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

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

@SuppressWarnings("restriction")
public class Crypto {
	private static final char[] PASSWORD = "(l_[m^5][:]FQ8D* ;zoG,7".toCharArray();

	private static final byte[] SALT = {(byte) 0x56, (byte) 0x40, (byte) 0x77, (byte) 0x32, (byte) 0x10, (byte) 0x63, (byte) 0x25, (byte) 0x3C,};

	private static String base64Encode(final byte[] bytes) {
		return new String(Base64.encodeBase64(bytes));
	}

	private static byte[] base64Decode(final String str) throws IOException {
		return Base64.decodeBase64(str.getBytes());
	}

	public static String Encrypt(final String str) {
		// create cryptography object
		final SecretKeyFactory factory;
		try {
			factory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
		} catch (final NoSuchAlgorithmException e) {
			// We could try another algorithm, but it is highly unlikely that this would be the case
			return "";
		}

		// init key
		final SecretKey key;

		try {
			key = factory.generateSecret(new PBEKeySpec(net.sf.jftp.config.Crypto.PASSWORD));
		} catch (final InvalidKeySpecException e) {
			// The password is hard coded - this exception can't be the case
			return "";
		}

		// init cipher
		final Cipher pbeCipher;
		try {
			pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
		} catch (final javax.crypto.NoSuchPaddingException | java.security.NoSuchAlgorithmException e) {
			// We could try another algorithm, but it is highly unlikely that this would be the case
			return "";
		}

		try {
			pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(net.sf.jftp.config.Crypto.SALT, 20));
		} catch (final java.security.InvalidKeyException | java.security.InvalidAlgorithmParameterException e) {
			return "";
		}

		// encode & return encoded string
		try {
			return net.sf.jftp.config.Crypto.base64Encode(pbeCipher.doFinal(str.getBytes()));
		} catch (final javax.crypto.IllegalBlockSizeException | javax.crypto.BadPaddingException e) {
			return "";
		}
	}

	public static String Decrypt(final String str) {
		// create cryptography object
		final SecretKeyFactory keyFactory;
		try {
			keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
		} catch (final NoSuchAlgorithmException e) {
			// We could try another algorithm, but it is highly unlikely that this would be the case
			return "";
		}

		// init key
		final SecretKey key;
		try {
			key = keyFactory.generateSecret(new PBEKeySpec(net.sf.jftp.config.Crypto.PASSWORD));
		} catch (final InvalidKeySpecException e) {
			return "";
		}

		// init cipher
		final Cipher pbeCipher;
		try {
			pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
		} catch (final java.security.NoSuchAlgorithmException | javax.crypto.NoSuchPaddingException e) {
			return "";
		}

		try {
			pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(net.sf.jftp.config.Crypto.SALT, 20));
		} catch (final java.security.InvalidKeyException | java.security.InvalidAlgorithmParameterException e) {
			return "";
		}

		// decode & return decoded string
		final String dec;

		try {
			dec = new String(pbeCipher.doFinal(net.sf.jftp.config.Crypto.base64Decode(str)));
		} catch (final javax.crypto.IllegalBlockSizeException | java.io.IOException | javax.crypto.BadPaddingException e) {
			return "";
		}

		return dec;
	}

}