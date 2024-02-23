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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.security.encryption;

import net.sourceforge.jcetaglib.lib.X509Cert;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.security.authentication.password.encryption.UnixDESEncryption;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.lang.SystemWrapper;
import org.silverpeas.core.util.logging.SilverLogger;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class X509Factory {

  private static X509Factory instance;

  private final String truststoreFile;
  private final String truststorePwd;

  private final String p12Dir;
  private final String p12Salt;

  private final int validity;
  private final String subjectDNSuffix;

  public static synchronized X509Factory getFactory() {
    if (instance == null) {
      instance = new X509Factory();
    }
    return instance;
  }

  private X509Factory() {
    SettingBundle settings = ResourceLocator.getSettingBundle("org.silverpeas.util.security");
    String home = SystemWrapper.get().getenv("SILVERPEAS_HOME");
    Path defaultTrustStorePath = Path.of(home, "configuration", "security", "servercert");
    Path defaultP12DirPath = Path.of(home, "configuration", "security", "p12");

    truststoreFile = settings
        .getString("x509.TruststoreFile", "server.truststore");
    truststorePwd = settings.getString("x509.TruststorePwd", defaultTrustStorePath.toString());

    String ou = settings.getString("x509.DN_OU", "silverpeas.com"); // Organizational
    // Unit
    String o = settings.getString("x509.DN_O", "Silverpeas"); // Organization
    // Name
    String l = settings.getString("x509.DN_L", "Grenoble"); // City or Locality
    String c = settings.getString("x509.DN_C", "FR"); // Two-letter country code

    subjectDNSuffix = "C=" + c + ", L=" + l + ", O=" + o + ", OU=" + ou;

    validity = Integer.parseInt(settings.getString("x509.Validity", "365"));

    p12Dir = settings.getString("p12.dir", defaultP12DirPath.toString());
    p12Salt = settings.getString("p12.salt", "SP");
  }

  /**
   * Creates a self-signed X509 certificate in P12 format for the specified user.
   * @param user the user for which a self X509 certificate has to be created.
   * @throws SilverpeasRuntimeException if an error occurs while creating the certificate.
   */
  public void buildP12(final User user) {
    String userId = user.getId();
    String login = user.getLogin();
    String userFirstName = user.getFirstName();
    String userLastName = user.getLastName();
    String domainId = user.getDomainId();
    KeyPair keyPair;
    try {
      keyPair = X509Cert.generateKeyPair("RSA", 1024, new byte[0]);
    } catch (Exception e) {
      throw new SilverpeasRuntimeException("Cannot generate key pair", e);
    }

    PrivateKey privateKey = keyPair.getPrivate();
    PublicKey publicKey = keyPair.getPublic();

    String firstName = userFirstName == null ? "" : userFirstName;
    String subjectDN = subjectDNSuffix + ", CN=" + firstName + " " + userLastName;

    // Generate certificate
    X509Certificate myCert;
    try {
      myCert = X509Cert.selfsign(privateKey, publicKey, "MD5WithRSAEncryption",
          validity, subjectDN, false, "client");
    } catch (CertificateException e) {
      throw new SilverpeasRuntimeException("Cannot create self-signed X509 certificate", e);
    }

    KeyStore keyStore = getKeyStore();

    try {
      keyStore.setCertificateEntry(userId, myCert);
    } catch (KeyStoreException e) {
      throw new SilverpeasRuntimeException("Cannot store X509 certificate into the truststore", e);
    }

    writeKeyStore(keyStore);

    // Build P12 file
    String p12File = p12Dir + login + "_" + domainId + ".p12";
    UnixDESEncryption desEncryption = new UnixDESEncryption();
    String password = desEncryption.encrypt(login, p12Salt.getBytes());

    try {
      X509Cert.saveAsP12(myCert, null, privateKey, p12File, userId,
          new StringBuffer(password));
    } catch (Exception e) {
      throw new SilverpeasRuntimeException("Cannot create PKCS12 file", e);
    }
  }

  /**
   * Revokes the self-signed X509 certificate of the specified user.
   * @param userId the unique identifier of a user in Silverpeas.
   * @throws SilverpeasRuntimeException if an error occurs while revoking the user certificate.
   */
  public void revokeUserCertificate(String userId) {
    KeyStore keyStore = getKeyStore();

    if (keyStore != null) {
      try {
        keyStore.deleteEntry(userId);
      } catch (KeyStoreException e) {
        throw new SilverpeasRuntimeException("Cannot delete X509 certificate from the truststore",
            e);
      }

      writeKeyStore(keyStore);
    }
  }

  private KeyStore getKeyStore() {
    KeyStore keyStore;
    try {
      keyStore = KeyStore.getInstance("jks");
    } catch (KeyStoreException e) {
      throw new SilverpeasRuntimeException("Cannot get a Keystore", e);
    }

    // Get KeyStore objet from file (the truststore)
    try(InputStream fis = new FileInputStream(truststoreFile)) {
      keyStore.load(fis, truststorePwd.toCharArray());
    } catch (Exception e) {
      SilverLogger.getLogger(X509Factory.class).error(e.getMessage(), e);
    }

    return keyStore;
  }

  private void writeKeyStore(KeyStore keyStore) {
    // Writing Keystore back to file
    try (FileOutputStream fos = new FileOutputStream(truststoreFile)) {
      keyStore.store(fos, truststorePwd.toCharArray());
    } catch (Exception e) {
      SilverLogger.getLogger(X509Factory.class).error(e.getMessage(), e);
    }
  }
}