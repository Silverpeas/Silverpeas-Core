package com.silverpeas.util.security;

import com.silverpeas.util.StringUtil;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.engines.CAST5Engine;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Before;
import org.junit.Test;
import org.silverpeas.util.Charsets;
import org.silverpeas.util.crypto.Cipher;
import org.silverpeas.util.crypto.CipherFactory;
import org.silverpeas.util.crypto.CipherKey;
import org.silverpeas.util.crypto.CryptographicAlgorithmName;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.Security;
import java.util.Arrays;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class ContentEncryptionServiceTest {

  private static final String PLAIN_TEXT = "Bonjour tout le monde";

  @Before
  public void setUp() throws Exception {

  }

  @Test
  public void testUpdateKey() throws Exception {

  }
}
