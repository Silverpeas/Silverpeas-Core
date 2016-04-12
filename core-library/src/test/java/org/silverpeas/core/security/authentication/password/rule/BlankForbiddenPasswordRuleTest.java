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

import org.junit.Ignore;
import org.junit.Test;
import org.silverpeas.core.security.authentication.password.constant.PasswordRuleType;

import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * User: Yohann Chastagnier
 * Date: 08/01/13
 */
public class BlankForbiddenPasswordRuleTest
    extends AbstractPasswordRuleTest<BlankForbiddenPasswordRule> {

  @Test
  public void testCommons() {
    BlankForbiddenPasswordRule rule = newRuleInstanceForTest();
    assertThat(rule.getType(), is(PasswordRuleType.BLANK_FORBIDDEN));
    assertThat(rule.check("a1234 ;"), is(false));
    assertThat(rule.check("a1234;"), is(true));
    for (char c = 97; c < (97 + 26); c++) {
      assertThat(rule.check("0" + c + "2135 "), is(false));
    }
    for (char c = 65; c < (65 + 26); c++) {
      assertThat(rule.check("0" + c + "123456789"), is(true));
    }
    for (int i = 0; i < NB_LOOP; i++) {
      assertThat(Pattern.compile("[a-z]+").matcher(rule.random()).find(), is(false));
      assertThat(Pattern.compile("[A-Z]+").matcher(rule.random()).find(), is(false));
      assertThat(Pattern.compile("[0-9]+").matcher(rule.random()).find(), is(false));
    }
  }

  @Test
  @Override
  public void testDefinedPropertyValues() {
    setDefinedSettings();
    BlankForbiddenPasswordRule rule = newRuleInstanceForTest();
    assertThat(rule.getValue(), is(true));
    assertThat(rule.isRequired(), is(true));
    assertThat(rule.isCombined(), is(false));
    assertThat(Pattern.compile("[ ]+").matcher(rule.random()).find(), is(false));
  }

  @Ignore
  @Override
  public void testDefinedMoreThanOnePropertyValues() {
  }

  @Ignore
  @Override
  public void testCombinationDefinedMoreThanOnePropertyValues() {
  }

  @Test
  @Override
  public void testNotDefinedPropertyValues() {
    setNotDefinedSettings();
    BlankForbiddenPasswordRule rule = newRuleInstanceForTest();
    assertThat(rule.getValue(), is(true));
    assertThat(rule.isRequired(), is(true));
    assertThat(rule.isCombined(), is(false));
    assertThat(Pattern.compile("[ ]+").matcher(rule.random()).find(), is(false));
  }

  @Test
  @Override
  public void testBadDefinedPropertyValues() {
    setBadDefinedSettings();
    BlankForbiddenPasswordRule rule = newRuleInstanceForTest();
    assertThat(rule.getValue(), is(true));
    assertThat(rule.isRequired(), is(true));
    assertThat(rule.isCombined(), is(false));
    assertThat(Pattern.compile("[ ]+").matcher(rule.random()).find(), is(false));
  }

  @Test
  @Override
  public void testNotRequiredPropertyValues() {
    setNotRequiredSettings();
    BlankForbiddenPasswordRule rule = newRuleInstanceForTest();
    assertThat(rule.getValue(), is(false));
    assertThat(rule.isRequired(), is(false));
    assertThat(rule.isCombined(), is(false));
    assertThat(Pattern.compile("[ ]+").matcher(rule.random()).find(), is(true));
  }
}
