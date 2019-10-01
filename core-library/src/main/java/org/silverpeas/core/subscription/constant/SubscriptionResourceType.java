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
 * The type of a resource that can be targeted by a subscription.
 * @author Yohann Chastagnier
 * Date: 19/02/13
 */
public enum SubscriptionResourceType {
  /**
   * The type of the resource is unknown. Assimilable to nothing.
   */
  UNKNOWN,
  /**
   * The resource is a component instance. And thus the subscription is about all of the resources
   * handled by this component instance. If is the more high level subscription.
   */
  COMPONENT,
  /**
   * The resource is a node in a given component instance. Nodes are generic objects used to
   * categorize the resources in some component instances.
   */
  NODE,
  /**
   * The resource is a forum. Used by component instances handling forums.
   */
  FORUM,
  /**
   * The resource is a message in a given forum. Used by component instances handling forums.
   */
  FORUM_MESSAGE;

  private static final Collection<SubscriptionResourceType> VALID_VALUES;

  static {
    VALID_VALUES =
        new ArrayList<>(Arrays.asList(SubscriptionResourceType.values()));
    VALID_VALUES.remove(SubscriptionResourceType.UNKNOWN);
  }

  /**
   * Is this type is valid? It is valid if the type of the resource isn't unknown.
   * @return true if the type of the resource targeted by a subscription is known, false otherwise.
   */
  public boolean isValid() {
    return !this.equals(UNKNOWN);
  }

  @JsonValue
  public String getName() {
    return name();
  }

  @JsonCreator
  public static SubscriptionResourceType from(String name) {
    if (name != null) {
      for (SubscriptionResourceType subscriptionResourceType : SubscriptionResourceType.values()) {
        if (name.equalsIgnoreCase(subscriptionResourceType.name())) {
          return subscriptionResourceType;
        }
      }
    }
    return UNKNOWN;
  }

  /**
   * All resource types are returned into a collection excepted the UNKNOWN one.
   * @return a collection of all of the valid resource types.
   */
  public static Collection<SubscriptionResourceType> getValidValues() {
    return VALID_VALUES;
  }
}
