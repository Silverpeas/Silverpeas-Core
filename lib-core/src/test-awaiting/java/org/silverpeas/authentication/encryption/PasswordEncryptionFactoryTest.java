package org.silverpeas.authentication.encryption;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

/**
 * Unit tests on the getting of the correct PasswordEncryption implementations with the
 * PasswordEncryptionFactory instances.
 */
public class PasswordEncryptionFactoryTest {

  static final String DES_DIGEST = "sa5zYATwASgaM";
  static final String MD5_DIGEST = "$1$saltstri$E33SwU4pwr7k4cYt7LIty0";
  static final String SHA512_DIGEST = "$6$saltstring$FwwMuPiGNXz8H78tCv7t9djhhB8drD7mo2gjpEpkQ8xnSDPZwvm05vIhse.tgr9lOxZpxL2r/Bvl96m5sDFic.";

  static final Class CURRENT_ENCRYPTION = UnixSHA512Encryption.class;

  PasswordEncryptionFactory encryptionFactory;

  @Before
  public void setUp() throws Exception {
    encryptionFactory = PasswordEncryptionFactory.getFactory();
  }

  @Test
  public void testGetDefaultPasswordEncryption() throws Exception {
    PasswordEncryption defaultEncryption = encryptionFactory.getDefaultPasswordEncryption();
    assertThat(defaultEncryption, instanceOf(CURRENT_ENCRYPTION));
  }

  @Test
  public void testGetPasswordEncryptionFromADESDigest() throws Exception {
    PasswordEncryption encryption = encryptionFactory.getPasswordEncryption(DES_DIGEST);
    assertThat(encryption, instanceOf(UnixDESEncryption.class));
  }

  @Test
  public void testGetPasswordEncryptionFromAMD5Digest() throws Exception {
    PasswordEncryption encryption = encryptionFactory.getPasswordEncryption(MD5_DIGEST);
    assertThat(encryption, instanceOf(UnixMD5Encryption.class));
  }

  @Test
  public void testGetPasswordEncryptionFromASHA512Digest() throws Exception {
    PasswordEncryption encryption = encryptionFactory.getPasswordEncryption(SHA512_DIGEST);
    assertThat(encryption, instanceOf(UnixSHA512Encryption.class));
  }
}
