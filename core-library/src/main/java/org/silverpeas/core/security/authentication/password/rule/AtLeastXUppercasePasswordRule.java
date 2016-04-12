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
 * At least X uppercase in password.
 * User: Yohann Chastagnier
 * Date: 07/01/13
 */
public class AtLeastXUppercasePasswordRule extends AbstractPasswordRule {

  private Integer value;
  private boolean required;
  private boolean combined;

  /**
   * Default constructor.
   */
  public AtLeastXUppercasePasswordRule() {
    super(PasswordRuleType.AT_LEAST_X_UPPERCASE);
    required = settings.getBoolean(getType().getSettingKey(), false);
    value = getIntegerFromSettings(getType().getSettingKey() + ".X", 0);
    combined = settings.getBoolean(getType().getSettingKey() + ".combined", false);
    if (value == 0) {
      required = false;
      combined = false;
    }
  }

  @Override
  public boolean isRequired() {
    return required;
  }

  @Override
  public boolean isCombined() {
    return combined;
  }

  @Override
  public Integer getValue() {
    return value;
  }

  @Override
  public boolean check(final String password) {
    return isDefined(password) && countRegexOccur(password, "[A-Z]") >= value;
  }

  @Override
  public String random() {
    final StringBuilder random = new StringBuilder();
    for (int i = 0; i < value; i++) {
      // A - Z : 65 - 90
      random.append(((char) (65 + random(26))));
    }
    return random.toString();
  }
}
