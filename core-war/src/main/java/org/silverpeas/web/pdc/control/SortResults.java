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

package org.silverpeas.web.pdc.control;

import org.silverpeas.core.pdc.pdc.model.GlobalSilverResult;
import java.util.List;

/**
 * This interface define services allowing to filter or sort a list of GlobalSilverResult object
 * @author David Derigent
 */
public interface SortResults {

  /**
   * realizes the sort or the sorting or filtering of a list of GlobalSilverResult
   * @param results List of GlobalSilverResult object
   * @param sortOrder order of sort
   * @param sortValue type of sort to realize
   * @param language
   * @return a sorting and/or filtering list
   */
  public List<GlobalSilverResult> execute(List<GlobalSilverResult> results, String sortOrder,
      String sortValue, String language);

  /**
   * Sets a PdcSearchSessionController in case this is needed by the sort
   * @param controller
   */
  public void setPdcSearchSessionController(PdcSearchSessionController controller);
}
