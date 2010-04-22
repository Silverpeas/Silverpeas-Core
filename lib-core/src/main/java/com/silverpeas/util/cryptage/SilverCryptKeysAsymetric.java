/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
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

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Vector;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

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
      if (filep12 != null)
        ks.load(filep12, password);
      else
        System.out.println("Erreur: fichier .p12" + "introuvable");

    } catch (Exception e) {
      System.out.println("Erreur: fichier .p12"
          + " n'est pas un fichier pkcs#12 valide ou passphrase incorrect");
      return;
    }

    // RECUPERATION DU COUPLE CLE PRIVEE/PUBLIQUE ET DU CERTIFICAT PUBLIQUE
    try {
      Enumeration en = ks.aliases();
      String ALIAS = "";
      Vector vectaliases = new Vector();

      while (en.hasMoreElements())
        vectaliases.add(en.nextElement());
      String[] aliases = (String[]) (vectaliases.toArray(new String[0]));
      for (int i = 0; i < aliases.length; i++)
        if (ks.isKeyEntry(aliases[i])) {
          ALIAS = aliases[i];
          break;
        }
      privatekey = (PrivateKey) ks.getKey(ALIAS, password);
      cert = (X509Certificate) ks.getCertificate(ALIAS);
      publickey = ks.getCertificate(ALIAS).getPublicKey();
    } catch (Exception e) {
      e.printStackTrace();
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
