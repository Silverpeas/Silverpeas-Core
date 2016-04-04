/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.security.encryption.cipher;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
 * <p/>
 * The PKS (Public-Key Cryptography Standards) is a group of standards in cryptography that are
 * under the control of the the RSA Security company. The standards promote a cryptographic
 * infrastructure for the identification of users, the signature and the exchange of data.
 * <p/>
 * The format used to store the private key and the public key certificate is described in the
 * PKS#12 specification written by the RSA Security company in California.
 */
public class PKS12KeyStore {

  private X509Certificate cert = null;
  private PrivateKey privatekey = null;
  private PublicKey publickey = null;

  private static final String LOAD_FAILURE = "The load of the public and secret keys, and of the" +
      " X509 certificate failed!";

  /**
   * Constructs a new PKS#12 key store from the specified key store file.
   *
   * It loads the secret and public key as well the X509 certificate from the specified file.
   * @param p12FilePath the path of the PKS#12 key store file.
   * @param password the password which protects the key store file.
   * @throws KeyStoreException if an error occurs while opening the PKS#12 key store file.
   * @throws FileNotFoundException if the PKS#12 key store file doesn't exist.
   * @throws CryptoException if an error occurs while loading the keys and the certificate from
   * the PKS#12 key store file.
   */
  public PKS12KeyStore(String p12FilePath, String password)
      throws KeyStoreException, CryptoException {
    // CHARGEMENT DU FICHIER PKCS#12
    KeyStore ks = null;
    Security.addProvider(new BouncyCastleProvider());
    ks = KeyStore.getInstance("PKCS12");
    // Password pour le fichier filep12
    if (p12FilePath != null) {
      try {
        ks.load(new FileInputStream(p12FilePath), password.toCharArray());
      } catch (Exception ex) {
        throw new CryptoException(LOAD_FAILURE, ex);
      }
    }

    // RECUPERATION DU COUPLE CLE PRIVEE/PUBLIQUE ET DU CERTIFICAT PUBLIQUE
    try {
      Enumeration<String> en = ks.aliases();
      String alias = "";
      List<String> vectaliases = new ArrayList<String>();

      while (en.hasMoreElements()) {
        vectaliases.add(en.nextElement());
      }
      String[] aliases = (vectaliases.toArray(new String[vectaliases.size()]));
      for (String aliase : aliases) {
        if (ks.isKeyEntry(aliase)) {
          alias = aliase;
          break;
        }
      }
      privatekey = (PrivateKey) ks.getKey(alias, password.toCharArray());
      cert = (X509Certificate) ks.getCertificate(alias);
      publickey = ks.getCertificate(alias).getPublicKey();
    } catch (NoSuchAlgorithmException ex) {
      throw new CryptoException(LOAD_FAILURE, ex);
    } catch (UnrecoverableEntryException ex) {
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
