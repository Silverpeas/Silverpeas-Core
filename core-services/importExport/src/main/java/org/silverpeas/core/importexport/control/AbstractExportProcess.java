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

package org.silverpeas.core.importexport.control;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.silverpeas.core.importexport.model.ImportExportException;
import org.silverpeas.core.importexport.report.ExportReport;
import org.silverpeas.core.util.ZipUtil;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileServerUtils;
import org.silverpeas.core.exception.UtilException;
import org.silverpeas.core.util.file.FileFolderManager;

public abstract class AbstractExportProcess {

  protected AbstractExportProcess() {
    super();
  }

  protected File createExportDir(UserDetail userDetail) throws ImportExportException {
    String thisExportDir = generateExportDirName(userDetail, "export");
    String tempDir = FileRepositoryManager.getTemporaryPath();
    File fileExportDir = new File(tempDir + thisExportDir);
    if (!fileExportDir.exists()) {
      try {
        FileFolderManager.createFolder(fileExportDir);
      } catch (UtilException ex) {
        throw new ImportExportException("ImportExport", "importExport.EX_CANT_CREATE_FOLDER", ex);
      }
    }
    return fileExportDir;
  }

  protected void createZipFile(File fileExportDir, ExportReport exportReport)
      throws ImportExportException {
    try {
      String zipFileName = fileExportDir.getName() + ".zip";
      String tempDir = FileRepositoryManager.getTemporaryPath();
      long zipFileSize = ZipUtil.compressPathToZip(fileExportDir.getPath(), tempDir + zipFileName);
      exportReport.setZipFileName(zipFileName);
      exportReport.setZipFileSize(zipFileSize);
      exportReport.setZipFilePath(FileServerUtils.getUrlToTempDir(zipFileName));
      exportReport.setDateFin(new Date());
    } catch (IOException ex) {
      throw new ImportExportException("ImportExport", "root.EX_CANT_WRITE_FILE", ex);
    }
  }

  /**
   * Generate export name as follow : "prefixNameAAAA-MM-JJ-hh'H'mm'm'ss's'_userId"
   * @param userDetail - the user detail
   * @param prefixName : prefix export directory name
   * @return name of exported directory
   */
  protected String generateExportDirName(UserDetail userDetail, String prefixName) {
    StringBuilder sb = new StringBuilder(prefixName);
    Date date = new Date();
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH'H'mm'm'ss's'");
    String dateFormatee = dateFormat.format(date);
    sb.append(dateFormatee);
    sb.append('_').append(userDetail.getId());
    return sb.toString();
  }

}