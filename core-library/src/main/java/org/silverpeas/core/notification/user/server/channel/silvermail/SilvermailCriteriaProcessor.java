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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.notification.user.server.channel.silvermail;

import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.notification.user.server.channel.silvermail.SilvermailCriteria
    .QUERY_ORDER_BY;

import java.util.List;

/**
 * A processor of a user notification criteria. The aim of a such processor is to process each
 * criterion of the criteria in the order expected by the caller in order to perform some specific
 * works.
 * @author Yohann Chastagnier
 */
public interface SilvermailCriteriaProcessor {

  /**
   * Informs the processor the start of the process. The processor use this method to allocate all
   * the resources required by the processing here. It uses it to initialize the processor state
   * machine.
   */
  void startProcessing();

  /**
   * Informs the processor the process is ended. The processor use this method to deallocate all
   * the resources that were used during the processing. It uses it to tear down the processor
   * state
   * machine or to finalize some treatments.
   * <p>
   * The processing has to stop once this method is called. Hence, the call of process methods
   * should result to nothing or to an exception.
   */
  void endProcessing();

  /**
   * Informs the processor that there is a new criterion to process. This method must be used by
   * the caller to chain the different criterion processings.
   * @return the processor itself.
   */
  SilvermailCriteriaProcessor then();

  /**
   * Processes the criterion on the message identifiers.
   * @param ids the identifier of messages.
   * @return the processor itself.
   */
  SilvermailCriteriaProcessor processByIds(List<Long> ids);

  /**
   * Processes the criterion on the user identifier.
   * @param userId the identifier of a user.
   * @return the processor itself.
   */
  SilvermailCriteriaProcessor processUserId(final long userId);

  /**
   * Processes the criterion on the folder identifier.
   * @param folderId the identifier of a folder.
   * @return the processor itself.
   */
  SilvermailCriteriaProcessor processFolderId(final long folderId);

  /**
   * Processes the criterion on the read state.
   * @param readState the read state.
   * @return the processor itself.
   */
  SilvermailCriteriaProcessor processReadState(final int readState);

  /**
   * Processes the criterion on orderings of the user notification matching the criteria.
   * @param orderings the result orderings concerned by the criterion.
   * @return the processor itself.
   */
  SilvermailCriteriaProcessor processOrdering(final List<QUERY_ORDER_BY> orderings);

  /**
   * Processes the criterion on the pagination to apply on the user notification to return.
   * @param pagination a pagination definition.
   * @return the processor itself.
   */
  SilvermailCriteriaProcessor processPagination(final PaginationPage pagination);

  /**
   * Gets the result of the processing. Warning, the result can be incomplete if called before the
   * processing ending (triggered with the call of {@link #endProcessing()} method).
   * @param <T> the type of the result.
   * @return the processing result.
   */
  <T> T result();
}
