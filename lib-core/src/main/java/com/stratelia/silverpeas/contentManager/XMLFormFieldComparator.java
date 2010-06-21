/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.silverpeas.contentManager;

import java.util.Comparator;

import org.apache.commons.lang.StringUtils;

/**
 * class used to compare GlobalSilverContent according to the XML form sortable field
 * @author David Derigent
 */
public class XMLFormFieldComparator implements Comparator<GlobalSilverContent> {

  private String fieldName = null;

  private String sortOrder = null;

  private final String SORT_ORDER_ASC = "ASC";
  private final String SORT_ORDER_DESC = "DESC";

  /**
   * @param fieldName
   * @param sortOrder
   */
  public XMLFormFieldComparator(String fieldName, String sortOrder) {
    super();
    this.fieldName = fieldName;
    this.sortOrder = sortOrder;
  }

  @Override
  public int compare(GlobalSilverContent firstContent, GlobalSilverContent secondContent) {
    String firstField = firstContent.getSortableXMLFormFields().get(fieldName);
    String secondField = secondContent.getSortableXMLFormFields().get(fieldName);
    boolean isComparable =
        (StringUtils.isNotEmpty(firstField) && StringUtils.isNotEmpty(secondField));

    if (SORT_ORDER_ASC.equals(sortOrder) && isComparable) {
      return firstField.compareTo(secondField);
    } else if (SORT_ORDER_DESC.equals(sortOrder) && isComparable) {
      return secondField.compareTo(firstField);
    } else {
      return 0;
    }

  }

}
