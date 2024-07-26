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
package org.silverpeas.core.security.authentication.password.service;

import org.silverpeas.core.security.authentication.password.constant.PasswordRuleType;
import org.silverpeas.core.security.authentication.password.rule.PasswordRule;

import java.util.Collection;

/**
 * A service to access password rules and to check a password satisfies the rules.
 * @author Yohann Chastagnier
 */
public interface PasswordRulesService {

  /**
   * Gets a server password rule by its type.
   * @return a password rule or null if no such rule of this type exists
   */
  PasswordRule getRule(PasswordRuleType passwordRuleType);

  /**
   * Gets all defined the password rules.
   * @return a collection of all the possible password rules
   */
  Collection<PasswordRule> getRules();

  /**
   * Gets all the required password rules.
   * @return a collection of all rules a password has to satisfy.
   */
  Collection<PasswordRule> getRequiredRules();

  /**
   * Gets the combined password rules.
   * @return a collection of combined password rules.
   */
  Collection<PasswordRule> getCombinedRules();

  /**
   * Checks the specified password satisfy both the required and the combined rules.
   * @param password the password to check
   * @return Password rules in error if any.
   */
  PasswordCheck check(String password);

  /**
   * Is the specified password has been successfully checked by the service?
   * @param checkId the unique identifier of a possible previous check.
   * @param password a password.
   * @return true if the given password has been checked by the given check process and the
   * checking result was successful. False otherwise.
   */
  boolean isChecked(String checkId, String password);

  /**
   * Generates a random password satisfying the existing rules.
   * @return a new random password.
   */
  String generate();

  /**
   * Gets additional rule message.
   * All rules explicated in this message are not verifiable within Silverpeas services.
   * @param language the ISO 639-1 code of the language in which is written the message.
   * @return the message about the extra rule.
   */
  String getExtraRuleMessage(String language);
}
