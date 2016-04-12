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
package org.silverpeas.core.admin.space.mock;

import org.silverpeas.core.admin.space.quota.ComponentSpaceQuotaKey;
import org.silverpeas.core.admin.space.quota.ComponentSpaceQuotaService;
import org.silverpeas.quota.exception.QuotaException;
import org.silverpeas.quota.model.Quota;
import org.silverpeas.quota.offset.AbstractQuotaCountingOffset;

import com.silverpeas.annotation.Service;

/**
 * No quota handling into unit tests
 * @author Yohann Chastagnier
 */
@Service
public class DefaultComponentSpaceQuotaService implements ComponentSpaceQuotaService {

  /*
   * (non-Javadoc)
   * @see QuotaService#getCurrentCount(QuotaKey)
   */
  @Override
  public long getCurrentCount(final ComponentSpaceQuotaKey key) throws QuotaException {
    return 0;
  }

  /*
   * (non-Javadoc)
   * @see QuotaService#initialize(QuotaKey, long)
   */
  @Override
  public Quota initialize(final ComponentSpaceQuotaKey key, final long maxCount)
      throws QuotaException {
    return new Quota();
  }

  /*
   * (non-Javadoc)
   * @see QuotaService#initialize(QuotaKey, long,
   * long)
   */
  @Override
  public Quota initialize(final ComponentSpaceQuotaKey key, final long minCount, final long maxCount)
      throws QuotaException {
    return new Quota();
  }

  @Override
  public Quota initialize(final ComponentSpaceQuotaKey key, final Quota quota)
      throws QuotaException {
    return null;
  }

  /*
     * (non-Javadoc)
     * @see QuotaService#get(QuotaKey)
     */
  @Override
  public Quota get(final ComponentSpaceQuotaKey key) throws QuotaException {
    return new Quota();
  }

  /*
   * (non-Javadoc)
   * @see QuotaService#verify(QuotaKey)
   */
  @Override
  public Quota verify(final ComponentSpaceQuotaKey key) throws QuotaException {
    return new Quota();
  }

  /*
   * (non-Javadoc)
   * @see QuotaService#verify(QuotaKey,
   * AbstractQuotaCountingOffset)
   */
  @Override
  public Quota verify(final ComponentSpaceQuotaKey key,
      final AbstractQuotaCountingOffset countingOffset) throws QuotaException {
    return new Quota();
  }

  /*
   * (non-Javadoc)
   * @see QuotaService#remove(QuotaKey)
   */
  @Override
  public void remove(final ComponentSpaceQuotaKey key) {
  }

  /*
   * (non-Javadoc)
   * @see
   * org.silverpeas.core.admin.space.quota.ComponentSpaceQuotaService#getQuotaReachedFromSpacePath(org.
   * silverpeas.admin.space.quota.ComponentSpaceQuotaKey)
   */
  @Override
  public Quota getQuotaReachedFromSpacePath(final ComponentSpaceQuotaKey key) {
    return new Quota();
  }
}
