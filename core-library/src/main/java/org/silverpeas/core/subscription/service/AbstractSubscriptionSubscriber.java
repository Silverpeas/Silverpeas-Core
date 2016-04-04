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
package org.silverpeas.core.subscription.service;

import org.silverpeas.core.subscription.SubscriptionSubscriber;
import org.silverpeas.core.subscription.constant.SubscriberType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * User: Yohann Chastagnier
 * Date: 20/02/13
 */
public class AbstractSubscriptionSubscriber implements SubscriptionSubscriber {

  private final String id;
  private final SubscriberType type;

  /**
   * Default constructor.
   * @param id
   * @param type
   */
  protected AbstractSubscriptionSubscriber(final String id, final SubscriberType type) {
    this.id = id;
    this.type = type;
  }

  /**
   * Gets the identifier of the subscriber that aimed a subscription
   * @return
   */
  @Override
  public String getId() {
    return id;
  }

  /**
   * Gets the type of the subscriber that aimed a subscription
   * @return
   */
  @Override
  public SubscriberType getType() {
    return type;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    AbstractSubscriptionSubscriber that = (AbstractSubscriptionSubscriber) obj;

    return new EqualsBuilder().append(id, that.id).append(type, that.type).isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(id).append(type).toHashCode();
  }
}
