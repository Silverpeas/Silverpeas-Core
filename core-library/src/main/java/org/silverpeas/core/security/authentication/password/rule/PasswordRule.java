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

/**
 * Interface that defines methods of a password rule.
 * User: Yohann Chastagnier
 * Date: 07/01/13
 */
public interface PasswordRule {

  /**
   * Gets the type password rule.
   * @return
   */
  PasswordRuleType getType();

  /**
   * Indicates if the rule is required.
   * @return
   */
  boolean isRequired();

  /**
   * Indicates if the rule is combined one.
   * @return
   */
  boolean isCombined();

  /**
   * Gets the value of the parameter defined in settings for the rule.
   * @param <T>
   * @return
   */
  <T> T getValue();

  /**
   * Generates a random password part according to the nature of the rule.
   * @return the random part of a password
   */
  String random();

  /**
   * Checks the given password.
   * @return
   */
  boolean check(String password);

  /**
   * Gets the description of the rule according to a language.
   * @param language
   * @return
   */
  String getDescription(String language);
}
