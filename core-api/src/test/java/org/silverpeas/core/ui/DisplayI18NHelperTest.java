/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.core.ui;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.silverpeas.core.test.rule.CommonAPI4Test;
import org.silverpeas.core.test.rule.MockByReflectionRule;

import static org.silverpeas.core.ui.DisplayI18NHelper.getDefaultLanguage;
import static org.silverpeas.core.ui.DisplayI18NHelper.verifyLanguage;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class DisplayI18NHelperTest {

  @Rule
  public CommonAPI4Test commonAPI4Test = new CommonAPI4Test();

  @Rule
  public MockByReflectionRule reflectionRule = new MockByReflectionRule();

  @Before
  public void setup() {
    reflectionRule.setField(DisplayI18NHelper.class, asList("fr", "en", "de"), "languages");
    reflectionRule.setField(DisplayI18NHelper.class, "en", "defaultLanguage");
  }

  @Test
  public void verifyLanguageByGivingNullOne() {
    assertThat(getDefaultLanguage(), is("en"));
    assertThat(verifyLanguage(null), is(getDefaultLanguage()));
  }

  @Test
  public void verifyLanguageByGivingNotHandledOne() {
    assertThat(getDefaultLanguage(), is("en"));
    assertThat(verifyLanguage("ru"), is(getDefaultLanguage()));
  }

  @Test
  public void verifyLanguageByGivingHandledOne() {
    assertThat(getDefaultLanguage(), is("en"));
    assertThat(verifyLanguage("de"), is("de"));
  }
}