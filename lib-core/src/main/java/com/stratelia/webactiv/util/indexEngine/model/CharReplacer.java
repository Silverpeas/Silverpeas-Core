package com.stratelia.webactiv.util.indexEngine.model;

import java.util.HashMap;
import java.util.Map;

/**
 * A CharReplacer replace all the occurences of a given chars' set
 * in a String with replacement chars.
 *
 * <PRE>
 *   CharReplacer r = new CharReplacer();
 *   r.setReplacement("éèê", "e");
 *   r.setReplacement("àâ", "a");
 *   r.setReplacement("!,.;", null);
 *
 *   // print "evenement a Grenoble"
 *   System.out.println(r.replace("événement à Grenoble!"));
 * </PRE>
 */
public class CharReplacer
{
  /**
   * When built with no parameters a CharReplacer is initialized
   * with an empty replacement map.
   *
   * Replacements should be added with calls to the setReplacement method,
   * before the new CharReplacer is used.
   */
  public CharReplacer()
  {
  }

  /**
   * Builds a new CharReplacer from a (toBeReplaced, replacement) character
   * pairs list.
   */
  public CharReplacer(String toBeReplaced,
                      String replacement)
  {
    setReplacement(toBeReplaced, replacement);
  }

  /**
   * Set a list of (toBeReplaced, replacement) character pairs.
   *
   * The (toBeReplaced, replacement) pairs are given with two
   * strings where the replacement char of each char in the
   * toBeReplaced string is given by the char at the same index in
   * the replacement string.
   *
   * If the replacement string is longer then the toBeReplaced string
   * all the extra characters are ignored.
   *
   * If the replacement string is shorter then the toBeReplaced string
   * all the missing characters are assumed equal to the last.
   *
   * If the replacement string is empty all occurences of the toBeReplaced
   * characters will be pruned.
   *
   * This statements are equivalent :
   * <PRE>
   *   setReplacement("éèê", "e");
   *   setReplacement("éèê", "eee");
   *   setReplacement("éèê", "eeeeeeeee");
   * </PRE>
   */
  public void setReplacement(String toBeReplaced,
                             String replacement)
  {
    for (int i=0 ; i<toBeReplaced.length() ; i++)
    {
      char replacementChar;
      if (replacement == null || replacement.length() == 0)
      {
        // pruned character
        replacementChar = '\u0000';
      }
      else if (i<replacement.length())
      {
        replacementChar = replacement.charAt(i);
      }
      else
      {
        replacementChar = replacement.charAt(replacement.length()-1);
      }

      replacementMap.put(new Character(toBeReplaced.charAt(i)),
                         new Character(replacementChar));
    }
  }
  
  /**
   * Return a new string resulting from replacing
   * all occurences of the to be replaced chars in the given string s
   * with the corresponding replacement char.
   */
  public String replace(String s)
  {
    final int length = s.length();
    StringBuffer result = new StringBuffer(length);

    int       getPos;
    char      oldChar;
    char      newChar;
    Character c;
    
    for(getPos=0; getPos<length; getPos++)
    {
      oldChar = s.charAt(getPos);

      c = (Character) replacementMap.get(new Character(oldChar));
      if (c != null) newChar = c.charValue();
      else           newChar = oldChar;

      if ( newChar != '\u0000' ) 
      {
        result.append(newChar);
      }
    }

    return result.toString();
  }

  /**
   * The characters to be replaced and their corresponding replacement
   * chars are saved in a Map (Character -> Character).
   *
   * If a character has to be pruned, it's mapped to \u0000.
   */
  private final Map replacementMap = new HashMap();
}
