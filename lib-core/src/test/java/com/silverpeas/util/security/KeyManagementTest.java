package com.silverpeas.util.security;

import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.silverpeas.util.crypto.Cipher;
import org.silverpeas.util.crypto.CipherFactory;
import org.silverpeas.util.crypto.CipherKey;
import org.silverpeas.util.crypto.CryptoException;
import org.silverpeas.util.crypto.CryptographicAlgorithmName;

import java.io.File;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Unit tests on encryption management done by the ContentEncryptionService instances.
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
    final int count = 10;
    MyOwnContentIterator[] iterators = getEncryptionContentIterators(count);
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
  public void testTheKeyUpdateIsBlocking() {
    ExecutorService executor = Executors.newCachedThreadPool();
    executor.submit(new Runnable() {
      @Override
      public void run() {
        try {
          String key = generateAESKey();
          getContentEncryptionService().updateCipherKey(key);
        } catch (Exception e) {
          fail(e.getMessage());
        }
      }
    });

    executor.submit(new Runnable() {
      @Override
      public void run() {
        try {
          TextContent[] contents = generateTextContents(1);
          Map<String, String> content = contents[0].getProperties();
          getContentEncryptionService().encryptContent(content);
          fail("An error should be thrown");
        } catch (Exception e) {
          assertThat(e instanceof IllegalStateException, is(true));
        }
      }
    });

    executor.shutdown();
    while (!executor.isTerminated()) {

    }
  }

  @Test
  public void testTheKeyUpdateIsBlockedWhenAContentIsEncrypted() {
    ExecutorService executor = Executors.newCachedThreadPool();
    final long[] time = new long[1];
    final Future future = executor.submit(new Runnable() {
      @Override
      public void run() {
        try {
          TextContent[] contents = generateTextContents(1);
          Map<String, String> content = contents[0].getProperties();
          getContentEncryptionService().encryptContent(content);
          time[0] = System.currentTimeMillis();
        } catch (Exception e) {
          fail(e.getMessage());
        }
      }
    });

    executor.submit(new Runnable() {
      @Override
      public void run() {
        try {
          assertThat(future.isDone(), is(false));
          String key = generateAESKey();
          getContentEncryptionService().updateCipherKey(key);
          assertThat(System.currentTimeMillis(), greaterThan(time[0]));
          assertThat(future.isDone(), is(true));
        } catch (Exception e) {
          fail(e.getMessage());
        }
      }
    });

    executor.shutdown();
    while (!executor.isTerminated()) {

    }
  }

  private static MyOwnContentIterator[] getEncryptionContentIterators(final int count) {
    MyOwnContentIterator[] iterators = new MyOwnContentIterator[count];
    for (int i = 0; i < count; i++) {
      iterators[i] = new MyOwnContentIterator(count);
    }
    return iterators;
  }

  private static TextContent[] generateTextContents(int count) {
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

  private static void assertKeyFileExistsWithKey(File keyFile, String expectedKey)
      throws Exception {
    assertThat(keyFile.exists(), is(true));
    assertThat(keyFile.canWrite(), is(false));
    String encryptedKey = FileUtils.readFileToString(keyFile);
    assertThat(StringUtil.isDefined(encryptedKey), is(true));
    CipherFactory cipherFactory = CipherFactory.getFactory();
    Cipher cast125 = cipherFactory.getCipher(CryptographicAlgorithmName.CAST5);
    String actualKey = cast125.decrypt(StringUtil.fromBase64(encryptedKey), CIPHER_KEY);
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
        String title = aes.decrypt(StringUtil.fromBase64(encryptedContents[i].getTitle()), cipherKey);
        String description = aes.decrypt(StringUtil.fromBase64(encryptedContents[i].getDescription()), cipherKey);
        String text = aes.decrypt(StringUtil.fromBase64(encryptedContents[i].getText()), cipherKey);

        assertThat(title, is(contents[i].getTitle()));
        assertThat(description, is(contents[i].getDescription()));
        assertThat(text, is(contents[i].getText()));
      }
    }
  }

  private static class MyOwnContentIterator implements EncryptionContentIterator {
    private int count;
    private final TextContent[] contents = generateTextContents(count);
    private final TextContent[] encryptedContents = new TextContent[contents.length];
    private int current = -1;

    public MyOwnContentIterator(int contentCount) {
      this.count = contentCount;
    }

    public TextContent[] getContents() {
      return this.contents;
    }

    public TextContent[] getEncryptedContents() {
      return encryptedContents;
    }

    @Override
    public Map<String, String> next() {
      return contents[current].getProperties();
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
      fail(ex.getMessage());
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}
