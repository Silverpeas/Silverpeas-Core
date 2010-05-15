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

/*
 * Created on 24 janv. 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.silverpeas.importExport.report;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tleroi To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class MassiveReport {

  private String repositoryPath;
  private int error = UnitReport.ERROR_NO_ERROR;
  private ComponentReport componentReport;

  private int nbPublicationsCreated = -1;
  private int nbPublicationsUpdated = -1;
  private int nbTopicsCreated = 0;

  private List listUnitReports;

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

  public List getListUnitReports() {
    return listUnitReports;
  }

  public void addUnitReport(UnitReport unitReport) {
    if (listUnitReports == null)
      listUnitReports = new ArrayList();
    listUnitReports.add(unitReport);
    /*
     * switch (unitReport.getStatus()) { case UnitReport.STATUS_PUBLICATION_CREATED:
     * this.nbPublicationsCreated++; getComponentReport
     * ().setNbPublicationsCreated(getComponentReport().getNbPublicationsCreated ()+1); break; case
     * UnitReport.STATUS_PUBLICATION_UPDATED: this.nbPublicationsUpdated++;
     * getComponentReport().setNbPublicationsUpdated
     * (getComponentReport().getNbPublicationsUpdated()+1); break; }
     */
  }

  private void processStats() {
    nbPublicationsCreated = 0;
    nbPublicationsUpdated = 0;
    // nbTopicsCreated = 0;

    UnitReport unitReport = null;
    for (int u = 0; listUnitReports != null && u < listUnitReports.size(); u++) {
      unitReport = (UnitReport) listUnitReports.get(u);
      switch (unitReport.getStatus()) {
        case UnitReport.STATUS_PUBLICATION_CREATED:
          nbPublicationsCreated++;
          break;

        case UnitReport.STATUS_PUBLICATION_UPDATED:
          nbPublicationsUpdated++;
          break;

        // case UnitReport.STATUS_TOPIC_CREATED : nbTopicsCreated++;

      }
    }
  }

  public void addOneTopicCreated() {
    nbTopicsCreated++;
  }
}
