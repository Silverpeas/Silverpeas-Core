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

import org.silverpeas.core.security.authentication.password.PasswordBundle;
import org.silverpeas.core.security.authentication.password.rule.PasswordRule;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;

import java.util.ArrayList;
import java.util.Collection;

/**
 * User: Yohann Chastagnier
 * Date: 15/01/13
 */
public class PasswordCheck {
  protected static SettingBundle settings =
      ResourceLocator.getSettingBundle("org.silverpeas.password.settings.password");

  private final Collection<PasswordRule> requiredRulesInError = new ArrayList<>();
  private final Collection<PasswordRule> combinedRules;
  private final Collection<PasswordRule> combinedRulesInError = new ArrayList<>();
  private final int nbMatchingCombinedRules;
  private final int nbCombinedErrorsAuthorized;
  private String id;

  /**
   * Default hidden constructor
   */
  PasswordCheck(Collection<PasswordRule> combinedRules, int nbMatchingCombinedRules) {
    this.combinedRules = combinedRules;
    this.nbMatchingCombinedRules = nbMatchingCombinedRules;
    this.nbCombinedErrorsAuthorized = combinedRules.size() - nbMatchingCombinedRules;
  }

  void addRequiredError(PasswordRule rule) {
    requiredRulesInError.add(rule);
  }

  void addCombinedError(PasswordRule rule) {
    combinedRulesInError.add(rule);
  }

  void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  /**
   * Gets required rules in error.
   * @return a collection of failed required rules
   */
  public Collection<PasswordRule> getRequiredRulesInError() {
    return requiredRulesInError;
  }

  /**
   * Gets combined rules in error.
   * @return a collection of failed combined rules
   */
  public Collection<PasswordRule> getCombinedRulesInError() {
    return combinedRulesInError;
  }

  /**
   * Indicated if the checked password is correct.
   * @return true if the password satisfied all the password rules. False otherwise.
   */
  public boolean isCorrect() {
    return requiredRulesInError.isEmpty() && isRuleCombinationRespected();
  }

  /**
   * Indicated if the combination of rules is respected.
   * @return true if the combined rules has been satisfied by the password. False otherwise.
   */
  public boolean isRuleCombinationRespected() {
    return combinedRulesInError.isEmpty() ||
        combinedRulesInError.size() <= nbCombinedErrorsAuthorized;
  }

  /**
   * Gets a formatted error message according to the given language
   * @param language the ISO 639-1 code of a language in which the message has to be written.
   * @return a l10n message about the error.
   */
  public String getFormattedErrorMessage(String language) {
    StringBuilder errorMessage = new StringBuilder();
    if (!requiredRulesInError.isEmpty()) {
      errorMessage.append(getString("password.checking.error.message", language,
          getFormattedPasswordListMessage(requiredRulesInError, language)));
    }
    if (!combinedRulesInError.isEmpty()) {
      if (errorMessage.length() > 0) {
        errorMessage.append(". ");
      }
      errorMessage.append(getString("password.checking.combined.error.message", language,
          String.valueOf(nbMatchingCombinedRules),
          getFormattedPasswordListMessage(combinedRules, language)));
    }
    return errorMessage.toString();
  }

  private String getFormattedPasswordListMessage(Collection<PasswordRule> rules, String language) {
    StringBuilder formattedMessage = new StringBuilder();
    for (PasswordRule rule : rules) {
      if (formattedMessage.length() > 0) {
        formattedMessage.append(", ");
      }
      formattedMessage.append(rule.getDescription(language));
    }
    return formattedMessage.toString();
  }

  protected String getString(final String key, final String language, final String... params) {
    PasswordBundle bundle = new PasswordBundle(language);
    return bundle.getString(key, params);
  }
}
