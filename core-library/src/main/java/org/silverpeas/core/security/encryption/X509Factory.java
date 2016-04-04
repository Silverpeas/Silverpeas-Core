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

package org.silverpeas.core.security.encryption;

import net.sourceforge.jcetaglib.lib.X509Cert;
import org.silverpeas.core.security.authentication.password.encryption.UnixDESEncryption;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.exception.UtilException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class X509Factory {

  private static String truststoreFile = null;
  private static String truststorePwd = null;

  private static String p12Dir = null;
  private static String p12Salt = null;

  private static int validity = -1;
  private static String subjectDNSuffix = null;

  static {
    SettingBundle settings = ResourceLocator.getSettingBundle("org.silverpeas.util.security");
    truststoreFile = settings
        .getString(
        "x509.TruststoreFile",
        "C:\\Silverpeas\\KMEdition\\Tools\\jboss403\\server\\default\\conf\\server.truststore");
    truststorePwd = settings.getString("x509.TruststorePwd", "servercert");

    String ou = settings.getString("x509.DN_OU", "silverpeas.com"); // Organizational
    // Unit
    String o = settings.getString("x509.DN_O", "Silverpeas"); // Organization
    // Name
    String l = settings.getString("x509.DN_L", "Grenoble"); // City or Locality
    String c = settings.getString("x509.DN_C", "FR"); // Two-letter country code

    // subjectDNSuffix = "OU="+ou+", O="+o+", L="+l+", C="+c;
    subjectDNSuffix = "C=" + c + ", L=" + l + ", O=" + o + ", OU=" + ou;

    validity = Integer.parseInt(settings.getString("x509.Validity", "365"));

    p12Dir = settings.getString("p12.dir",
        "C:\\Silverpeas\\KMEdition\\Tools\\jboss403\\server\\default\\conf\\");
    p12Salt = settings.getString("p12.salt", "SP");
  }

  public static void buildP12(String userId, String login, String userLastName,
      String userFirstName, String domainId) throws UtilException {
    // Create self signed public/private key pair for client
    KeyPair keyPair = null;
    try {
      keyPair = X509Cert.generateKeyPair("RSA", 1024, new byte[0]);
    } catch (Exception e) {
      throw new UtilException("X509Factory.buildP12",
          SilverpeasException.ERROR, "util.CANT_GENERATE_KEYPAIR", e);
    }

    PrivateKey privateKey = keyPair.getPrivate();
    PublicKey publicKey = keyPair.getPublic();

    // String subjectDN = "CN="+userLastName+","+subjectDNSuffix;
    if (userFirstName == null)
      userFirstName = "";

    String subjectDN = subjectDNSuffix + ", CN=" + userFirstName + " "
        + userLastName;

    // Generate certificate
    X509Certificate myCert = null;
    try {
      myCert = X509Cert.selfsign(privateKey, publicKey, "MD5WithRSAEncryption",
          validity, subjectDN, false, "client");
    } catch (CertificateException e) {
      throw new UtilException("X509Factory.buildP12",
          SilverpeasException.ERROR,
          "util.CANT_CREATE_SELFSIGNED_X509_CERTIFICATE", e);
    }

    KeyStore keyStore = getKeyStore();

    String alias = userId;

    try {
      keyStore.setCertificateEntry(alias, myCert);
    } catch (KeyStoreException e) {
      throw new UtilException("X509Factory.buildP12",
          SilverpeasException.ERROR,
          "util.CANT_STORE_X509_CERTIFICATE_INTO_TRUSTSTORE", e);
    }

    writeKeyStore(keyStore);

    // Build P12 file
    String p12File = p12Dir + login + "_" + domainId + ".p12";
    UnixDESEncryption desEncryption = new UnixDESEncryption();
    String password = desEncryption.encrypt(login, p12Salt.getBytes());

    try {
      X509Cert.saveAsP12(myCert, null, privateKey, p12File, alias,
          new StringBuffer(password));
    } catch (Exception e) {
      throw new UtilException("X509Factory.buildP12",
          SilverpeasException.ERROR, "util.CANT_CREATE_PKCS12_FILE", e);
    }
  }

  public static void revocateUserCertificate(String userId)
      throws UtilException {
    KeyStore keyStore = getKeyStore();

    if (keyStore != null) {
      try {
        keyStore.deleteEntry(userId);
      } catch (KeyStoreException e) {
        throw new UtilException("X509Factory.revocateUserCertificate",
            SilverpeasException.ERROR,
            "util.CANT_DELETE_X509_CERTIFICATE_FROM_TRUSTSTORE", e);
      }

      writeKeyStore(keyStore);
    }
  }

  private static KeyStore getKeyStore() throws UtilException {
    KeyStore keyStore = null;
    try {
      keyStore = KeyStore.getInstance("jks");
    } catch (KeyStoreException e) {
      throw new UtilException("X509Factory.getKeyStore",
          SilverpeasException.ERROR, "util.CANT_GET_KEYSTORE_INSTANCE", e);
    }

    // Get KeyStore objet from file (the truststore)
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(truststoreFile);
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    try {
      keyStore.load(fis, truststorePwd.toCharArray());
    } catch (NoSuchAlgorithmException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (CertificateException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    try {
      if (fis != null)
        fis.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return keyStore;
  }

  private static void writeKeyStore(KeyStore keyStore) {
    // Writing Keystore back to file
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(truststoreFile);
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    try {
      keyStore.store(fos, truststorePwd.toCharArray());
    } catch (KeyStoreException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (NoSuchAlgorithmException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (CertificateException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    try {
      fos.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}