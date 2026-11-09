/*
 * Copyright (C) 2000 - 2026 Silverpeas
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

import jakarta.annotation.PostConstruct;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.security.authentication.password.constant.PasswordRuleType;
import org.silverpeas.core.security.authentication.password.rule.*;
import org.silverpeas.core.template.SilverpeasTemplates;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;
import org.silverpeas.kernel.cache.model.ExternalCache;
import org.silverpeas.kernel.util.StringUtil;

import java.security.SecureRandom;
import java.util.*;

/**
 * Service dedicated to check the rules on passwords are satisfied.
 * @author  Yohann Chastagnier
 */
@Service
public class DefaultPasswordRulesService implements PasswordRulesService {
  protected static SettingBundle settings =
      ResourceLocator.getSettingBundle("org.silverpeas.password.settings.password");
  protected static int nbMatchingCombinedRules =
      settings.getInteger("password.combination.nbMatchingRules", 0);

  /* All server password rules */
  private final Map<PasswordRuleType, PasswordRule> allPasswordRules =
      LinkedHashMap.newLinkedHashMap(PasswordRuleType.values().length);

  /* All required server password rules */
  private final Map<PasswordRuleType, PasswordRule> requiredPasswordRules =
      LinkedHashMap.newLinkedHashMap(PasswordRuleType.values().length);

  /* All combined server password rules */
  private final Map<PasswordRuleType, PasswordRule> combinedPasswordRules =
      LinkedHashMap.newLinkedHashMap(PasswordRuleType.values().length);

  private final SecureRandom random = new SecureRandom();

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
    final int minLength = Math.max(1, getRule(PasswordRuleType.MIN_LENGTH).<Integer>getValue());
    final PasswordRule maximum = getRule(PasswordRuleType.MAX_LENGTH);
    final int maxLength = maximum.isRequired() ? maximum.<Integer>getValue() : Integer.MAX_VALUE;
    if (maxLength < minLength) {
      throw new IllegalStateException("Inconsistent password length constraints");
    }
    for (int attempt = 0; attempt < 256; attempt++) {
      final String candidate = generateCandidate(minLength, maxLength);
      if (candidate != null && satisfiesPolicy(candidate)) {
        return candidate;
      }
    }
    throw new IllegalStateException("Unable to generate a password satisfying the configured policy");
  }

  private String generateCandidate(int minLength, int maxLength) {
    final Set<PasswordRule> selected = new LinkedHashSet<>(getRequiredRules());
    final List<PasswordRule> combined = new ArrayList<>(getCombinedRules());
    Collections.shuffle(combined, random);
    long alreadySelected = combined.stream().filter(selected::contains).count();
    combined.stream().filter(rule -> !selected.contains(rule))
        .limit(Math.max(0L, nbMatchingCombinedRules - alreadySelected)).forEach(selected::add);
    final StringBuilder candidate = new StringBuilder();
    selected.forEach(rule -> candidate.append(rule.random()));
    if (candidate.length() > maxLength) {
      return null;
    }
    final String alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789" +
        getRule(PasswordRuleType.AT_LEAST_X_SPECIAL_CHAR).<String>getValue();
    final int length = Math.min(maxLength, Math.max(16, Math.max(minLength, candidate.length())));
    while (candidate.length() < length) {
      candidate.append(alphabet.charAt(random.nextInt(alphabet.length())));
    }
    for (int i = candidate.length() - 1; i > 0; i--) {
      int j = random.nextInt(i + 1);
      char value = candidate.charAt(i);
      candidate.setCharAt(i, candidate.charAt(j));
      candidate.setCharAt(j, value);
    }
    return candidate.toString();
  }

  private boolean satisfiesPolicy(String password) {
    if (!getRequiredRules().stream().allMatch(rule -> rule.check(password))) {
      return false;
    }
    final Collection<PasswordRule> combined = getCombinedRules();
    final long matching = combined.stream().filter(rule -> rule.check(password)).count();
    return matching == combined.size() || matching >= nbMatchingCombinedRules;
  }

  @Override
  public String getExtraRuleMessage(final String language) {
    return SilverpeasTemplates.createSilverpeasTemplateOnCore("password").
        applyFileTemplate("extraRules_" + language).trim();
  }
}
