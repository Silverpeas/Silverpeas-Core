/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.core.webapi.sharing;

import java.net.URI;

import javax.xml.bind.annotation.XmlElement;

import org.silverpeas.core.sharing.model.Ticket;
import org.silverpeas.core.webapi.base.WebEntity;
import org.silverpeas.core.admin.user.model.UserDetail;

public class SharingEntity implements WebEntity {

  private static final long serialVersionUID = 1L;

  @XmlElement(defaultValue = "")
  private URI uri;
  @XmlElement(defaultValue = "")
  private URI webApplicationRootUri;
  @XmlElement(defaultValue = "")
  private String expiration;
  @XmlElement(defaultValue = "")
  private String creationDate;
  @XmlElement(defaultValue = "")
  private String user;

  @Override
  public URI getURI() {
    return uri;
  }

  public SharingEntity(URI uri, URI webApplicationRootUri, Ticket ticket) {
    this.uri = uri;
    this.webApplicationRootUri = webApplicationRootUri;
    if (ticket.getEndDate() != null) {
      this.expiration = Long.toString(ticket.getEndDate().getTime());
    }
    this.creationDate = Long.toString(ticket.getCreationDate().getTime());
    this.user = UserDetail.getById(ticket.getCreatorId()).getDisplayedName();
  }

}
