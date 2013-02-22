package com.silverpeas.util.security;

import com.silverpeas.util.StringUtil;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.silverpeas.util.crypto.Cipher;
import org.silverpeas.util.crypto.CipherFactory;
import org.silverpeas.util.crypto.CryptographicAlgorithmName;

import java.io.File;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

/**
 * Unit tests on encryption management done by the ContentEncryptionService instances.
 */
public class KeyManagementTest extends ContentEncryptionServiceTest {

  @Test
  public void testKeyCreation() throws Exception {
    // no existing key file
    File keyFile = new File(ContentEncryptionService.ACTUAL_KEY_FILE_PATH);
    assertThat(keyFile.exists(), is(false));

    // encryption key
    String key = generateAESKey();
    getContentEncryptionService().updateEncryptionKey(key);

    // check
    assertKeyFileExistsWithKey(keyFile, key);
  }

  @Test
  public void testKeyUpdate() throws Exception {
    // create the key file with an encryption key
    File keyFile = new File(ContentEncryptionService.ACTUAL_KEY_FILE_PATH);
    String key = generateAESKey();
    createKeyFileWithTheKey(key);
    // the key encrypted by the service
    String previousEncryptedKey = FileUtils.readFileToString(keyFile);

    // new encryption key
    key = generateAESKey();
    getContentEncryptionService().updateEncryptionKey(key);

    // check
    assertKeyFileExistsWithKey(keyFile, key);
    assertOldKeyFileContainsThePreviousKey(previousEncryptedKey);
  }

  private static void assertKeyFileExistsWithKey(File keyFile, String expectedKey)
      throws Exception {
    assertThat(keyFile.exists(), is(true));
    assertThat(keyFile.canWrite(), is(false));
    String encryptedKey = FileUtils.readFileToString(keyFile);
    assertThat(StringUtil.isDefined(encryptedKey), is(true));
    CipherFactory cipherFactory = CipherFactory.getFactory();
    Cipher cast125 = cipherFactory.getCipher(CryptographicAlgorithmName.CAST5);
    String actualKey = cast125.decrypt(StringUtil.fromBase64(encryptedKey),
        ContentEncryptionService.CIPHER_KEY);
    assertThat(actualKey, is(expectedKey));
  }

  private void assertOldKeyFileContainsThePreviousKey(String previousEncryptedKey) throws Exception {
    File deprecatedKeyFile = new File(ContentEncryptionService.DEPRECATED_KEY_FILE_PATH);
    File keyFile = new File(ContentEncryptionService.ACTUAL_KEY_FILE_PATH);

    String encryptedKey = FileUtils.readFileToString(keyFile);
    assertThat(encryptedKey, not(previousEncryptedKey));
    assertThat(deprecatedKeyFile.exists(), is(true));
    String deprecatedKey = FileUtils.readFileToString(deprecatedKeyFile);
    assertThat(deprecatedKey, is(previousEncryptedKey));
  }
}
