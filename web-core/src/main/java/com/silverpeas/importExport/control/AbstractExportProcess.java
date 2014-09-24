package com.silverpeas.importExport.control;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.silverpeas.importExport.model.ImportExportException;
import com.silverpeas.importExport.report.ExportReport;
import org.silverpeas.util.ZipManager;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.FileServerUtils;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;

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

  protected void createZipFile(File fileExportDir, ExportReport exportReport) throws ImportExportException {
    try {
      String zipFileName = fileExportDir.getName() + ".zip";
      String tempDir = FileRepositoryManager.getTemporaryPath();
      long zipFileSize = ZipManager.compressPathToZip(fileExportDir.getPath(), tempDir
          + zipFileName);
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
   *
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