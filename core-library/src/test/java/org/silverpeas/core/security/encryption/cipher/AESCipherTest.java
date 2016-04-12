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
 * Unit tests on the AES cipher as implemented in Silverpeas.
 * The implemented AES cipher uses a random IV (Initialization Vector) at each text encryption. So,
 * in order this vector is available in the cipher's decryption, it is embedded in the
 * computed encrypted data. that means this AES cipher cannot decrypt a ciphertext coming from
 * another AES cipher as well another cipher cannot decrypt the encrypted data computed by the
 * Silverpeas AES cipher.
 */
public class AESCipherTest {

  private static final String KNOWN_PLAIN_TEXT = "Il était une fois un joli petit pois tout argenté";

  private Cipher aes;
  private CipherKey key;

  @BeforeClass
  public static final void loadJCRProvider() {
    Security.addProvider(new BouncyCastleProvider());
  }

  @Before
  public void setUp() throws Exception {
    aes = initAESCipher();
    key = initCipherKey();
  }

  @Test
  public void testGetAlgorithmName() throws Exception {
    assertThat(aes.getAlgorithmName(), is(CryptographicAlgorithmName.AES));
  }

  @Test
  public void testEncrypt() throws Exception {
    byte[] encryptedText = aes.encrypt(KNOWN_PLAIN_TEXT, key);
    byte[][] encryptionData = BlockCipherWithPadding.extractEncryptionData(encryptedText,
        (BlockCipherWithPadding)aes);
    byte[] expectedEncryptedText = encryptTextWithKeyAndIV(KNOWN_PLAIN_TEXT, key.getRawKey(),
        encryptionData[1]);
    assertThat(encryptedText, is(expectedEncryptedText));
  }

  @Test
  public void testDecrypt() throws Exception {
    byte[] encryptedText = encryptTextWithKeyAndIV(KNOWN_PLAIN_TEXT, key.getRawKey(), null);
    String plainText = aes.decrypt(encryptedText, key);
    assertThat(plainText, is(KNOWN_PLAIN_TEXT));
  }

  @Test
  public void testEncryptionThenDecryption() throws Exception {
    byte[] cipherText = aes.encrypt(KNOWN_PLAIN_TEXT, key);
    String plainText = aes.decrypt(cipherText, key);
    assertThat(plainText, is(KNOWN_PLAIN_TEXT));
  }

  private Cipher initAESCipher() {
    CipherFactory cipherFactory = CipherFactory.getFactory();
    Cipher cipher = cipherFactory.getCipher(CryptographicAlgorithmName.AES);
    assertNotNull(cipher);
    return cipher;
  }

  private CipherKey initCipherKey() throws Exception {
    KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
    keyGenerator.init(256);
    SecretKey secretKey = keyGenerator.generateKey();
    return CipherKey.aKeyFromBinary(secretKey.getEncoded());
  }

  private byte[] encryptTextWithKeyAndIV(String text, byte[] encodedKey, byte[] iv)
      throws Exception {
    SecretKeySpec keySpec = new SecretKeySpec(encodedKey, "AES");
    javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/CBC/PKCS5Padding");
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
