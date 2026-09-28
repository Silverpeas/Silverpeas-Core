/*
 * Copyright (C) 2000 - 2026 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
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
package org.silverpeas.core.sharing.model;

import java.util.Date;

/**
 * The attributes required to create a sharing ticket.
 */
public final class TicketDetail {
  private final long sharedObjectId;
  private final String componentId;
  private final String creatorId;
  private final Date creationDate;
  private final Date endDate;
  private final int nbAccessMax;
  private final String securityCode;
  private final String sharedObjectType;
  private final String token;

  private TicketDetail(Builder builder) {
    this.sharedObjectId = builder.sharedObjectId;
    this.componentId = builder.componentId;
    this.creatorId = builder.creatorId;
    this.creationDate = builder.creationDate;
    this.endDate = builder.endDate;
    this.nbAccessMax = builder.nbAccessMax;
    this.securityCode = builder.securityCode;
    this.sharedObjectType = builder.sharedObjectType;
    this.token = builder.token;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(TicketDetail detail) {
    return new Builder(detail);
  }

  public long getSharedObjectId() {
    return sharedObjectId;
  }

  public String getComponentId() {
    return componentId;
  }

  public String getCreatorId() {
    return creatorId;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  public int getNbAccessMax() {
    return nbAccessMax;
  }

  public String getSecurityCode() {
    return securityCode;
  }

  public String getSharedObjectType() {
    return sharedObjectType;
  }

  public String getToken() {
    return token;
  }

  public static class Builder {
    private long sharedObjectId;
    private String componentId;
    private String creatorId;
    private Date creationDate;
    private Date endDate;
    private int nbAccessMax;
    private String securityCode;
    private String sharedObjectType;
    private String token;

    protected Builder() {
    }

    protected Builder(TicketDetail detail) {
      this.sharedObjectId = detail.getSharedObjectId();
      this.componentId = detail.getComponentId();
      this.creatorId = detail.getCreatorId();
      this.creationDate = detail.getCreationDate();
      this.endDate = detail.getEndDate();
      this.nbAccessMax = detail.getNbAccessMax();
      this.securityCode = detail.getSecurityCode();
      this.sharedObjectType = detail.getSharedObjectType();
      this.token = detail.getToken();
    }

    public Builder setSharedObjectId(long sharedObjectId) {
      this.sharedObjectId = sharedObjectId;
      return this;
    }

    public Builder setComponentId(String componentId) {
      this.componentId = componentId;
      return this;
    }

    public Builder setCreatorId(String creatorId) {
      this.creatorId = creatorId;
      return this;
    }

    public Builder setCreationDate(Date creationDate) {
      this.creationDate = creationDate;
      return this;
    }

    public Builder setEndDate(Date endDate) {
      this.endDate = endDate;
      return this;
    }

    public Builder setNbAccessMax(int nbAccessMax) {
      this.nbAccessMax = nbAccessMax;
      return this;
    }

    public Builder setSecurityCode(String securityCode) {
      this.securityCode = securityCode;
      return this;
    }

    public Builder setSharedObjectType(String sharedObjectType) {
      this.sharedObjectType = sharedObjectType;
      return this;
    }

    public Builder setToken(String token) {
      this.token = token;
      return this;
    }

    public TicketDetail build() {
      return new TicketDetail(this);
    }
  }
}
