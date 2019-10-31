/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.subscription.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * The type of a subscriber. It can be either a user or a group of users.
 * @author Yohann Chastagnier
 * Date: 19/02/13
 */
public enum SubscriberType {
  /**
   * The type is unknown. Assimilable to nothing.
   */
  UNKNOWN,
  /**
   * The subscriber is a user.
   */
  USER,
  /**
   * The subscriber is a group of users.
   */
  GROUP;

  private static final Collection<SubscriberType> VALID_VALUES;

  static {
    VALID_VALUES = new ArrayList<>(Arrays.asList(SubscriberType.values()));
    VALID_VALUES.remove(SubscriberType.UNKNOWN);
  }

  /**
   * Is this type is valid? It is valid if the type of the subscriber isn't unknown.
   * @return true if the type of the subscriber is known, false otherwise.
   */
  public boolean isValid() {
    return !this.equals(UNKNOWN);
  }

  @JsonValue
  public String getName() {
    return name();
  }

  @JsonCreator
  public static SubscriberType from(String name) {
    if (name != null) {
      for (SubscriberType subscriberType : SubscriberType.values()) {
        if (name.equals(subscriberType.name())) {
          return subscriberType;
        }
      }
    }
    return UNKNOWN;
  }

  /**
   * All subscriber types are returned into a collection excepted the UNKNOWN one.
   * @return a collection of all of the valid subscriber types.
   */
  public static Collection<SubscriberType> getValidValues() {
    return VALID_VALUES;
  }
}
