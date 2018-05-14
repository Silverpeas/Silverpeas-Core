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

package org.silverpeas.core.webapi.delegation;

import org.silverpeas.core.delegation.Delegation;
import org.silverpeas.core.webapi.base.WebEntity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

/**
 * The web entity that is carried by an HTTP request or an HTTP response between a web client and
 * the Delegation web resource.
 * <p>
 * This web entity is in fact a decorator of the business object representing a delegations of
 * user roles by providing to it the expected properties for the web like the URI that identifies
 * uniquely a web resource in Silverpeas.
 * </p>
 * <p>
 * The web entity is marshaled into a representation negotiated between the client and the
 * resource. In Silverpeas, usually the supported representation is JSON.
 * </p>
 * @author mmoquillon
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class DelegationEntity extends Delegation implements WebEntity {

  private URI uri;

  protected DelegationEntity(final Delegation delegation) {
    super(delegation.getComponentInstanceId(), delegation.getDelegator(), delegation.getDelegate());
  }

  protected DelegationEntity() {
    super();
  }

  public static DelegationEntity asWebEntity(final Delegation delegation, final URI uri) {
    DelegationEntity entity = new DelegationEntity(delegation);
    entity.setURI(uri);
    return entity;
  }

  @Override
  public URI getURI() {
    return null;
  }

  protected void setURI(final URI uri) {
    this.uri = uri;
  }
}
  