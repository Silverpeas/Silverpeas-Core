/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
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
package com.silverpeas.subscribe.constant;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * User: Yohann Chastagnier
 * Date: 19/02/13
 */
public enum SubscriptionMethod {
  UNKNOWN,
  SELF_CREATION,
  FORCED;

  private static final Collection<SubscriptionMethod> VALID_VALUES;

  static {
    VALID_VALUES = new ArrayList<SubscriptionMethod>(Arrays.asList(SubscriptionMethod.values()));
    VALID_VALUES.remove(SubscriptionMethod.UNKNOWN);
  }

  /**
   * Valid if current instance is not the one of UNKNOWN.
   * @return
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
   * All SubscriptionMethod are returned into a Collection excepted UNKNOWN type.
   * @return
   */
  public static Collection<SubscriptionMethod> getValidValues() {
    return VALID_VALUES;
  }
}
