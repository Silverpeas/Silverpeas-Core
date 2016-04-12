/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.core.persistence;


import javax.transaction.Transactional;

/**
 * A transaction. All processes it performs will be in charge by the JPA transaction manager.
 * @author mmoquillon
 */
public class Transaction {

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
   * @param <RETURN_VALUE> the type of the return value.
   * @return the result of the process.
   */
  public static <RETURN_VALUE> RETURN_VALUE performInOne(final Process<RETURN_VALUE> process) {
    return getTransaction().perform(process);
  }

  /**
   * Performs in a new transaction the specified process.
   * @param process the process to execute in a transaction.
   * @param <RETURN_VALUE> the type of the return value.
   * @return the result of the process.
   */
  public static <RETURN_VALUE> RETURN_VALUE performInNew(final Process<RETURN_VALUE> process) {
    return getTransaction().performNew(process);
  }

  /**
   * The given process is executed in a transaction : support a current transaction,
   * create a new one if none exists.
   * Analogous to EJB transaction attribute of the same name.
   * @param process the process to execute in a transaction.
   * @param <RETURN_VALUE> the type of the return value.
   * @return the result of the process.
   */
  @Transactional
  public <RETURN_VALUE> RETURN_VALUE perform(final Process<RETURN_VALUE> process) {
    try {
      return process.execute();
    } catch (Exception e) {
      throw new TransactionRuntimeException(e);
    }
  }

  /**
   * The given process is executed in a new transaction, even it is called from an existent one.
   * Analogous to EJB transaction attribute of the same name.
   * @param process the process to execute in a transaction.
   * @param <RETURN_VALUE> the type of the return value.
   * @return the result of the process.
   */
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public <RETURN_VALUE> RETURN_VALUE performNew(final Process<RETURN_VALUE> process) {
    try {
      return process.execute();
    } catch (Exception e) {
      throw new TransactionRuntimeException(e);
    }
  }

  /**
   * Defines a process to execute.
   * @param <RETURN_VALUE> the type of the returned value.
   */
  @FunctionalInterface
  public interface Process<RETURN_VALUE> {
    RETURN_VALUE execute() throws Exception;
  }
}
