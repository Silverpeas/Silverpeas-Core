/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class that permits to set message search criteria for user notification.
 * @author Yohann Chastagnier
 */
public class SilvermailCriteria {

  public enum QUERY_ORDER_BY {

    RECEPTION_DATE_ASC("id", true), RECEPTION_DATE_DESC("id", false),
    SOURCE_ASC("lower(source)", true), SOURCE_DESC("lower(source)", false),
    FROM_ASC("lower(senderName)", true), FROM_DESC("lower(senderName)", false),
    SUBJECT_ASC("lower(subject)", true), SUBJECT_DESC("lower(subject)", false);

    private final String propertyName;
    private final boolean asc;

    QUERY_ORDER_BY(final String propertyName, final boolean asc) {
      this.propertyName = propertyName;
      this.asc = asc;
    }

    public String getPropertyName() {
      return propertyName;
    }

    public boolean isAsc() {
      return asc;
    }
  }

  private List<Long> ids = new ArrayList<>();
  private Long userId;
  private Long folderId;
  private Integer readState;
  private PaginationPage pagination;
  private final List<QUERY_ORDER_BY> orderByList = new ArrayList<>();

  private SilvermailCriteria() {
  }

  /**
   * Initializes user notification search criteria.
   * @return an instance of user notification criteria.
   */
  public static SilvermailCriteria get() {
    return new SilvermailCriteria();
  }

  /**
   * Sets the criteria of user identifier.
   * @param ids the message identifiers.
   * @return itself.
   */
  public SilvermailCriteria byId(Long... ids) {
    this.ids.addAll(Arrays.stream(ids).collect(Collectors.toList())) ;
    return this;
  }

  /**
   * Sets the criteria of user identifier.
   * @param userId the identifier of a user.
   * @return itself.
   */
  public SilvermailCriteria aboutUser(String userId) {
    this.userId = Long.valueOf(userId);
    return this;
  }

  /**
   * Sets the criteria of folder name.
   * @param folderName the name of a folder.
   * @return itself.
   */
  public SilvermailCriteria into(String folderName) {
    this.folderId = convertFolderNameToId(folderName);
    return this;
  }

  /**
   * Converts from the folder name to the identifier.
   * @param folderName the folder name.
   * @return the folder identifier.
   */
  private static long convertFolderNameToId(String folderName) {
    long result = -1;
    if ("INBOX".equals(folderName)) {
      result = 0;
    } else if (StringUtil.isLong(folderName)) {
      result = Long.parseLong(folderName);
    }
    return result;
  }

  /**
   * Sets read user notification.
   * @return itself.
   */
  public SilvermailCriteria read() {
    this.readState = 1;
    return this;
  }

  /**
   * Sets read user notification.
   * @return itself.
   */
  public SilvermailCriteria unread() {
    this.readState = 0;
    return this;
  }

  /**
   * Sets the criteria of pagination.
   * @param pagination the pagination.
   * @return itself.
   */
  public SilvermailCriteria paginatedBy(PaginationPage pagination) {
    this.pagination = pagination;
    return this;
  }

  /**
   * Configures the order of the user notification list.
   * @param orderBies the list of order by directives.
   * @return itself.
   */
  public SilvermailCriteria orderedBy(QUERY_ORDER_BY... orderBies) {
    CollectionUtil.addAllIgnoreNull(this.orderByList, orderBies);
    return this;
  }

  Long getUserId() {
    return userId;
  }

  /**
   * Processes this criteria with the specified processor. It chains in a given order the different
   * criterion to process.
   * @param processor the processor to use for processing each criterion in this criteria.
   */
  public void processWith(final SilvermailCriteriaProcessor processor) {
    processor.startProcessing();

    if (!ids.isEmpty()) {
      processor.then().processByIds(ids);
    }

    if (userId != null) {
      processor.then().processUserId(userId);
    }

    if (folderId != null) {
      processor.then().processFolderId(folderId);
    }

    if (readState != null) {
      processor.then().processReadState(readState);
    }

    if (orderByList.isEmpty()) {
      orderByList.add(QUERY_ORDER_BY.RECEPTION_DATE_DESC);
    }
    processor.then().processOrdering(orderByList);

    if (pagination != null) {
      processor.then().processPagination(pagination);
    }

    processor.endProcessing();
  }
}
