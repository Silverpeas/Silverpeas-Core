/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.security.encryption.cipher.CryptoException;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Unit tests on the execution of the different DefaultContentEncryptionService's methods in a concurrent
 * way.
 */
class ConcurrentExecutionTest extends ContentEncryptionServiceTest {

  // the AES key in hexadecimal stored in the key file.
  private String key;

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    generateKeyFile();
  }

  private void generateKeyFile() throws Exception {
    Assumptions.assumeTrue(sufficientAvailableProcessors());
    key = generateAESKey();
    createKeyFileWithTheActualKey(key);
  }

  @Test
  void testTheCipherRenewingIsBlocking() {
    ExecutorService executor = Executors.newCachedThreadPool();
    executor.submit(() -> {
      try {
        renewContentCipher();
      } catch (Exception e) {
        fail(e.getMessage());
      }
    });

    executor.submit(() -> {
      try {
        TextContent[] contents = generateTextContents(1);
        Map<String, String> content = contents[0].getProperties();
        getContentEncryptionService().encryptContent(content);
        fail("An error should be thrown");
      } catch (Exception e) {
        assertThat(e instanceof IllegalStateException, is(true));
      }
    });

    executor.submit(() -> {
      EncryptionContentIterator iterator = null;
      try {
        iterator = getEncryptionContentIteratorForDecryption();
      } catch (Exception e) {
        fail(e.getMessage());
      }
      try {
        getContentEncryptionService().encryptContents(iterator);
        fail("An error should be thrown");
      } catch (Exception e) {
        assertThat(e instanceof IllegalStateException, is(true));
      }
    });

    executor.shutdown();
    while (!executor.isTerminated()) {

    }
  }

  @Test
  void testTheCipherRenewingIsBlockedWhenAContentIsEncrypted() {
    ExecutorService executor = Executors.newCachedThreadPool();
    final long[] time = new long[1];
    final Future[] futures = new Future[3];
    futures[0] = executor.submit(() -> {
      try {
        TextContent[] contents = generateTextContents(1);
        Map<String, String> content = contents[0].getProperties();
        getContentEncryptionService().encryptContent(content);
        time[0] = System.currentTimeMillis();
      } catch (Exception e) {
        fail(e.getMessage());
      }
    });

    futures[1] = executor.submit(() -> {
      try {
        TextContent[] contents = generateTextContents(1);
        getContentEncryptionService()
            .encryptContent(contents[0].getText(), contents[0].getDescription(),
                contents[0].getText());
        time[0] = System.currentTimeMillis();
      } catch (Exception e) {
        fail(e.getMessage());
      }
    });

    futures[2] = executor.submit(() -> {
      try {
        EncryptionContentIterator iterator = getEncryptionContentIteratorForEncryption();
        getContentEncryptionService().encryptContents(iterator);
        time[0] = System.currentTimeMillis();
      } catch (Exception e) {
        fail(e.getMessage());
      }
    });

    executor.submit(() -> {
      try {
        for (Future future : futures) {
          assertThat(future.isDone(), is(false));
        }
        renewContentCipher();
        assertThat(System.currentTimeMillis(), greaterThan(time[0]));
        for (Future future : futures) {
          assertThat(future.isDone(), is(true));
        }
      } catch (Exception e) {
        fail(e.getMessage());
      }
    });

    executor.shutdown();
    while (!executor.isTerminated()) {

    }
  }

  @Test
  void testTheCipherRenewingIsBlockedWhenAContentIsDecrypted() {
    ExecutorService executor = Executors.newCachedThreadPool();
    final long[] time = new long[1];
    final Future[] futures = new Future[3];
    futures[0] = executor.submit(() -> {
      try {
        TextContent[] contents = generateTextContents(1);
        TextContent[] encryptedContents = encryptTextContents(contents, key);
        Map<String, String> encryptedContent = encryptedContents[0].getProperties();
        getContentEncryptionService().decryptContent(encryptedContent);
        time[0] = System.currentTimeMillis();
      } catch (Exception e) {
        fail(e.getMessage());
      }
    });

    futures[1] = executor.submit(() -> {
      try {
        TextContent[] contents = generateTextContents(1);
        TextContent[] encryptedContents = encryptTextContents(contents, key);
        getContentEncryptionService()
            .decryptContent(encryptedContents[0].getText(), encryptedContents[0].getDescription(),
                encryptedContents[0].getText());
        time[0] = System.currentTimeMillis();
      } catch (Exception e) {
        fail(e.getMessage());
      }
    });

    futures[2] = executor.submit(() -> {
      try {
        EncryptionContentIterator iterator = getEncryptionContentIteratorForDecryption();
        getContentEncryptionService().decryptContents(iterator);
        time[0] = System.currentTimeMillis();
      } catch (Exception e) {
        fail(e.getMessage());
      }
    });

    executor.submit(() -> {
      try {
        for (Future future : futures) {
          assertThat(future.isDone(), is(false));
        }
        renewContentCipher();
        assertThat(System.currentTimeMillis(), greaterThan(time[0]));
        for (Future future : futures) {
          assertThat(future.isDone(), is(true));
        }
      } catch (Exception e) {
        fail(e.getMessage());
      }
    });

    executor.shutdown();
    while (!executor.isTerminated()) {

    }
  }

  @Test
  void testTheKeyUpdateIsBlocking() {
    ExecutorService executor = Executors.newCachedThreadPool();
    executor.submit(() -> {
      try {
        String key1 = generateAESKey();
        getContentEncryptionService().updateCipherKey(key1);
      } catch (Exception e) {
        fail(e.getMessage());
      }
    });

    executor.submit(() -> {
      try {
        TextContent[] contents = generateTextContents(1);
        Map<String, String> content = contents[0].getProperties();
        getContentEncryptionService().encryptContent(content);
        fail("An error should be thrown");
      } catch (Exception e) {
        assertThat(e instanceof IllegalStateException, is(true));
      }
    });

    executor.submit(() -> {
      EncryptionContentIterator iterator = null;
      try {
        iterator = getEncryptionContentIteratorForDecryption();
      } catch (Exception e) {
        fail(e.getMessage());
      }
      try {
        getContentEncryptionService().encryptContents(iterator);
        fail("An error should be thrown");
      } catch (Exception e) {
        assertThat(e instanceof IllegalStateException, is(true));
      }
    });

    executor.shutdown();
    while (!executor.isTerminated()) {

    }
  }

  @Test
  void testTheKeyUpdateIsBlockedWhenAContentIsEncrypted() {
    ExecutorService executor = Executors.newCachedThreadPool();
    final long[] time = new long[1];
    final Future[] futures = new Future[3];
    futures[0] = executor.submit((Runnable) () -> {
      try {
        TextContent[] contents = generateTextContents(1);
        Map<String, String> content = contents[0].getProperties();
        getContentEncryptionService().encryptContent(content);
        time[0] = System.currentTimeMillis();
      } catch (Exception e) {
        fail(e.getMessage());
      }
    });

    futures[1] = executor.submit((Runnable) () -> {
      try {
        TextContent[] contents = generateTextContents(1);
        getContentEncryptionService()
            .encryptContent(contents[0].getText(), contents[0].getDescription(),
                contents[0].getText());
        time[0] = System.currentTimeMillis();
      } catch (Exception e) {
        fail(e.getMessage());
      }
    });

    futures[2] = executor.submit(() -> {
      try {
        EncryptionContentIterator iterator = getEncryptionContentIteratorForEncryption();
        getContentEncryptionService().encryptContents(iterator);
        time[0] = System.currentTimeMillis();
      } catch (Exception e) {
        fail(e.getMessage());
      }
    });

    executor.submit(() -> {
      try {
        for (Future future : futures) {
          assertThat(future.isDone(), is(false));
        }
        String key1 = generateAESKey();
        getContentEncryptionService().updateCipherKey(key1);
        assertThat(System.currentTimeMillis(), greaterThan(time[0]));
        for (Future future : futures) {
          assertThat(future.isDone(), is(true));
        }
      } catch (Exception e) {
        fail(e.getMessage());
      }
    });

    executor.shutdown();
    while (!executor.isTerminated()) {

    }
  }

  @Test
  void testTheKeyUpdateIsBlockedWhenAContentIsDecrypted() {
    ExecutorService executor = Executors.newCachedThreadPool();
    final long[] time = new long[1];
    final Future[] futures = new Future[3];
    futures[0] = executor.submit(() -> {
      try {
        TextContent[] contents = generateTextContents(1);
        TextContent[] encryptedContents = encryptTextContents(contents, key);
        Map<String, String> encryptedContent = encryptedContents[0].getProperties();
        getContentEncryptionService().decryptContent(encryptedContent);
        time[0] = System.currentTimeMillis();
      } catch (Exception e) {
        fail(e.getMessage());
      }
    });

    futures[1] = executor.submit(() -> {
      try {
        TextContent[] contents = generateTextContents(1);
        TextContent[] encryptedContents = encryptTextContents(contents, key);
        getContentEncryptionService()
            .decryptContent(encryptedContents[0].getText(), encryptedContents[0].getDescription(),
                encryptedContents[0].getText());
        time[0] = System.currentTimeMillis();
      } catch (Exception e) {
        fail(e.getMessage());
      }
    });

    futures[2] = executor.submit(() -> {
      try {
        EncryptionContentIterator iterator = getEncryptionContentIteratorForDecryption();
        getContentEncryptionService().decryptContents(iterator);
        time[0] = System.currentTimeMillis();
      } catch (Exception e) {
        fail(e.getMessage());
      }
    });

    executor.submit(() -> {
      try {
        for (Future future : futures) {
          assertThat(future.isDone(), is(false));
        }
        String key1 = generateAESKey();
        getContentEncryptionService().updateCipherKey(key1);
        assertThat(System.currentTimeMillis(), greaterThan(time[0]));
        for (Future future : futures) {
          assertThat(future.isDone(), is(true));
        }
      } catch (Exception e) {
        fail(e.getMessage());
      }
    });

    executor.shutdown();
    while (!executor.isTerminated()) {

    }
  }

  private void renewContentCipher() throws Exception {
    final String newKey = generateAESKey();
    createKeyFileWithTheDeprecatedKey(this.key);
    createKeyFileWithTheActualKey(newKey);
    EncryptionContentIterator iterator = getEncryptionContentIteratorForCipherRenewing();
    getContentEncryptionService().renewCipherOfContents(iterator);
  }

  private EncryptionContentIterator getEncryptionContentIteratorForEncryption() throws Exception {
    final int count = 10;
    return new EncryptionContentIterator() {

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
      public void update(final Map<String, String> newEncryptedContent) {

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
    };
  }

  private EncryptionContentIterator getEncryptionContentIteratorForDecryption() throws Exception {
    final int count = 10;
    return new EncryptionContentIterator() {

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
      public void update(final Map<String, String> newEncryptedContent) {

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
    };
  }

  private EncryptionContentIterator getEncryptionContentIteratorForCipherRenewing()
      throws Exception {
    return getEncryptionContentIteratorForDecryption();
  }

  private static boolean sufficientAvailableProcessors() {
    return Runtime.getRuntime().availableProcessors() >= 4;
  }
}
