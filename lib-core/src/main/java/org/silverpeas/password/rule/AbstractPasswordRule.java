/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.password.rule;

import com.stratelia.webactiv.util.ResourceLocator;
import org.silverpeas.password.constant.PasswordRuleType;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.silverpeas.util.StringUtil.isDefined;

/**
 * User: Yohann Chastagnier
 * Date: 07/01/13
 */
public abstract class AbstractPasswordRule implements PasswordRule {
  protected static final int DEFAULT_LENGTH = 8;
  protected static ResourceLocator settings =
      new ResourceLocator("org.silverpeas.password.settings.password", "");
  private static final Map<String, ResourceLocator> multilang = new HashMap<String, ResourceLocator>();

  private PasswordRuleType passwordRuleType;

  protected AbstractPasswordRule(final PasswordRuleType passwordRuleType) {
    this.passwordRuleType = passwordRuleType;
  }

  @Override
  public PasswordRuleType getType() {
    return passwordRuleType;
  }

  @Override
  public String getDescription(final String language) {
    final Object value = getValue();
    return getString(getType().getBundleKey(), language,
        (value instanceof Boolean) ? null : String.valueOf(value));
  }

  @Override
  public boolean isRequired() {
    final Object value = getValue();
    if (value instanceof String) {
      return isDefined((String) value);
    } else if (value instanceof Boolean) {
      return (Boolean) value;
    } else if (value instanceof Number) {
      return ((Number) value).doubleValue() > 0;
    }
    return value != null;
  }

  @Override
  public boolean isCombined() {
    return false;
  }

  /**
   * Returns a random integer value included between 0 and (given maxValue - 1).
   * @return
   */
  protected int random(int maxValue) {
    return (int) (maxValue * Math.random());
  }

  /**
   * Gets a string message according to the given language.
   * @param key
   * @param language
   * @param params
   * @return
   */
  protected String getString(final String key, final String language, final String... params) {
    ResourceLocator messages = multilang.get(language);
    if (messages == null) {
      synchronized (multilang) {
        messages =
            new ResourceLocator("org.silverpeas.password.multilang.passwordBundle", language);
        multilang.put(language, messages);
      }
    }
    return (params != null && params.length > 0) ? messages.getStringWithParams(key, params) :
        messages.getString(key, "");
  }

  /**
   * Gets an integer from settings
   * @param key
   * @param defaultValue
   * @return
   */
  protected Integer getIntegerFromSettings(final String key, final Integer defaultValue) {
    Integer value = 0;
    try {
      value = settings.getInteger(key, 0);
    } catch (NumberFormatException nfe) {
      // Nothing to do
    }
    return value >= 0 ? value : 0;
  }

  /**
   * Counting regexpr occurences in a String
   * @param text
   * @param regex
   * @return
   */
  protected int countRegexOccur(String text, String regex) {
    Matcher matcher = Pattern.compile(regex).matcher(text);
    int occur = 0;
    while (matcher.find()) {
      occur++;
    }
    return occur;
  }
}
