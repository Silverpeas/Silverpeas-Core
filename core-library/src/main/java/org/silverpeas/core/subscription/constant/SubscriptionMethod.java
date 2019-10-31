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
 * The method used to subscribe a user or a group of users to a given resource in Silverpeas.
 * @author Yohann Chastagnier
 * Date: 19/02/13
 */
public enum SubscriptionMethod {
  /**
   * The subscription is unknown. assimilable to nothing.
   */
  UNKNOWN,
  /**
   * The subscription was done by the subscriber himself.
   */
  SELF_CREATION,
  /**
   * The subscription was done by another user with management rights.
   */
  FORCED;

  private static final Collection<SubscriptionMethod> VALID_VALUES;

  static {
    VALID_VALUES = new ArrayList<>(Arrays.asList(SubscriptionMethod.values()));
    VALID_VALUES.remove(SubscriptionMethod.UNKNOWN);
  }

  /**
   * Is this method is valid? It is valid if the method isn't unknown.
   * @return true if the method used in the subscription is known, false otherwise.
   */
  public boolean isValid() {
    return !this.equals(UNKNOWN);
  }

  @JsonValue
  public String getName() {
    return name();
  }

  @JsonCreator
  public static SubscriptionMethod from(String name) {
    if (name != null) {
      for (SubscriptionMethod subscriptionMethod : SubscriptionMethod.values()) {
        if (name.equals(subscriptionMethod.name())) {
          return subscriptionMethod;
        }
      }
    }
    return UNKNOWN;
  }

  /**
   * All Subscription methods are returned into a collection excepted the UNKNOWN one.
   * @return a collection of all of the valid subscription methods.
   */
  public static Collection<SubscriptionMethod> getValidValues() {
    return VALID_VALUES;
  }
}
