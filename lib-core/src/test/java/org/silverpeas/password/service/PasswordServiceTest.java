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

import com.stratelia.webactiv.util.ResourceLocator;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.password.constant.PasswordRuleType;
import org.silverpeas.password.rule.AbstractPasswordRule;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * User: Yohann Chastagnier
 * Date: 08/01/13
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring-password.xml"})
public class PasswordServiceTest {
  private PasswordServiceTestContext context = new PasswordServiceTestContext();

  @Inject
  private PasswordService passwordService;

  @Test
  public void testGetRule() {
    final Set<Class<?>> rules = new HashSet<Class<?>>();
    for (PasswordRuleType ruleType : PasswordRuleType.values()) {
      rules.add(passwordService.getRule(ruleType).getClass());
    }
    assertThat(rules.size(), is(PasswordRuleType.values().length));
  }

  @Test
  public void testGetRules() {
    assertThat(passwordService.getRules().size(), is(PasswordRuleType.values().length));
  }

  @Test
  public void testGetRequiredRules() {
    assertThat(passwordService.getRequiredRules().size(), is(PasswordRuleType.values().length));
  }

  @Test
  public void testGetRulesNoneRequiredInSettings() {
    assertThat(passwordService.getRules().size(), is(PasswordRuleType.values().length));
  }

  @Test
  public void testGetRequiredRulesNoneRequiredInSettings() {
    setNotDefinedSettings();
    // Max length and min length are always required.
    assertThat(passwordService.getRequiredRules().size(), is(2));
  }

  @Test
  public void testCheck() {
    assertThat(passwordService.check("aA0$1234").size(), is(0));

    // Min length is not validated
    assertThat(passwordService.check("aA0$123").iterator().next().getType(),
        is(PasswordRuleType.MIN_LENGTH));
    // Max length is not validated
    assertThat(passwordService.check("aA0$1234123456789").iterator().next().getType(),
        is(PasswordRuleType.MAX_LENGTH));
    // Blank forbidden is not validated
    assertThat(passwordService.check("aa0 $1234").iterator().next().getType(),
        is(PasswordRuleType.BLANK_FORBIDDEN));
    // At least one uppercase is not validated
    assertThat(passwordService.check("aa0$1234").iterator().next().getType(),
        is(PasswordRuleType.AT_LEAST_ONE_UPPERCASE));
    // At least one lowercase is not validated
    assertThat(passwordService.check("AA0$1234").iterator().next().getType(),
        is(PasswordRuleType.AT_LEAST_ONE_LOWERCASE));
    // At least one special char is not validated
    assertThat(passwordService.check("aAb01234").iterator().next().getType(),
        is(PasswordRuleType.AT_LEAST_ONE_SPECIAL_CHAR));
    // At least one digit is not validated
    assertThat(passwordService.check("aAb$cdef").iterator().next().getType(),
        is(PasswordRuleType.AT_LEAST_ONE_DIGIT));

    // Several errors :
    // - Min length is not validated
    // - At least one uppercase is not validated
    // - At least one special char is not validated
    assertThat(passwordService.check("ab0c123").size(), is(3));
  }

  @Test
  public void testGenerate() {
    int nbGenerations = 1000;
    final Set<String> generatedPasswords = new HashSet<String>(nbGenerations);
    for (int i = 0; i < nbGenerations; i++) {
      generatedPasswords.add(passwordService.generate());
    }
    assertThat("Identical passwords have been generated", generatedPasswords.size(),
        is(nbGenerations));
    for (String password : generatedPasswords) {
      assertThat("At least one generated password is not valid",
          passwordService.check(password).size(), is(0));
    }
  }

  @After
  public void afterTest() {
    context.settings("org.silverpeas.password.settings.password");
  }

  protected void setNotDefinedSettings() {
    context.settings("org.silverpeas.password.settings.passwordNotDefined");
  }

  /**
   * This class exists to change settings during service tests.
   */
  private class PasswordServiceTestContext extends AbstractPasswordRule {

    public PasswordServiceTestContext() {
      super(PasswordRuleType.MIN_LENGTH);
    }

    @Override
    public <T> T getValue() {
      return null;
    }

    @Override
    public String random() {
      return null;
    }

    @Override
    public boolean check(final String password) {
      return false;
    }

    public void settings(String resourceLocator) {
      settings = new ResourceLocator(resourceLocator, "");
      ((DefaultPasswordService) passwordService).loadRules();
    }
  }
}
