package org.silverpeas.core.security.authentication.encryption;

import org.junit.Before;
import org.junit.Test;
import org.silverpeas.util.Charsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Unit tests on the Unix variant of the SHA-512 encryption for passwords.
 */
public class UnixSHA512EncryptionTest {

  static final String SIMPLE_DIGEST = "$6$saltstring$FwwMuPiGNXz8H78tCv7t9djhhB8drD7mo2gjpEpkQ8xnSDPZwvm05vIhse.tgr9lOxZpxL2r/Bvl96m5sDFic.";
  static final String COMPLETE_DIGEST = "$6$rounds=10000$saltstring$6CQL08eiNceWQP69zZ4GiVdq2X20CJ3XrrSeqYcAIkABtUDrObxnmrwwCs11SwXKHW7t5umufCvTKSH34HteK.";
  static final String DIGEST_OF_EMPTY_PASSWORD = "$6$saltstring$kyGrqt6gmjAdtFLPrflEFifSYLCWWq1pyx95SvqinLDy2UHmj0sTF0MSLMwxPFZc3tu5kQckI8fks0zOPda3n1";
  static final String MD5_DIGEST = "$1$saltstring$cV2aCPNGeOcei00Op3/Oo/";

  static final String password = "Hello World!";
  static final byte[] salt = "saltstring".getBytes(Charsets.UTF_8);
  static final byte[] saltWithRounds = "rounds=10000$saltstring".getBytes(Charsets.UTF_8);
  static final byte[] emptySalt = new byte[0];

  private UnixSHA512Encryption encryption;

  @Before
  public void setUp() throws Exception {
    encryption = new UnixSHA512Encryption();
  }

  @Test(expected = NullPointerException.class)
  public void testEncryptionOfANullPassword() throws Exception {
    encryption.encrypt(null, salt);
  }

  @Test
  public void testEncryptionOfAnEmptyPassword() throws Exception {
    String digest = encryption.encrypt("", salt);
    assertThat(digest, is(DIGEST_OF_EMPTY_PASSWORD));
  }

  @Test
  public void testEncryptionOfANonEmptyPassword() throws Exception {
    String digest = encryption.encrypt(password, salt);
    assertThat(digest, is(SIMPLE_DIGEST));
  }

  @Test
  public void testEncryptionWithARoundsInstruction() throws Exception {
    String digest = encryption.encrypt(password, saltWithRounds);
    assertThat(digest, is(COMPLETE_DIGEST));
  }

  @Test
  public void testCheckSuccess() throws Exception {
    encryption.check(password, SIMPLE_DIGEST);
  }

  @Test
  public void testCheckSuccessWithEmptyPassword() throws Exception {
    encryption.check("", DIGEST_OF_EMPTY_PASSWORD);
  }

  @Test(expected = NullPointerException.class)
  public void testCheckWithNullPassword() throws Exception {
    encryption.check(null, DIGEST_OF_EMPTY_PASSWORD);
  }

  @Test(expected = AssertionError.class)
  public void testCheckFailure() throws Exception {
    encryption.check(password, COMPLETE_DIGEST);
  }

  @Test(expected = AssertionError.class)
  public void testCheckWithNonConformDigest() throws Exception {
    encryption.check(password, MD5_DIGEST);
  }

  @Test
  public void testGetSaltUsedInDigest() throws Exception {
    byte[] actualSalt = encryption.getSaltUsedInDigest(SIMPLE_DIGEST);
    assertThat(actualSalt, is(salt));
  }

  @Test
  public void testGetSaltUsedInACompleteDigest() throws Exception {
    byte[] actualSalt = encryption.getSaltUsedInDigest(COMPLETE_DIGEST);
    assertThat(actualSalt, is(salt));
  }

  @Test
  public void testGetSaltUsedInANonConformDigest() throws Exception {
    byte[] actualSalt = encryption.getSaltUsedInDigest(MD5_DIGEST);
    assertThat(actualSalt, is(emptySalt));
  }

  @Test
  public void testGetSaltUsedInAnEmptyDigest() throws Exception {
    byte[] actualSalt = encryption.getSaltUsedInDigest("");
    assertThat(actualSalt, is(emptySalt));
  }

  @Test(expected = NullPointerException.class)
  public void testGetSaltUsedInANullDigest() throws Exception {
    encryption.getSaltUsedInDigest(null);
  }

  @Test
  public void testDoUnderstandDigestWithoutRounds() throws Exception {
    assertThat(encryption.doUnderstandDigest(SIMPLE_DIGEST), is(true));
  }

  @Test
  public void testDoUnderstandCompleteDigest() throws Exception {
    assertThat(encryption.doUnderstandDigest(COMPLETE_DIGEST), is(true));
  }

  @Test
  public void testDoUnderstandDigestOfEmptyPassword() throws Exception {
    assertThat(encryption.doUnderstandDigest(DIGEST_OF_EMPTY_PASSWORD), is(true));
  }

  @Test
  public void testDoUnderstandNonConformDigest() throws Exception {
    assertThat(encryption.doUnderstandDigest(MD5_DIGEST), is(false));
  }

  @Test
  public void testDoUnderstandEmptyDigest() throws Exception {
    assertThat(encryption.doUnderstandDigest(""), is(false));
  }

  @Test(expected = NullPointerException.class)
  public void testDoUnderstandNullDigest() throws Exception {
    assertThat(encryption.doUnderstandDigest(null), is(false));
  }
}
