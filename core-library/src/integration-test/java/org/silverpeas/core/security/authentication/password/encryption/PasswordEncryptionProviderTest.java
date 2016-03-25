package org.silverpeas.core.security.authentication.password.encryption;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.security.authentication.password.PasswordEncryption;
import org.silverpeas.core.security.authentication.password.PasswordEncryptionProvider;
import org.silverpeas.core.test.WarBuilder4LibCore;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

/**
 * Unit tests on the getting of the correct PasswordEncryption implementations with the
 * PasswordEncryptionFactory instances.
 */
@RunWith(Arquillian.class)
public class PasswordEncryptionProviderTest {

  static final String DES_DIGEST = "sa5zYATwASgaM";
  static final String MD5_DIGEST = "$1$saltstri$E33SwU4pwr7k4cYt7LIty0";
  static final String SHA512_DIGEST = "$6$saltstring$FwwMuPiGNXz8H78tCv7t9djhhB8drD7mo2gjpEpkQ8xnSDPZwvm05vIhse.tgr9lOxZpxL2r/Bvl96m5sDFic.";

  static final Class CURRENT_ENCRYPTION = UnixSHA512Encryption.class;

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(PasswordEncryptionProviderTest.class)
        .testFocusedOn((warBuilder) -> {
          warBuilder.addPackages(true, "org.silverpeas.core.security.authentication.password");
        }).build();
  }

  @Test
  public void testGetDefaultPasswordEncryption() throws Exception {
    PasswordEncryption defaultEncryption =
        PasswordEncryptionProvider.getDefaultPasswordEncryption();
    assertThat(defaultEncryption, instanceOf(CURRENT_ENCRYPTION));
  }

  @Test
  public void testGetPasswordEncryptionFromADESDigest() throws Exception {
    PasswordEncryption encryption = PasswordEncryptionProvider.getPasswordEncryption(DES_DIGEST);
    assertThat(encryption, instanceOf(UnixDESEncryption.class));
  }

  @Test
  public void testGetPasswordEncryptionFromAMD5Digest() throws Exception {
    PasswordEncryption encryption = PasswordEncryptionProvider.getPasswordEncryption(MD5_DIGEST);
    assertThat(encryption, instanceOf(UnixMD5Encryption.class));
  }

  @Test
  public void testGetPasswordEncryptionFromASHA512Digest() throws Exception {
    PasswordEncryption encryption = PasswordEncryptionProvider.getPasswordEncryption(SHA512_DIGEST);
    assertThat(encryption, instanceOf(UnixSHA512Encryption.class));
  }
}
