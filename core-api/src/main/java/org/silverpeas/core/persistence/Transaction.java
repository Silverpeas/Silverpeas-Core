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
package org.silverpeas.core.persistence;


import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.annotation.Technical;
import org.silverpeas.core.util.Process;

import javax.annotation.Resource;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.Transactional;

/**
 * A transaction. All processes it performs will be in charge by the JPA transaction manager.
 * @author mmoquillon
 */
@Technical
@Bean
public class Transaction {

  @Resource(mappedName = "java:/TransactionManager")
  private TransactionManager transactionManager;

  /**
   * Gets a transaction instance in order to perform some transactional tasks.
   * @return a transaction.
   */
  public static Transaction getTransaction() {
    return TransactionProvider.getTransaction();
  }

  /**
   * Performs in a single transaction the specified process.
   * @param process the process to execute in a transaction.
   * @param <V> the type of the return value.
   * @return the result of the process.
   */
  public static <V> V performInOne(final Process<V> process) {
    return getTransaction().perform(process);
  }

  /**
   * Performs in a new transaction the specified process.
   * @param process the process to execute in a transaction.
   * @param <V> the type of the return value.
   * @return the result of the process.
   */
  public static <V> V performInNew(final Process<V> process) {
    return getTransaction().performNew(process);
  }

  /**
   * Is there a transaction currently active in the current thread?
   * @return true if there is a transaction active in the current thread, false otherwise.
   */
  public static boolean isTransactionActive() {
    return getTransaction().isActive();
  }

  /**
   * Gets the status of the transaction currently in the current thread. If there is no
   * transaction, then {@link javax.transaction.Status#STATUS_NO_TRANSACTION} is returned.
   * @see javax.transaction.Status
   * @return the current transaction's status.
   */
  @SuppressWarnings("unused")
  public static int getTransactionStatus() {
    return getTransaction().getStatus();
  }

  /**
   * The given process is executed in a transaction: support a current transaction,
   * create a new one if none exists.
   * Analogous to EJB transaction attribute of the same name.
   * @param process the process to execute in a transaction.
   * @param <V> the type of the return value.
   * @return the result of the process.
   */
  @Transactional
  public <V> V perform(final Process<V> process) {
    try {
      return process.execute();
    } catch (Exception e) {
      throw new TransactionRuntimeException(e);
    }
  }

  /**
   * Gets the current status of this transaction.
   * @return the status code of this transaction.
   * @see javax.transaction.Status
   */
  public int getStatus() {
    try {
      return transactionManager.getStatus();
    } catch (SystemException e) {
      throw new TransactionRuntimeException(e);
    }
  }

  /**
   * The given process is executed in a new transaction, even it is called from an existent one.
   * Analogous to EJB transaction attribute of the same name.
   * @param process the process to execute in a transaction.
   * @param <V> the type of the return value.
   * @return the result of the process.
   */
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public <V> V performNew(final Process<V> process) {
    try {
      return process.execute();
    } catch (Exception e) {
      throw new TransactionRuntimeException(e);
    }
  }

  @Transactional(Transactional.TxType.MANDATORY)
  protected boolean isActive() {
    return getStatus() == Status.STATUS_ACTIVE;
  }

}
