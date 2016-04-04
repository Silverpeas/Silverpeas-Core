/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.contribution.contentcontainer.content;

import java.util.Comparator;

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
    String firstFieldValue = null;
    String secondFieldValue = null;
    int result;

    if (firstContent.getSortableXMLFormFields() != null) {
      firstFieldValue = firstContent.getSortableXMLFormFields().get(fieldName);
    }

    if (secondContent.getSortableXMLFormFields() != null) {
      secondFieldValue = secondContent.getSortableXMLFormFields().get(fieldName);
    }

    if (firstFieldValue == null) {
      if (secondFieldValue == null) {
        result = 0;
      } else {
        // firstFieldValue (null) > secondFieldValue (not null)
        result = 1;
      }
    } else {
      if (secondFieldValue == null) {
        // firstFieldValue (not null) < secondFieldValue (null)
        result = -1;
      } else {
        result = firstFieldValue.compareTo(secondFieldValue);
      }
    }

    if (SORT_ORDER_ASC.equals(sortOrder)) {
      return result;
    } else {
      return -result;
    }
  }

}
