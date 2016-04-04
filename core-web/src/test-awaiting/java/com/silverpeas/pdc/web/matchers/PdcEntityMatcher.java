/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package com.silverpeas.pdc.web.matchers;

import com.silverpeas.pdc.web.PdcEntity;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * A matcher of PdcEntity objects to be used in unit tests.
 */
public class PdcEntityMatcher extends TypeSafeMatcher<PdcEntity> {

  private PdcEntity expected;
  private String whatIsExpected = "";

  @Factory
  public static Matcher<PdcEntity> equalTo(final PdcEntity expected) {
    return new PdcEntityMatcher().withExpectedPdcEntity(expected);
  }

  @Override
  protected boolean matchesSafely(PdcEntity actual) {
    boolean matches = true;
    if (!actual.getURI().equals(expected.getURI())) {
      matches = false;
    } else if (actual.getAxis().size() != expected.getAxis().size()) {
      matches = false;
    } else if (!actual.getAxis().containsAll(expected.getAxis())) {
      matches = false;
    }
    if (!matches) {
      whatIsExpected = expected.toString();
    }
    return matches;
  }

  @Override
  public void describeTo(Description description) {
    description.appendText(whatIsExpected);
  }

  private PdcEntityMatcher() {
  }

  protected PdcEntityMatcher withExpectedPdcEntity(
          final PdcEntity pdcEntity) {
    this.expected = pdcEntity;
    return this;
  }
}
