/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.pdc.subscription.model;

import org.silverpeas.core.subscription.SubscriptionResourceType;

/**
 * The constants defining the resources of the PdC that can be aimed by a subscription.
 * @author mmoquillon
 */
public class PdcSubscriptionConstants {

  private PdcSubscriptionConstants() {
    // Constant class
  }

  /**
   * The identifier of the component instance that is set for any resources of the PdC. As the PdC
   * is transverse to all the applications in Silverpeas, no component instance handles its
   * resources. Nevertheless the instance identifier cannot be left empty: the column of the
   * subscriptions data source is non-nullable and Oracle stores an empty string as a null value.
   * Hence this marker, following the convention already used for the space of a subscription
   * resource.
   */
  public static final String NO_COMPONENT_INSTANCE = "-";

  /**
   * The resource is position criteria on some axis of the PdC. Unlike the other types of
   * subscription resources, such a resource isn't handled by a given component instance: the PdC
   * is a classification plan that is transverse to all the applications in Silverpeas.
   */
  public static final SubscriptionResourceType PDC = new SubscriptionResourceType() {
    private static final long serialVersionUID = -4127930287553364096L;

    @Override
    public int priority() {
      return 30;
    }

    @Override
    public String getName() {
      return "PDC";
    }
  };
}
