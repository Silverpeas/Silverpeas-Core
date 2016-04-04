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

package com.silverpeas.pdc.matchers;

import com.silverpeas.pdc.model.PdcAxisValue;
import com.silverpeas.pdc.model.PdcClassification;
import com.silverpeas.pdc.model.PdcPosition;
import java.util.Collection;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import static com.silverpeas.pdc.model.PdcModelHelper.*;

/**
 * A matcher of the PdC classifications.
 */
public class PdcClassificationMatcher extends TypeSafeMatcher<PdcClassification> {

  private PdcClassification expected;
  private String description = "";

  public static TypeSafeMatcher<PdcClassification> equalTo(final PdcClassification expected) {
    return new PdcClassificationMatcher(expected);
  }

  @Override
  protected boolean matchesSafely(PdcClassification actual) {
    boolean matches = true;

    if (idOf(actual) != idOf(expected)) {
      matches = false;
      description += "(actual id: " + idOf(actual) + ", expected id: " + idOf(expected) + ")";
    } else if (!actual.getComponentInstanceId().equals(expected.getComponentInstanceId())) {
      matches = false;
      description += "(actual component instance id: " + actual.getComponentInstanceId()
              + ", expected component instance id: " + expected.getComponentInstanceId() + ")";
    } else if ((actual.getContentId() == null && expected.getContentId() != null) || (actual.
            getContentId()
            != null && !actual.getContentId().equals(expected.getContentId()))) {
      matches = false;
      description += "(actual content id: " + actual.getContentId() + ", expected content id: "
              + expected.getContentId() + ")";
    } else if ((actual.getNodeId() == null && expected.getNodeId() != null) || (actual.getNodeId()
            != null && !actual.getNodeId().equals(expected.getNodeId()))) {
      matches = false;
      description += "(actual node id: " + actual.getNodeId() + ", expected node id: "
              + expected.getNodeId() + ")";
    } else if (actual.isModifiable() != expected.isModifiable()) {
      matches = false;
      description += "(actual modifiable: " + actual.isModifiable() + ", expected modifiable: "
              + expected.isModifiable() + ")";
    } else if (actual.getPositions().size() != expected.getPositions().size()) {
      matches = false;
      description += "(actual positions count: " + actual.getPositions().size()
              + ", expected positions count: " + expected.getPositions().size() + ")";
    } else {
      matches = checkPositions(actual.getPositions(), expected.getPositions());
    }
    return matches;
  }

  @Override
  public void describeTo(Description description) {
    description.appendText(this.expected.toString());
    description.appendText(this.description);
  }

  protected String getDescription() {
    return this.description;
  }

  protected PdcClassificationMatcher(final PdcClassification expected) {
    this.expected = expected;
  }

  private boolean checkPositions(final Collection<PdcPosition> actualPositions,
          final Collection<PdcPosition> expectedPositions) {
    boolean matches = true;
    int position = -1;
    for (PdcPosition expectedPosition : expectedPositions) {
      boolean found = false;
      position++;
      for (PdcPosition actualPosition : actualPositions) {
        if (actualPosition.getId().equals(expectedPosition.getId())) {
          found = true;
          if (actualPosition.getValues().size() != expectedPosition.getValues().size()) {
            matches = false;
            description += "(actual position " + position + " values count: " + actualPosition.
                    getValues().
                    size()
                    + ", expected position " + position + " values count: " + expectedPosition.
                    getValues().
                    size() + ")";
          } else {
            matches =
                    checkValues(position, actualPosition.getValues(), expectedPosition.getValues());
          }
        }
      }
      if (!found) {
        matches = false;
        description += "(expected position of id " + expectedPosition.getId()
                + "not found in actual)";
      }
      break;
    }
    return matches;
  }

  private boolean checkValues(int position, final Collection<PdcAxisValue> actualValues,
          final Collection<PdcAxisValue> expectedValues) {
    boolean matches = true;
    int value = -1;
    for (PdcAxisValue expectedValue : expectedValues) {
      boolean found = false;
      value++;
      for (PdcAxisValue actualValue : actualValues) {
        if (actualValue.getId().equals(expectedValue.getId())) {
          found = true;
          if (!actualValue.getAxisId().equals(expectedValue.getAxisId())) {
            matches = false;
            description += "(actual position " + position + " value " + value + " axis: "
                    + actualValue.getAxisId()
                    + ", expected position " + position + " value " + value + " axis: "
                    + expectedValue.getAxisId()
                    + ")";
          } else if (!actualValue.getId().equals(expectedValue.getId())) {
            matches = false;
            description += "(actual position " + position + " value " + value + " id: "
                    + actualValue.getId()
                    + ", expected position " + position + " value " + value + " id: "
                    + expectedValue.getId()
                    + ")";
          }
        }
      }
      if (!found) {
        matches = false;
        description += "(expected value of id " + expectedValue.getId()
                + " not found in actual position " + position + ")";
        break;
      }
    }
    return matches;
  }
}
