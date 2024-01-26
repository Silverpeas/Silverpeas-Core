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
package org.silverpeas.core.ui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.kernel.test.UnitTest;
import org.silverpeas.kernel.test.util.Reflections;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.silverpeas.core.ui.DisplayI18NHelper.getDefaultLanguage;
import static org.silverpeas.core.ui.DisplayI18NHelper.verifyLanguage;

@UnitTest
class DisplayI18NHelperTest {

  @Test
  void verifyLanguageByGivingNullOne() {
    assertThat(getDefaultLanguage(), is("fr"));
    assertThat(verifyLanguage(null), is(getDefaultLanguage()));
  }

  @Test
  void verifyLanguageByGivingNotHandledOne() {
    assertThat(getDefaultLanguage(), is("fr"));
    assertThat(verifyLanguage("ru"), is(getDefaultLanguage()));
  }

  @Test
  void verifyLanguageByGivingHandledOne() {
    assertThat(getDefaultLanguage(), is("fr"));
    assertThat(verifyLanguage("de"), is("de"));
  }
}