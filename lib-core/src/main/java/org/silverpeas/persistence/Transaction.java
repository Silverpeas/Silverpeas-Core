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
package org.silverpeas.persistence;

import com.silverpeas.annotation.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * A transaction. All processes it performs will be in charge by the JPA transaction manager.
 * @author mmoquillon
 */
@Service
public class Transaction {

  public static Transaction getTransaction() {
    TransactionFactory factory = TransactionFactory.getFactory();
    return factory.getTransaction();
  }

  /**
   * The given process is executed in a transaction : support a current transaction,
   * create a new one if none exists.
   * Analogous to EJB transaction attribute of the same name.
   * @param process
   * @param <RETURN_VALUE>
   * @return
   */
  @Transactional
  public <RETURN_VALUE> RETURN_VALUE perform(final Process<RETURN_VALUE> process) {
    return process.execute();
  }

  /**
   * Defines a process to execute.
   * @param <RETURN_VALUE> the type of the returned value.
   */
  public interface Process<RETURN_VALUE> {
    RETURN_VALUE execute();
  }
}
