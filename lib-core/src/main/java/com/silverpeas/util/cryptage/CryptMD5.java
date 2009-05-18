package com.silverpeas.util.cryptage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.UtilException;

public class CryptMD5 {

  /*
   * 
   * Encode la chaine passée en paramètre avec l’algorithme MD5
   * 
   * @param original : la chaine à encoder
   * 
   * @return la valeur (string) hexadécimale sur 32 bits
   * 
   */
  public static String crypt(String original) throws UtilException {

    byte[] uniqueKey = original.getBytes();

    // on récupère un objet qui permettra de crypter la chaine
    byte[] hash;
    try {
      hash = MessageDigest.getInstance("MD5").digest(uniqueKey);
    } catch (NoSuchAlgorithmException e) {
      // TODO utiliser le CryptageException
      // throw new CryptageException
      throw new UtilException("CryptMD5.crypt()", SilverpeasException.ERROR,
          "root.EX_NO_MESSAGE", e);
    }

    StringBuffer hashString = new StringBuffer();
    String hex;
    for (int i = 0; i < hash.length; ++i) {
      hex = Integer.toHexString(hash[i]);

      if (hex.length() == 1) {
        hashString.append('0');
        hashString.append(hex.charAt(hex.length() - 1));
      } else {
        hashString.append(hex.substring(hex.length() - 2));
      }
    }

    return hashString.toString();
  }

  /*
   * 
   * Compute the MD5 hash of a file.
   * @param file: the file to be MD5 hashed.
   * @return the MD5 hash as a String.
   * 
   */
  public static String hash(File file) throws UtilException {
    MessageDigest digest;
    FileInputStream fileInputStream = null;
    try {
      digest = MessageDigest.getInstance("MD5");
      fileInputStream = new FileInputStream(file);
      byte[] buffer = new byte[8];
      int c = 0;
      while ((c = fileInputStream.read(buffer)) != -1) {
        digest.update(buffer, 0, c);
      }
      byte[] signature = digest.digest();
      return getHexString(signature);
    } catch (NoSuchAlgorithmException e) {
      throw new UtilException("CryptMD5.hash()", SilverpeasException.ERROR,
          "root.EX_NO_MESSAGE", e);
    } catch (IOException e) {
      throw new UtilException("CryptMD5.hash()", SilverpeasException.ERROR,
          "root.EX_NO_MESSAGE", e);
    } finally {
      if (fileInputStream != null) {
        try {
          fileInputStream.close();
        } catch (IOException e) {
          throw new UtilException("CryptMD5.hash()", SilverpeasException.ERROR,
              "root.EX_NO_MESSAGE", e);
        }
      }
    }
  }

  static final byte[] HEX_CHAR_TABLE = { (byte) '0', (byte) '1', (byte) '2',
      (byte) '3', (byte) '4', (byte) '5', (byte) '6', (byte) '7', (byte) '8',
      (byte) '9', (byte) 'a', (byte) 'b', (byte) 'c', (byte) 'd', (byte) 'e',
      (byte) 'f' };

  protected static String getHexString(byte[] raw)
      throws UnsupportedEncodingException {
    byte[] hex = new byte[2 * raw.length];
    int index = 0;

    for (int i = 0; i < raw.length; i++) {
      int v = raw[i] & 0xFF;
      hex[index++] = HEX_CHAR_TABLE[v >>> 4];
      hex[index++] = HEX_CHAR_TABLE[v & 0xF];
    }
    return new String(hex, "ASCII");
  }
}