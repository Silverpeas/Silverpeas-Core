/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.personalization;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public class PersonalizeDetailMatcher extends BaseMatcher<UserPreferences> {

  private UserPreferences detail;

  /**
   * Creates a new matcher with the specified personalization detail.
   * @param detail the personalization detail to match.
   * @return a personalization detail matcher.
   */
  public static PersonalizeDetailMatcher matches(final UserPreferences detail) {
    return new PersonalizeDetailMatcher(detail);
  }
  
  
  private PersonalizeDetailMatcher(UserPreferences detail) {
    this.detail = detail;
  }

  @Override
  public boolean matches(Object item) {
    boolean match = false;
    if (item instanceof UserPreferences) {
      UserPreferences actual = (UserPreferences) item;
      match = detail.equals(actual);
    }
    return match;
  }

  @Override
  public void describeTo(Description description) {
    description.appendValue(detail.toString());
  }
}
