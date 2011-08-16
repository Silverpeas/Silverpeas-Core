/**
 * Copyright (C) 2000 - 2011 Silverpeas
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

package com.silverpeas.util;

import com.stratelia.webactiv.util.ResourceLocator;

import java.util.ArrayList;

public class PasswordGenerator {

  private static final int DEFAULT_LENGTH = 8;
  private static final char[] DEFAULT_SPECIAL_CHAR = {
      '%', '*', '!', '?', '$', '-', '.', '#', '&', '=' };

  private final ResourceLocator resource = new ResourceLocator(
      "com.silverpeas.util.passwordGenerator", "");

  // a - z : 97 - 122
  // A - Z : 65 - 90
  // 0 - 9 : 48 - 57

  private boolean atLeastOneUpperCaseChar;
  private boolean atLeastOneLowerCaseChar;
  private boolean atLeastOneNumberChar;
  private boolean atLeastOneSpecialChar;
  private int minLength;
  private int maxLength;
  private char[] specialChars;

  public PasswordGenerator() {
    init();
  }

  private void init() {
    String minLengthParam = resource.getString("passwordGenerator.minLength");
    if (StringUtil.isDefined(minLengthParam)) {
      try {
        minLength = Integer.parseInt(minLengthParam);
      } catch (NumberFormatException nfe) {
        minLength = DEFAULT_LENGTH;
      }
    } else {
      minLength = DEFAULT_LENGTH;
    }

    String maxLengthParam = resource.getString("passwordGenerator.maxLength");
    if (StringUtil.isDefined(maxLengthParam)) {
      try {
        maxLength = Integer.parseInt(maxLengthParam);
      } catch (NumberFormatException nfe) {
        maxLength = DEFAULT_LENGTH;
      }
    } else {
      maxLength = DEFAULT_LENGTH;
    }

    if (minLength > maxLength) {
      minLength = DEFAULT_LENGTH;
      maxLength = DEFAULT_LENGTH;
    }

    String specialCharsParam = resource.getString("passwordGenerator.specialChars");
    if (StringUtil.isDefined(specialCharsParam)) {
      specialCharsParam = specialCharsParam.trim();
      specialChars = new char[specialCharsParam.length()];
      for (int i = 0; i < specialCharsParam.length(); i++) {
        specialChars[i] = specialCharsParam.charAt(i);
      }
    } else {
      specialChars = DEFAULT_SPECIAL_CHAR;
    }

    atLeastOneUpperCaseChar = "true".equals(
        resource.getString("passwordGenerator.atLeastOneUpperCaseChar"));
    atLeastOneLowerCaseChar = "true".equals(
        resource.getString("passwordGenerator.atLeastOneLowerCaseChar"));
    atLeastOneNumberChar = "true".equals(
        resource.getString("passwordGenerator.atLeastOneNumberChar"));
    atLeastOneSpecialChar = "true".equals(
        resource.getString("passwordGenerator.atLeastOneSpecialChar"));
  }

  public String random() {
    ArrayList<Integer> indexes = new ArrayList<Integer>();
    int length = (maxLength > minLength
        ? minLength + random(maxLength - minLength + 1) : minLength);
    for (int i = 0; i < length; i++) {
      indexes.add(new Integer(i));
    }
    char[] result = new char[length];
    int currentIndex;
    if (atLeastOneUpperCaseChar) {
      currentIndex = random(indexes.size());
      result[indexes.get(currentIndex).intValue()] = randomUpperCaseChar();
      indexes.remove(currentIndex);
    }
    if (atLeastOneLowerCaseChar) {
      currentIndex = random(indexes.size());
      result[indexes.get(currentIndex).intValue()] = randomLowerCaseChar();
      indexes.remove(currentIndex);
    }
    if (atLeastOneNumberChar) {
      currentIndex = random(indexes.size());
      result[indexes.get(currentIndex).intValue()] = randomNumberChar();
      indexes.remove(currentIndex);
    }
    if (atLeastOneSpecialChar) {
      currentIndex = random(indexes.size());
      result[indexes.get(currentIndex).intValue()] = randomSpecialChar();
      indexes.remove(currentIndex);
    }
    for (Integer indexe : indexes) {
      switch (random(4)) {
        case 0:
          result[indexe.intValue()] = randomUpperCaseChar();
          break;
        case 1:
          result[indexe.intValue()] = randomLowerCaseChar();
          break;
        case 2:
          result[indexe.intValue()] = randomNumberChar();
          break;
        case 3:
          result[indexe.intValue()] = randomSpecialChar();
          break;
      }
    }
    StringBuffer sb = new StringBuffer(length);
    for (int i = 0; i < length; i++) {
      sb.append(result[i]);
    }
    return sb.toString();
  }

  private char randomUpperCaseChar() {
    return (char) (65 + random(26));
  }

  private char randomLowerCaseChar() {
    return (char) (97 + random(26));
  }

  private char randomNumberChar() {
    return (char) (48 + random(10));
  }

  private char randomSpecialChar() {
    return specialChars[random(specialChars.length)];
  }

  private int random(int maxValue) {
    return (int) (maxValue * Math.random());
  }

}