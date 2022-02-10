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

package org.silverpeas.core.subscription.constant;

import org.silverpeas.core.subscription.SubscriptionResourceType;

/**
 * @author silveryocha
 */
public class CommonSubscriptionResourceConstants {

  private CommonSubscriptionResourceConstants() {
    // Constant class
  }

  /**
   * The type of the resource is unknown. Assimilable to nothing.
   */
  public static final SubscriptionResourceType UNKNOWN = new SubscriptionResourceType() {
    private static final long serialVersionUID = -6432282101653685638L;

    @Override
    public String getName() {
      return "UNKNOWN";
    }

    @Override
    public boolean isValid() {
      return false;
    }

    @Override
    public int priority() {
      return -1;
    }
  };

  /**
   * The resource is a component instance. And thus the subscription is about all of the resources
   * handled by this component instance. If is the more high level subscription.
   */
  public static final SubscriptionResourceType COMPONENT = new SubscriptionResourceType() {
    private static final long serialVersionUID = 5646162430437108940L;

    @Override
    public int priority() {
      return 0;
    }

    @Override
    public String getName() {
      return "COMPONENT";
    }
  };

  /**
   * The resource is a node in a given component instance. Nodes are generic objects used to
   * categorize the resources in some component instances.
   */
  public static final SubscriptionResourceType NODE = new SubscriptionResourceType() {
    private static final long serialVersionUID = 651012169699581187L;

    @Override
    public int priority() {
      return 1;
    }

    @Override
    public String getName() {
      return "NODE";
    }
  };
}
