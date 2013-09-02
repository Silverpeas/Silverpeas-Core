/*
 * Copyright (C) 2000 - 2012 Silverpeas
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
package org.silverpeas.token.model;

import static com.silverpeas.util.StringUtil.isDefined;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.silverpeas.token.constant.TokenType;
import org.silverpeas.token.exception.TokenException;

/**
 * @author Yohann Chastagnier
 */
@Entity
@Table(name = "st_token")
public class Token implements Serializable {
  private static final long serialVersionUID = 5956074363457906409L;

  @Id
  @TableGenerator(name = "UNIQUE_ID_GEN", table = "uniqueId", pkColumnName = "tablename",
      valueColumnName = "maxId", pkColumnValue = "st_token", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "UNIQUE_ID_GEN")
  @Column(name = "id")
  private Long id;

  @Column(name = "tokenType", nullable = false)
  private String type = TokenType.UNKNOWN.name();

  @Column(name = "resourceId", nullable = false)
  private String resourceId;

  @Column(name = "token", nullable = false)
  private String value;

  @Column(name = "saveCount", nullable = false)
  private int saveCount = 0;

  @Column(name = "saveDate", nullable = false)
  @Temporal(value = TemporalType.TIMESTAMP)
  private Date saveDate;

  @PrePersist
  @PreUpdate
  private void performSaveDate() {
    setSaveCount(getSaveCount() + 1);
    setSaveDate(new Date());
  }

  /**
   * Indicates if the token is well registred
   * @return
   */
  public boolean exists() {
    return getId() != null;
  }

  /**
   * Validates data
   * @throws TokenException
   */
  public void validate() throws TokenException {
    if (getType() == null || TokenType.UNKNOWN.equals(getType()) || !isDefined(getResourceId())) {
      throw new TokenException(this, "EX_DATA_ARE_MISSING");
    }
  }

  /**
   * @return the id
   */
  public Long getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(final Long id) {
    this.id = id;
  }

  /**
   * @return the type
   */
  public TokenType getType() {
    if (type == null) {
      return null;
    }
    return TokenType.valueOf(type);
  }

  /**
   * @param type the type to set
   */
  public void setType(final TokenType type) {
    if (type == null) {
      this.type = null;
    } else {
      this.type = type.name();
    }
  }

  /**
   * @return the resourceId
   */
  public String getResourceId() {
    return resourceId;
  }

  /**
   * @param resourceId the resourceId to set
   */
  public void setResourceId(final String resourceId) {
    this.resourceId = resourceId;
  }

  /**
   * @return the value
   */
  public String getValue() {
    return value;
  }

  /**
   * @param value the value to set
   */
  public void setValue(final String value) {
    this.value = value;
  }

  /**
   * @return the saveCount
   */
  public int getSaveCount() {
    return saveCount;
  }

  /**
   * @param saveCount the saveCount to set
   */
  public void setSaveCount(final int saveCount) {
    this.saveCount = saveCount;
  }

  /**
   * @return the saveDate
   */
  public Date getSaveDate() {
    return saveDate;
  }

  /**
   * @param saveDate the saveDate to set
   */
  public void setSaveDate(final Date saveDate) {
    this.saveDate = saveDate;
  }
}
