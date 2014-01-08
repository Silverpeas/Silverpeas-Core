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

import com.silverpeas.profile.web.UserProfileEntity;
import static com.silverpeas.profile.web.UserProfileTestResources.USER_PROFILE_PATH;
import com.stratelia.webactiv.beans.admin.UserDetail;
import java.util.List;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * A matcher of one or more users details.
 */
public class UsersMatcher extends TypeSafeMatcher<UserProfileEntity[]> {

  public static Matcher<UserProfileEntity[]> contains(final UserDetail[] users) {
    return new UsersMatcher(users);
  }
  public static Matcher<UserProfileEntity[]> contains(final List<? extends UserDetail> users) {
    return new UsersMatcher(users.toArray(new UserDetail[users.size()]));
  }
  private final UserDetail[] expected;
  private String whatIsExpected = "";

  private UsersMatcher(UserDetail[] users) {
    this.expected = users;
  }

  @Override
  protected boolean matchesSafely(UserProfileEntity[] actual) {
    boolean match = true;
    if (actual.length != expected.length) {
      match = false;
      whatIsExpected = "The count of actual users should be the count of the expected users";
    } else {
      for (UserDetail expectedUser : expected) {
        boolean found = false;
        for (UserProfileEntity actualUser : actual) {
          if (actualUser.getId().equals(expectedUser.getId())) {
            found = true;
            if (!actualUser.getURI().toString().endsWith(USER_PROFILE_PATH + '/' + actualUser.getId())) {
              match = false;
              whatIsExpected += "The actual user URI is incorrect: " + actualUser.getURI().toString();
            } else {
              match = actualUser.equals(expectedUser);
              if (!match) {
                whatIsExpected += "The actual user of id '" + actualUser.getId() + " should match the "
                      + "expected user with the same id. ";
              }
            }
            break;
          }
        }
        if (!found) {
          match = false;
          whatIsExpected += "The expected user of id '" + expectedUser.getId() + " isn't found " +
                  "amoung the actual users. ";
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
