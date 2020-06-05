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
package org.silverpeas.web.pdc.control;

import org.apache.commons.lang3.NotImplementedException;
import org.silverpeas.core.index.search.model.IndexSearcher;
import org.silverpeas.core.pdc.pdc.model.GlobalSilverResult;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * This class sort the result using a form xml field and filter the results to display only the
 * publication
 *
 * @author david derigent
 */
@Named
public class SortResultsXFormWithoutPub implements SortResults {

  @Inject
  private IndexSearcher indexSearcher;

  private SortResultsXFormWithoutPub() {
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.web.pdc.control.SortResults#execute(java.util.List, java.lang.String,
   * java.lang.String, java.lang.String)
   */
  @Override
  public List<GlobalSilverResult> execute(List<GlobalSilverResult> originalResults, String sortOrder,
      String sortValue, String language) {

    throw new NotImplementedException("No more implemented !");
  }

  @Override
  public void setPdcSearchSessionController(PdcSearchSessionController controller) {
    // do nothing
  }
}