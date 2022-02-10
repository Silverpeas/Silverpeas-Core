/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.subscription;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.io.Serializable;

/**
 * The type of a resource that can be targeted by a subscription.
 * @author Yohann Chastagnier
 */
public interface SubscriptionResourceType extends Serializable {

  /**
   * Is this type is valid? It is valid if the type of the resource isn't unknown.
   * @return true if the type of the resource targeted by a subscription is known, false otherwise.
   */
  default boolean isValid() {
    return true;
  }

  /**
   * Indicates a priority which can be used by UI as example.
   * @return an integer which lowest value means the highest priority.
   */
  int priority();

  /**
   * Gets the name of the subscription type.
   * <p>
   *   '@' character is not authorized because it is used internally for technical purposes.
   * </p>
   * @return a string.
   */
  @JsonValue
  String getName();

  @JsonCreator
  static SubscriptionResourceType from(String name) {
    return SubscriptionFactory.get().getSubscriptionResourceTypeByName(name);
  }
}
