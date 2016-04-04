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
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * An Harmcrest matcher to check a classification on the PdC doesn't contain a given value.
 */
public class PdcClassificationWithoutAGivenValueMatcher extends TypeSafeMatcher<PdcClassification> {

  public static TypeSafeMatcher<PdcClassification> hasNo(final PdcAxisValue aValue) {
    return new PdcClassificationWithoutAGivenValueMatcher(aValue);
  }
  private final PdcAxisValue value;

  private PdcClassificationWithoutAGivenValueMatcher(PdcAxisValue aValue) {
    this.value = aValue;
  }

  @Override
  protected boolean matchesSafely(PdcClassification classification) {
    boolean matches = true;
    for (PdcPosition position : classification.getPositions()) {
      if (position.getValues().contains(value)) {
        matches = false;
        break;
      }
    }

    return matches;
  }

  @Override
  public void describeTo(Description description) {

  }

}
