package com.silverpeas.util.cryptage;

import java.security.Key;

import javax.crypto.spec.SecretKeySpec;

public class SilverCryptKeysSymetric {

  private Key key = null;

  public SilverCryptKeysSymetric() {
    if (key == null) {
      byte[] keybyte = "ƒþX]Lh/‘".getBytes();
      // key = new SecretKeySpec(keybyte,"DES");
      key = new SecretKeySpec(keybyte, "Blowfish");
    }
  }

  /*
   * public static Key generateDESKey() throws NoSuchAlgorithmException {
   * System.out.println("\nStart generating DES key"); KeyGenerator keyGen =
   * KeyGenerator.getInstance("DES"); keyGen.init(56); Key key =
   * keyGen.generateKey(); System.out.println("Finish generating DES key");
   * return key; }
   */

  public Key getKey() {
    return key;
  }

}
