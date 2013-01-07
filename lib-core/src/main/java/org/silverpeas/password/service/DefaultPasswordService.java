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

import com.silverpeas.annotation.Service;
import com.silverpeas.util.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.silverpeas.password.constant.PasswordRuleType;
import org.silverpeas.password.rule.AtLeastOneDigitPasswordRule;
import org.silverpeas.password.rule.AtLeastOneLowercasePasswordRule;
import org.silverpeas.password.rule.AtLeastOneSpecialCharPasswordRule;
import org.silverpeas.password.rule.AtLeastOneUppercasePasswordRule;
import org.silverpeas.password.rule.BlankForbiddenPasswordRule;
import org.silverpeas.password.rule.MaxLengthPasswordRule;
import org.silverpeas.password.rule.MinLengthPasswordRule;
import org.silverpeas.password.rule.PasswordRule;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * User: Yohann Chastagnier
 * Date: 07/01/13
 */
@Service
public class DefaultPasswordService implements PasswordService {

  /* All server password rules */
  private Map<PasswordRuleType, PasswordRule> allPasswordRules =
      new LinkedHashMap<PasswordRuleType, PasswordRule>(PasswordRuleType.values().length);

  /* All required server password rules */
  private Map<PasswordRuleType, PasswordRule> requiredPasswordRules =
      new LinkedHashMap<PasswordRuleType, PasswordRule>(PasswordRuleType.values().length);

  /**
   * Loading just after the server is started all activated server password rules.
   */
  @PostConstruct
  protected void loadRules() {
    allPasswordRules.clear();
    requiredPasswordRules.clear();
    for (PasswordRule rule : new PasswordRule[]{new MinLengthPasswordRule(),
        new MaxLengthPasswordRule(), new BlankForbiddenPasswordRule(),
        new AtLeastOneUppercasePasswordRule(), new AtLeastOneLowercasePasswordRule(),
        new AtLeastOneDigitPasswordRule(), new AtLeastOneSpecialCharPasswordRule()}) {
      allPasswordRules.put(rule.getType(), rule);
      if (rule.isRequired()) {
        requiredPasswordRules.put(rule.getType(), rule);
      }
    }
  }

  @Override
  public PasswordRule getRule(final PasswordRuleType passwordRuleType) {
    return allPasswordRules.get(passwordRuleType);
  }

  @Override
  public Collection<PasswordRule> getRules() {
    return new ArrayList<PasswordRule>(allPasswordRules.values());
  }

  @Override
  public Collection<PasswordRule> getRequiredRules() {
    return new ArrayList<PasswordRule>(requiredPasswordRules.values());
  }

  @Override
  public Collection<PasswordRule> check(final String password) {
    final Collection<PasswordRule> notVerifiedRules = new ArrayList<PasswordRule>();
    for (final PasswordRule rule : getRequiredRules()) {
      if (!rule.check(password)) {
        notVerifiedRules.add(rule);
      }
    }
    return notVerifiedRules;
  }

  @Override
  public String generate() {

    // Context
    final List<PasswordRule> rules = new ArrayList<PasswordRule>(getRules());
    final List<PasswordRule> requiredRules = new ArrayList<PasswordRule>(getRequiredRules());
    int minLength = (Integer) getRule(PasswordRuleType.MIN_LENGTH).getValue();
    int maxLength = (Integer) getRule(PasswordRuleType.MAX_LENGTH).getValue();

    // Length of the random password
    int requiredPasswordLength = minLength + random(maxLength - minLength + 1);

    // Random parts of the password
    int currentPasswordLength = 0;
    final List<String> randomPasswordParts = new ArrayList<String>();
    PasswordRule currentRule;
    String currentRandomPasswordPart;
    while (currentPasswordLength < requiredPasswordLength) {

      // Gets a password rule
      if (!requiredRules.isEmpty()) {
        currentRule = requiredRules.remove(random(requiredRules.size()));
      } else {
        currentRule = rules.get(random(rules.size()));
      }

      // Generate and store a random part of the password
      currentRandomPasswordPart = currentRule.random();
      if (StringUtil.isDefined(currentRandomPasswordPart)) {
        randomPasswordParts.add(currentRandomPasswordPart);
      }

      // Add the current length of the random password part to the current password length
      currentPasswordLength += currentRandomPasswordPart.length();
    }

    // The generated random password
    Collections.shuffle(randomPasswordParts);
    return StringUtils.join(randomPasswordParts, "");
  }

  /**
   * Returns a random integer value included between 0 and (given maxValue - 1).
   * @return
   */
  private int random(int maxValue) {
    return (int) (maxValue * Math.random());
  }
}
