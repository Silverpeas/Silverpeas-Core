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
package org.silverpeas.core.admin;

import org.silverpeas.core.admin.user.model.UserDetail;

import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author silveryocha
 */
public interface TransactionTestService {

  @Transactional
  Connection transaction1() throws SQLException, InterruptedException;

  @Transactional
  Connection transaction2(UserDetail user) throws SQLException;

  @Transactional
  void performInDefaultTransaction(TransactionTestProcess process, Exception exceptionAtEnd)
      throws Exception;

  @Transactional
  void transactionWithDefaultTransactionManagement(Exception exceptionAtEnd) throws Exception;

  @Transactional(rollbackOn = TransactionCheckedException.class)
  void transactionWithCheckedTransactionManagement(Exception exceptionAtEnd) throws Exception;

  @Transactional(value = Transactional.TxType.MANDATORY)
  void saveNewUserWithMandatoryTransactionAndDefaultHandledException(Exception exceptionAtEnd)
      throws Exception;

  @Transactional(value = Transactional.TxType.MANDATORY, rollbackOn = TransactionCheckedException
      .class)
  void saveNewUserWithMandatoryTransactionAndCheckedHandledException(Exception exceptionAtEnd)
      throws Exception;

  @Transactional(value = Transactional.TxType.REQUIRED)
  void saveNewUserWithRequiredTransactionAndDefaultHandledException(Exception exceptionAtEnd)
      throws Exception;

  @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = TransactionCheckedException
      .class)
  void saveNewUserWithRequiredTransactionAndCheckedHandledException(Exception exceptionAtEnd)
      throws Exception;

  @Transactional(value = Transactional.TxType.REQUIRES_NEW)
  void saveNewUserWithRequiresNewTransactionAndDefaultHandledException(Exception exceptionAtEnd)
      throws Exception;

  @Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn =
      TransactionCheckedException.class)
  void saveNewUserWithRequiresNewTransactionAndCheckedHandledException(Exception exceptionAtEnd)
      throws Exception;
}
