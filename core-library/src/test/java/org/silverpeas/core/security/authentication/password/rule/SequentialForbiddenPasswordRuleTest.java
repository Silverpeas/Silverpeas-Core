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
package org.silverpeas.core.security.authentication.password.rule;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.security.authentication.password.constant.PasswordRuleType;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * User: Yohann Chastagnier
 * Date: 08/01/13
 */
class SequentialForbiddenPasswordRuleTest
    extends AbstractPasswordRuleTest<SequentialForbiddenPasswordRule> {

  @Test
  void testCommons() {
    SequentialForbiddenPasswordRule rule = newRuleInstanceForTest();
    assertThat(rule.getType(), is(PasswordRuleType.SEQUENTIAL_FORBIDDEN));
    assertThat(rule.check("a1234 ;"), is(true));
    assertThat(rule.check("a1234;"), is(true));
    for (char c = 97; c < (97 + 26); c++) {
      assertThat(rule.check("0" + c + "2135 "), is(true));
    }
    for (char c = 65; c < (65 + 26); c++) {
      assertThat(rule.check("0" + c + "123456789"), is(true));
    }
  }

  @Test
  @Override
  public void testDefinedPropertyValues() {
    setDefinedSettings();
    SequentialForbiddenPasswordRule rule = newRuleInstanceForTest();
    assertThat(rule.getValue(), is(true));
    assertThat(rule.isRequired(), is(true));
    assertThat(rule.isCombined(), is(false));
    assertThat(rule.check("121"), is(true));
    assertThat(rule.check("aba"), is(true));
    assertThat(rule.check("aA"), is(true));
    assertThat(rule.check("11"), is(false));
    assertThat(rule.check("aa"), is(false));
    assertThat(rule.check("AA"), is(false));
    assertThat(rule.check("@@"), is(false));
  }

  @Disabled("Doesn't apply in this test class")
  @Override
  public void testDefinedMoreThanOnePropertyValues() {
  }

  @Disabled("Doesn't apply in this test class")
  @Override
  public void testCombinationDefinedMoreThanOnePropertyValues() {
  }

  @Test
  @Override
  public void testNotDefinedPropertyValues() {
    setNotDefinedSettings();
    SequentialForbiddenPasswordRule rule = newRuleInstanceForTest();
    assertThat(rule.getValue(), is(false));
    assertThat(rule.isRequired(), is(false));
    assertThat(rule.isCombined(), is(false));
  }

  @Test
  @Override
  public void testBadDefinedPropertyValues() {
    setBadDefinedSettings();
    SequentialForbiddenPasswordRule rule = newRuleInstanceForTest();
    assertThat(rule.getValue(), is(false));
    assertThat(rule.isRequired(), is(false));
    assertThat(rule.isCombined(), is(false));
  }

  @Test
  @Override
  public void testNotRequiredPropertyValues() {
    setNotRequiredSettings();
    SequentialForbiddenPasswordRule rule = newRuleInstanceForTest();
    assertThat(rule.getValue(), is(false));
    assertThat(rule.isRequired(), is(false));
    assertThat(rule.isCombined(), is(false));
  }
}
