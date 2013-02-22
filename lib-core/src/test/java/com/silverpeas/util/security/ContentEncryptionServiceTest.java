package com.silverpeas.util.security;

import com.silverpeas.util.FileUtil;
import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.util.FileRepositoryManager;
import org.apache.commons.io.FileUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.silverpeas.util.crypto.Cipher;
import org.silverpeas.util.crypto.CipherFactory;
import org.silverpeas.util.crypto.CryptographicAlgorithmName;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.security.Security;

/**
 * The base class of all tests on the services provided by the ContentEncryptionService instances.
 * It allocates all the required resources for the tests and frees them once the tests are done.
 * It creates the security directory to receive the key file, but the key file and it constructs
 * a ContentEncryptionService instance ready to be tested.
 */
public class ContentEncryptionServiceTest {

  private ContentEncryptionService service;

  @BeforeClass
  public static void createSecurityDirectoryAndSetupJCEProviders() throws IOException {
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

  @AfterClass
  public static void deleteSecurityDirectory() throws IOException {
    String securityPath = FileRepositoryManager.getSecurityDirPath();
    File securityDir = new File(securityPath);
    if (securityDir.exists()) {
      File keyFile = new File(ContentEncryptionService.ACTUAL_KEY_FILE_PATH);
      if (keyFile.exists()) {
        keyFile.setWritable(true);
        FileUtils.forceDelete(keyFile);
      }
      FileUtils.forceDelete(securityDir);
    }
  }

  @Before
  public void setUpContentEncryptionService() throws Exception {
    service = new ContentEncryptionService();
  }

  @After
  public void deleteKeyFile() throws Exception {
    File keyFile = new File(ContentEncryptionService.ACTUAL_KEY_FILE_PATH);
    if (keyFile.exists()) {
      keyFile.setWritable(true);
      FileUtils.forceDelete(keyFile);
    }
  }

  public ContentEncryptionService getContentEncryptionService() {
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
    return StringUtil.asHex(skey.getEncoded());
  }

  /**
   * Creates a key file with the specified key in hexadecimal.
   * @param key the key used in a content encryption and to store in the key file.
   * @throws Exception if the key file creation failed.
   */
  public static void createKeyFileWithTheKey(String key) throws Exception {
    File keyFile = new File(ContentEncryptionService.ACTUAL_KEY_FILE_PATH);
    if (keyFile.exists()) {
      keyFile.setWritable(true);
    }
    String encryptedKey = encryptKey(key);
    FileUtil.writeFile(keyFile, new StringReader(encryptedKey));
    keyFile.setReadOnly();
  }

  private static String encryptKey(String key) throws Exception {
    CipherFactory cipherFactory = CipherFactory.getFactory();
    Cipher cast5 = cipherFactory.getCipher(CryptographicAlgorithmName.CAST5);
    byte[] encryptedKey = cast5.encrypt(key, ContentEncryptionService.CIPHER_KEY);
    return StringUtil.asBase64(encryptedKey);
  }
}
