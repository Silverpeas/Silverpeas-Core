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
package org.silverpeas.core.admin.quota.service;

import org.silverpeas.core.admin.quota.constant.QuotaLoad;
import org.silverpeas.core.admin.quota.exception.QuotaException;
import org.silverpeas.core.admin.quota.exception.QuotaNotEnoughException;
import org.silverpeas.core.admin.quota.offset.AbstractQuotaCountingOffset;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.TransactionRuntimeException;
import org.silverpeas.core.admin.quota.QuotaKey;
import org.silverpeas.core.admin.quota.exception.QuotaFullException;
import org.silverpeas.core.admin.quota.exception.QuotaOutOfBoundsException;
import org.silverpeas.core.admin.quota.model.Quota;
import org.silverpeas.core.admin.quota.offset.SimpleQuotaCountingOffset;
import org.silverpeas.core.admin.quota.repository.QuotaManager;

import javax.inject.Inject;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractQuotaService<T extends QuotaKey> implements QuotaService<T> {

  @Inject
  private QuotaManager quotaRepository;

  @Override
  public Quota initialize(final T key, final long maxCount) throws QuotaException {
    return initialize(key, 0, maxCount);
  }

  @Override
  public Quota initialize(final T key, final Quota quota) throws QuotaException {
    return initialize(key, quota.getMinCount(), quota.getMaxCount());
  }

  @Override
  public Quota initialize(final T key, final long minCount, final long maxCount)
      throws QuotaException {
    return requiredTransaction(() -> {

      // Checking that it does not exist a quota with same key
      final Quota quota = getByQuotaKey(key);
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
    });
  }

  @Override
  public Quota get(final T key) throws QuotaException {
    return requiredTransaction(() -> {
      final Quota quota = getByQuotaKey(key);
      if (quota.exists()) {
        final long currentCount = getCurrentCount(key);
        if (quota.getCount() != currentCount) {
          quota.setCount(currentCount);
          quotaRepository.saveAndFlush(quota);
        }
      }
      return quota;
    });
  }

  /**
   * Private method to retrieve a Quota
   * @param key the key of the quota
   * @return the quota corresponding to the key if any, a new empty quota otherwise
   */
  private Quota getByQuotaKey(final T key) {
    Quota quota = null;
    if (key.isValid()) {
        quota =
            quotaRepository.getByTypeAndResourceId(key.getQuotaType().name(), key.getResourceId());
    }
    if (quota == null) {
      quota = new Quota();
    }
    return quota;
  }

  @Override
  public Quota verify(final T key) throws QuotaException {
    if (!isActivated()) {
      return new Quota();
    }
    return verify(key, SimpleQuotaCountingOffset.from(0));
  }

  @Override
  public Quota verify(final T key, final AbstractQuotaCountingOffset countingOffset)
      throws QuotaException {
    if (!isActivated()) {
      return new Quota();
    }
    // Returning the quota used by this verify process
    return verify(key, getByQuotaKey(key), countingOffset);
  }

  /**
   * Verify from a given quota
   */
  protected Quota verify(final T key, final Quota quota,
      final AbstractQuotaCountingOffset countingOffset) throws QuotaException {
    if (!isActivated()) {
      return new Quota();
    }
    if (quota.exists()) {
      long offset = countingOffset.getOffset();
      quota.setCount(getCurrentCount(key) + offset);
      final QuotaLoad quotaLoad = quota.getLoad();
      if (QuotaLoad.OUT_OF_BOUNDS.equals(quotaLoad)) {
        throw new QuotaOutOfBoundsException(quota);
      } else if (QuotaLoad.FULL.equals(quotaLoad)) {
        throw new QuotaFullException(quota);
      } else if (QuotaLoad.NOT_ENOUGH.equals(quotaLoad)) {
        throw new QuotaNotEnoughException(quota);
      }
    }
    return quota;
  }

  @Override
  public void remove(final T key) {
    Transaction.performInOne(() -> {
      final Quota quota = getByQuotaKey(key);
      if (quota.exists()) {
        quotaRepository.delete(quota);
      }
      return null;
    });
  }

  private <RETURN_VALUE> RETURN_VALUE requiredTransaction(
      final Transaction.Process<RETURN_VALUE> process) throws QuotaException {
    try {
      return Transaction.performInOne(process::execute);
    } catch (TransactionRuntimeException e) {
      if (e.getCause() instanceof QuotaException) {
        throw (QuotaException) e.getCause();
      }
      throw e;
    }
  }

  /**
   * Indicates if the type of quota is activated
   * @return true if activated, false otherwise
   */
  abstract protected boolean isActivated();
}
