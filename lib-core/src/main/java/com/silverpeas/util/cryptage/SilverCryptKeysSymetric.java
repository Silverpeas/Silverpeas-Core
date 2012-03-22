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

import java.io.UnsupportedEncodingException;
import java.security.Key;
import javax.crypto.spec.SecretKeySpec;

public class SilverCryptKeysSymetric {

  private Key key = null;

  public SilverCryptKeysSymetric() {
    if (key == null) {
      byte[] keybyte = getKeyBytes("ƒþX]Lh/‘");
      key = new SecretKeySpec(keybyte, SilverCryptFactorySymetric.ALGORITHM);
    }
  }

  public SilverCryptKeysSymetric(String keyCode) {
    if (key == null) {
      byte[] keybyte = getKeyBytes(keyCode);
      key = new SecretKeySpec(keybyte, SilverCryptFactorySymetric.ALGORITHM);
    }
  }

  protected final byte[] getKeyBytes(String keyCode) {
    byte[] keybyte;
    try {
      keybyte = "ƒþX]Lh/‘".getBytes("ISO-8859-1");
    } catch (UnsupportedEncodingException e) {
      keybyte = "ƒþX]Lh/‘".getBytes();
    }
    return keybyte;
  }

  public Key getKey() {
    return key;
  }

}
