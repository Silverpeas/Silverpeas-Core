/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.security.encryption;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.security.encryption.cipher.Cipher;
import org.silverpeas.core.security.encryption.cipher.CipherFactory;
import org.silverpeas.core.security.encryption.cipher.CipherKey;
import org.silverpeas.core.security.encryption.cipher.CryptographicAlgorithmName;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import org.silverpeas.core.util.EncodingUtil;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.util.file.FileRepositoryManager;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.Security;
import java.text.ParseException;
import java.util.Random;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * The base class of all tests on the services provided by the DefaultContentEncryptionService instances.
 * It allocates all the required resources for the tests and frees them once the tests are done.
 * It creates the security directory to receive the key file, but the key file and it constructs
 * a DefaultContentEncryptionService instance ready to be tested.
 */
@EnableSilverTestEnv(context = JEETestContext.class)
public abstract class ContentEncryptionServiceTest {

  String ACTUAL_KEY_FILE_PATH;
  String DEPRECATED_KEY_FILE_PATH;
  private CipherKey CIPHER_KEY;

  private DefaultContentEncryptionService service;

  @BeforeEach
  public void setUp() throws Exception {
    createSecurityDirectoryAndSetupJCEProviders();
  }

  @AfterEach
  public void tearDown() throws Exception {
    deleteSecurityDirectory();
  }

  private void createSecurityDirectoryAndSetupJCEProviders() throws IOException {
    ACTUAL_KEY_FILE_PATH =
        FileRepositoryManager.getSecurityDirPath() + ".aid_key";
    DEPRECATED_KEY_FILE_PATH =
        FileRepositoryManager.getSecurityDirPath() + ".did_key";
    try {
      CIPHER_KEY = CipherKey.aKeyFromHexText("06277d1ce530c94bd9a13a72a58342be");
    } catch (ParseException e) {
      throw new RuntimeException("Cannot create the cryptographic key!", e);
    }

    String securityPath = FileRepositoryManager.getSecurityDirPath();
    File securityDir = new File(securityPath);
    if (!securityDir.exists()) {
      FileUtils.forceMkdir(securityDir);
    }
    securityDir.setWritable(true);
    securityDir.setExecutable(true);
    securityDir.setReadable(true);
    if (System.getProperty("os.name").toLowerCase().contains("windows")) {
      Runtime.getRuntime().exec("attrib +H " + securityPath);
    }

    Security.addProvider(new BouncyCastleProvider());
  }

  private void deleteSecurityDirectory() throws IOException {
    String securityPath = FileRepositoryManager.getSecurityDirPath();
    File securityDir = new File(securityPath);
    if (securityDir.exists()) {
      File keyFile = new File(ACTUAL_KEY_FILE_PATH);
      if (keyFile.exists()) {
        keyFile.setWritable(true);
        FileUtils.forceDelete(keyFile);
      }
      keyFile = new File(DEPRECATED_KEY_FILE_PATH);
      if (keyFile.exists()) {
        keyFile.setWritable(true);
        FileUtils.forceDelete(keyFile);
      }
      FileUtils.forceDelete(securityDir);
    }
  }

  @BeforeEach
  public void setUpContentEncryptionService() throws Exception {
    service = new DefaultContentEncryptionService();
  }

  @AfterEach
  public void deleteKeyFile() throws Exception {
    File keyFile = new File(ACTUAL_KEY_FILE_PATH);
    if (keyFile.exists()) {
      keyFile.setWritable(true);
      FileUtils.forceDelete(keyFile);
    }
    keyFile = new File(DEPRECATED_KEY_FILE_PATH);
    if (keyFile.exists()) {
      keyFile.setWritable(true);
      FileUtils.forceDelete(keyFile);
    }
  }

  public DefaultContentEncryptionService getContentEncryptionService() {
    return service;
  }

  /**
   * Generates a key for an AES enciphering.
   * @return the key in hexadecimal.
   * @throws Exception if the key cannot be generated.
   */
  public static String generateAESKey() throws Exception {
    KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
    keyGenerator.init(256);
    SecretKey skey = keyGenerator.generateKey();
    return EncodingUtil.asHex(skey.getEncoded());
  }

  /**
   * Creates the key file with the specified actual key in hexadecimal.
   * @param key the key used in a content encryption and to store in the key file.
   * @throws Exception if the key file creation failed.
   */
  public void createKeyFileWithTheActualKey(String key) throws Exception {
    File keyFile = new File(ACTUAL_KEY_FILE_PATH);
    if (keyFile.exists()) {
      keyFile.setWritable(true);
    } else {
      System.out.println("WARNIIIINNNNNG: the key file " + ACTUAL_KEY_FILE_PATH + " DOESN'T EXIST!");
    }
    String encryptedKey = encryptKey(key);
    String encryptedContent = StringUtil.asBase64(CIPHER_KEY.getRawKey()) + " " + encryptedKey;
    Files.copy(new ByteArrayInputStream(encryptedContent.getBytes()), keyFile.toPath(),
        REPLACE_EXISTING);
    keyFile.setReadOnly();
  }

  /**
   * Creates the key file with the specified deprecated key in hexadecimal.
   * @param key the key used in a content encryption and to store in the old key file.
   * @throws Exception if the key file creation failed.
   */
  public void createKeyFileWithTheDeprecatedKey(String key) throws Exception {
    File keyFile = new File(DEPRECATED_KEY_FILE_PATH);
    if (keyFile.exists()) {
      keyFile.setWritable(true);
    }
    String encryptedKey = encryptKey(key);
    String encryptedContent = StringUtil.asBase64(CIPHER_KEY.getRawKey()) + " " + encryptedKey;
    Files.copy(new ByteArrayInputStream(encryptedContent.getBytes()), keyFile.toPath(),
        REPLACE_EXISTING);
    keyFile.setReadOnly();
  }

  /**
   * Generates the specified count of text contents for testing purpose.
   * @param count the number of text contents to generate.
   * @return an array with the generated text contents.
   */
  public static TextContent[] generateTextContents(int count) {
    TextContent[] contents = new TextContent[count];
    UserDetail creator = new UserDetail();
    creator.setFirstName("Bart");
    creator.setLastName("Simpson");
    Random random = new Random();
    for (int i = 0; i < count; i++) {
      TextContent aContent = new TextContent(String.valueOf(i), "", creator);
      aContent.setTitle(RandomStringUtils.randomAscii(random.nextInt(32)));
      aContent.setDescription(RandomStringUtils.randomAscii(random.nextInt(128)));
      aContent.setText(RandomStringUtils.randomAscii(random.nextInt(1024)));
      contents[i] = aContent;
    }
    return contents;
  }

  /**
   * Encrypts the specified text contents by using the specified cipher key in hexadecimal.
   * @param contents the contents to encrypt.
   * @param key the cipher key to use in hexadecimal.
   * @return an array with the encrypted text contents.
   * @throws Exception if an error occurs while encrypting the contents.
   */
  public static TextContent[] encryptTextContents(TextContent[] contents, String key)
      throws Exception {
    CipherFactory cipherFactory = CipherFactory.getFactory();
    Cipher aes = cipherFactory.getCipher(CryptographicAlgorithmName.AES);
    CipherKey cipherKey = CipherKey.aKeyFromHexText(key);
    TextContent[] encryptedContents = new TextContent[contents.length];
    for (int i = 0; i < contents.length; i++) {
      TextContent content =
          new TextContent(contents[i].getId(), contents[i].getComponentInstanceId(),
              contents[i].getCreator());
      content.setTitle(StringUtil.asBase64(aes.encrypt(contents[i].getTitle(), cipherKey)));
      content.setDescription(
          StringUtil.asBase64(aes.encrypt(contents[i].getDescription(), cipherKey)));
      content.setText(StringUtil.asBase64(aes.encrypt(contents[i].getText(), cipherKey)));
      encryptedContents[i] = content;
    }
    return encryptedContents;
  }

  private String encryptKey(String key) throws Exception {
    CipherFactory cipherFactory = CipherFactory.getFactory();
    Cipher cast5 = cipherFactory.getCipher(CryptographicAlgorithmName.CAST5);
    byte[] encryptedKey = cast5.encrypt(key, CIPHER_KEY);
    return StringUtil.asBase64(encryptedKey);
  }
}
