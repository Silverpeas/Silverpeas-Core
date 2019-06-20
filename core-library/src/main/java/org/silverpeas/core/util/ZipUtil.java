/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.util;

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
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Objects;

import static org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream.UnicodeExtraFieldPolicy.NOT_ENCODEABLE;

/**
 * Classe contenant des méthodes statiques de gestion des fichiers zip
 *
 * @author sdevolder
 */
public class ZipUtil {

  private ZipUtil() {

  }

  /**
   * Compress a file into a zip file.
   *
   * @param filePath
   * @param zipFilePath
   * @return
   * @throws IOException
   */
  public static long compressFile(String filePath, String zipFilePath) throws IOException {
    try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(new FileOutputStream(zipFilePath));
         InputStream in = new FileInputStream(filePath)) {
      zos.setFallbackToUTF8(true);
      zos.setCreateUnicodeExtraFields(NOT_ENCODEABLE);
      zos.setEncoding(Charsets.UTF_8.name());
      String entryName = FilenameUtils.getName(filePath);
      entryName = entryName.replace(File.separatorChar, '/');
      zos.putArchiveEntry(new ZipArchiveEntry(entryName));
      IOUtils.copy(in, zos);
      zos.closeArchiveEntry();
      return new File(zipFilePath).length();
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
    try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(new FileOutputStream(zipFile))) {
      zos.setFallbackToUTF8(true);
      zos.setCreateUnicodeExtraFields(NOT_ENCODEABLE);
      zos.setEncoding(Charsets.UTF_8.name());
      Collection<File> folderContent = FileUtils.listFiles(folderToZip, null, true);
      for (File file : folderContent) {
        String entryName = file.getPath().substring(folderToZip.getParent().length() + 1);
        entryName = FilenameUtils.separatorsToUnix(entryName);
        zos.putArchiveEntry(new ZipArchiveEntry(entryName));
        try (InputStream in = new FileInputStream(file)) {
          IOUtils.copy(in, zos);
          zos.closeArchiveEntry();
        }
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
    try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(
        new FileOutputStream(outfilename))) {
      zos.setFallbackToUTF8(true);
      zos.setCreateUnicodeExtraFields(NOT_ENCODEABLE);
      zos.setEncoding("UTF-8");
      zos.putArchiveEntry(new ZipArchiveEntry(filePathNameToCreate));
      IOUtils.copy(inputStream, zos);
      zos.closeArchiveEntry();
    }
  }

  private static ArchiveInputStream openArchive(final String archive, final InputStream in)
      throws IOException {
    ArchiveInputStream archiveStream;
    try {
      archiveStream = new ArchiveStreamFactory().createArchiveInputStream(in);
    } catch (ArchiveException aex) {
      if (FilenameUtils.getExtension(archive).toLowerCase().endsWith("gz")) {
        archiveStream = new TarArchiveInputStream(new GzipCompressorInputStream(in));
      } else {
        archiveStream = new TarArchiveInputStream(new BZip2CompressorInputStream(in));
      }
    }
    return archiveStream;
  }

  /**
   * Extract the content of an archive into a directory.
   *
   * @param source the archive. Must be non null.
   * @param dest the destination directory. Must be non null.
   */
  public static void extract(File source, File dest) {
    Objects.requireNonNull(source);
    Objects.requireNonNull(dest);
    try (final InputStream in = new BufferedInputStream(new FileInputStream(source));
         final ArchiveInputStream archiveStream = openArchive(source.getName(), in)) {
      ArchiveEntry archiveEntry;
      while ((archiveEntry = archiveStream.getNextEntry()) != null) {
        File currentFile = new File(dest, archiveEntry.getName());
        FileUtil.validateFilename(currentFile.getCanonicalPath(), dest.getCanonicalPath());
        createPath(archiveStream, archiveEntry, currentFile);
      }
    } catch (IOException ioe) {
      SilverLogger.getLogger(ZipUtil.class)
          .error("Cannot extract archive " + source.getPath(), ioe);
    }
  }

  private static void createPath(final ArchiveInputStream archiveStream,
      final ArchiveEntry archiveEntry, final File currentFile) throws IOException {
    try {
      currentFile.getParentFile().mkdirs();
      if (archiveEntry.isDirectory()) {
        currentFile.mkdirs();
      } else {
        try (final FileOutputStream fos = new FileOutputStream(currentFile)) {
          IOUtils.copy(archiveStream, fos);
        }
      }
    } catch (FileNotFoundException ex) {
      SilverLogger.getLogger(ZipUtil.class).error("File not found " + currentFile.getPath(), ex);
    }
  }

  /**
   * Indicates the number of files (not directories) inside the archive.
   *
   * @param archive the archive whose content is analyzed.
   * @return the number of files (not directories) inside the archive.
   */
  public static int getNbFiles(File archive) {
    int nbFiles = 0;
    try (ZipFile zipFile = new ZipFile(archive)) {
      @SuppressWarnings("unchecked")
      Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
      while (entries.hasMoreElements()) {
        ZipArchiveEntry ze = entries.nextElement();
        if (!ze.isDirectory()) {
          nbFiles++;
        }
      }
    } catch (IOException ioe) {
      SilverLogger.getLogger(ZipUtil.class)
          .error("Error while counting file in archive " + archive.getPath(), ioe);
    }
    return nbFiles;
  }

}
