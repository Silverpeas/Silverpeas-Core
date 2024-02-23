/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.security.encryption.cipher;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.UnrecoverableEntryException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * A key store of the public and secret keys, and of the X509 certificate in a PKS infrastructure.
 * <p>
 * The PKS (Public-Key Cryptography Standards) is a group of standards in cryptography that are
 * under the control of the the RSA Security company. The standards promote a cryptographic
 * infrastructure for the identification of users, the signature and the exchange of data.
 * <p>
 * The format used to store the private key and the public key certificate is described in the
 * PKS#12 specification written by the RSA Security company in California.
 */
public class PKS12KeyStore {

  private final X509Certificate cert;
  private final PrivateKey privatekey;
  private final PublicKey publickey;

  private static final String LOAD_FAILURE = "The load of the public and secret keys, and of the" +
      " X509 certificate failed!";

  /**
   * Constructs a new PKS#12 key store from the specified key store file.
   * <p>
   * It loads the secret and public key as well the X509 certificate from the specified file.
   * </p>
   * @param p12FilePath the path of the PKS#12 key store file.
   * @param password the password which protects the key store file.
   * @throws KeyStoreException if an error occurs while opening the PKS#12 key store file.
   * @throws CryptoException if an error occurs while loading the keys and the certificate from
   * the PKS#12 key store file.
   */
  public PKS12KeyStore(String p12FilePath, String password)
      throws KeyStoreException, CryptoException {
    KeyStore ks;
    Security.addProvider(new BouncyCastleProvider());
    ks = KeyStore.getInstance("PKCS12");
    if (p12FilePath != null) {
      try(final InputStream input = new FileInputStream(p12FilePath)) {
        ks.load(input, password.toCharArray());
      } catch (Exception ex) {
        throw new CryptoException(LOAD_FAILURE, ex);
      }
    }

    try {
      Enumeration<String> en = ks.aliases();
      String alias = "";
      List<String> listOfAliases = new ArrayList<>();

      while (en.hasMoreElements()) {
        listOfAliases.add(en.nextElement());
      }
      String[] allAliases = (listOfAliases.toArray(new String[0]));
      for (String anAliases : allAliases) {
        if (ks.isKeyEntry(anAliases)) {
          alias = anAliases;
          break;
        }
      }
      privatekey = (PrivateKey) ks.getKey(alias, password.toCharArray());
      cert = (X509Certificate) ks.getCertificate(alias);
      publickey = ks.getCertificate(alias).getPublicKey();
    } catch (NoSuchAlgorithmException | UnrecoverableEntryException ex) {
      throw new CryptoException(LOAD_FAILURE, ex);
    }
  }

  /**
   * Gets the X509 certificate in this PKS#12 key store.
   * @return an X509Certificate instance.
   */
  public X509Certificate getCertificate() {
    return cert;
  }

  /**
   * Gets the private key in this PKS#12 key store.
   * @return a PrivateKey instance.
   */
  public PrivateKey getPrivatekey() {
    return privatekey;
  }

  /**
   * Gets the public key in this PKS#12 key store.
   * @return a PublicKey instance.
   */
  public PublicKey getPublickey() {
    return publickey;
  }

}
