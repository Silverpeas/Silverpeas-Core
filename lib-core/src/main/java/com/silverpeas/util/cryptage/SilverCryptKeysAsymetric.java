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

  // utilisation de bouncycastle
  // http://www.bouncycastle.org/fr/index.html
  // tutoriel:
  // http://nyal.developpez.com/tutoriel/java/bouncycastle/

  // classe trousseau de clé
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
