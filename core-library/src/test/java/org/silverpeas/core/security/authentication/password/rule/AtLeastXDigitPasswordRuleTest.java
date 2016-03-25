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
package org.silverpeas.core.security.authentication.password.rule;

import org.junit.Test;
import org.silverpeas.core.security.authentication.password.constant.PasswordRuleType;

import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * User: Yohann Chastagnier
 * Date: 08/01/13
 */
public class AtLeastXDigitPasswordRuleTest
    extends AbstractPasswordRuleTest<AtLeastXDigitPasswordRule> {

  @Test
  public void testCommons() {
    AtLeastXDigitPasswordRule rule = newRuleInstanceForTest();
    assertThat(rule.getType(), is(PasswordRuleType.AT_LEAST_X_DIGIT));
    assertThat(rule.check("ajlkaslkj"), is(false));
    for (char c = 97; c < (97 + 26); c++) {
      assertThat(rule.check("a" + c + "zdzdz"), is(false));
    }
    for (char c = 65; c < (65 + 26); c++) {
      assertThat(rule.check("a" + c + "dzzd"), is(false));
    }
    for (int i = 0; i < 10; i++) {
      assertThat(rule.check("ajlkaslkj" + i), is(true));
    }
    for (int i = 0; i < NB_LOOP; i++) {
      assertThat(Pattern.compile("[a-z]+").matcher(rule.random()).find(), is(false));
      assertThat(Pattern.compile("[A-Z]+").matcher(rule.random()).find(), is(false));
      assertThat(Pattern.compile("[0-9]+").matcher(rule.random()).find(), is(true));
    }
  }

  @Test
  @Override
  public void testDefinedPropertyValues() {
    setDefinedSettings();
    AtLeastXDigitPasswordRule rule = newRuleInstanceForTest();
    assertThat(rule.getValue(), is(1));
    assertThat(rule.isRequired(), is(true));
    assertThat(rule.isCombined(), is(false));
  }

  @Test
  @Override
  public void testDefinedMoreThanOnePropertyValues() {
    setDefinedMoreThanOneSettings();
    AtLeastXDigitPasswordRule rule = newRuleInstanceForTest();
    assertThat(rule.getValue(), is(2));
    assertThat(rule.isRequired(), is(true));
    assertThat(rule.isCombined(), is(false));
    for (int i = 0; i < NB_LOOP; i++) {
      assertThat(Pattern.compile("[0-9]{2,}").matcher(rule.random()).find(), is(true));
    }
    assertThat(rule.check("1RbRZ"), is(false));
    assertThat(rule.check("1R9Rz"), is(true));
    assertThat(rule.check("1R0Rz"), is(true));
  }

  @Test
  @Override
  public void testCombinationDefinedMoreThanOnePropertyValues() {
    setCombinationDefinedMoreThanOneSettings();
    AtLeastXDigitPasswordRule rule = newRuleInstanceForTest();
    assertThat(rule.getValue(), is(2));
    assertThat(rule.isRequired(), is(false));
    assertThat(rule.isCombined(), is(true));
  }

  @Test
  @Override
  public void testNotDefinedPropertyValues() {
    setNotDefinedSettings();
    AtLeastXDigitPasswordRule rule = newRuleInstanceForTest();
    assertThat(rule.getValue(), is(0));
    assertThat(rule.isRequired(), is(false));
    assertThat(rule.isCombined(), is(false));
  }

  @Test
  @Override
  public void testBadDefinedPropertyValues() {
    setBadDefinedSettings();
    AtLeastXDigitPasswordRule rule = newRuleInstanceForTest();
    assertThat(rule.getValue(), is(0));
    assertThat(rule.isRequired(), is(false));
    assertThat(rule.isCombined(), is(false));
  }

  @Test
  @Override
  public void testNotRequiredPropertyValues() {
    setNotRequiredSettings();
    AtLeastXDigitPasswordRule rule = newRuleInstanceForTest();
    assertThat(rule.getValue(), is(1));
    assertThat(rule.isRequired(), is(false));
    assertThat(rule.isCombined(), is(false));
  }
}
