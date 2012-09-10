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

package org.silverpeas.search.indexEngine.model;

import java.util.HashMap;
import java.util.Map;

/**
 * A CharReplacer replace all the occurences of a given chars' set in a String with replacement
 * chars.
 * <p/>
 * 
 * <PRE>
 * CharReplacer r = new CharReplacer(); r.setReplacement(&quot;éèê&quot;, &quot;e&quot;);
 * r.setReplacement(&quot;àâ&quot;, &quot;a&quot;); r.setReplacement(&quot;!,.;&quot;, null);
 * &lt;p/&gt;
 * // print &quot;evenement a Grenoble&quot; System.out.println(r.replace(&quot;événement à
 * Grenoble!&quot;));
 * </PRE>
 */
public class CharReplacer {
  /**
   * When built with no parameters a CharReplacer is initialized with an empty replacement map.
   * Replacements should be added with calls to the setReplacement method, before the new
   * CharReplacer is used.
   */
  public CharReplacer() {
  }

  /**
   * Builds a new CharReplacer from a (toBeReplaced, replacement) character pairs list.
   */
  public CharReplacer(String toBeReplaced, String replacement) {
    setReplacement(toBeReplaced, replacement);
  }

  /**
   * Set a list of (toBeReplaced, replacement) character pairs. The (toBeReplaced, replacement)
   * pairs are given with two strings where the replacement char of each char in the toBeReplaced
   * string is given by the char at the same index in the replacement string. If the replacement
   * string is longer then the toBeReplaced string all the extra characters are ignored. If the
   * replacement string is shorter then the toBeReplaced string all the missing characters are
   * assumed equal to the last. If the replacement string is empty all occurences of the
   * toBeReplaced characters will be pruned. This statements are equivalent :
   * <p/>
   * 
   * <PRE>
   * setReplacement(&quot;éèê&quot;, &quot;e&quot;);
   * setReplacement(&quot;éèê&quot;, &quot;eee&quot;);
   * setReplacement(&quot;éèê&quot;, &quot;eeeeeeeee&quot;);
   * </PRE>
   */
  public final void setReplacement(String toBeReplaced, String replacement) {
    for (int i = 0; i < toBeReplaced.length(); i++) {
      char replacementChar;
      if (replacement == null || replacement.length() == 0) {
        // pruned character
        replacementChar = '\u0000';
      } else if (i < replacement.length()) {
        replacementChar = replacement.charAt(i);
      } else {
        replacementChar = replacement.charAt(replacement.length() - 1);
      }

      replacementMap.put(toBeReplaced.charAt(i), replacementChar);
    }
  }

  /**
   * Return a new string resulting from replacing all occurences of the to be replaced chars in the
   * given string s with the corresponding replacement char.
   */
  public String replace(String s) {
    final int length = s.length();
    StringBuilder result = new StringBuilder(length);
    char newChar;
    for (int getPos = 0; getPos < length; getPos++) {
      char oldChar = s.charAt(getPos);

      Character c = (Character) replacementMap.get(oldChar);
      if (c != null) {
        newChar = c;
      } else {
        newChar = oldChar;
      }

      if (newChar != '\u0000') {
        result.append(newChar);
      }
    }

    return result.toString();
  }

  /**
   * The characters to be replaced and their corresponding replacement chars are saved in a Map
   * (Character -> Character). If a character has to be pruned, it's mapped to \u0000.
   */
  private final Map replacementMap = new HashMap();
}