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
package org.silverpeas.core.security.authentication.password.service;

import org.silverpeas.core.security.authentication.password.constant.PasswordRuleType;
import org.silverpeas.core.security.authentication.password.rule.PasswordRule;

import java.util.Collection;

/**
 * User: Yohann Chastagnier
 * Date: 07/01/13
 */
public interface PasswordRulesService {

  /**
   * Gets a server password rule from its type.
   * @return
   */
  PasswordRule getRule(PasswordRuleType passwordRuleType);

  /**
   * Gets server password rules.
   * @return
   */
  Collection<PasswordRule> getRules();

  /**
   * Gets server required password rules.
   * @return
   */
  Collection<PasswordRule> getRequiredRules();

  /**
   * Gets server combined password rules.
   * @return
   */
  Collection<PasswordRule> getCombinedRules();

  /**
   * Checks server required and combined password rule on the given password.
   * @param password
   * @return Password rules in error if any.
   */
  PasswordCheck check(String password);

  /**
   * Generates a random password from existing rules.
   * @return
   */
  String generate();

  /**
   * Gets additional rule message.
   * All rules explicited in this message are not verifiable within Silverpeas services.
   * @param language
   * @return
   */
  String getExtraRuleMessage(String language);
}
