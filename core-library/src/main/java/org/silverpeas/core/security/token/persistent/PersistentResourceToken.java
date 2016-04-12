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
package org.silverpeas.core.security.token.persistent;

import org.silverpeas.core.persistence.EntityReference;
import org.silverpeas.core.persistence.datasource.model.identifier.UniqueLongIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.AbstractJpaCustomEntity;
import org.silverpeas.core.security.token.annotation.TokenGenerator;
import org.silverpeas.core.security.token.Token;
import org.silverpeas.core.security.token.annotation.TokenGenerator;
import org.silverpeas.core.security.token.exception.TokenException;
import org.silverpeas.core.security.token.exception.TokenValidationException;
import org.silverpeas.core.security.token.persistent.service.PersistentResourceTokenService;
import org.silverpeas.core.security.token.persistent.service.TokenServiceProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * A persistent token used to identify uniquely a resource.
 *
 * This token has the particularity to be persisted in a data source and to refer the resource it
 * identifies uniquely both by the resource identifier and by the resource type.
 *
 * @author Yohann Chastagnier
 */
@Entity
@Table(name = "st_token")
@TokenGenerator(PersistentResourceTokenGenerator.class)
@NamedQueries({@NamedQuery(name = "PersistentResourceToken.getByTypeAndResourceId",
    query = "from PersistentResourceToken where resourceType = :type and resourceId = :resourceId"),
    @NamedQuery(name = "PersistentResourceToken.getByToken",
        query = "from PersistentResourceToken where token = :token")})
public class PersistentResourceToken
    extends AbstractJpaCustomEntity<PersistentResourceToken, UniqueLongIdentifier>
    implements Token {

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
  protected PersistentResourceToken(final EntityReference resource, String value) {
    this.value = value;
    this.resourceId = resource.getId();
    this.resourceType = resource.getType();
  }

  /**
   * Creates a token for the specified resource.
   *
   * If the specified resource has already a token, then renews it. Otherwise a new token is
   * generated and persisted into the data source.
   *
   * @param resource the resource for which the token has to be generated.
   * @return a token for the specified resource.
   * @throws TokenException
   */
  public static PersistentResourceToken createToken(final EntityReference resource) throws
      TokenException {
    PersistentResourceTokenService service = TokenServiceProvider.getTokenService();
    return service.initialize(resource);
  }

  /**
   * Gets a token for the specified resource and creates it if it doesn't exist.
   *
   * If the specified resource has already a token, then returns it. Otherwise a new token is
   * generated and persisted into the data source.
   *
   * @param resource the resource for which the token has to be generated.
   * @return a token for the specified resource.
   * @throws TokenException
   */
  public static PersistentResourceToken getOrCreateToken(final EntityReference resource) throws
      TokenException {
    PersistentResourceTokenService service = TokenServiceProvider.getTokenService();
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
    PersistentResourceTokenService service = TokenServiceProvider.getTokenService();
    return service.get(token);
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
   * Indicates if the token is well registred
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
   * @throws TokenValidationException
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
   * Gets a reference to the resource this token is for.
   *
   * @param <E> the concrete type of the entity.
   * @param <R> the concrete type of the reference to the entity.
   * @param referenceClass the expected concrete class of the <code>EntityReference</code>. This
   * class must be conform to the type of the resource.
   * @return a reference to the resource that owns this token or null if there is neither no
   * resource defined for this token nor no reference defined for the targeted type of resource.
   */
  public <E, R extends EntityReference<E>> R getResource(Class<R> referenceClass) {
    R ref = null;
    if (resourceType != null && !resourceType.equals(EntityReference.UNKNOWN_TYPE) && StringUtil.
        isDefined(resourceId)) {
      try {
        ref = referenceClass.getConstructor(String.class).newInstance(resourceId);
        if (!ref.getType().equals(resourceType)) {
          ref = null;
        }
      } catch (Exception ex) {
        SilverLogger.getLogger("core").error(ex.getMessage(), ex);
      }
    }
    return ref;
  }

  /**
   * Sets the resource to which this token belongs.
   *
   * @param resource an identifier of the resource for which this token is.
   */
  public void setResource(final EntityReference resource) {
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
}
