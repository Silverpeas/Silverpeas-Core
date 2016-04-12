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
package org.silverpeas.core.admin.quota.model;

import org.silverpeas.core.admin.quota.constant.QuotaLoad;
import org.silverpeas.core.admin.quota.constant.QuotaType;
import org.silverpeas.core.admin.quota.exception.QuotaException;
import org.silverpeas.core.persistence.datasource.model.identifier.UniqueLongIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.AbstractJpaCustomEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import static java.util.EnumSet.of;
import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * @author Yohann Chastagnier
 */
@Entity
@Table(name = "st_quota")
@NamedQueries({@NamedQuery(name = "Quota.getByTypeAndResourceId",
    query = "from Quota where type = :type and resourceId = :resourceId")})
public class Quota extends AbstractJpaCustomEntity<Quota, UniqueLongIdentifier>
    implements Serializable, Cloneable {
  private static final long serialVersionUID = 6564633879921455848L;

  @Column(name = "quotaType", nullable = false)
  private String type;

  @Column(name = "resourceId", nullable = false)
  private String resourceId;

  @Column(name = "minCount", nullable = false)
  private long minCount = 0;

  @Column(name = "maxCount", nullable = false)
  private long maxCount = 0;

  @Column(name = "currentCount", nullable = false)
  private long count = 0;

  @Column(name = "saveDate", nullable = false)
  @Temporal(value = TemporalType.TIMESTAMP)
  private Date saveDate;

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
    setSaveDate(new Date());
  }

  /**
   * Indicates if the quota is well registred
   * @return
   */
  public boolean exists() {
    return getId() != null;
  }

  /**
   * Validates data
   * @throws QuotaException
   */
  public void validate() throws QuotaException {
    if (getType() == null || !isDefined(getResourceId())) {
      throw new QuotaException(this, "HAS_BAD_DATA");
    }
    validateBounds();
  }

  /**
   * Validates count data
   * @throws QuotaException
   */
  public void validateBounds() throws QuotaException {
    if (getMinCount() < 0 || getMaxCount() < 0 || getMinCount() > getMaxCount()) {
      throw new QuotaException(this, "HAS_BAD_DATA");
    }
  }

  /**
   * Indicates by a basic way the load of the data
   * @return
   */
  public QuotaLoad getLoad() {
    final QuotaLoad quotaLoad;
    if (getMaxCount() > 0) {
      if (getCount() > getMaxCount()) {
        quotaLoad = QuotaLoad.OUT_OF_BOUNDS;
      } else if (getCount() == 0) {
        quotaLoad = QuotaLoad.EMPTY;
      } else if (getCount() < getMinCount()) {
        quotaLoad = QuotaLoad.NOT_ENOUGH;
      } else if (getCount() == getMaxCount()) {
        quotaLoad = QuotaLoad.FULL;
      } else {
        quotaLoad = QuotaLoad.NOT_FULL;
      }
    } else {
      quotaLoad = QuotaLoad.UNLIMITED;
    }
    return quotaLoad;
  }

  /**
   * Indicates if the quota is reached or not
   * @return
   */
  public boolean isReached() {
    return exists() && of(QuotaLoad.FULL, QuotaLoad.OUT_OF_BOUNDS).contains(getLoad());
  }

  /**
   * Calculates the load rate of the quota without rounded rounded at 20 decimals
   * @return
   */
  public BigDecimal getLoadRate() {
    final BigDecimal loadRate;
    if (!QuotaLoad.UNLIMITED.equals(getLoad())) {
      loadRate =
          new BigDecimal(String.valueOf(getCount())).divide(
              new BigDecimal(String.valueOf(getMaxCount())), 20, BigDecimal.ROUND_HALF_DOWN);
    } else {
      loadRate = BigDecimal.ZERO;
    }
    return loadRate;
  }

  /**
   * Calculates the load percentage of the quota rounded at two decimals
   * @return
   */
  public BigDecimal getLoadPercentage() {
    return getLoadRate().multiply(new BigDecimal(String.valueOf(100))).setScale(2,
        BigDecimal.ROUND_HALF_DOWN);
  }

  /**
   * @param id the id to set
   */
  public void setQuotaId(final Long id) {
    setId(String.valueOf(id));
  }

  /**
   * @return the type
   */
  public QuotaType getType() {
    if (type == null) {
      return null;
    }
    return QuotaType.valueOf(type);
  }

  /**
   * @param type the type to set
   */
  public void setType(final QuotaType type) {
    if (type == null) {
      this.type = null;
    } else {
      this.type = type.name();
    }
  }

  /**
   * @param type the type to set
   */
  public void setType(final String type) {
    this.type = type;
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
   * @return the minCount
   */
  public long getMinCount() {
    return minCount;
  }

  /**
   * @param minCount the minCount to set
   */
  public void setMinCount(final long minCount) {
    this.minCount = minCount;
  }

  /**
   * @param minCount the minCount to set
   */
  public void setMinCount(final String minCount) throws QuotaException {
    try {
      setMinCount(Long.valueOf(minCount));
    } catch (final NumberFormatException nfe) {
      throw new QuotaException(this, "BAD_MIN_COUNT");
    }
  }

  /**
   * @return the maxCount
   */
  public long getMaxCount() {
    return maxCount;
  }

  /**
   * @param maxCount the maxCount to set
   */
  public void setMaxCount(final long maxCount) {
    this.maxCount = maxCount;
  }

  /**
   * @param maxCount the maxCount to set
   */
  public void setMaxCount(final String maxCount) throws QuotaException {
    try {
      setMaxCount(Long.valueOf(maxCount));
    } catch (final NumberFormatException nfe) {
      throw new QuotaException(this, "BAD_MAX_COUNT");
    }
  }

  /**
   * @return the count
   */
  public long getCount() {
    return count;
  }

  /**
   * @param count the count to set
   */
  public void setCount(final long count) {
    this.count = count;
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
    StringBuilder builder = new StringBuilder();
    builder.append("Quota infos for ");
    builder.append("resourceId=").append(getResourceId()).append(" and ");
    builder.append("type=").append(getType()).append(": ");
    if (!exists()) {
      builder.append("does not exist");
    } else {
      builder.append("load=").append(getLoad()).append(", ");
      builder.append("count=").append(getCount()).append(", ");
      builder.append("mincount=").append(getMinCount()).append(", ");
      builder.append("maxcount=").append(getMaxCount());
    }
    return builder.toString();
  }
}
