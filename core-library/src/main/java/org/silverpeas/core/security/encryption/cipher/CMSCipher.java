/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.security.encryption.cipher;

import org.bouncycastle.cms.CMSAlgorithm;
import org.bouncycastle.cms.CMSEnvelopedData;
import org.bouncycastle.cms.CMSEnvelopedDataGenerator;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.RecipientInfoGenerator;
import org.bouncycastle.cms.RecipientInformation;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.cms.jcajce.JceKeyTransEnvelopedRecipient;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.bouncycastle.operator.OutputEncryptor;
import org.silverpeas.core.util.Charsets;

import java.io.FileNotFoundException;
import java.util.Collection;

/**
 * The Cryptographic Message Syntax (CMS) is the IETF's standard for cryptographically protected
 * messages. It can be used to digitally sign, digest, authenticate or encrypt any form of digital
 * data. It is based on the syntax of PKCS#7, which in turn is based on the Privacy-Enhanced Mail
 * standard. The newest version of CMS (as of 2009) is specified in RFC 5652 (but see also RFC 5911
 * for updated ASN.1 modules conforming to ASN.1 2002).
 * <p/>
 * The architecture of CMS is built around certificate-based key management, such as the profile
 * defined by the PKIX working group.
 * <p/>
 * CMS is used as the key cryptographic component of many other cryptographic standards, such as
 * S/MIME, PKCS#12 and the RFC 3161 Digital timestamping protocol.
 * <p/>
 * This implementation wraps the all the mechanism required to encrypt and to decrypt messages
 * within a PKS/CMS infrastructure. For doing it uses the public key, the secret key and the X509
 * certificate provided by a PKS#12 key store (an instance of the PKS12KeyStore class).
 * <p/>
 * Instances of this class is for signing and encrypting, and checking and decrypting data
 * exchanged
 * between two interlocutors.
 */
public class CMSCipher implements Cipher {

  protected CMSCipher() {
  }

  /**
   * Gets the name of the algorithm of the cipher.
   * @return the algorithm name.
   */
  @Override
  public CryptographicAlgorithmName getAlgorithmName() {
    return CryptographicAlgorithmName.CMS;
  }

  /**
   * Encrypts the specified data by using the specified cryptographic key.
   * <p/>
   * The String objects handled by the encryption is done according the UTF-8 charset.
   * @param data the data to encode.
   * @param keyFilePath the file in which is stored the public key to use in the encryption.
   * @return the encrypted data in bytes.
   */
  @Override
  public byte[] encrypt(String data, CipherKey keyFilePath) throws CryptoException {
    try {
      // Chargement de la chaine à crypter
      byte[] buffer = stringToByteArray(data);

      // Chiffrement du document
      CMSEnvelopedDataGenerator gen = new CMSEnvelopedDataGenerator();
      // La variable cert correspond au certificat du destinataire
      // La clé publique de ce certificat servira à chiffrer la clé
      // symétrique
      if (!keyFilePath.isInFile()) {
        throw new FileNotFoundException("The PKS#12 file '" + keyFilePath + "' doesn't exist!");
      }
      PKS12KeyStoreWallet wallet = PKS12KeyStoreWallet.getInstance();
      PKS12KeyStore keyStore = wallet.getKeyStore(keyFilePath.getKeyFilePath());
      RecipientInfoGenerator generator =
          new JceKeyTransRecipientInfoGenerator(keyStore.getCertificate()).setProvider("BC");
      gen.addRecipientInfoGenerator(generator);

      // Choix de l'algorithme à clé symétrique pour chiffrer le document.
      // AES est un standard. Vous pouvez donc l'utiliser sans crainte.
      // Il faut savoir qu'en france la taille maximum autorisée est de 128
      // bits pour les clés symétriques (ou clés secrètes)
      OutputEncryptor encryptor =
          new JceCMSContentEncryptorBuilder(CMSAlgorithm.AES128_CBC).setProvider("BC").build();
      CMSEnvelopedData envData = gen.generate(new CMSProcessableByteArray(buffer), encryptor);
      return envData.getEncoded();
    } catch (CryptoException e) {
      throw e;
    } catch (Exception e) {
      throw new CryptoException(CryptoException.ENCRYPTION_FAILURE, e);
    }
  }

  /**
   * Decrypt the specified code or cipher by using the specified cryptographic key.
   * <p/>
   * The String objects handled by the encryption is done according the UTF-8 charset.
   * @param encryptedData the data in bytes that was encrypted by this cipher.
   * @param keyFilePath the file in which is stored the secret key to use in the decryption.
   * @return the decrypted data.
   */
  @Override
  public String decrypt(byte[] encryptedData, CipherKey keyFilePath) throws CryptoException {
    try {
      // Déchiffrement de la chaine
      CMSEnvelopedData ced = new CMSEnvelopedData(encryptedData);
      @SuppressWarnings("unchecked")
      Collection<RecipientInformation> recip = ced.getRecipientInfos().getRecipients();

      RecipientInformation rinfo = recip.iterator().next();
      // privatekey est la clé privée permettant de déchiffrer la clé
      // secrète (symétrique)
      if (!keyFilePath.isInFile()) {
        throw new FileNotFoundException("The PKS#12 file '" + keyFilePath + "' doesn't exist!");
      }
      PKS12KeyStoreWallet wallet = PKS12KeyStoreWallet.getInstance();
      PKS12KeyStore keyStore = wallet.getKeyStore(keyFilePath.getKeyFilePath());
      byte[] contents =
          rinfo.getContent(new JceKeyTransEnvelopedRecipient(keyStore.getPrivatekey()));
      return byteArrayToString(contents);
    } catch (CryptoException e) {
      throw e;
    } catch (Exception e) {
      throw new CryptoException(CryptoException.DECRYPTION_FAILURE, e);
    }
  }


  private String byteArrayToString(byte[] bArray) {
    // A n'utiliser qu'avec des Strings décryptés!!!
    return new String(bArray, Charsets.UTF_8);
  }

  private byte[] stringToByteArray(String theString) {
    return theString.getBytes(Charsets.UTF_8);
  }

  /**
   * This operation isn't yet supported by this cipher.
   * @return nothing, throws an UnsupportedOperationException exception.
   */
  @Override
  public CipherKey generateCipherKey() throws CryptoException {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
