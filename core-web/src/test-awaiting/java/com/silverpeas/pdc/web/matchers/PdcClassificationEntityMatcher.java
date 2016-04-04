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

import com.silverpeas.pdc.web.PdcClassificationEntity;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * A matcher of PdcClassificationEntity objects to be used in unit tests.
 */
public class PdcClassificationEntityMatcher extends TypeSafeMatcher<PdcClassificationEntity> {

  private PdcClassificationEntity expected;
  private String whatIsExpected = "";

  @Factory
  public static Matcher<PdcClassificationEntity> equalTo(final PdcClassificationEntity expected) {
    return new PdcClassificationEntityMatcher().withExpectedPdcClassificationEntity(expected);
  }

  @Factory
  public static Matcher<PdcClassificationEntity> undefined() {
    PdcClassificationEntity expected = PdcClassificationEntity.undefinedClassification();
    return new PdcClassificationEntityMatcher().withExpectedPdcClassificationEntity(expected);
  }

  @Override
  protected boolean matchesSafely(PdcClassificationEntity actual) {
    boolean matches = true;
    if (expected.isUndefined() != actual.isUndefined()) {
      matches = false;
    } else if (actual.getURI() != null && expected.getURI() != null && !actual.getURI().equals(expected.
            getURI())) {
      matches = false;
    } else if (actual.getClassificationPositions().size() != expected.getClassificationPositions().
            size()) {
      matches = false;
    } else if (!actual.getClassificationPositions().containsAll(
            expected.getClassificationPositions())) {
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

  private PdcClassificationEntityMatcher() {
  }

  protected PdcClassificationEntityMatcher withExpectedPdcClassificationEntity(
          final PdcClassificationEntity classificationEntity) {
    this.expected = classificationEntity;
    return this;
  }
}
