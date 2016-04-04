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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ExportPDFReport {

  private Date dateDebut;
  private Date dateFin;
  private String pdfFileName;
  private String pdfFilePath;
  private long pdfFileSize;
  private Map<String, HtmlExportPublicationGenerator> mapPublicationPath = new HashMap<>();

  public void addHtmlIndex(String publicationId, HtmlExportPublicationGenerator sommaireEntry) {
    mapPublicationPath.put(publicationId, sommaireEntry);
  }

  public Map<String, HtmlExportPublicationGenerator> getMapIndexHtmlPaths() {
    return mapPublicationPath;
  }

  public long getDuration() {
    return getDateFin().getTime() - getDateDebut().getTime();
  }

  // Getters & Setters
  public Date getDateDebut() {
    return dateDebut;
  }

  public void setDateDebut(Date dateDebut) {
    this.dateDebut = dateDebut;
  }

  public Date getDateFin() {
    return dateFin;
  }

  public void setDateFin(Date dateFin) {
    this.dateFin = dateFin;
  }

  public String getPdfFileName() {
    return pdfFileName;
  }

  public void setPdfFileName(String pdfFileName) {
    this.pdfFileName = pdfFileName;
  }

  public String getPdfFilePath() {
    return pdfFilePath;
  }

  public void setPdfFilePath(String pdfFilePath) {
    this.pdfFilePath = pdfFilePath;
  }

  public long getPdfFileSize() {
    return pdfFileSize;
  }

  public void setPdfFileSize(long pdfFileSize) {
    this.pdfFileSize = pdfFileSize;
  }
}