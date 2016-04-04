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

import com.silverpeas.pdc.model.PdcClassification;
import java.util.List;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import static com.silverpeas.pdc.model.PdcModelHelper.*;

/**
 * A matcher of a list of PdC classifications.
 */
public class PdcClassificationListMatcher extends TypeSafeMatcher<List<PdcClassification>> {

  private List<PdcClassification> expected;
  private String description = "";

  public static TypeSafeMatcher<List<PdcClassification>> containsAll(final List<PdcClassification> expected) {
    return new PdcClassificationListMatcher(expected);
  }

  @Override
  protected boolean matchesSafely(List<PdcClassification> actual) {
    boolean matches = true;
    for (PdcClassification expectedClassification : expected) {
      boolean found = false;
      for (PdcClassification actualClassification : actual) {
        if (idOf(actualClassification).equals(idOf(expectedClassification))) {
          found = true;
          PdcClassificationMatcher matcher = new PdcClassificationMatcher(expectedClassification);
          matches = matcher.matchesSafely(actualClassification);
          if (!matches) {
            description = matcher.getDescription();
            break;
          }
        }
      }
      if (!found || !matches) {
        matches = false;
        break;
      }
    }
    return matches;
  }

  @Override
  public void describeTo(Description description) {
    description.appendText(this.expected.toString());
    description.appendText(this.description);
  }

  protected PdcClassificationListMatcher(final List<PdcClassification> classifications) {
    this.expected = classifications;
  }

}
