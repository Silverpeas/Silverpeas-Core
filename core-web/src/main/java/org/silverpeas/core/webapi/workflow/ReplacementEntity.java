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

package org.silverpeas.core.webapi.workflow;

import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.webapi.base.WebEntity;
import org.silverpeas.core.webapi.util.UserEntity;
import org.silverpeas.core.workflow.api.user.Replacement;
import org.silverpeas.core.workflow.api.user.User;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.time.LocalDate;

/**
 * The web entity that is carried by an HTTP request or an HTTP response between a web client and
 * the replacement web resource.
 * The web entity is marshaled into a representation negotiated between the client and the
 * resource. In Silverpeas, usually the supported representation is JSON.
 * @author mmoquillon
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ReplacementEntity implements WebEntity {

  @XmlElement(nillable = true)
  private URI uri;
  @XmlElement(required = true)
  private UserEntity incumbent;
  @XmlElement(required = true)
  private UserEntity substitute;
  @XmlElement(required = true)
  private String startDate;
  @XmlElement(required = true)
  private String endDate;
  @XmlElement(required = true)
  private String workflowId;


  ReplacementEntity(final Replacement replacement) {
    this.incumbent = new UserEntity(asUserDetail(replacement.getIncumbent()));
    this.substitute = new UserEntity(asUserDetail(replacement.getSubstitute()));
    this.startDate = replacement.getPeriod().getStartDate().toString();
    this.endDate = replacement.getPeriod().getEndDate().toString();
    this.workflowId = replacement.getWorkflowInstanceId();
  }

  protected ReplacementEntity() {
    // for the JSON/XML unmarshaller
  }

  public static ReplacementEntity asWebEntity(final Replacement replacement, final URI uri) {
    ReplacementEntity entity = new ReplacementEntity(replacement);
    entity.uri = uri;
    return entity;
  }

  @Override
  public URI getURI() {
    return this.uri;
  }

  public UserEntity getIncumbent() {
    return incumbent;
  }

  public UserEntity getSubstitute() {
    return substitute;
  }

  public LocalDate getStartDate() {
    return LocalDate.parse(this.startDate);
  }

  public LocalDate getEndDate() {
    return LocalDate.parse(this.endDate);
  }

  public String getWorkflowInstanceId() {
    return this.workflowId;
  }

  private UserDetail asUserDetail(final User user) {
    return UserDetail.getById(user.getUserId());
  }

  public void setSubstitute(final User substitute) {
    this.substitute = new UserEntity(asUserDetail(substitute));
  }

  public void setStartDate(final LocalDate date) {
    this.startDate = date.toString();
  }

  public void setEndDate(final LocalDate date) {
    this.endDate = date.toString();
  }

}
  