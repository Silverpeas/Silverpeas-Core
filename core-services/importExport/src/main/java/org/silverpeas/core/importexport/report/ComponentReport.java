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

/**
 * @author tleroi
 */
public class ComponentReport {

  private String componentId;
  private String componentName;

  private int nbPublicationsCreated = -1;
  private int nbPublicationsUpdated = -1;
  private int nbTopicsCreated = -1;
  private long totalImportedFileSize = 0;

  private List<MassiveReport> listMassiveReports = new ArrayList<>();
  private List<UnitReport> listUnitReports = new ArrayList<>();

  public ComponentReport(String componentId) {
    this.componentId = componentId;
  }

  public String getComponentId() {
    return componentId;
  }

  public String getComponentName() {
    return componentName;
  }

  /**
   * @param report
   */
  public void addUnitReport(UnitReport report) {
    listUnitReports.add(report);
  }

  /**
   * @param report
   */
  public void addMassiveReport(MassiveReport report) {
    listMassiveReports.add(report);
    report.setComponentReport(this);
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
  public List<MassiveReport> getListMassiveReports() {
    return listMassiveReports;
  }

  /**
   * @return Returns the listUnitReports.
   */
  public List<UnitReport> getListUnitReports() {
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
    for (UnitReport unitReport : listUnitReports) {
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

    for (MassiveReport massiveReport : listMassiveReports) {
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
