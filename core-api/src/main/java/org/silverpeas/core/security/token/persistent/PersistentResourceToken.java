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
 * FLOSS exception. You should have received a copy of the text describing
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
package org.silverpeas.core.security.token.persistent;

import org.silverpeas.core.persistence.EntityReference;
import org.silverpeas.core.persistence.ResourceBelonging;
import org.silverpeas.core.persistence.datasource.model.identifier.UniqueLongIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.BasicJpaEntity;
import org.silverpeas.core.security.token.Token;
import org.silverpeas.core.security.token.annotation.TokenGenerator;
import org.silverpeas.core.security.token.exception.TokenException;
import org.silverpeas.core.security.token.exception.TokenValidationException;
import org.silverpeas.core.security.token.persistent.service.PersistentResourceTokenService;
import org.silverpeas.kernel.util.StringUtil;

import javax.persistence.*;
import java.util.Date;

/**
 * A persistent token used to identify uniquely a resource.
 * This token has the particularity to be persisted in a data source and to refer the resource it
 * identifies uniquely both by the resource identifier and by the resource type.
 *
 * @author Yohann Chastagnier
 */
@Entity
@Table(name = "st_token")
@TokenGenerator(PersistentResourceTokenGenerator.class)
@NamedQuery(name = "PersistentResourceToken.getByTypeAndResourceId",
    query = "select p from PersistentResourceToken p where p.resourceType = :type " +
        "and p.resourceId = :resourceId")
@NamedQuery(name = "PersistentResourceToken.getByToken",
    query = "select p from PersistentResourceToken p where p.value = :token")
public class PersistentResourceToken
    extends BasicJpaEntity<PersistentResourceToken, UniqueLongIdentifier>
    implements Token, ResourceBelonging {

  private static final long serialVersionUID = 5956074363457906409L;

  /**
   * Represents none token to replace in more typing way the null keyword.
   */
  public static final PersistentResourceToken NoneToken = new PersistentResourceToken();

  @Column(name = "tokenType", nullable = false)
  private String resourceType = EntityReference.UNKNOWN_TYPE;

  @Column(name = "resourceId", nullable = false)
  private String resourceId;

  @Column(name = "token", nullable = false)
  private String value;

  @Column(name = "saveCount", nullable = false)
  private int saveCount = 0;

  @Column(name = "saveDate", nullable = false)
  @Temporal(value = TemporalType.TIMESTAMP)
  private Date saveDate;

  protected PersistentResourceToken() {

  }

  /**
   * Constructs a new persistent token for the specified resource and with the specified value.
   *
   * @param resource a reference to the resource for which this token is constructed.
   * @param value the token value.
   */
  protected PersistentResourceToken(final EntityReference<?> resource, String value) {
    this.value = value;
    this.resourceId = resource.getId();
    this.resourceType = resource.getType();
  }

  /**
   * Gets a token for the specified resource and creates it if it doesn't exist.
   * <p>
   * If the specified resource has already a token, then returns it. Otherwise a new token is
   * generated and persisted into the data source.
   * </p>
   * @param resource the resource for which the token has to be generated.
   * @return a token for the specified resource.
   * @throws TokenException if the token cannot be created
   */
  public static PersistentResourceToken getOrCreateToken(final EntityReference<?> resource) throws
      TokenException {
    PersistentResourceTokenService service = PersistentResourceTokenService.get();
    PersistentResourceToken token = service.get(resource);
    if (!token.isDefined()) {
      token = service.initialize(resource);
    }
    return token;
  }

  /**
   * Gets a the token from the specified value. If not token exist with the specified value, then
   * <code>NoneToken</code> is returned.
   *
   * @param token the value of the token to get.
   * @return the token that matches the specified value.
   */
  public static PersistentResourceToken getToken(String token) {
    PersistentResourceTokenService service = PersistentResourceTokenService.get();
    return service.get(token);
  }

  /**
   * Removes the token for the specified resource.
   *
   * @param resource the resource for which the token has to be removed.
   */
  public static void removeToken(final EntityReference<?> resource) {
    PersistentResourceTokenService service = PersistentResourceTokenService.get();
    service.remove(resource);
  }

  @Override
  protected void performBeforePersist() {
    super.performBeforePersist();
    performSaveDate();
  }

  @Override
  protected void performBeforeUpdate() {
    super.performBeforeUpdate();
    performSaveDate();
  }

  private void performSaveDate() {
    setSaveCount(getSaveCount() + 1);
    setSaveDate(new Date());
  }

  /**
   * Indicates if the token is well registered
   *
   * @return a boolean indicating if this token exists in the data source.
   */
  public boolean exists() {
    return getId() != null;
  }

  /**
   * Indicates if the token isn't registered.
   *
   * @return a boolean indicating if this token doesn't exist in the data source.
   */
  public boolean notExists() {
    return getId() == null;
  }

  /**
   * Validates data
   *
   * @throws TokenValidationException if the data aren't valid.
   */
  public void validate() throws TokenValidationException {
    if (this.resourceType == null || EntityReference.UNKNOWN_TYPE.equals(resourceType)
        || !StringUtil.isDefined(resourceId)) {
      throw new TokenValidationException("The token isn't valid! Missing resource reference");
    }
  }

  /**
   * @param id the id to set
   */
  public void setId(final Long id) {
    setId(String.valueOf(id));
  }

  /**
   * Sets the resource to which this token belongs.
   *
   * @param resource an identifier of the resource for which this token is.
   */
  public void setResource(final EntityReference<?> resource) {
    if (resource != null) {
      this.resourceType = resource.getType();
      this.resourceId = resource.getId();
    }
  }

  /**
   * @return the value
   */
  @Override
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

  @Override
  public String toString() {
    return "PersistentResourceToken{" + "resourceType='" + resourceType + '\'' + ", resourceId='"
        + resourceId + '\'' + ", value='" + value + '\'' + '}';
  }

  @Override
  public boolean isDefined() {
    return this.exists() && this != NoneToken;
  }

  @Override
  public String getResourceType() {
    return this.resourceType;
  }

  @Override
  public String getResourceId() {
    return this.resourceId;
  }
}
