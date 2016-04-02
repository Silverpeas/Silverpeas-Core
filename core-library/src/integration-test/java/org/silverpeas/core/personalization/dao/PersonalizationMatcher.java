/*
 * Copyright (C) 2000 - 2014 Silverpeas
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

package org.silverpeas.core.personalization.dao;

import org.silverpeas.core.personalization.UserPreferences;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public class PersonalizationMatcher extends BaseMatcher<UserPreferences> {

  private UserPreferences expected;

  /**
   * Creates a new matcher with the specified personalization detail.
   * @param expected the personalization detail to match.
   * @return a personalization detail matcher.
   */
  public static PersonalizationMatcher matches(final UserPreferences expected) {
    return new PersonalizationMatcher(expected);
  }


  private PersonalizationMatcher(UserPreferences expected) {
    this.expected = expected;
  }

  @Override
  public boolean matches(Object item) {
    boolean match = false;
    if (item instanceof UserPreferences) {
      UserPreferences actual = (UserPreferences) item;
      EqualsBuilder matcher = new EqualsBuilder();
      matcher.append(expected.getId(), actual.getId());
      matcher.append(expected.getLanguage(), actual.getLanguage());
      matcher.append(expected.getLook(), actual.getLook());
      matcher.append(expected.getPersonalWorkSpaceId(), actual.getPersonalWorkSpaceId());
      matcher.append(expected.isThesaurusEnabled(), actual.isThesaurusEnabled());
      matcher.append(expected.isDragAndDropEnabled(), actual.isDragAndDropEnabled());
      matcher.append(expected.isWebdavEditionEnabled(), actual.isWebdavEditionEnabled());
      matcher.append(expected.getDisplay(), actual.getDisplay());
      match = matcher.isEquals();
    }
    return match;
  }

  @Override
  public void describeTo(Description description) {
    description.appendValue(expected.toString());
  }
}
