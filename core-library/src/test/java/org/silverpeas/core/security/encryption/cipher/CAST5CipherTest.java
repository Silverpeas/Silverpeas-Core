package org.silverpeas.core.security.encryption.cipher;

import org.silverpeas.core.util.ArrayUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.silverpeas.core.util.Charsets;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.security.AlgorithmParameters;
import java.security.Security;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Unit tests on the CAST5 cipher as implemented in Silverpeas.
 * The implemented CAST5 cipher uses a random IV (Initialization Vector) at each text encryption. So,
 * in order this vector is available in the cipher's decryption, it is embedded in the
 * computed encrypted data. that means this CAST5 cipher cannot decrypt a ciphertext coming from
 * another CAST5 cipher as well another cipher cannot decrypt the encrypted data computed by the
 * Silverpeas CAST5 cipher.
 */
public class CAST5CipherTest {

  private static final String KNOWN_PLAIN_TEXT = "Il était une fois un joli petit pois tout argenté";

  private Cipher cast5;
  private CipherKey key;

  @BeforeClass
  public static final void loadJCRProvider() {
    Security.addProvider(new BouncyCastleProvider());
  }

  @Before
  public void setUp() throws Exception {
    cast5 = initCAST5Cipher();
    key = initCipherKey();
  }

  @Test
  public void testGetAlgorithmName() throws Exception {
    assertThat(cast5.getAlgorithmName(), is(CryptographicAlgorithmName.CAST5));
  }

  @Test
  public void testEncrypt() throws Exception {
    byte[] encryptedText = cast5.encrypt(KNOWN_PLAIN_TEXT, key);
    byte[][] encryptionData = BlockCipherWithPadding.extractEncryptionData(encryptedText,
        (BlockCipherWithPadding) cast5);
    byte[] expectedEncryptedText = encryptTextWithKeyAndIV(KNOWN_PLAIN_TEXT, key.getRawKey(),
        encryptionData[1]);
    assertThat(encryptedText, is(expectedEncryptedText));
  }

  @Test
  public void testDecrypt() throws Exception {
    byte[] encryptedText = encryptTextWithKeyAndIV(KNOWN_PLAIN_TEXT, key.getRawKey(), null);
    String plainText = cast5.decrypt(encryptedText, key);
    assertThat(plainText, is(KNOWN_PLAIN_TEXT));
  }

  @Test
  public void testEncryptionThenDecryption() throws Exception {
    byte[] cipherText = cast5.encrypt(KNOWN_PLAIN_TEXT, key);
    String plainText = cast5.decrypt(cipherText, key);
    assertThat(plainText, is(KNOWN_PLAIN_TEXT));
  }

  private Cipher initCAST5Cipher() {
    CipherFactory cipherFactory = CipherFactory.getFactory();
    Cipher cipher = cipherFactory.getCipher(CryptographicAlgorithmName.CAST5);
    assertNotNull(cipher);
    return cipher;
  }

  private CipherKey initCipherKey() throws Exception {
    KeyGenerator keyGenerator = KeyGenerator.getInstance("CAST5", "BC");
    keyGenerator.init(128);
    SecretKey secretKey = keyGenerator.generateKey();
    return CipherKey.aKeyFromBinary(secretKey.getEncoded());
  }

  private byte[] encryptTextWithKeyAndIV(String text, byte[] encodedKey, byte[] iv)
      throws Exception {
    SecretKeySpec keySpec = new SecretKeySpec(encodedKey, "CAST5");
    javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("CAST5/CBC/PKCS5Padding", "BC");
    byte[] usedIV;
    if (iv == null) {
      cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, keySpec);
      AlgorithmParameters params = cipher.getParameters();
      usedIV = params.getParameterSpec(IvParameterSpec.class).getIV();
    } else {
      cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv));
      usedIV = iv;
    }
    byte[] cipherText = cipher.doFinal(text.getBytes(Charsets.UTF_8));
    return ArrayUtil.addAll(usedIV, cipherText);
  }
}