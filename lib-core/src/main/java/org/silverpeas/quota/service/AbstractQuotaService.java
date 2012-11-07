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
package org.silverpeas.quota.service;

import javax.inject.Inject;

import org.silverpeas.quota.QuotaKey;
import org.silverpeas.quota.contant.QuotaLoad;
import org.silverpeas.quota.exception.QuotaException;
import org.silverpeas.quota.exception.QuotaFullException;
import org.silverpeas.quota.exception.QuotaNotEnoughException;
import org.silverpeas.quota.exception.QuotaOutOfBoundsException;
import org.silverpeas.quota.model.Quota;
import org.silverpeas.quota.offset.AbstractQuotaCountingOffset;
import org.silverpeas.quota.offset.SimpleQuotaCountingOffset;
import org.silverpeas.quota.repository.QuotaRepository;
import org.silverpeas.quota.service.dao.QuotaDAO;
import org.silverpeas.quota.service.dao.jdbc.JDBCQuotaDAO;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractQuotaService<T extends QuotaKey> implements QuotaService<T> {

  @Inject
  private QuotaRepository quotaRepository;

  // This QuotaDAO is a workaround in the aim to deal with different transaction systems.
  // That is why there is no injection at this level and just an Quota DAO Class instantiation.
  // By this way, no Spring specification file is impacted.
  private static QuotaDAO quotaDAO = new JDBCQuotaDAO();

  /*
   * (non-Javadoc)
   * @see org.silverpeas.quota.service.QuotaService#initialize(org.silverpeas.quota.QuotaKey, int)
   */
  @Override
  public Quota initialize(final T key, final long maxCount) throws QuotaException {
    return initialize(key, 0, maxCount);
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.quota.service.QuotaService#initialize(org.silverpeas.quota.QuotaKey, int,
   * int)
   */
  @Transactional(propagation = Propagation.REQUIRED)
  @Override
  public Quota initialize(final T key, final long minCount, final long maxCount)
      throws QuotaException {

    // Checking that it does not exist a quota with same key
    final Quota quota = getByQuotaKey(key, false);
    if (!quota.exists()) {

      // If quota does not exist and maxCount is zero : stop
      if (maxCount == 0) {
        return quota;
      }

      // Initializing the quota
      quota.setType(key.getQuotaType());
      quota.setResourceId(key.getResourceId());
    }

    // Modifying and saving if changes are detected
    if (!quota.exists() || minCount != quota.getMinCount() || maxCount != quota.getMaxCount()) {

      // Setting the quota
      quota.setMinCount(minCount);
      quota.setMaxCount(maxCount);

      // Validating
      quota.validate();

      // Saving
      quotaRepository.saveAndFlush(quota);
    }

    // Returning the initialized quota
    return quota;
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.quota.service.QuotaService#get(org.silverpeas.quota.QuotaKey)
   */
  @Transactional(propagation = Propagation.REQUIRED)
  @Override
  public Quota get(final T key) throws QuotaException {
    final Quota quota = getByQuotaKey(key, false);
    if (quota.exists()) {
      final long currentCount = getCurrentCount(key);
      if (quota.getCount() != currentCount) {
        quota.setCount(currentCount);
        quotaRepository.saveAndFlush(quota);
      }
    }
    return quota;
  }

  /**
   * Private method to retrieve a Quota
   * @param key
   * @param jpaBypass
   * @return
   */
  private Quota getByQuotaKey(final T key, final boolean jpaBypass) {
    Quota quota = null;
    if (key.isValid()) {
      if (jpaBypass) {
        quota = quotaDAO.getByTypeAndResourceId(key.getQuotaType().name(), key.getResourceId());
      } else {
        quota =
            quotaRepository.getByTypeAndResourceId(key.getQuotaType().name(), key.getResourceId());
      }
    }
    if (quota == null) {
      quota = new Quota();
    }
    return quota;
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.quota.service.QuotaService#verify(org.silverpeas.quota.QuotaKey)
   */
  @Override
  public Quota verify(final T key) throws QuotaException {
    return verify(key, SimpleQuotaCountingOffset.from(0));
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.quota.service.QuotaService#verify(org.silverpeas.quota.QuotaKey, int)
   */
  @Override
  public Quota verify(final T key, final AbstractQuotaCountingOffset countingOffset)
      throws QuotaException {
    final Quota quota = getByQuotaKey(key, true);
    if (quota.exists()) {
      quota.setCount(getCurrentCount(key) + countingOffset.getOffset());
      final QuotaLoad quotaLoad = quota.getLoad();
      if (QuotaLoad.OUT_OF_BOUNDS.equals(quotaLoad)) {
        throw new QuotaOutOfBoundsException(quota);
      } else if (QuotaLoad.FULL.equals(quotaLoad)) {
        throw new QuotaFullException(quota);
      } else if (QuotaLoad.NOT_ENOUGH.equals(quotaLoad)) {
        throw new QuotaNotEnoughException(quota);
      }
    }

    // Returning the quota used by this verify process
    return quota;
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.quota.service.QuotaService#remove(org.silverpeas.quota.QuotaKey)
   */
  @Transactional(propagation = Propagation.REQUIRED)
  @Override
  public void remove(final T key) {
    final Quota quota = getByQuotaKey(key, false);
    if (quota.exists()) {
      quotaRepository.delete(quota);
    }
  }
}
