package org.silverpeas.core.security.encryption;

import org.silverpeas.core.security.encryption.cipher.Cipher;
import org.silverpeas.core.security.encryption.cipher.CipherFactory;
import org.silverpeas.core.security.encryption.cipher.CipherKey;
import org.silverpeas.core.security.encryption.cipher.CryptoException;
import org.silverpeas.core.security.encryption.cipher.CryptographicAlgorithmName;
import org.silverpeas.core.util.StringUtil;
import org.junit.Test;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Unit tests on the encryption of contents done by the DefaultContentEncryptionService instances.
 */
public class TestContentEncryption extends ContentEncryptionServiceTest {

  // the AES key in hexadecimal stored in the key file.
  private String key;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    generateKeyFile();
  }

  private void generateKeyFile() throws Exception {
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

      @Override
      public void init() {
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

        @Override
        public void init() {
        }
      };
    }

    getContentEncryptionService().encryptContents(providers);
  }

  @Test
  public void decryptInlineAContent() throws Exception {
    TextContent[] contents = generateTextContents(1);
    TextContent[] encryptedContents = encryptTextContents(contents, this.key);

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
    TextContent[] encryptedContents = encryptTextContents(contents, this.key);

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
      TextContent[] encryptedContents = encryptTextContents(contents, key);
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

      @Override
      public void init() {

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
        TextContent[] encryptedContents = encryptTextContents(contents, key);
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

        @Override
        public void init() {

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
      TextContent[] encryptedContents = encryptTextContents(expectedContents, key);
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

      @Override
      public void init() {

      }
    });
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
