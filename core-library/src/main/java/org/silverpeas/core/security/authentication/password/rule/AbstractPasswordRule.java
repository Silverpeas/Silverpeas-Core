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
package org.silverpeas.core.security.authentication.password.rule;

import org.silverpeas.core.security.authentication.password.PasswordBundle;
import org.silverpeas.core.security.authentication.password.constant.PasswordRuleType;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.silverpeas.kernel.util.StringUtil.isDefined;

/**
 * User: Yohann Chastagnier
 * Date: 07/01/13
 */
public abstract class AbstractPasswordRule implements PasswordRule {
  protected static final int DEFAULT_LENGTH = 8;
  protected static SettingBundle settings =
      ResourceLocator.getSettingBundle("org.silverpeas.password.settings.password");
  private final PasswordRuleType passwordRuleType;
  private final Random random = new Random();

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
    final String param = value instanceof Boolean ? null : String.valueOf(value);
    return getString(getType().getBundleKey(), language, param);
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
   * @return a random integer value.
   */
  protected int random(int maxValue) {
    return (int) (maxValue * random.nextDouble());
  }

  /**
   * Gets a string message according to the given language.
   * @param key the key of the message template to get.
   * @param language the language in which the message has to be written.
   * @param params parameters to pass to the message.
   * @return the message generated from a template and the parameters.
   */
  protected String getString(final String key, final String language, final String... params) {
    PasswordBundle bundle = new PasswordBundle(language);
    return bundle.getString(key, params);
  }

  /**
   * Gets an integer from settings
   * @param key the key of the property.
   * @param defaultValue the default value if the property isn't defined.
   * @return the property value or 0 if the value got from the settings is negative.
   */
  protected Integer getIntegerFromSettings(final String key, final Integer defaultValue) {
    Integer value = defaultValue;
    try {
      value = settings.getInteger(key, defaultValue);
    } catch (NumberFormatException nfe) {
      // Nothing to do
    }
    return value >= 0 ? value : 0;
  }

  /**
   * Counting the number of occurrences matching the specified regexp in the given text.
   * @param text a text.
   * @param regex the regexp.
   * @return the number of matching patterns found in the text with the regexp.
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
