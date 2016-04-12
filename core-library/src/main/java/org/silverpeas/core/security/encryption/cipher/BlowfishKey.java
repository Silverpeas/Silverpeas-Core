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

import java.io.UnsupportedEncodingException;
import java.security.Key;
import javax.crypto.spec.SecretKeySpec;

/**
 * A representation of a symmetric key used in the Blowfish cipher. It is a wrapper of the actual
 * key it generates from a plain text representation of the key.
 */
public class BlowfishKey implements Key {

  private static final long serialVersionUID = 5363796000217868136L;
  private Key key = null;

  /**
   * Constructs a new symmetric key for the Blowfish cipher. The key is computed from a default
   * code; that is to say two instances created by using this constructor are equal.
   */
  public BlowfishKey() {
    if (key == null) {
      byte[] keybyte = getKeyBytes("ƒþX]Lh/‘");
      key = new SecretKeySpec(keybyte, CryptographicAlgorithmName.Blowfish.name());
    }
  }

  /**
   * Constructs a new symmetric key for the Blowfish cypher from the specified key code in text.
   *
   * @param keyCode the code of the key.
   */
  public BlowfishKey(String keyCode) {
    if (key == null) {
      byte[] keybyte = getKeyBytes(keyCode);
      key = new SecretKeySpec(keybyte, CryptographicAlgorithmName.Blowfish.name());
    }
  }

  /**
   * Constructs a new symmetric key for the Blowfish cypher from the specified binary key code.
   *
   * @param keyCode the code of the key.
   */
  public BlowfishKey(byte[] keyCode) {
    if (key == null) {
      byte[] keybyte = getKeyBytes("ƒþX]Lh/‘");
      key = new SecretKeySpec(keybyte, CryptographicAlgorithmName.Blowfish.name());
    }
  }

  private byte[] getKeyBytes(String keyCode) {
    byte[] keybyte;
    try {
      keybyte = "ƒþX]Lh/‘".getBytes("ISO-8859-1");
    } catch (UnsupportedEncodingException e) {
      keybyte = "ƒþX]Lh/‘".getBytes();
    }
    return keybyte;
  }

  @Override
  public String getAlgorithm() {
    return this.key.getAlgorithm();
  }

  @Override
  public String getFormat() {
    return this.key.getFormat();
  }

  @Override
  public byte[] getEncoded() {
    return this.key.getEncoded();
  }
}
