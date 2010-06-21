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
package com.stratelia.silverpeas.pdcPeas;

import java.util.Collections;
import java.util.List;

import com.stratelia.silverpeas.contentManager.XMLFormFieldComparator;
import com.stratelia.silverpeas.pdcPeas.model.GlobalSilverResult;

/**
 * This class sort the result using a form xml field and filter the results to display only the
 * publication
 * @author david derigent
 */
public class SortResultsXFormWithoutPub implements SortResults {

  /**
   * 
   */
  public SortResultsXFormWithoutPub() {
  }

  /*
   * (non-Javadoc)
   * @see com.stratelia.silverpeas.pdcPeas.SortResults#execute(java.util.List, java.lang.String,
   * java.lang.String, java.lang.String)
   */
  @Override
  public List<GlobalSilverResult> execute(List<GlobalSilverResult> results, String sortOrder,
      String sortValue, String language) {
    // filters the results to keep only the publication
    for (GlobalSilverResult globalSilverResult : results) {
      if (!"Publication".equals(globalSilverResult.getType())) {
        results.remove(globalSilverResult);
      }
    }

    // sorts the result on a XML form field
    XMLFormFieldComparator comparator = new XMLFormFieldComparator(sortValue, sortOrder);
    Collections.sort(results, comparator);

    return results;
  }

}
