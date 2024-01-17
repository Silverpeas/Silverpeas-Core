/*
 * Copyright (C) 2000 - 2024 Silverpeas
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

package org.silverpeas.web.pdcsubscription.control;

import org.silverpeas.core.subscription.SubscriptionResourceType;

import java.util.List;

/**
 * This abstract class permits to manage categories into personal page of user subscriptions.
 * <p>
 * By this way, there is a separation between the list of
 * {@link org.silverpeas.core.subscription.SubscriptionResourceType} and the categories displayed
 * to a user.
 * </p>
 * @author silveryocha
 */
public abstract class SubscriptionCategory {
  private final PdcSubscriptionSessionController ctrl;

  public SubscriptionCategory(final PdcSubscriptionSessionController ctrl) {
    this.ctrl = ctrl;
  }

  /**
   * Gets the identifier of the category.
   * @return a string.
   */
  public abstract String getId();

  /**
   * The priority into which the category MUST be displayed.
   * @return an integer which the lowest value means the highest priority.
   */
  public abstract int priority();

  /**
   * Gets the label of the category.
   * @return a string.
   */
  public abstract String getLabel();

  /**
   * Gets the label of the type of resources handled into category.
   * @return a string.
   */
  public abstract String getResourceTypeLabel();

  /**
   * Gets the list of {@link SubscriptionResourceType} the category handles.
   * @return a list of {@link SubscriptionResourceType} instances.
   */
  public abstract List<SubscriptionResourceType> getHandledTypes();

  protected PdcSubscriptionSessionController getCtrl() {
    return ctrl;
  }
}
