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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
 * @author tleroi
 * 
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ComponentReport {

  private String componentId;
  private String componentName;

  private int nbPublicationsCreated = -1;
  private int nbPublicationsUpdated = -1;
  private int nbTopicsCreated = -1;
  private long totalImportedFileSize = 0;

  private List listMassiveReports;
  private List listUnitReports;

  public ComponentReport(String componentId) {
    setComponentId(componentId);
  }

  public String getComponentId() {
    return componentId;
  }

  public String getComponentName() {
    return componentName;
  }

  /**
   * @param Report
   */
  public void addUnitReport(UnitReport report) {
    if (listUnitReports == null)
      listUnitReports = new ArrayList();
    listUnitReports.add(report);
    /*
     * switch (report.getStatus()) { case UnitReport.STATUS_PUBLICATION_CREATED:
     * this.nbPublicationsCreated++; break; case
     * UnitReport.STATUS_PUBLICATION_UPDATED: this.nbPublicationsUpdated++;
     * break; }
     */

  }

  /**
   * @param report
   */
  public void addMassiveReport(MassiveReport report) {
    if (listMassiveReports == null)
      listMassiveReports = new ArrayList();
    listMassiveReports.add(report);
    report.setComponentReport(this);
    /*
     * nbPublicationsCreated += report.getNbPublicationsCreated();
     * nbPublicationsUpdated += report.getNbPublicationsUpdated();
     */
  }

  /**
   * @return
   */
  public int getNbPublicationsCreated() {
    if (nbPublicationsCreated == -1) {
      processStats();
    }
    return nbPublicationsCreated;
  }

  /**
   * @return
   */
  public int getNbTopicsCreated() {
    if (nbTopicsCreated == -1) {
      processStats();
    }
    return nbTopicsCreated;
  }

  /**
   * @param string
   */
  public void setComponentId(String string) {
    componentId = string;
  }

  /**
   * @param string
   */
  public void setComponentName(String string) {
    componentName = string;
  }

  /**
   * @param i
   */
  public void setNbTopicsCreated(int i) {
    nbTopicsCreated = i;
  }

  /**
   * @return Returns the listMassiveReports.
   */
  public List getListMassiveReports() {
    return listMassiveReports;
  }

  /**
   * @return Returns the listUnitReports.
   */
  public List getListUnitReports() {
    return listUnitReports;
  }

  public int getNbPublicationsUpdated() {
    if (nbPublicationsUpdated == -1) {
      processStats();
    }
    return nbPublicationsUpdated;
  }

  public void setNbPublicationsCreated(int nbPublicationsCreated) {
    this.nbPublicationsCreated = nbPublicationsCreated;
  }

  public void setNbPublicationsUpdated(int nbPublicationsUpdated) {
    this.nbPublicationsUpdated = nbPublicationsUpdated;
  }

  public void processStats() {
    nbPublicationsCreated = 0;
    nbPublicationsUpdated = 0;
    nbTopicsCreated = 0;

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

        case UnitReport.STATUS_TOPIC_CREATED:
          nbTopicsCreated++;

      }
    }

    MassiveReport massiveReport = null;
    for (int u = 0; listMassiveReports != null && u < listMassiveReports.size(); u++) {
      massiveReport = (MassiveReport) listMassiveReports.get(u);

      nbPublicationsCreated += massiveReport.getNbPublicationsCreated();
      nbPublicationsUpdated += massiveReport.getNbPublicationsUpdated();
      nbTopicsCreated += massiveReport.getNbTopicsCreated();
    }
  }

  public void addImportedFileSize(long size) {
    totalImportedFileSize += size;
  }

  public long getTotalImportedFileSize() {
    return totalImportedFileSize;
  }
}
