/*
 * Copyright (C) 2000 - 2018 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.security.encryption;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.security.encryption.cipher.Cipher;
import org.silverpeas.core.security.encryption.cipher.CipherFactory;
import org.silverpeas.core.security.encryption.cipher.CipherKey;
import org.silverpeas.core.security.encryption.cipher.CryptoException;
import org.silverpeas.core.security.encryption.cipher.CryptographicAlgorithmName;
import org.silverpeas.core.util.StringUtil;

import java.io.File;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Unit tests on encryption management done by the DefaultContentEncryptionService instances.
 */
public class KeyManagementTest extends ContentEncryptionServiceTest {

  @Test
  public void testKeyCreation() throws Exception {
    // no existing key file
    File keyFile = new File(ACTUAL_KEY_FILE_PATH);
    assertThat(keyFile.exists(), is(false));

    // encryption key
    String key = generateAESKey();
    getContentEncryptionService().updateCipherKey(key);

    // check
    assertKeyFileExistsWithKey(keyFile, key);
  }

  @Test
  public void testKeyUpdate() throws Exception {
    // create the key file with an encryption key
    File keyFile = new File(ACTUAL_KEY_FILE_PATH);
    String key = generateAESKey();
    createKeyFileWithTheActualKey(key);
    // the key encrypted by the service
    String previousEncryptedKey = FileUtils.readFileToString(keyFile);

    // new encryption key
    key = generateAESKey();
    getContentEncryptionService().updateCipherKey(key);

    // check
    assertKeyFileExistsWithKey(keyFile, key);
    assertOldKeyFileContainsThePreviousKey(previousEncryptedKey);
  }

  @Test
  public void testKeyUpdateWithRegisteredContentIterators() throws Exception {
    // create the key file with an encryption key
    File keyFile = new File(ACTUAL_KEY_FILE_PATH);
    String key = generateAESKey();
    createKeyFileWithTheActualKey(key);
    // the key encrypted by the service
    String previousEncryptedKey = FileUtils.readFileToString(keyFile);
    // some iterators on encrypted contents
    final int count = 5;
    MyOwnContentIterator[] iterators = getEncryptionContentIterators(count, key);
    for (int i = 0; i < iterators.length; i++) {
      getContentEncryptionService().registerForRenewingContentCipher(iterators[i]);
    }

    // new encryption key
    key = generateAESKey();
    getContentEncryptionService().updateCipherKey(key);

    // check
    assertKeyFileExistsWithKey(keyFile, key);
    assertOldKeyFileContainsThePreviousKey(previousEncryptedKey);
    assertTheContentCipherIsRenewed(iterators, key);
  }

  @Test
  public void testCipherRenewFailureWhenUpdatingCipherKey() throws Exception {
    // create the key file with the actual cipher key
    String encryptedKey = generateAESKey();
    createKeyFileWithTheActualKey(encryptedKey);

    // create the key file with a previous cipher key
    String deprecatedEncryptedKey = generateAESKey();
    createKeyFileWithTheDeprecatedKey(deprecatedEncryptedKey);

    // an iterator on encrypted contents that will provoke an error with the cipher renewing
    final int count = 5;
    getContentEncryptionService().registerForRenewingContentCipher(new MyOwnContentIterator(count,
        true));

    // new encryption key
    String key = generateAESKey();
    try {
      getContentEncryptionService().updateCipherKey(key);
      fail("A CryptoException exception should be thrown");
    } catch (CryptoException ex) {
      assertKeyFileExistsWithKey(new File(ACTUAL_KEY_FILE_PATH), encryptedKey);
      assertKeyFileExistsWithKey(new File(DEPRECATED_KEY_FILE_PATH), deprecatedEncryptedKey);
    }
  }

  private static MyOwnContentIterator[] getEncryptionContentIterators(final int count, String key)
      throws Exception {
    MyOwnContentIterator[] iterators = new MyOwnContentIterator[count];
    for (int i = 0; i < count; i++) {
      iterators[i] = new MyOwnContentIterator(count, false);
      iterators[i].encryptContents(key);
    }
    return iterators;
  }

  private static void assertKeyFileExistsWithKey(File keyFile, String expectedKey)
      throws Exception {
    assertThat(keyFile.exists(), is(true));
    //assertThat(keyFile.canWrite(), is(false)); doesn't work when test is running as root
    String[] encryptedKeys = FileUtils.readFileToString(keyFile).split(" ");
    assertThat(StringUtil.isDefined(encryptedKeys[1]), is(true));
    CipherFactory cipherFactory = CipherFactory.getFactory();
    Cipher cast125 = cipherFactory.getCipher(CryptographicAlgorithmName.CAST5);
    String actualKey = cast125.decrypt(StringUtil.fromBase64(encryptedKeys[1]),
        CipherKey.aKeyFromBase64Text(encryptedKeys[0]));
    assertThat(actualKey, is(expectedKey));
  }

  private void assertOldKeyFileContainsThePreviousKey(String previousEncryptedKey)
      throws Exception {
    File deprecatedKeyFile = new File(DEPRECATED_KEY_FILE_PATH);
    File keyFile = new File(ACTUAL_KEY_FILE_PATH);

    String encryptedKey = FileUtils.readFileToString(keyFile);
    assertThat(encryptedKey, not(previousEncryptedKey));
    assertThat(deprecatedKeyFile.exists(), is(true));
    String deprecatedKey = FileUtils.readFileToString(deprecatedKeyFile);
    assertThat(deprecatedKey, is(previousEncryptedKey));
  }

  private void assertTheContentCipherIsRenewed(MyOwnContentIterator[] iterators, String key)
      throws Exception {
    CipherFactory cipherFactory = CipherFactory.getFactory();
    Cipher aes = cipherFactory.getCipher(CryptographicAlgorithmName.AES);
    CipherKey cipherKey = CipherKey.aKeyFromHexText(key);
    for (MyOwnContentIterator iterator : iterators) {
      TextContent[] contents = iterator.getContents();
      TextContent[] encryptedContents = iterator.getEncryptedContents();
      for (int i = 0; i < encryptedContents.length; i++) {
        String title = aes.
            decrypt(StringUtil.fromBase64(encryptedContents[i].getTitle()), cipherKey);
        String description = aes.decrypt(StringUtil.
            fromBase64(encryptedContents[i].getDescription()), cipherKey);
        String text = aes.decrypt(StringUtil.fromBase64(encryptedContents[i].getText()), cipherKey);

        assertThat(title, is(contents[i].getTitle()));
        assertThat(description, is(contents[i].getDescription()));
        assertThat(text, is(contents[i].getText()));
      }
    }
  }

  private static class MyOwnContentIterator implements EncryptionContentIterator {

    private int count;
    private TextContent[] contents;
    private TextContent[] encryptedContents;
    private int current = -1;
    private boolean error = false;

    public MyOwnContentIterator(int contentCount, boolean withError) {
      this.count = contentCount;
      this.error = withError;
      contents = generateTextContents(count);
    }

    public TextContent[] getContents() {
      return this.contents;
    }

    public TextContent[] getEncryptedContents() {
      return encryptedContents;
    }

    @Override
    public Map<String, String> next() {
      if (error) {
        return null;
      } else {
        return encryptedContents[current].getProperties();
      }
    }

    @Override
    public boolean hasNext() {
      return ++current < contents.length;
    }

    @Override
    public void update(final Map<String, String> updatedContent) {
      TextContent content =
          new TextContent(contents[current].getId(), contents[current].getComponentInstanceId(),
          contents[current].getCreator());
      content.setTitle(updatedContent.get(TextContent.Properties.Title.name()));
      content.setDescription(updatedContent.get(TextContent.Properties.Description.name()));
      content.setText(updatedContent.get(TextContent.Properties.Text.name()));
      encryptedContents[current] = content;
    }

    @Override
    public void onError(final Map<String, String> content, final CryptoException ex) {
      if (!this.error) {
        fail(ex.getMessage());
      }
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void init() {
    }

    private void encryptContents(String key) throws Exception {
      encryptedContents = encryptTextContents(contents, key);
    }
  }
}