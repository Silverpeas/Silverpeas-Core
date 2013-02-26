package com.silverpeas.util.security;

import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.silverpeas.util.crypto.Cipher;
import org.silverpeas.util.crypto.CipherFactory;
import org.silverpeas.util.crypto.CipherKey;
import org.silverpeas.util.crypto.CryptoException;
import org.silverpeas.util.crypto.CryptographicAlgorithmName;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Unit tests on the encryption of contents done by the ContentEncryptionService instances.
 */
public class ContentEncryptionTest extends ContentEncryptionServiceTest {

  // the AES key in hexadecimal stored in the key file.
  private String key;

  @Before
  public void generateKeyFile() throws Exception {
    key = generateAESKey();
    createKeyFileWithTheActualKey(key);
  }

  @Test
  public void encryptInlineAContent() throws Exception {
    TextContent[] contents = generateTextContents(1);

    String[] encryptedContentFields = getContentEncryptionService().encryptContent(
        contents[0].getTitle(),
        contents[0].getDescription(),
        contents[0].getText());

    assertThat(encryptedContentFields.length, is(3));
    assertContentIsCorrectlyEncrypted(contents[0], encryptedContentFields);
  }

  @Test
  public void encryptAContent() throws Exception {
    TextContent[] contents = generateTextContents(1);

    Map<String, String> content = contents[0].getProperties();
    Map<String, String> encryptedContent = getContentEncryptionService().encryptContent(content);

    assertThat(encryptedContent.size(), is(content.size()));
    assertContentIsCorrectlyEncrypted(contents[0], encryptedContent);
  }

  @Test
  public void encryptSeveralContentsInBatch() throws Exception {
    final int count = 10;
    getContentEncryptionService().encryptContents(new EncryptionContentIterator() {

      TextContent[] contents = generateTextContents(count);
      int current = -1;

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
        assertContentIsCorrectlyEncrypted(contents[current], updatedContent);
      }

      @Override
      public void onError(Map<String, String> content, final CryptoException ex) {
        fail(ex.getMessage());
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    });
  }

  @Test
  public void encryptSeveralContentsFromSeveralProvidersInBatch() throws Exception {
    final int count = 10;
    EncryptionContentIterator[] providers = new EncryptionContentIterator[count];
    for (int i = 0; i < count; i++) {
      providers[i] = new EncryptionContentIterator() {

        TextContent[] contents = generateTextContents(count);
        int current = -1;

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
          assertContentIsCorrectlyEncrypted(contents[current], updatedContent);
        }

        @Override
        public void onError(Map<String, String> content, final CryptoException ex) {
          fail(ex.getMessage());
        }

        @Override
        public void remove() {
          throw new UnsupportedOperationException();
        }
      };
    }

    getContentEncryptionService().encryptContents(providers);
  }

  @Test
  public void decryptInlineAContent() throws Exception {
    TextContent[] contents = generateTextContents(1);
    TextContent[] encryptedContents = encryptTextContents(contents);

    String[] contentFields = getContentEncryptionService().decryptContent(
        encryptedContents[0].getTitle(),
        encryptedContents[0].getDescription(),
        encryptedContents[0].getText());

    assertThat(encryptedContents.length, is(contents.length));
    assertContentIsCorrect(contents[0], contentFields);
  }

  @Test
  public void decryptAContent() throws Exception {
    TextContent[] contents = generateTextContents(1);
    TextContent[] encryptedContents = encryptTextContents(contents);

    Map<String, String> encryptedContent = encryptedContents[0].getProperties();
    Map<String, String> content = getContentEncryptionService().decryptContent(encryptedContent);

    assertThat(content.size(), is(encryptedContent.size()));
    assertThat(content, is(contents[0].getProperties()));
  }

  @Test
  public void decryptSeveralContentsInBatch() throws Exception {
    final int count = 10;
    getContentEncryptionService().decryptContents(new EncryptionContentIterator() {

      TextContent[] contents = generateTextContents(count);
      TextContent[] encryptedContents = encryptTextContents(contents);
      int current = -1;

      @Override
      public Map<String, String> next() {
        return encryptedContents[current].getProperties();
      }

      @Override
      public boolean hasNext() {
        return ++current < encryptedContents.length;
      }

      @Override
      public void update(final Map<String, String> updatedContent) {
        assertThat(updatedContent, is(contents[current].getProperties()));
      }

      @Override
      public void onError(Map<String, String> content, final CryptoException ex) {
        fail(ex.getMessage());
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    });
  }

  @Test
  public void decryptSeveralContentsFromSeveralProvidersInBatch() throws Exception {
    final int count = 10;
    EncryptionContentIterator[] providers = new EncryptionContentIterator[count];
    for (int i = 0; i < count; i++) {
      providers[i] = new EncryptionContentIterator() {

        TextContent[] contents = generateTextContents(count);
        TextContent[] encryptedContents = encryptTextContents(contents);
        int current = -1;

        @Override
        public Map<String, String> next() {
          return encryptedContents[current].getProperties();
        }

        @Override
        public boolean hasNext() {
          return ++current < encryptedContents.length;
        }

        @Override
        public void update(final Map<String, String> updatedContent) {
          assertThat(updatedContent, is(contents[current].getProperties()));
        }

        @Override
        public void onError(Map<String, String> content, final CryptoException ex) {
          fail(ex.getMessage());
        }

        @Override
        public void remove() {
          throw new UnsupportedOperationException();
        }
      };
    }
    getContentEncryptionService().decryptContents(providers);
  }

  @Test
  public void renewContentCipher() throws Exception {
    final int count = 10;
    final String newKey = generateAESKey();
    createKeyFileWithTheDeprecatedKey(this.key);
    createKeyFileWithTheActualKey(newKey);

    getContentEncryptionService().renewCipherOfContents(new EncryptionContentIterator() {

      TextContent[] expectedContents = generateTextContents(count);
      TextContent[] encryptedContents = encryptTextContents(expectedContents);
      int current = -1;

      @Override
      public Map<String, String> next() {
        return encryptedContents[current].getProperties();
      }

      @Override
      public boolean hasNext() {
        return ++current < encryptedContents.length;
      }

      @Override
      public void update(final Map<String, String> newEncryptedContent) {
        assertContentIsCorrectlyEncrypted(expectedContents[current], newEncryptedContent, newKey);
      }

      @Override
      public void onError(final Map<String, String> content, final CryptoException ex) {
        fail(ex.getMessage());
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    });
  }

  @Test
  public void testTheCipherRenewingIsBlocking() {
    ExecutorService executor = Executors.newCachedThreadPool();
    executor.submit(new Runnable() {
      @Override
      public void run() {
        try {
          renewContentCipher();
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
    while(!executor.isTerminated()) {

    }
  }

  @Test
  public void testTheCipherRenewingIsBlockedWhenAContentIsEncrypted() {
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
          renewContentCipher();
          assertThat(System.currentTimeMillis(), greaterThan(time[0]));
          assertThat(future.isDone(), is(true));
        } catch (Exception e) {
          fail(e.getMessage());
        }
      }
    });

    executor.shutdown();
    while(!executor.isTerminated()) {

    }
  }

  private  static TextContent[] generateTextContents(int count) {
    TextContent[] contents = new TextContent[count];
    UserDetail creator = new UserDetail();
    creator.setFirstName("Bart");
    creator.setLastName("Simpson");
    Random random = new Random();
    for(int i = 0; i < count; i++) {
      TextContent aContent = new TextContent(String.valueOf(i), "", creator);
      aContent.setTitle(RandomStringUtils.randomAscii(random.nextInt(32)));
      aContent.setDescription(RandomStringUtils.randomAscii(random.nextInt(128)));
      aContent.setText(RandomStringUtils.randomAscii(random.nextInt(1024)));
      contents[i] = aContent;
    }
    return contents;
  }

  private TextContent[] encryptTextContents(TextContent[] contents) throws Exception {
    CipherFactory cipherFactory = CipherFactory.getFactory();
    Cipher aes = cipherFactory.getCipher(CryptographicAlgorithmName.AES);
    CipherKey cipherKey = CipherKey.aKeyFromHexText(this.key);
    TextContent[] encryptedContents = new TextContent[contents.length];
    for (int i = 0; i < contents.length; i++) {
      TextContent content = new TextContent(contents[i].getId(),
          contents[i].getComponentInstanceId(), contents[i].getCreator());
      content.setTitle(StringUtil.asBase64(aes.encrypt(contents[i].getTitle(), cipherKey)));
      content.setDescription(
          StringUtil.asBase64(aes.encrypt(contents[i].getDescription(), cipherKey)));
      content.setText(StringUtil.asBase64(aes.encrypt(contents[i].getText(), cipherKey)));
      encryptedContents[i] = content;
    }
    return encryptedContents;
  }

  private void assertContentIsCorrectlyEncrypted(TextContent content,
      String ... encryptedContent) {
    CipherFactory cipherFactory = CipherFactory.getFactory();
    Cipher aes = cipherFactory.getCipher(CryptographicAlgorithmName.AES);
    String title, description, text;
    try {
      CipherKey cipherKey = CipherKey.aKeyFromHexText(this.key);
      title = aes.decrypt(StringUtil.fromBase64(encryptedContent[0]), cipherKey);
      description = aes.decrypt(StringUtil.fromBase64(encryptedContent[1]), cipherKey);
      text = aes.decrypt(StringUtil.fromBase64(encryptedContent[2]), cipherKey);
    } catch (ParseException e) {
      throw new AssertionError(e);
    } catch (CryptoException e) {
      throw new AssertionError(e);
    }

    assertContentIsCorrect(content, title, description, text);
  }

  private void assertContentIsCorrectlyEncrypted(TextContent content,
        Map<String, String> encryptedContent, String key) {
    CipherFactory cipherFactory = CipherFactory.getFactory();
    Cipher aes = cipherFactory.getCipher(CryptographicAlgorithmName.AES);
    Map<String, String> actualProperties = new HashMap<String, String>();
    try {
      CipherKey cipherKey = CipherKey.aKeyFromHexText(key);
      for(Map.Entry<String, String> aProperty: encryptedContent.entrySet()) {
        String value = aes.decrypt(StringUtil.fromBase64(aProperty.getValue()), cipherKey);
        actualProperties.put(aProperty.getKey(), value);
      }
    } catch (ParseException e) {
      throw new AssertionError(e);
    } catch (CryptoException e) {
      throw new AssertionError(e);
    }

    assertThat(actualProperties, is(content.getProperties()));
  }

  private void assertContentIsCorrectlyEncrypted(TextContent content,
      Map<String, String> encryptedContent) {
    assertContentIsCorrectlyEncrypted(content, encryptedContent, this.key);
  }

  private static void assertContentIsCorrect(TextContent content, String ... fields) {
    assertThat(fields[0], is(content.getTitle()));
    assertThat(fields[1], is(content.getDescription()));
    assertThat(fields[2], is(content.getText()));
  }
}
