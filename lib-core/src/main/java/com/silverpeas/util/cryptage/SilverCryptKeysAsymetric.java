/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.util.cryptage;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class SilverCryptKeysAsymetric {

  // classe trousseau de cle
  private X509Certificate cert = null;
  private PrivateKey privatekey = null;
  private PublicKey publickey = null;

  public SilverCryptKeysAsymetric(FileInputStream filep12, String pwd) {
    // CHARGEMENT DU FICHIER PKCS#12
    KeyStore ks = null;
    char[] password = null;
    Security.addProvider(new BouncyCastleProvider());
    try {
      ks = KeyStore.getInstance("PKCS12");
      // Password pour le fichier filep12
      password = pwd.toCharArray();
      if (filep12 != null) {
        ks.load(filep12, password);
      } else {
        System.out.println("Erreur: fichier .p12" + "introuvable");
      }

    } catch (Exception e) {
      System.out.println("Erreur: fichier .p12"
          + " n'est pas un fichier pkcs#12 valide ou passphrase incorrect");
      return;
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
      privatekey = (PrivateKey) ks.getKey(alias, password);
      cert = (X509Certificate) ks.getCertificate(alias);
      publickey = ks.getCertificate(alias).getPublicKey();
    } catch (Exception e) {
      SilverTrace.error("util", "SilverCryptKeysAsymetric.Error",
          "root.MSG_GEN_PARAM_VALUE", "In init", e);
      return;
    }
  }

  public X509Certificate getCert() {
    return cert;
  }

  public PrivateKey getPrivatekey() {
    return privatekey;
  }

  public PublicKey getPublickey() {
    return publickey;
  }

}
