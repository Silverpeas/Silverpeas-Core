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

package org.silverpeas.core.web.search;

import org.silverpeas.core.pdc.pdc.model.GlobalSilverResult;
import org.silverpeas.core.util.MultiSilverpeasBundle;

/**
 * This class encapsulates all the data used to create a ResultDisplayer implementation.
 */
public class SearchResultContentVO {
  private String userId = null;
  private GlobalSilverResult gsr = null;
  private Integer sortValue = null;
  private Boolean activeSelection = false;
  private Boolean exportEnabled = false;
  private MultiSilverpeasBundle settings = null;

  /**
   * @param userId the user identifier
   * @param gsr the current globalSilverResult object
   * @param sortValue the sort value
   * @param activeSelection the active selection
   * @param exportEnabled the export enabled
   * @param settings the pdcPeas settings with bundle and properties
   */
  public SearchResultContentVO(String userId, GlobalSilverResult gsr, Integer sortValue,
      Boolean activeSelection, Boolean exportEnabled, MultiSilverpeasBundle settings) {
    super();
    this.userId = userId;
    this.gsr = gsr;
    this.sortValue = sortValue;
    this.activeSelection = activeSelection;
    this.exportEnabled = exportEnabled;
    this.settings = settings;
  }

  /**
   * @return the userId
   */
  public String getUserId() {
    return userId;
  }

  /**
   * @return the gsr
   */
  public GlobalSilverResult getGsr() {
    return gsr;
  }

  /**
   * @return the sortValue
   */
  public Integer getSortValue() {
    return sortValue;
  }

  /**
   * @return the activeSelection
   */
  public Boolean getActiveSelection() {
    return activeSelection;
  }

  /**
   * @return the exportEnabled
   */
  public Boolean getExportEnabled() {
    return exportEnabled;
  }

  /**
   * @return the settings
   */
  public MultiSilverpeasBundle getSettings() {
    return settings;
  }

}
