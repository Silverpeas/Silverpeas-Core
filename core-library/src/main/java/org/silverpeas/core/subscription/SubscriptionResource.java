/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
 * User: Yohann Chastagnier
 * Date: 20/02/13
 */
public interface SubscriptionResource {

  /**
   * Gets the identifier of the resource aimed by subscription
   * @return
   */
  public String getId();

  /**
   * Gets the type of the resource aimed by the subscription
   * @return
   */
  public SubscriptionResourceType getType();

  /**
   * Gets the instance identifier of component
   * @return
   */
  public String getInstanceId();

  /**
   * Gets the Silverpeas Primary Key of the aimed resource
   * @param <T>
   * @return
   */
  public <T extends WAPrimaryKey> T getPK();
}