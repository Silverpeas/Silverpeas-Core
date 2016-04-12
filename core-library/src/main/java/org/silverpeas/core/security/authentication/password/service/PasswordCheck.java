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

import org.silverpeas.core.security.authentication.password.rule.PasswordRule;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.MissingResourceException;

/**
 * User: Yohann Chastagnier
 * Date: 15/01/13
 */
public class PasswordCheck {
  protected static SettingBundle settings =
      ResourceLocator.getSettingBundle("org.silverpeas.password.settings.password");

  private Collection<PasswordRule> requiredRulesInError = new ArrayList<PasswordRule>();
  private Collection<PasswordRule> combinedRules = new ArrayList<PasswordRule>();
  private Collection<PasswordRule> combinedRulesInError = new ArrayList<PasswordRule>();
  private int nbMatchingCombinedRules;
  private int nbCombinedErrorsAuthorized;

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

  /**
   * Gets required rules in error.
   * @return
   */
  public Collection<PasswordRule> getRequiredRulesInError() {
    return requiredRulesInError;
  }

  /**
   * Gets combined rules in error.
   * @return
   */
  public Collection<PasswordRule> getCombinedRulesInError() {
    return combinedRulesInError;
  }

  /**
   * Indicated if the checked password is correct.
   * @return
   */
  public boolean isCorrect() {
    return requiredRulesInError.isEmpty() && isRuleCombinationRespected();
  }

  /**
   * Indicated if the combination of rules is respected.
   * @return
   */
  public boolean isRuleCombinationRespected() {
    return combinedRulesInError.isEmpty() ||
        combinedRulesInError.size() <= nbCombinedErrorsAuthorized;
  }

  /**
   * Gets a formatted error message according to the given language
   * @param language
   * @return
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

  /**
   * Gets a string message according to the given language.
   * @param key
   * @param language
   * @param params
   * @return
   */
  protected String getString(final String key, final String language, final String... params) {
    LocalizationBundle messages =
        ResourceLocator.getLocalizationBundle("org.silverpeas.password.multilang.passwordBundle",
            language);
    String translation;
    try {
      translation =
          (params != null && params.length > 0) ? messages.getStringWithParams(key, params) :
              messages.getString(key);
    } catch (MissingResourceException ex) {
      translation = "";
    }
    return translation;
  }
}
