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

package com.silverpeas.importExport.report;

import java.util.Date;
import java.util.HashMap;

/**
 * @author sdevolder
 */
public class ExportReport {

  // Variables
  private Date dateDebut;
  private Date dateFin;
  private String zipFileName;
  private String zipFilePath;
  private long zipFileSize;
  private HashMap mapPublicationPath = null;

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

  public String getZipFilePath() {
    return zipFilePath;
  }

  public void setZipFilePath(String zipFilePath) {
    this.zipFilePath = zipFilePath;
  }

  public long getZipFileSize() {
    return zipFileSize;
  }

  public void setZipFileSize(long zipFileSize) {
    this.zipFileSize = zipFileSize;
  }

  public void addHtmlIndex(String publicationId,
      HtmlExportPublicationGenerator sommaireEntry) {
    if (mapPublicationPath == null)
      mapPublicationPath = new HashMap();
    mapPublicationPath.put(publicationId, sommaireEntry);
  }

  public HashMap getMapIndexHtmlPaths() {
    return mapPublicationPath;
  }

  public long getDuration() {
    return getDateFin().getTime() - getDateDebut().getTime();
  }

  public String getZipFileName() {
    return zipFileName;
  }

  public void setZipFileName(String string) {
    zipFileName = string;
  }

}