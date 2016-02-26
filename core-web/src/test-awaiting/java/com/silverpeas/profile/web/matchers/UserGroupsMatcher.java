/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.silverpeas.profile.web.matchers;

import com.silverpeas.profile.web.UserGroupProfileEntity;
import com.stratelia.webactiv.beans.admin.Group;
import java.util.List;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * A matcher of one or more user groups.
 */
public class UserGroupsMatcher extends TypeSafeMatcher<UserGroupProfileEntity[]> {

  public static Matcher<UserGroupProfileEntity[]> contains(final Group[] groups) {
    return new UserGroupsMatcher(groups);
  }

  public static Matcher<UserGroupProfileEntity[]> contains(final List<? extends Group> groups) {
    return new UserGroupsMatcher((groups.toArray(new Group[groups.size()])));
  }

  private final Group[] expected;
  private String whatIsExpected = "";

  private UserGroupsMatcher(Group[] groups) {
    this.expected = groups;
  }

  @Override
  protected boolean matchesSafely(UserGroupProfileEntity[] actual) {
    boolean match = true;
    if (actual.length != expected.length) {
      match = false;
      whatIsExpected = "The count of actual user groups should be the count of the expected groups";
    } else {
      for (Group expectedGroup : expected) {
        boolean found = false;
        for (UserGroupProfileEntity actualGroup : actual) {
          if (actualGroup.getId().equals(expectedGroup.getId())) {
            found = true;
            if (!actualGroup.getURI().toString().endsWith("/" + actualGroup.getId())) {
              match = false;
              whatIsExpected += "The actual user group URI is incorrect: " + actualGroup.getURI().toString();
            } else {
              match = actualGroup.getName().equals(expectedGroup.getName()) && actualGroup.
                      getDescription().equals(expectedGroup.getDescription()) && actualGroup.
                      getDomainId().equals(expectedGroup.getDomainId());
              if (!match) {
                whatIsExpected += "The actual user group of id '" + actualGroup.getId()
                        + " should match the "
                        + "expected group with the same id. ";
              }
            }
            break;
          }
        }
        if (!found) {
          match = false;
          whatIsExpected += "The expected user group of id '" + expectedGroup.getId()
                  + " isn't found " + "amoung the actual groups. ";
        }
      }
    }
    return match;
  }

  @Override
  public void describeTo(Description description) {
    description.appendText(whatIsExpected);
  }
}
