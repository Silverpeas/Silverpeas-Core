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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Unit tests on the encryption of contents done by the ContentEncryptionService instances.
 */
public class ContentEncryptionTest extends ContentEncryptionServiceTest {

  // the AES key in hexadecimal stored in the key file.
  private String key;

  private static final String CONTENT_TITLE = "title";
  private static final String CONTENT_DESCRIPTION = "description";
  private static final String CONTENT_TEXT = "text";

  @Before
  public void generateKeyFile() throws Exception {
    key = generateAESKey();
    createKeyFileWithTheKey(key);
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

    Map<String, String> content = new HashMap<String, String>();
    content.put(CONTENT_TITLE, contents[0].getTitle());
    content.put(CONTENT_DESCRIPTION, contents[0].getDescription());
    content.put(CONTENT_TEXT, contents[0].getText());
    Map<String, String> encryptedContent = getContentEncryptionService().encryptContent(content);

    assertThat(encryptedContent.size(), is(content.size()));
    assertContentIsCorrectlyEncrypted(contents[0], encryptedContent);
  }

  @Test
  public void encryptSeveralContentsInBatch() throws Exception {
    final int count = 10;
    getContentEncryptionService().encryptContents(new ContentProvider() {

      TextContent[] contents = generateTextContents(count);
      int current = -1;

      @Override
      public Map<String, String> getContent() {
        Map<String, String> content = new HashMap<String, String>();
        content.put(CONTENT_TITLE, contents[current].getTitle());
        content.put(CONTENT_DESCRIPTION, contents[current].getDescription());
        content.put(CONTENT_TEXT, contents[current].getText());
        return content;
      }

      @Override
      public boolean hasNextContent() {
        return ++current < contents.length;
      }

      @Override
      public void setUpdatedContent(final Map<String, String> updatedContent) {
        assertContentIsCorrectlyEncrypted(contents[current], updatedContent);
      }

      @Override
      public void onError(Map<String, String> content, final CryptoException ex) {
        fail(ex.getMessage());
      }
    });
  }

  @Test
  public void encryptSeveralContentsFromSeveralProvidersInBatch() throws Exception {
    final int count = 100;
    ContentProvider[] providers = new ContentProvider[count];
    for (int i = 0; i < count; i++) {
      providers[i] = new ContentProvider() {

        TextContent[] contents = generateTextContents(count);
        int current = -1;

        @Override
        public Map<String, String> getContent() {
          Map<String, String> content = new HashMap<String, String>();
          content.put(CONTENT_TITLE, contents[current].getTitle());
          content.put(CONTENT_DESCRIPTION, contents[current].getDescription());
          content.put(CONTENT_TEXT, contents[current].getText());
          return content;
        }

        @Override
        public boolean hasNextContent() {
          return ++current < contents.length;
        }

        @Override
        public void setUpdatedContent(final Map<String, String> updatedContent) {
          assertContentIsCorrectlyEncrypted(contents[current], updatedContent);
        }

        @Override
        public void onError(Map<String, String> content, final CryptoException ex) {
          fail(ex.getMessage());
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
    assertContentIsCorrectlyDecrypted(contents[0], contentFields);
  }

  @Test
  public void decryptAContent() throws Exception {
    TextContent[] contents = generateTextContents(1);
    TextContent[] encryptedContents = encryptTextContents(contents);

    Map<String, String> encryptedContent = new HashMap<String, String>();
    encryptedContent.put(CONTENT_TITLE, encryptedContents[0].getTitle());
    encryptedContent.put(CONTENT_DESCRIPTION, encryptedContents[0].getDescription());
    encryptedContent.put(CONTENT_TEXT, encryptedContents[0].getText());
    Map<String, String> content = getContentEncryptionService().decryptContent(encryptedContent);

    assertThat(content.size(), is(encryptedContent.size()));
    assertContentIsCorrectlyDecrypted(contents[0], content);
  }

  @Test
  public void decryptSeveralContentsInBatch() throws Exception {
    final int count = 100;
    getContentEncryptionService().decryptContents(new ContentProvider() {

      TextContent[] contents = generateTextContents(count);
      TextContent[] encryptedContents = encryptTextContents(contents);
      int current = -1;

      @Override
      public Map<String, String> getContent() {
        Map<String, String> encryptedContent = new HashMap<String, String>();
        encryptedContent.put(CONTENT_TITLE, encryptedContents[current].getTitle());
        encryptedContent.put(CONTENT_DESCRIPTION, encryptedContents[current].getDescription());
        encryptedContent.put(CONTENT_TEXT, encryptedContents[current].getText());
        return encryptedContent;
      }

      @Override
      public boolean hasNextContent() {
        return ++current < encryptedContents.length;
      }

      @Override
      public void setUpdatedContent(final Map<String, String> updatedContent) {
        assertContentIsCorrectlyDecrypted(contents[current], updatedContent);
      }

      @Override
      public void onError(Map<String, String> content, final CryptoException ex) {
        fail(ex.getMessage());
      }
    });
  }

  @Test
  public void decryptSeveralContentsFromSeveralProvidersInBatch() throws Exception {
    final int count = 10;
    ContentProvider[] providers = new ContentProvider[count];
    for (int i = 0; i < count; i++) {
      providers[i] = new ContentProvider() {

        TextContent[] contents = generateTextContents(count);
        TextContent[] encryptedContents = encryptTextContents(contents);
        int current = -1;

        @Override
        public Map<String, String> getContent() {
          Map<String, String> encryptedContent = new HashMap<String, String>();
          encryptedContent.put(CONTENT_TITLE, encryptedContents[current].getTitle());
          encryptedContent.put(CONTENT_DESCRIPTION, encryptedContents[current].getDescription());
          encryptedContent.put(CONTENT_TEXT, encryptedContents[current].getText());
          return encryptedContent;
        }

        @Override
        public boolean hasNextContent() {
          return ++current < encryptedContents.length;
        }

        @Override
        public void setUpdatedContent(final Map<String, String> updatedContent) {
          assertContentIsCorrectlyDecrypted(contents[current], updatedContent);
        }

        @Override
        public void onError(Map<String, String> content, final CryptoException ex) {
          fail(ex.getMessage());
        }
      };
    }
    getContentEncryptionService().decryptContents(providers);
  }

  private  static TextContent[] generateTextContents(int count) {
    TextContent[] contents = new TextContent[count];
    Random random = new Random();
    for(int i = 0; i < count; i++) {
      TextContent aContent = new TextContent(String.valueOf(i), "", new UserDetail());
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
      content.setDescription(StringUtil.asBase64(aes.encrypt(contents[i].getDescription(), cipherKey)));
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
    assertThat(content.getTitle(), is(title));
    assertThat(content.getDescription(), is(description));
    assertThat(content.getText(), is(text));
  }

  private void assertContentIsCorrectlyEncrypted(TextContent content,
        Map<String, String> encryptedContent) {
      CipherFactory cipherFactory = CipherFactory.getFactory();
      Cipher aes = cipherFactory.getCipher(CryptographicAlgorithmName.AES);
      String title, description, text;
      try {
        CipherKey cipherKey = CipherKey.aKeyFromHexText(this.key);
        title = aes.decrypt(
            StringUtil.fromBase64(encryptedContent.get(CONTENT_TITLE)), cipherKey);
        description = aes.decrypt(
            StringUtil.fromBase64(encryptedContent.get(CONTENT_DESCRIPTION)), cipherKey);
        text = aes.decrypt(
            StringUtil.fromBase64(encryptedContent.get(CONTENT_TEXT)), cipherKey);
      } catch (ParseException e) {
        throw new AssertionError(e);
      } catch (CryptoException e) {
        throw new AssertionError(e);
      }
      assertThat(content.getTitle(), is(title));
      assertThat(content.getDescription(), is(description));
      assertThat(content.getText(), is(text));
  }

  private void assertContentIsCorrectlyDecrypted(TextContent expectedContent,
      String ... actualContent) {
    assertThat(actualContent[0], is(expectedContent.getTitle()));
    assertThat(actualContent[1], is(expectedContent.getDescription()));
    assertThat(actualContent[2], is(expectedContent.getText()));
  }

  private void assertContentIsCorrectlyDecrypted(TextContent expectedContent,
      Map<String, String> actualContent) {
    assertThat(actualContent.get(CONTENT_TITLE), is(expectedContent.getTitle()));
    assertThat(actualContent.get(CONTENT_DESCRIPTION), is(expectedContent.getDescription()));
    assertThat(actualContent.get(CONTENT_TEXT), is(expectedContent.getText()));
  }
}
