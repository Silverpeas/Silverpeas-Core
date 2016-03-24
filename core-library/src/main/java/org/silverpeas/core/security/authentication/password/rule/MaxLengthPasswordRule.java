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
package org.silverpeas.core.security.authentication.password.rule;

import org.silverpeas.core.security.authentication.password.constant.PasswordRuleType;

import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * Maximum length of a password : 8 by default.
 * User: Yohann Chastagnier
 * Date: 07/01/13
 */
public class MaxLengthPasswordRule extends AbstractPasswordRule {

  private boolean required = true;
  private Integer value;

  /**
   * Default constructor.
   */
  public MaxLengthPasswordRule() {
    super(PasswordRuleType.MAX_LENGTH);
    required = settings.getBoolean(getType().getSettingKey(), false);
    value = settings.getInteger(getType().getSettingKey() + ".value", DEFAULT_LENGTH);
    if (settings.getInteger(PasswordRuleType.MIN_LENGTH.getSettingKey(), DEFAULT_LENGTH) > value) {
      value = DEFAULT_LENGTH;
    }
  }

  @Override
  public boolean isRequired() {
    return required;
  }

  @Override
  public Integer getValue() {
    return value;
  }

  @Override
  public boolean check(final String password) {
    return isDefined(password) && password.length() <= getValue();
  }

  @Override
  public String random() {
    // This rule is not able to generate a random character.
    return "";
  }
}
