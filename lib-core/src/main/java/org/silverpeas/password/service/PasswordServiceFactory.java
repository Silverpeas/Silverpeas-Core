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
package org.silverpeas.password.service;

import org.silverpeas.password.constant.PasswordRuleType;

import javax.inject.Inject;

/**
 * @author Yohann Chastagnier
 */
public class PasswordServiceFactory {

  private static final PasswordServiceFactory instance = new PasswordServiceFactory();

  @Inject
  private PasswordService passwordService;

  /**
   * @return the passwordService
   */
  public static PasswordService getPasswordService() {
    return getInstance().passwordService;
  }

  /**
   * Gets the minimum length of passwords.
   * @return
   */
  public static int getPasswordMinimumLength() {
    return (Integer) getPasswordService().getRule(PasswordRuleType.MIN_LENGTH).getValue();
  }

  /**
   * Gets an instance of this PasswordServiceFactory class.
   * @return a PasswordServiceFactory instance.
   */
  private static PasswordServiceFactory getInstance() {
    return instance;
  }
}
