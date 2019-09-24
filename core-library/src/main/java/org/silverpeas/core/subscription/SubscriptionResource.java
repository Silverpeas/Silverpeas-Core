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
   * Gets the identifier of the resource aimed by subscription
   * @return the resource identifier
   */
  String getId();

  /**
   * Gets the type of the resource aimed by the subscription
   * @return the resource type.
   */
  SubscriptionResourceType getType();

  /**
   * Gets the instance identifier of a component instance in Silverpeas that manages the resource.
   * @return the unique identifier of a component instance.
   */
  String getInstanceId();

  /**
   * Gets the unique data source identifier of the resource.
   * @param <T> the concrete type of the identifier.
   * @return the unique identifier of the resource in the data source.
   */
  <T extends WAPrimaryKey> T getPK();
}
