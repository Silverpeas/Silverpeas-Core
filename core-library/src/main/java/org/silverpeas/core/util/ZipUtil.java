/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.util;

import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.exception.UtilException;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.silverpeas.core.util.file.FileUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Enumeration;

import static org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
    .UnicodeExtraFieldPolicy.NOT_ENCODEABLE;

/**
 * Classe contenant des méthodes statiques de gestion des fichiers zip
 *
 * @author sdevolder
 */
public class ZipUtil {

  /**
   * Compress a file into a zip file.
   *
   * @param filePath
   * @param zipFilePath
   * @return
   * @throws IOException
   */
  public static long compressFile(String filePath, String zipFilePath) throws IOException {
    ZipArchiveOutputStream zos = new ZipArchiveOutputStream(new FileOutputStream(zipFilePath));
    InputStream in = new FileInputStream(filePath);
    try {
      // création du flux zip
      zos = new ZipArchiveOutputStream(new FileOutputStream(zipFilePath));
      zos.setFallbackToUTF8(true);
      zos.setCreateUnicodeExtraFields(NOT_ENCODEABLE);
      zos.setEncoding(CharEncoding.UTF_8);
      String entryName = FilenameUtils.getName(filePath);
      entryName = entryName.replace(File.separatorChar, '/');
      zos.putArchiveEntry(new ZipArchiveEntry(entryName));
      IOUtils.copy(in, zos);
      zos.closeArchiveEntry();
      return new File(zipFilePath).length();
    } finally {
      IOUtils.closeQuietly(in);
      IOUtils.closeQuietly(zos);
    }
  }

  /**
   * Méthode compressant un dossier de façon récursive au format zip.
   *
   * @param folderToZip - dossier à compresser
   * @param zipFile - fichier zip à creer
   * @return la taille du fichier zip généré en octets
   * @throws FileNotFoundException
   * @throws IOException
   */
  public static long compressPathToZip(String folderToZip, String zipFile) throws IOException {
    return compressPathToZip(new File(folderToZip), new File(zipFile));
  }

  /**
   * Méthode compressant un dossier de façon récursive au format zip.
   *
   * @param folderToZip - dossier à compresser
   * @param zipFile - fichier zip à creer
   * @return la taille du fichier zip généré en octets
   * @throws FileNotFoundException
   * @throws IOException
   */
  public static long compressPathToZip(File folderToZip, File zipFile) throws IOException {
    ZipArchiveOutputStream zos = null;
    try {
      // création du flux zip
      zos = new ZipArchiveOutputStream(new FileOutputStream(zipFile));
      zos.setFallbackToUTF8(true);
      zos.setCreateUnicodeExtraFields(NOT_ENCODEABLE);
      zos.setEncoding(CharEncoding.UTF_8);
      Collection<File> folderContent = FileUtils.listFiles(folderToZip, null, true);
      for (File file : folderContent) {
        String entryName = file.getPath().substring(folderToZip.getParent().length() + 1);
        entryName = FilenameUtils.separatorsToUnix(entryName);
        zos.putArchiveEntry(new ZipArchiveEntry(entryName));
        InputStream in = new FileInputStream(file);
        IOUtils.copy(in, zos);
        zos.closeArchiveEntry();
        IOUtils.closeQuietly(in);
      }
    } finally {
      if (zos != null) {
        IOUtils.closeQuietly(zos);
      }
    }
    return zipFile.length();
  }

  /**
   * Méthode permettant la création et l'organisation d'un fichier zip en lui passant directement un
   * flux d'entrée
   *
   * @param inputStream - flux de données à enregistrer dans le zip
   * @param filePathNameToCreate - chemin et nom du fichier porté par les données du flux dans le
   * zip
   * @param outfilename - chemin et nom du fichier zip à creer ou compléter
   * @throws IOException
   */
  public static void compressStreamToZip(InputStream inputStream, String filePathNameToCreate,
      String outfilename) throws IOException {
    ZipArchiveOutputStream zos = null;
    try {
      zos = new ZipArchiveOutputStream(new FileOutputStream(outfilename));
      zos.setFallbackToUTF8(true);
      zos.setCreateUnicodeExtraFields(NOT_ENCODEABLE);
      zos.setEncoding("UTF-8");
      zos.putArchiveEntry(new ZipArchiveEntry(filePathNameToCreate));
      IOUtils.copy(inputStream, zos);
      zos.closeArchiveEntry();
    } finally {
      if (zos != null) {
        IOUtils.closeQuietly(zos);
      }
    }
  }

  /**
   * Extract the content of an archive into a directory.
   *
   * @param source the archive.
   * @param dest the destination directory.
   * @throws UtilException
   */
  /*public static void extract(File source, File dest) throws UtilException {
   if (source == null) {
   throw new UtilException("Expand.execute()", SilverpeasException.ERROR,
   "util.EXE_SOURCE_FILE_ATTRIBUTE_MUST_BE_SPECIFIED");
   }
   if (dest == null) {
   throw new UtilException("Expand.execute()", SilverpeasException.ERROR,
   "util.EXE_DESTINATION_FILE_ATTRIBUTE_MUST_BE_SPECIFIED");
   }
   ZipFile zf = null;
   try {
   zf = new ZipFile(source);
   @SuppressWarnings("unchecked")
   Enumeration<ZipArchiveEntry> entries = (Enumeration<ZipArchiveEntry>) zf.getEntries();
   while (entries.hasMoreElements()) {
   ZipArchiveEntry ze = entries.nextElement();
   File currentFile = new File(dest, ze.getName());
   try {
   currentFile.getParentFile().mkdirs();
   if (ze.isDirectory()) {
   currentFile.mkdirs();
   } else {
   currentFile.getParentFile().mkdirs();
   InputStream zis = zf.getInputStream(ze);
   FileOutputStream fos = new FileOutputStream(currentFile);
   IOUtils.copy(zis, fos);
   IOUtils.closeQuietly(zis);
   IOUtils.closeQuietly(fos);
   }
   } catch (FileNotFoundException ex) {
   SilverTrace.warn("util", "ZipManager.extractFile()",
   "root.EX_FILE_NOT_FOUND", "file = " + currentFile.getPath(), ex);
   }
   }
   } catch (IOException ioe) {
   SilverTrace.warn("util", "ZipManager.extractFile()",
   "util.EXE_ERROR_WHILE_EXTRACTING_FILE", "sourceFile = "
   + source.getPath(), ioe);
   } finally {
   if (zf != null) {
   ZipFile.closeQuietly(zf);
   }
   }
   }  */
  public static void extract(File source, File dest) throws UtilException {
    if (source == null) {
      throw new UtilException("Expand.execute()", SilverpeasException.ERROR,
          "util.EXE_SOURCE_FILE_ATTRIBUTE_MUST_BE_SPECIFIED");
    }
    if (dest == null) {
      throw new UtilException("Expand.execute()", SilverpeasException.ERROR,
          "util.EXE_DESTINATION_FILE_ATTRIBUTE_MUST_BE_SPECIFIED");
    }
    InputStream in = null;
    ArchiveInputStream archiveStream = null;
    try {
      in = new BufferedInputStream(new FileInputStream(source));
      try {
        archiveStream = new ArchiveStreamFactory().createArchiveInputStream(in);
      } catch (ArchiveException aex) {
        if (FilenameUtils.getExtension(source.getName()).toLowerCase().endsWith("gz")) {
          archiveStream = new TarArchiveInputStream(new GzipCompressorInputStream(in));
        } else {
          archiveStream = new TarArchiveInputStream(new BZip2CompressorInputStream(in));
        }
      }
      ArchiveEntry archiveEntry;
      while ((archiveEntry = archiveStream.getNextEntry()) != null) {
        File currentFile = new File(dest, archiveEntry.getName());
        FileUtil.validateFilename(currentFile.getCanonicalPath(), dest.getCanonicalPath());
        try {
          currentFile.getParentFile().mkdirs();
          if (archiveEntry.isDirectory()) {
            currentFile.mkdirs();
          } else {
            currentFile.getParentFile().mkdirs();
            FileOutputStream fos = new FileOutputStream(currentFile);
            IOUtils.copy(archiveStream, fos);
            IOUtils.closeQuietly(fos);
          }
        } catch (FileNotFoundException ex) {
          SilverTrace.warn("util", "ZipManager.extractFile()",
              "root.EX_FILE_NOT_FOUND", "file = " + currentFile.getPath(), ex);
        }
      }
    } catch (IOException ioe) {
      SilverTrace.warn("util", "ZipManager.extractFile()",
          "util.EXE_ERROR_WHILE_EXTRACTING_FILE", "sourceFile = "
          + source.getPath(), ioe);
    } finally {
      IOUtils.closeQuietly(in);
      IOUtils.closeQuietly(archiveStream);
    }
  }

  /**
   * Indicates the number of files (not directories) inside the archive.
   *
   * @param archive the archive whose content is analyzed.
   * @return the number of files (not directories) inside the archive.
   */
  public static int getNbFiles(File archive) {
    ZipFile zipFile = null;
    int nbFiles = 0;
    try {
      zipFile = new ZipFile(archive);
      @SuppressWarnings("unchecked")
      Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
      while (entries.hasMoreElements()) {
        ZipArchiveEntry ze = entries.nextElement();
        if (!ze.isDirectory()) {
          nbFiles++;
        }
      }
    } catch (IOException ioe) {
      SilverTrace.warn("util", "ZipManager.getNbFiles()",
          "util.EXE_ERROR_WHILE_COUNTING_FILE", "sourceFile = "
          + archive.getPath(), ioe);
    } finally {
      if (zipFile != null) {
        ZipFile.closeQuietly(zipFile);
      }
    }
    return nbFiles;
  }

}
