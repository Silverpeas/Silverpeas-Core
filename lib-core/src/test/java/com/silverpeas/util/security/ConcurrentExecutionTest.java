package com.silverpeas.util.security;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.silverpeas.util.crypto.CryptoException;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Unit tests on the execution of the different DefaultContentEncryptionService's methods in a concurrent
 * way.
 */
public class ConcurrentExecutionTest extends ContentEncryptionServiceTest {

  // the AES key in hexadecimal stored in the key file.
  private String key;

  @Before
  public void generateKeyFile() throws Exception {
    Assume.assumeTrue(sufficientAvailableProcessors());
    key = generateAESKey();
    createKeyFileWithTheActualKey(key);
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

    executor.submit(new Runnable() {
      @Override
      public void run() {
        try {
          EncryptionContentIterator iterator = getEncryptionContentIteratorForDecryption();
          getContentEncryptionService().encryptContents(iterator);
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
  public void testTheCipherRenewingIsBlockedWhenAContentIsEncrypted() {
    ExecutorService executor = Executors.newCachedThreadPool();
    final long[] time = new long[1];
    final Future[] futures = new Future[3];
    futures[0] = executor.submit(new Runnable() {
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

    futures[1] = executor.submit(new Runnable() {
      @Override
      public void run() {
        try {
          TextContent[] contents = generateTextContents(1);
          getContentEncryptionService()
              .encryptContent(contents[0].getText(), contents[0].getDescription(),
                  contents[0].getText());
          time[0] = System.currentTimeMillis();
        } catch (Exception e) {
          fail(e.getMessage());
        }
      }
    });

    futures[2] = executor.submit(new Runnable() {
      @Override
      public void run() {
        try {
          EncryptionContentIterator iterator = getEncryptionContentIteratorForEncryption();
          getContentEncryptionService().encryptContents(iterator);
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
      }
    });

    executor.shutdown();
    while (!executor.isTerminated()) {

    }
  }

  @Test
  public void testTheCipherRenewingIsBlockedWhenAContentIsDecrypted() {
    ExecutorService executor = Executors.newCachedThreadPool();
    final long[] time = new long[1];
    final Future[] futures = new Future[3];
    futures[0] = executor.submit(new Runnable() {
      @Override
      public void run() {
        try {
          TextContent[] contents = generateTextContents(1);
          TextContent[] encryptedContents = encryptTextContents(contents, key);
          Map<String, String> encryptedContent = encryptedContents[0].getProperties();
          getContentEncryptionService().decryptContent(encryptedContent);
          time[0] = System.currentTimeMillis();
        } catch (Exception e) {
          fail(e.getMessage());
        }
      }
    });

    futures[1] = executor.submit(new Runnable() {
      @Override
      public void run() {
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
      }
    });

    futures[2] = executor.submit(new Runnable() {
      @Override
      public void run() {
        try {
          EncryptionContentIterator iterator = getEncryptionContentIteratorForDecryption();
          getContentEncryptionService().decryptContents(iterator);
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
      }
    });

    executor.shutdown();
    while (!executor.isTerminated()) {

    }
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

    executor.submit(new Runnable() {
      @Override
      public void run() {
        try {
          EncryptionContentIterator iterator = getEncryptionContentIteratorForDecryption();
          getContentEncryptionService().encryptContents(iterator);
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
    final Future[] futures = new Future[3];
    futures[0] = executor.submit(new Runnable() {
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

    futures[1] = executor.submit(new Runnable() {
      @Override
      public void run() {
        try {
          TextContent[] contents = generateTextContents(1);
          getContentEncryptionService()
              .encryptContent(contents[0].getText(), contents[0].getDescription(),
                  contents[0].getText());
          time[0] = System.currentTimeMillis();
        } catch (Exception e) {
          fail(e.getMessage());
        }
      }
    });

    futures[2] = executor.submit(new Runnable() {
      @Override
      public void run() {
        try {
          EncryptionContentIterator iterator = getEncryptionContentIteratorForEncryption();
          getContentEncryptionService().encryptContents(iterator);
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
          for (Future future : futures) {
            assertThat(future.isDone(), is(false));
          }
          String key = generateAESKey();
          getContentEncryptionService().updateCipherKey(key);
          assertThat(System.currentTimeMillis(), greaterThan(time[0]));
          for (Future future : futures) {
            assertThat(future.isDone(), is(true));
          }
        } catch (Exception e) {
          fail(e.getMessage());
        }
      }
    });

    executor.shutdown();
    while (!executor.isTerminated()) {

    }
  }

  @Test
  public void testTheKeyUpdateIsBlockedWhenAContentIsDecrypted() {
    ExecutorService executor = Executors.newCachedThreadPool();
    final long[] time = new long[1];
    final Future[] futures = new Future[3];
    futures[0] = executor.submit(new Runnable() {
      @Override
      public void run() {
        try {
          TextContent[] contents = generateTextContents(1);
          TextContent[] encryptedContents = encryptTextContents(contents, key);
          Map<String, String> encryptedContent = encryptedContents[0].getProperties();
          getContentEncryptionService().decryptContent(encryptedContent);
          time[0] = System.currentTimeMillis();
        } catch (Exception e) {
          fail(e.getMessage());
        }
      }
    });

    futures[1] = executor.submit(new Runnable() {
      @Override
      public void run() {
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
      }
    });

    futures[2] = executor.submit(new Runnable() {
      @Override
      public void run() {
        try {
          EncryptionContentIterator iterator = getEncryptionContentIteratorForDecryption();
          getContentEncryptionService().decryptContents(iterator);
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
          for (Future future : futures) {
            assertThat(future.isDone(), is(false));
          }
          String key = generateAESKey();
          getContentEncryptionService().updateCipherKey(key);
          assertThat(System.currentTimeMillis(), greaterThan(time[0]));
          for (Future future : futures) {
            assertThat(future.isDone(), is(true));
          }
        } catch (Exception e) {
          fail(e.getMessage());
        }
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
