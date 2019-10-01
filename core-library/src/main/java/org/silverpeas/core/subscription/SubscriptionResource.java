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
package org.silverpeas.core.subscription;

import org.silverpeas.core.subscription.constant.SubscriptionResourceType;
import org.silverpeas.core.WAPrimaryKey;

/**
 * A resource implied in a user subscription.
 * @author Yohann Chastagnier
 * @since 20/02/13
 */
public interface SubscriptionResource {

  /**
   * Gets the identifier of the resource aimed by a subscription.
   * @return the unique identifier of a resource.
   */
  String getId();

  /**
   * Gets the type of the resource aimed by the subscription
   * @return a well predefined type of resource on which a subscription can be done.
   */
  SubscriptionResourceType getType();

  /**
   * Gets the instance identifier of the component instance that handles the resource in Silverpeas.
   * @return the unique identifier of a component instance.
   */
  String getInstanceId();

  /**
   * Gets the Silverpeas Primary Key of the aimed resource.
   * @param <T> the concrete type of the key.
   * @return the key identifying the resource in the data source used by Silverpeas to persist its
   * data.
   */
  <T extends WAPrimaryKey> T getPK();
}
