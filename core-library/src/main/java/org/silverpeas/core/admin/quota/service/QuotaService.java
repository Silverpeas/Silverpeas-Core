/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
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
package org.silverpeas.core.admin.quota.service;

import org.silverpeas.core.admin.quota.QuotaKey;
import org.silverpeas.core.admin.quota.exception.QuotaException;
import org.silverpeas.core.admin.quota.model.Quota;
import org.silverpeas.core.admin.quota.offset.AbstractQuotaCountingOffset;

/**
 * @author Yohann Chastagnier
 */
public interface QuotaService<T extends QuotaKey> {

  /**
   * Gets the current count from a given quota key
   * @param key the key
   * @return the current count
   * @throws QuotaException on error
   */
  long getCurrentCount(T key) throws QuotaException;

  /**
   * Initializes the quota of the resource for the given quota key.
   * @param key the key
   * @param maxCount the maximum count
   * @return the quota
   * @throws QuotaException on error
   */
  Quota initialize(T key, long maxCount) throws QuotaException;

  /**
   * Initializes the quota of the resource for the given quota key.
   * @param key the key
   * @param minCount the minimum count
   * @param maxCount the maximum count
   * @return the quota
   * @throws QuotaException on error
   */
  Quota initialize(T key, long minCount, long maxCount) throws QuotaException;

  /**
   * Initializes the quota of the resource for the given quota key and from an existing quota.
   * @param key the key
   * @param quota a quota
   * @return the quota
   * @throws QuotaException on error
   */
  Quota initialize(T key, Quota quota) throws QuotaException;

  /**
   * Gets the quota of the resource from a given quota key.
   * A save operation is done if the current count has changed.
   * @param key the key
   * @return the quota mapped with the key
   */
  Quota get(T key) throws QuotaException;

  /**
   * Verifies if the quota is full or not enough from a given quota key.
   * <p>
   * Be aware of that the quota count will be loaded and computed each time this signature is
   * called.
   * </p>
   * If full then a quota full exception is thrown
   * If not enough then a quota not enough exception is thrown.
   * @param key the key
   * @return the quota used by the verify treatment
   * @throws QuotaException on error
   */
  Quota verify(T key) throws QuotaException;

  /**
   * Verifies if the quota is full or not enough from a given quota key and adding a counting
   * offset.
   * <p>
   * Be aware of that the quota count will be loaded and computed each time this signature is
   * called.
   * </p>
   * If full then a quota full exception is throw.
   * If not enough then a quota not enough exception is throw.
   * @param key the key
   * @param countingOffset a counting offset
   * @return the quota used by the verify treatment
   * @throws QuotaException on error
   */
  Quota verify(T key, AbstractQuotaCountingOffset countingOffset) throws QuotaException;

  /**
   * Verifies if the given loaded and computed quota.
   * <p>
   * Be aware of that the quota count is not again computed by this service. When this signature
   * is called, it means that the {@link Quota} instance has been already loaded and there is no
   * need to perform again the counting.
   * </p>
   * If full then a quota full exception is thrown
   * If not enough then a quota not enough exception is thrown.
   * @param key the key
   * @param quota a loaded quota
   * @return the quota used by the verify treatment
   * @throws QuotaException on error
   */
  Quota verify(T key, Quota quota) throws QuotaException;

  /**
   * Verifies if the given loaded and computed quota by adding a counting offset.
   * <p>
   * Be aware of that the quota count is not again computed by this service. When this signature
   * is called, it means that the {@link Quota} instance has been already loaded and there is no
   * need to perform again the counting.
   * </p>
   * If full then a quota full exception is thrown.
   * If not enough then a quota not enough exception is thrown.
   * @param key the key
   * @param quota a loaded quota
   * @param countingOffset a counting offset
   * @throws QuotaException on error
   * @return a copied quota from the given one containing the count with the offset used by
   * the verify treatment
   */
  Quota verify(T key, Quota quota, AbstractQuotaCountingOffset countingOffset) throws QuotaException;

  /**
   * Removes quietly the quota of the resource from a given quota key.
   * @param key the key
   */
  void remove(T key);
}
