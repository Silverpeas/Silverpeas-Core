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

package com.silverpeas.admin;

import com.stratelia.webactiv.beans.admin.Domain;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 *
 * @author ehugonnet
 */
public class DomainMatcher extends BaseMatcher<Domain> {

  private Domain domain;

  @Override
  public boolean matches(Object item) {
    boolean match = false;
    if (item instanceof Domain) {
      Domain actual = (Domain) item;
      match = domain.equals(actual);
    }
    return match;
  }

  @Override
  public void describeTo(Description description) {
    description.appendValue(domain);
  }

  /**
   * Creates a new matcher with the specified domain.
   * @param domain the domain to match.
   * @return a domain matcher.
   */
  public static DomainMatcher matches(final Domain domain) {
    return new DomainMatcher(domain);
  }

  private DomainMatcher(final Domain domain) {
    this.domain = domain;
  }
}
