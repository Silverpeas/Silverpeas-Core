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

import org.silverpeas.core.subscription.SubscriptionResource;
import org.silverpeas.core.subscription.constant.SubscriptionResourceType;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.WAPrimaryKey;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * User: Yohann Chastagnier
 * Date: 20/02/13
 */
public abstract class AbstractSubscriptionResource<T extends WAPrimaryKey>
    implements SubscriptionResource {

  private final String id;
  private final SubscriptionResourceType type;
  private final T pk;

  /**
   * Default constructor.
   * @param id
   * @param type
   * @param pk
   */
  protected AbstractSubscriptionResource(final String id, final SubscriptionResourceType type,
      final T pk) {
    this.id = id;
    this.type = type;
    this.pk = pk;
  }

  /**
   * Gets the identifier of the resource aimed by subscription
   * @return
   */
  @Override
  public String getId() {
    return id;
  }

  /**
   * Gets the type of the resource aimed by the subscription
   * @return
   */
  @Override
  public SubscriptionResourceType getType() {
    return type;
  }

  @Override
  public String getInstanceId() {
    return (pk != null && StringUtil.isDefined(pk.getInstanceId())) ? pk.getInstanceId() : "";
  }

  /**
   * Gets the Silverpeas Primary Key of the aimed resource
   * @return
   */
  @Override
  public T getPK() {
    return pk;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    AbstractSubscriptionResource that = (AbstractSubscriptionResource) obj;

    return new EqualsBuilder().append(id, that.id).append(type, that.type).append(pk, that.pk)
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(id).append(type).append(pk).toHashCode();
  }
}
