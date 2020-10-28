/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.core.webapi.subscribe;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import static org.silverpeas.core.subscription.constant.CommonSubscriptionResourceConstants.COMPONENT;
import static org.silverpeas.core.subscription.constant.CommonSubscriptionResourceConstants.NODE;

/**
 * User: Yohann Chastagnier
 */
@XmlRootElement
public class SubscriptionResourceEntity {

  @XmlElement(required = true)
  private String id = "0";
  @XmlElement(required = true)
  private boolean node = false;
  @XmlElement(required = true)
  private boolean component = true;
  @XmlElement(required = true)
  private String instanceId;

  static SubscriptionResourceEntity from(
      org.silverpeas.core.subscription.SubscriptionResource resource) {
    SubscriptionResourceEntity entity = new SubscriptionResourceEntity();
    entity.id = resource.getId();
    entity.node = NODE.equals(resource.getType());
    entity.component = COMPONENT.equals(resource.getType());
    entity.instanceId = resource.getInstanceId();
    return entity;
  }

  public String getId() {
    return id;
  }

  public boolean isNode() {
    return node;
  }

  public boolean isComponent() {
    return component;
  }

  public String getInstanceId() {
    return instanceId;
  }
}
