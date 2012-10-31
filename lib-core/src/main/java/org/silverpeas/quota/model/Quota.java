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
package org.silverpeas.quota.model;

import static com.silverpeas.util.StringUtil.isDefined;
import static java.util.EnumSet.of;

import java.io.Serializable;
import java.math.BigDecimal;
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

import org.silverpeas.quota.contant.QuotaLoad;
import org.silverpeas.quota.contant.QuotaType;
import org.silverpeas.quota.exception.QuotaException;

/**
 * @author Yohann Chastagnier
 */
@Entity
@Table(name = "st_quota")
public class Quota implements Serializable {
  private static final long serialVersionUID = 6564633879921455848L;

  @Id
  @TableGenerator(name = "UNIQUE_ID_GEN", table = "uniqueId", pkColumnName = "tablename",
      valueColumnName = "maxId", pkColumnValue = "st_quota", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "UNIQUE_ID_GEN")
  @Column(name = "id")
  private Long id;

  @Column(name = "quotaType", nullable = false)
  private String type;

  @Column(name = "resourceId", nullable = false)
  private String resourceId;

  @Column(name = "minCount", nullable = false)
  private int minCount = 0;

  @Column(name = "maxCount", nullable = false)
  private int maxCount = 0;

  @Column(name = "currentCount", nullable = false)
  private int count = 0;

  @Column(name = "saveDate", nullable = false)
  @Temporal(value = TemporalType.TIMESTAMP)
  private Date saveDate;

  @PrePersist
  @PreUpdate
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
  public int getMinCount() {
    return minCount;
  }

  /**
   * @param minCount the minCount to set
   */
  public void setMinCount(final int minCount) {
    this.minCount = minCount;
  }

  /**
   * @param minCount the minCount to set
   */
  public void setMinCount(final String minCount) throws QuotaException {
    try {
      setMinCount(Integer.valueOf(minCount));
    } catch (final NumberFormatException nfe) {
      throw new QuotaException(this, "BAD_MIN_COUNT");
    }
  }

  /**
   * @return the maxCount
   */
  public int getMaxCount() {
    return maxCount;
  }

  /**
   * @param maxCount the maxCount to set
   */
  public void setMaxCount(final int maxCount) {
    this.maxCount = maxCount;
  }

  /**
   * @param maxCount the maxCount to set
   */
  public void setMaxCount(final String maxCount) throws QuotaException {
    try {
      setMaxCount(Integer.valueOf(maxCount));
    } catch (final NumberFormatException nfe) {
      throw new QuotaException(this, "BAD_MAX_COUNT");
    }
  }

  /**
   * @return the count
   */
  public int getCount() {
    return count;
  }

  /**
   * @param count the count to set
   */
  public void setCount(final int count) {
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
}
