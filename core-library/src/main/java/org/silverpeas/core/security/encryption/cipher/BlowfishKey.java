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
package org.silverpeas.core.security.encryption.cipher;

import org.silverpeas.core.util.Charsets;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

/**
 * A representation of a symmetric key used in the Blowfish cipher. It is a wrapper of the actual
 * key it generates from a plain text representation of the key.
 */
public class BlowfishKey implements Key {

  private static final long serialVersionUID = 5363796000217868136L;
  private static final String KEY_CODE = "ƒþX]Lh/‘";
  private Key key = null;

  /**
   * Constructs a new symmetric key for the Blowfish cipher. The key is computed from a default
   * code; that is to say two instances created by using this constructor are equal.
   */
  public BlowfishKey() {
    byte[] keyBytes = getKeyBytes(KEY_CODE);
    key = new SecretKeySpec(keyBytes, CryptographicAlgorithmName.BLOWFISH.getId());
  }

  /**
   * Constructs a new symmetric key for the Blowfish cypher from the specified key code in text.
   *
   * @param keyCode the code of the key. Must be in UTF-8.
   */
  @SuppressWarnings("unused")
  public BlowfishKey(String keyCode) {
    byte[] keyBytes = getKeyBytes(keyCode);
    key = new SecretKeySpec(keyBytes, CryptographicAlgorithmName.BLOWFISH.getId());
  }

  /**
   * Constructs a new symmetric key for the Blowfish cypher from the specified binary key code.
   *
   * @param keyCode the code of the key.
   */
  public BlowfishKey(byte[] keyCode) {
    key = new SecretKeySpec(keyCode, CryptographicAlgorithmName.BLOWFISH.getId());
  }

  private byte[] getKeyBytes(String keyCode) {
    return keyCode.getBytes(Charsets.UTF_8);
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
