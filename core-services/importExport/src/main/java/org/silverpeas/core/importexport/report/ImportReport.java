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

import org.silverpeas.core.util.MultiSilverpeasBundle;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.file.FileRepositoryManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ImportReport {

  private Date startDate;
  private Date endDate;
  private int nbFilesProcessed;
  private int nbFilesNotImported;
  private List<ComponentReport> listComponentReport = new ArrayList<>();

  public void addComponentReport(ComponentReport componentReport) {
    listComponentReport.add(componentReport);
  }

  /**
   * @return
   */
  public Date getEndDate() {
    return endDate;
  }

  /**
   * @return
   */
  public int getNbFilesProcessed() {
    return nbFilesProcessed;
  }

  public int getNbFilesNotImported() {
    return nbFilesNotImported;
  }

  /**
   * @return
   */
  public Date getStartDate() {
    return startDate;
  }

  /**
   * @param date
   */
  void setEndDate(Date date) {
    endDate = date;
  }

  /**
   * @param date
   */
  void setStartDate(Date date) {
    startDate = date;
  }

  /**
   * @return Returns the listComponentReport.
   */
  public List<ComponentReport> getListComponentReport() {
    return listComponentReport;
  }

  public void addNumberOfFilesProcessed(int n) {
    this.nbFilesProcessed = this.nbFilesProcessed + n;
  }

  public String getDuration() {
    return DateUtil.formatDuration(getEndDate().getTime() - getStartDate().getTime());
  }

  /**
   * @param i
   */
  public void addNbFilesNotImported(int i) {
    this.nbFilesNotImported = this.nbFilesNotImported + i;
  }

  public long getTotalImportedFileSize() {
    long size = 0;
    for (ComponentReport componentRpt : getListComponentReport()) {
      size += componentRpt.getTotalImportedFileSize();
    }
    return size;
  }

  /**
   * Méthode de formatage du rapport en vue de l'écriture d'un fichier de log
   * @param resource
   * @return
   */
  public String writeToLog(MultiSilverpeasBundle resource) {
    StringBuilder sb = new StringBuilder();
    SimpleDateFormat dateFormat = new SimpleDateFormat("[yyyy-MM-dd-HH'H'mm'm'ss's']");
    String dateFormatee = dateFormat.format(new Date());
    sb.append("**********************************************\n");
    sb.append(dateFormatee).append("\n\n");
    sb.append(resource.getString("importExportPeas.StatGlobal")).append("\n\n");
    sb.append(resource.getString("importExportPeas.ImportDuration")).append(" : ");
    sb.append(getDuration()).append("\n");
    sb.append(resource.getString("importExportPeas.NbFilesImported")).append(" : ");
    sb.append(getNbFilesProcessed()).append("\n");
    sb.append(resource.getString("importExportPeas.NbFilesNotFound")).append(" : ");
    sb.append(getNbFilesNotImported()).append("\n");
    sb.append(resource.getString("importExportPeas.TotalFileUploadedSize")).append(" : ");
    sb.append(FileRepositoryManager.formatFileSize(getTotalImportedFileSize())).append("\n\n");
    sb.append(resource.getString("importExportPeas.StatComponent")).append("\n");

    for (ComponentReport componentRpt : getListComponentReport()) {
      sb.append("\n").append(resource.getString("importExportPeas.Composant")).append(" : ");
      sb.append(componentRpt.getComponentName()).append(" : " + "(");
      sb.append(componentRpt.getComponentId()).append(")\n");
      sb.append(resource.getString("importExportPeas.NbPubCreated")).append(" : ");
      sb.append(componentRpt.getNbPublicationsCreated()).append("\n");
      sb.append(resource.getString("importExportPeas.NbPubUpdated")).append(" : ");
      sb.append(componentRpt.getNbPublicationsUpdated()).append("\n");
      sb.append(resource.getString("importExportPeas.NbTopicCreated")).append(" : ");
      sb.append(componentRpt.getNbTopicsCreated()).append("\n");
      sb.append(resource.getString("importExportPeas.TotalFileUploadedSize")).append(" : ");
      sb.append(FileRepositoryManager.formatFileSize(componentRpt.getTotalImportedFileSize()));
      sb.append("\n");
      // Affichage des rapports unitaires
      List<UnitReport> unitReports = componentRpt.getListUnitReports();
      if (unitReports != null) {
        for (UnitReport unitReport : unitReports) {
          if (unitReport.getError() != -1) {
            sb.append(logUnitReport(resource, unitReport));
          }
        }
      }
      // Affichage des rapports massifs
      List<MassiveReport> massiveReports = componentRpt.getListMassiveReports();
      if (massiveReports != null) {
        for (MassiveReport massiveReport : massiveReports) {
          sb.append(logMassiveReport(resource, massiveReport));
        }
      }
      sb.append("\n");
    }
    return sb.toString();
  }

  private String logUnitReport(MultiSilverpeasBundle resource, UnitReport unitReport) {
    StringBuilder sb = new StringBuilder(200);
    sb.append(unitReport.getLabel()).append(" : ").append(unitReport.getItemName());
    sb.append(", ").append(resource.getString("GML.error")).append(" : ");
    sb.append(resource.getString("importExportPeas.ImportError" + unitReport.getError()));
    sb.append(", ").append(resource.getString("importExportPeas.Status")).append(" : ");
    sb.append(unitReport.getStatus());
    sb.append("\n");
    return sb.toString();
  }

  private String logMassiveReport(MultiSilverpeasBundle resource, MassiveReport massiveReport) {
    StringBuilder sb = new StringBuilder(500);
    sb.append(resource.getString("importExportPeas.Repository")).append(" ");
    sb.append(massiveReport.getRepositoryPath()).append("\n");
    if (massiveReport.getError() != -1) {
      sb.append(resource.getString("GML.error")).append(" : ");
      sb.append(resource.getString("importExportPeas.ImportError" + massiveReport.getError()));
      sb.append("\n");
    }
    sb.append(resource.getString("importExportPeas.NbPubCreated")).append(" : ");
    sb.append(massiveReport.getNbPublicationsCreated()).append("\n");
    sb.append(resource.getString("importExportPeas.NbPubUpdated")).append(" : ");
    sb.append(massiveReport.getNbPublicationsUpdated()).append("\n");
    sb.append(resource.getString("importExportPeas.NbTopicCreated")).append(" : ");
    sb.append(massiveReport.getNbTopicsCreated()).append("\n");

    List<UnitReport> unitReports = massiveReport.getListUnitReports();
    if (unitReports != null) {
      for (UnitReport unitReport : unitReports) {
        if (unitReport.getError() != -1) {
          sb.append(logUnitReport(resource, unitReport));
        }
      }
    }
    return sb.toString();
  }
}