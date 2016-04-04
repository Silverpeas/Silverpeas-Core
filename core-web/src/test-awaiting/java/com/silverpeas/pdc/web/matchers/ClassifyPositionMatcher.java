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

import com.stratelia.silverpeas.pdc.model.ClassifyPosition;
import com.stratelia.silverpeas.pdc.model.ClassifyValue;
import java.util.Collections;
import java.util.List;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * A matcher of Classify objects to be used in unit tests.
 */
public class ClassifyPositionMatcher extends TypeSafeMatcher<List<ClassifyPosition>> {

  private List<ClassifyPosition> expectedPositions;
  private String whatIsExpected = "";

  @Factory
  public static Matcher<List<ClassifyPosition>> equalTo(final List<ClassifyPosition> expected) {
    return new ClassifyPositionMatcher().withExpectedClassifyPositions(expected);
  }

  @Override
  protected boolean matchesSafely(List<ClassifyPosition> actualPositions) {
    boolean matches = true;
    if (actualPositions.size() != expectedPositions.size()) {
      matches = false;
    } else {
      Collections.reverse(actualPositions);
      for (int p = 0; p < actualPositions.size(); p++) {
        List<ClassifyValue> actualValues = actualPositions.get(p).getListClassifyValue();
        List<ClassifyValue> expectedValues = expectedPositions.get(p).getListClassifyValue();
        if (actualValues.size() != expectedValues.size()) {
          matches = false;
        } else {
          for (int v = 0; v < actualValues.size(); v++) {
            ClassifyValue actual = actualValues.get(v);
            ClassifyValue expected = expectedValues.get(v);
            if (actual.getAxisId() != expected.getAxisId()) {
              matches = false;
              break;
            }
            if (!actual.getValue().equals(expected.getValue())) {
              matches = false;
              break;
            }
          }
        }
      }
    }
    if (!matches) {
      whatIsExpected = expectedPositions.toString();
    }
    return matches;
  }

  @Override
  public void describeTo(Description description) {
    description.appendText(whatIsExpected);
  }

  private ClassifyPositionMatcher() {
  }

  protected ClassifyPositionMatcher withExpectedClassifyPositions(
          final List<ClassifyPosition> positions) {
    this.expectedPositions = positions;
    return this;
  }
}
