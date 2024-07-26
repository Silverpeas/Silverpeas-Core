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

import org.apache.commons.lang3.StringUtils;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.security.authentication.password.constant.PasswordRuleType;
import org.silverpeas.core.security.authentication.password.rule.*;
import org.silverpeas.core.template.SilverpeasTemplateFactory;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;
import org.silverpeas.kernel.cache.model.ExternalCache;
import org.silverpeas.kernel.util.StringUtil;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.security.SecureRandom;
import java.util.*;

/**
 * Service dedicated to check the rules on passwords are satisfied.
 * @author  Yohann Chastagnier
 */
@Service
@Singleton
public class DefaultPasswordRulesService implements PasswordRulesService {
  protected static SettingBundle settings =
      ResourceLocator.getSettingBundle("org.silverpeas.password.settings.password");
  protected static int nbMatchingCombinedRules =
      settings.getInteger("password.combination.nbMatchingRules", 0);

  /* All server password rules */
  private final Map<PasswordRuleType, PasswordRule> allPasswordRules =
      new LinkedHashMap<>(PasswordRuleType.values().length);

  /* All required server password rules */
  private final Map<PasswordRuleType, PasswordRule> requiredPasswordRules =
      new LinkedHashMap<>(PasswordRuleType.values().length);

  /* All combined server password rules */
  private final Map<PasswordRuleType, PasswordRule> combinedPasswordRules =
      new LinkedHashMap<>(PasswordRuleType.values().length);

  private final Random random = new SecureRandom();

  /**
   * Loading just after the server is started all activated server password rules.
   */
  @PostConstruct
  protected void loadRules() {
    allPasswordRules.clear();
    requiredPasswordRules.clear();
    combinedPasswordRules.clear();
    for (PasswordRule rule : new PasswordRule[]{new MinLengthPasswordRule(),
        new MaxLengthPasswordRule(), new BlankForbiddenPasswordRule(),
        new SequentialForbiddenPasswordRule(), new AtLeastXUppercasePasswordRule(),
        new AtLeastXLowercasePasswordRule(), new AtLeastXDigitPasswordRule(),
        new AtLeastXSpecialCharPasswordRule()}) {
      allPasswordRules.put(rule.getType(), rule);
      if (rule.isRequired()) {
        requiredPasswordRules.put(rule.getType(), rule);
      }
      if (rule.isCombined()) {
        combinedPasswordRules.put(rule.getType(), rule);
      }
    }
  }

  @Override
  public PasswordRule getRule(final PasswordRuleType passwordRuleType) {
    return allPasswordRules.get(passwordRuleType);
  }

  @Override
  public Collection<PasswordRule> getRules() {
    return new ArrayList<>(allPasswordRules.values());
  }

  @Override
  public Collection<PasswordRule> getRequiredRules() {
    return new ArrayList<>(requiredPasswordRules.values());
  }

  @Override
  public Collection<PasswordRule> getCombinedRules() {
    return new ArrayList<>(combinedPasswordRules.values());
  }

  @Override
  public PasswordCheck check(final String password) {
    PasswordCheck passwordCheck = new PasswordCheck(getCombinedRules(), nbMatchingCombinedRules);
    for (final PasswordRule rule : getRequiredRules()) {
      if (!rule.check(password)) {
        passwordCheck.addRequiredError(rule);
      }
    }
    for (final PasswordRule rule : getCombinedRules()) {
      if (!rule.check(password)) {
        passwordCheck.addCombinedError(rule);
      }
    }
    if (passwordCheck.isCorrect()) {
      ExternalCache cache = CacheAccessorProvider.getApplicationCacheAccessor().getCache();
      // put the password in the application cache for 1mn.
      String id = cache.add(password, 60, 0);
      passwordCheck.setId(id);
    }
    return passwordCheck;
  }

  @Override
  public boolean isChecked(String checkId, String password) {
    ExternalCache cache = CacheAccessorProvider.getApplicationCacheAccessor().getCache();
    return StringUtil.isDefined(checkId) && StringUtil.isDefined(password) &&
        password.equals(cache.get(checkId));
  }

  @Override
  public String generate() {
    long start = System.currentTimeMillis();
    PasswordRule sequentialForbidden = getRule(PasswordRuleType.SEQUENTIAL_FORBIDDEN);
    String generatedPassword = generate(sequentialForbidden);
    if (sequentialForbidden.isRequired()) {
      // Trying during 5 seconds to generate a not sequential password before returning the result
      while ((System.currentTimeMillis() - start) <= 5000) {
        if (sequentialForbidden.check(generatedPassword)) {
          break;
        }
        generatedPassword = generate(sequentialForbidden);
      }
    }
    return generatedPassword;
  }

  private String generate(PasswordRule sequentialForbidden) {

    // Context
    final List<PasswordRule> rules = new ArrayList<>(getRules());
    final List<PasswordRule> requiredRules = new ArrayList<>(getRequiredRules());
    final List<PasswordRule> combinedRules = new ArrayList<>(getCombinedRules());
    int minLength = getRule(PasswordRuleType.MIN_LENGTH).getValue();
    int maxLength = getRule(PasswordRuleType.MAX_LENGTH).getValue();

    // Length of the random password
    int requiredPasswordLength = minLength + random(maxLength - minLength + 1);

    // Random parts of the password
    int currentPasswordLength = 0;
    List<String> randomPasswordParts = new ArrayList<>();
    PasswordRule currentRule;
    String currentRandomPasswordPart;
    Set<PasswordRule> combinedRulesPerformed = new HashSet<>();
    while (currentPasswordLength < requiredPasswordLength) {

      // Gets a password rule
      currentRule = getPasswordRule(requiredRules, combinedRules, combinedRulesPerformed, rules);

      // Storing combined rule performed
      if (currentRule.isCombined()) {
        combinedRulesPerformed.add(currentRule);
      }

      // Generate and store a random part of the password
      currentRandomPasswordPart = currentRule.random();
      if (StringUtil.isDefined(currentRandomPasswordPart)) {
        while ((currentPasswordLength + currentRandomPasswordPart.length()) > maxLength) {
          currentRandomPasswordPart =
              currentRandomPasswordPart.substring(0, (currentRandomPasswordPart.length() - 1));
        }
        randomPasswordParts.add(currentRandomPasswordPart);
      }

      // Add the current length of the random password part to the current password length
      currentPasswordLength += currentRandomPasswordPart.length();
    }

    // The generated random password
    // Trying 10 times to generate a not sequential password before returning the result
    Collections.shuffle(randomPasswordParts);
    String generatedPassword = StringUtils.join(randomPasswordParts, "");
    for (int i = 0; i < 10 && sequentialForbidden.isRequired(); i++) {
      if (sequentialForbidden.check(generatedPassword)) {
        break;
      }
      randomPasswordParts = Arrays.asList(generatedPassword.split(""));
      Collections.shuffle(randomPasswordParts);
      generatedPassword = StringUtils.join(randomPasswordParts, "");
    }
    return generatedPassword;
  }

  private PasswordRule getPasswordRule(List<PasswordRule> requiredRules,
      List<PasswordRule> combinedRules,
      Set<PasswordRule> combinedRulesPerformed,
      List<PasswordRule> rules) {
    PasswordRule currentRule;
    if (!requiredRules.isEmpty()) {
      currentRule = requiredRules.remove(random(requiredRules.size()));
    } else if (!combinedRules.isEmpty() &&
        combinedRulesPerformed.size() < nbMatchingCombinedRules) {
      currentRule = combinedRules.remove(random(combinedRules.size()));
    } else {
      currentRule = rules.get(random(rules.size()));
    }
    return currentRule;
  }

  /**
   * Returns a random integer value included between 0 and (given maxValue - 1).
   * @return a random integer.
   */
  private int random(int maxValue) {
    return random.nextInt(maxValue);
  }

  @Override
  public String getExtraRuleMessage(final String language) {
    return SilverpeasTemplateFactory.createSilverpeasTemplateOnCore("password").
        applyFileTemplate("extraRules_" + language).trim();
  }
}
