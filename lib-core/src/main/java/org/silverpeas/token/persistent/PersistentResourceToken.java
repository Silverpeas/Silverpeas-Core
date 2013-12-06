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
package org.silverpeas.token.persistent;

import com.silverpeas.util.StringUtil;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.silverpeas.EntityReference;
import org.silverpeas.token.Token;
import org.silverpeas.token.annotation.TokenGenerator;
import org.silverpeas.token.exception.TokenException;
import org.silverpeas.token.persistent.service.PersistentResourceTokenService;
import org.silverpeas.token.persistent.service.TokenServiceFactory;

import static com.silverpeas.util.StringUtil.isDefined;

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
public class PersistentResourceToken implements Token {

  private static final long serialVersionUID = 5956074363457906409L;

  /**
   * Represents none token to replace in more typing way the null keyword.
   */
  public static final PersistentResourceToken NoneToken = new PersistentResourceToken();

  @Id
  @TableGenerator(name = "UNIQUE_ID_GEN", table = "uniqueId", pkColumnName = "tablename",
      valueColumnName = "maxId", pkColumnValue = "st_token", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "UNIQUE_ID_GEN")
  @Column(name = "id")
  private Long id;

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

  /**
   * Creates a token for the specified resource.
   *
   * If the specified resource has already a token, then returns it. Otherwise a new token is
   * generated and persisted into the data source.
   *
   * @param resource the resource for which the token has to be generated.
   * @return a token for the specified resource.
   * @throws TokenException
   */
  public static PersistentResourceToken createToken(final EntityReference resource)
      throws TokenException {
    PersistentResourceTokenService service = TokenServiceFactory.getTokenService();
    return service.initialize(resource);
  }

  /**
   * Gets a the token from the specified value. If not token exist with the specified value, then
   * <code>NoneToken</code> is returned.
   *
   * @param token the value of the token to get.
   * @return the token that matches the specified value.
   */
  public static PersistentResourceToken getToken(String token) {
    PersistentResourceTokenService service = TokenServiceFactory.getTokenService();
    return service.get(token);
  }

  @PrePersist
  @PreUpdate
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
   * @throws TokenException
   */
  public void validate() throws TokenException {
    if (this.resourceType == null || EntityReference.UNKNOWN_TYPE.equals(resourceType)
        || !isDefined(resourceId)) {
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
        Logger.getLogger(PersistentResourceToken.class.getName()).log(Level.SEVERE, null, ex);
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
}
