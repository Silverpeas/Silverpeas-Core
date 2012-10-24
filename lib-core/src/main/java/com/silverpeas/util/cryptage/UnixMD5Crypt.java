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

package com.silverpeas.util.cryptage;

/****************************************************************************
 * Java-based implementation of the unix crypt(3) command
 *
 * Based upon C source code written by Eric Young, eay@psych.uq.oz.au
 * Java conversion by John F. Dumas, jdumas@zgs.com
 *
 * Found at http://locutus.kingwoodcable.com/jfd/crypt.html
 * Minor optimizations by Wes Biggs, wes@cacas.org
 *
 * Eric's original code is licensed under the BSD license.  As this is
 * derivative, the same license applies.
 *
 * Note: Crypt.class is much smaller when compiled with javac -O
 ****************************************************************************/

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.StringTokenizer;

/**
 * This class implements the popular MD5Crypt function as used by BSD and most modern Un*x systems.
 * It was basically converted from the C code write by Poul-Henning Kamp.
 */
public class UnixMD5Crypt {

  public static final String MAGIC = "$1$";
  public static final byte[] ITOA64 =
      "./0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
      .getBytes();

  public static void to64(StringBuffer sb, int n, int nCount) {
    while (--nCount >= 0) {
      sb.append((char) ITOA64[n & 0x3f]);
      n >>= 6;
    }
  }

  public static String crypt(String strPassword) {
    // Get random salt
    Random random = new Random();
    byte[] salt = new byte[] { ITOA64[random.nextInt(64)],
        ITOA64[random.nextInt(64)], ITOA64[random.nextInt(64)],
        ITOA64[random.nextInt(64)], ITOA64[random.nextInt(64)],
        ITOA64[random.nextInt(64)] };

    return crypt(MAGIC + new String(salt) + '$', strPassword);
  }

  public static String crypt(String strSalt, String strPassword) {

    String retVal = null;

    try {
      StringTokenizer st = new StringTokenizer(strSalt, "$");
      st.nextToken();
      byte[] abyPassword = strPassword.getBytes();
      byte[] abySalt = st.nextToken().getBytes();

      MessageDigest _md = MessageDigest.getInstance("MD5");

      _md.update(abyPassword);
      _md.update(MAGIC.getBytes());
      _md.update(abySalt);

      MessageDigest md2 = MessageDigest.getInstance("MD5");
      md2.update(abyPassword);
      md2.update(abySalt);
      md2.update(abyPassword);
      byte[] abyFinal = md2.digest();

      for (int n = abyPassword.length; n > 0; n -= 16) {
        _md.update(abyFinal, 0, n > 16 ? 16 : n);
      }

      abyFinal = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

      // "Something really weird"
      // Not sure why 'j' is here as it is always zero, but it's in Kamp's code
      // too
      for (int j = 0, i = abyPassword.length; i != 0; i >>>= 1) {
        if ((i & 1) == 1) {
          _md.update(abyFinal, j, 1);
        } else {
          _md.update(abyPassword, j, 1);
        }
      }

      // Build the output string
      StringBuffer sbPasswd = new StringBuffer();
      sbPasswd.append(MAGIC);
      sbPasswd.append(new String(abySalt));
      sbPasswd.append('$');

      abyFinal = _md.digest();

      // And now, just to make sure things don't run too fast
      // in C . . . "On a 60 Mhz Pentium this takes 34 msec, so you would
      // need 30 seconds to build a 1000 entry dictionary..."
      for (int n = 0; n < 1000; n++) {
        MessageDigest md3 = MessageDigest.getInstance("MD5");
        // MD5Init(&ctx1);
        if ((n & 1) != 0) {
          md3.update(abyPassword);
        } else {
          md3.update(abyFinal);
        }

        if ((n % 3) != 0) {
          md3.update(abySalt);
        }

        if ((n % 7) != 0) {
          md3.update(abyPassword);
        }

        if ((n & 1) != 0) {
          md3.update(abyFinal);
        } else {
          md3.update(abyPassword);
        }

        abyFinal = md3.digest();
      }

      // Convert to int's so we can do our bit manipulation
      // it's a bit tricky making the byte act unsigned
      int[] anFinal = new int[] { (abyFinal[0] & 0x7f) | (abyFinal[0] & 0x80),
          (abyFinal[1] & 0x7f) | (abyFinal[1] & 0x80),
          (abyFinal[2] & 0x7f) | (abyFinal[2] & 0x80),
          (abyFinal[3] & 0x7f) | (abyFinal[3] & 0x80),
          (abyFinal[4] & 0x7f) | (abyFinal[4] & 0x80),
          (abyFinal[5] & 0x7f) | (abyFinal[5] & 0x80),
          (abyFinal[6] & 0x7f) | (abyFinal[6] & 0x80),
          (abyFinal[7] & 0x7f) | (abyFinal[7] & 0x80),
          (abyFinal[8] & 0x7f) | (abyFinal[8] & 0x80),
          (abyFinal[9] & 0x7f) | (abyFinal[9] & 0x80),
          (abyFinal[10] & 0x7f) | (abyFinal[10] & 0x80),
          (abyFinal[11] & 0x7f) | (abyFinal[11] & 0x80),
          (abyFinal[12] & 0x7f) | (abyFinal[12] & 0x80),
          (abyFinal[13] & 0x7f) | (abyFinal[13] & 0x80),
          (abyFinal[14] & 0x7f) | (abyFinal[14] & 0x80),
          (abyFinal[15] & 0x7f) | (abyFinal[15] & 0x80) };

      to64(sbPasswd, anFinal[0] << 16 | anFinal[6] << 8 | anFinal[12], 4);
      to64(sbPasswd, anFinal[1] << 16 | anFinal[7] << 8 | anFinal[13], 4);
      to64(sbPasswd, anFinal[2] << 16 | anFinal[8] << 8 | anFinal[14], 4);
      to64(sbPasswd, anFinal[3] << 16 | anFinal[9] << 8 | anFinal[15], 4);
      to64(sbPasswd, anFinal[4] << 16 | anFinal[10] << 8 | anFinal[5], 4);
      to64(sbPasswd, anFinal[11], 2);

      retVal = sbPasswd.toString();
    } catch (NoSuchAlgorithmException e) {
      // log.warn( e.getMessage(), e );
    }

    return retVal;

  }

}
