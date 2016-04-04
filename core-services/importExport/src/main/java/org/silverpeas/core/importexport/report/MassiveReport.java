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

package org.silverpeas.core.importexport.report;

import java.util.ArrayList;
import java.util.List;

public class MassiveReport {

  private String repositoryPath;
  private int error = UnitReport.ERROR_NO_ERROR;
  private ComponentReport componentReport;

  private int nbPublicationsCreated = -1;
  private int nbPublicationsUpdated = -1;
  private int nbTopicsCreated = 0;

  private List<UnitReport> listUnitReports = new ArrayList<>();

  /**
   * @return
   */
  public ComponentReport getComponentReport() {
    return componentReport;
  }

  /**
   * @return
   */
  public int getError() {
    return error;
  }

  /**
   * @return
   */
  public String getRepositoryPath() {
    return repositoryPath;
  }

  /**
   * @param report
   */
  public void setComponentReport(ComponentReport report) {
    componentReport = report;
  }

  /**
   * @param i
   */
  public void setError(int i) {
    error = i;
  }

  /**
   * @param string
   */
  public void setRepositoryPath(String string) {
    repositoryPath = string;
  }

  public int getNbPublicationsCreated() {
    if (nbPublicationsCreated == -1) {
      processStats();
    }
    return nbPublicationsCreated;
  }

  public int getNbPublicationsUpdated() {
    if (nbPublicationsUpdated == -1) {
      processStats();
    }
    return nbPublicationsUpdated;
  }

  public int getNbTopicsCreated() {
    return nbTopicsCreated;
  }

  public List<UnitReport> getListUnitReports() {
    return listUnitReports;
  }

  public void addUnitReport(UnitReport unitReport) {
    listUnitReports.add(unitReport);
  }

  private void processStats() {
    nbPublicationsCreated = 0;
    nbPublicationsUpdated = 0;
    for (UnitReport unitReport : listUnitReports) {
      switch (unitReport.getStatus()) {
        case UnitReport.STATUS_PUBLICATION_CREATED:
          nbPublicationsCreated++;
          break;
        case UnitReport.STATUS_PUBLICATION_UPDATED:
          nbPublicationsUpdated++;
          break;
      }
    }
  }

  public void addOneTopicCreated() {
    nbTopicsCreated++;
  }
}
