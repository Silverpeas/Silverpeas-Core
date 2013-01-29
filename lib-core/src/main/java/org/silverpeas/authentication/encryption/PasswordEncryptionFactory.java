package org.silverpeas.authentication.encryption;

/**
 * Factory of password encryption objects implementing a given algorithm. It wraps the concrete
 * implementation of the <code>PasswordEncryption</code> interface used for encrypting a password
 * according to a chosen algorithm.
 * <p/>
 * This factory provides all of the available password encryption supported by Silverpeas,
 * nevertheless it returns only the main encryption used by default in Silverpeas (the one that is
 * considered as the more robust and secure) with the <code>getDefaultPasswordEncryption()</code>
 * method. Getting others encryption can be done in order to work with passwords encrypted with
 * old (and then deprecated) algorithms with the <code>getPasswordEncryption(digest)</code>
 * method.
 *
 * @author mmoquillon
 */
public class PasswordEncryptionFactory {

  private static PasswordEncryptionFactory instance = new PasswordEncryptionFactory();
  private final PasswordEncryption currentEncryption = new UnixMD5Encryption();

  /**
   * Gets an instance of the factory of password encryption.
   *
   * @return a instance of the PasswordEncryptionFactory class.
   */
  public static PasswordEncryptionFactory getFactory() {
    return instance;
  }

  /**
   * Gets the password encryption that is used by default in Silverpeas to encrypt the user
   * passwords and to check them.
   * @return the current default password encryption.
   */
  public PasswordEncryption getDefaultPasswordEncryption() {
    return currentEncryption;
  }

  /**
   * Gets the encryption that has computed the specified digest.
   * <p/>
   * As digests in password encryption are usually made up of an encryption algorithm identifier,
   * the factory can then find the algorithm that matches the specified digest. If the digest
   * doesn't contain any algorithm identifier, then the UnixDES is returned (yet it is the only one
   * supported by Silverpeas that doesn't generate an algorithm identifier in the digest). In the
   * case the identifier in the digest isn't known, then a exception is thrown.
   *
   * @param digest the digest from which the password encryption has be found.
   * @return the password encryption that has computed the specified digest.
   * @throws IllegalArgumentException if the digest was not computed by any of the password
   * encryption supported in Silverpeas.
   */
  public PasswordEncryption getPasswordEncryption(String digest) throws IllegalArgumentException {
    PasswordEncryption encryption;
    if (currentEncryption.doUnderstandDigest(digest)) {
      encryption = currentEncryption;
    } else {
      encryption = new UnixDESEncryption();
      if (!encryption.doUnderstandDigest(digest)) {
        throw new IllegalArgumentException("Digest '" + digest + "' not understand by any of the" +
            "available encryption in Silverpeas");
      }
    }
    return encryption;
  }
}
