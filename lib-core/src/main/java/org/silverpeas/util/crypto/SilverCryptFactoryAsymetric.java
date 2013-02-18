/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
package org.silverpeas.util.crypto;

import java.io.FileInputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bouncycastle.cms.CMSAlgorithm;
import org.bouncycastle.cms.CMSEnvelopedData;
import org.bouncycastle.cms.CMSEnvelopedDataGenerator;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.KeyTransRecipientInformation;
import org.bouncycastle.cms.RecipientInfoGenerator;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.cms.jcajce.JceKeyTransEnvelopedRecipient;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.bouncycastle.operator.OutputEncryptor;

import org.silverpeas.util.Charsets;

import com.stratelia.webactiv.util.exception.SilverpeasException;

public class SilverCryptFactoryAsymetric {
  // Singleton pour gèrer une seule Map de trousseaux de clés

  private static SilverCryptFactoryAsymetric factory = null;

  private SilverCryptFactoryAsymetric() {
  }
  private final Map<String, SilverCryptKeysAsymetric> keyMap = new HashMap<String, SilverCryptKeysAsymetric>();

  public static SilverCryptFactoryAsymetric getInstance() {
    synchronized (SilverCryptFactoryAsymetric.class) {
      if (factory == null) {
        factory = new SilverCryptFactoryAsymetric();
      }
    }
    return factory;
  }

  public byte[] goCrypting(String stringUnCrypted, String fileName) throws CryptoException {
    try {
      // Chargement de la chaine à crypter
      byte[] buffer = stringToByteArray(stringUnCrypted);

      // Chiffrement du document
      CMSEnvelopedDataGenerator gen = new CMSEnvelopedDataGenerator();
      // La variable cert correspond au certificat du destinataire
      // La clé publique de ce certificat servira à chiffrer la clé
      // symétrique
      RecipientInfoGenerator generator = new JceKeyTransRecipientInfoGenerator(
          getKeys(fileName).getCert()).setProvider("BC");
      gen.addRecipientInfoGenerator(generator);

      // Choix de l'algorithme à clé symétrique pour chiffrer le document.
      // AES est un standard. Vous pouvez donc l'utiliser sans crainte.
      // Il faut savoir qu'en france la taille maximum autorisée est de 128
      // bits pour les clés symétriques (ou clés secrètes)    
      OutputEncryptor encryptor = new JceCMSContentEncryptorBuilder(CMSAlgorithm.AES128_CBC)
          .setProvider("BC").build();
      CMSEnvelopedData envData = gen.generate(new CMSProcessableByteArray(buffer), encryptor);
      byte[] pkcs7envelopedData = envData.getEncoded();
      return pkcs7envelopedData;
    } catch (CryptoException e) {
      throw e;
    } catch (Exception e) {

      throw new CryptoException("SilverCryptFactory.goCrypting", SilverpeasException.ERROR,
          "util.CRYPT_FAILED", e);
    }
  }

  public String goUnCrypting(byte[] stringCrypted, String fileName)
      throws CryptoException {
    try {
      // Chargement de la chaine à déchiffrer
      byte[] pkcs7envelopedData = stringCrypted;

      // Déchiffrement de la chaine
      CMSEnvelopedData ced = new CMSEnvelopedData(pkcs7envelopedData);
      @SuppressWarnings("unchecked")
      Collection<KeyTransRecipientInformation> recip = ced.getRecipientInfos().getRecipients();

      KeyTransRecipientInformation rinfo = recip.iterator().next();
      // privatekey est la clé privée permettant de déchiffrer la clé
      // secrète (symétrique)
      byte[] contents = rinfo.getContent(new JceKeyTransEnvelopedRecipient(this.getKeys(fileName).
          getPrivatekey()));
      return byteArrayToString(contents);
    } catch (CryptoException e) {
      throw e;
    } catch (Exception e) {
      throw new CryptoException("SilverCryptFactory.goUnCrypting",
          SilverpeasException.ERROR, "util.UNCRYPT_FAILED", e);
    }
  }

  public synchronized void addKeys(String filename, String password)
      throws CryptoException {// ajout d'une trousseau de clé à partir d'un
    // chemin d'un fichier p12 + password
    if (this.keyMap.containsKey(filename)) {
      throw new CryptoException("SilverCryptFactory.addKeys",
          SilverpeasException.ERROR, "util.KEY_ALREADY_IN");
    } else {
      try {
        FileInputStream file = new FileInputStream(filename);
        SilverCryptKeysAsymetric silverkeys = new SilverCryptKeysAsymetric(
            file, password);
        this.keyMap.put(filename, silverkeys);
      } catch (Exception e) {
        throw new CryptoException("SilverCryptFactory.addKeys",
            SilverpeasException.ERROR, "util.KEYS_CREATION_FAILED");
      }
    }
  }

  private synchronized SilverCryptKeysAsymetric getKeys(String filename)
      throws CryptoException {// récupération du trousseau de clé!
    if (this.keyMap.containsKey(filename)) {
      return this.keyMap.get(filename);
    } else {
      throw new CryptoException("SilverCryptFactory.addKeys",
          SilverpeasException.ERROR, "util.KEY_NOT_FOUND");
    }
  }

  private String byteArrayToString(byte[] bArray) {// A n'utiliser qu'avec des
    // Strings décryptés!!!
    return new String(bArray, Charsets.UTF_8);
  }

  private byte[] stringToByteArray(String theString) {
    return theString.getBytes(Charsets.UTF_8);
  }
}
