/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.util.file;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.silverpeas.core.SilverpeasExceptionMessages;
import org.silverpeas.core.exception.RelativeFileAccessException;
import org.silverpeas.core.io.media.MetadataExtractor;
import org.silverpeas.core.mail.extractor.Mail;
import org.silverpeas.core.util.ImageUtil;
import org.silverpeas.core.util.OsEnum;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.StringTokenizer;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.silverpeas.core.cache.service.CacheServiceProvider.getRequestCacheService;
import static org.silverpeas.core.util.MimeTypes.*;

/**
 * Util class to perform file system operations.
 * All file operations wil be removed in the future in profit of the new Files class in the JDK.
 */
public class FileUtil {

  private static final SettingBundle MIME_TYPES_EXTENSIONS =
      ResourceLocator.getSettingBundle("org.silverpeas.util.attachment.mime_types");
  public static final String CONTEXT_TOKEN = ",";
  public static final String BASE_CONTEXT = "Attachment";
  private static final MimetypesFileTypeMap MIME_TYPES = new MimetypesFileTypeMap();
  private static final String MIME_TYPE_CACHE_KEY_PREFIX = "FileUtil.getMimeType$$";

  private FileUtil() {
  }

  /**
   * Detects the mime-type of the specified file.
   *
   * The mime-type is first extracted from its content. If the detection fails or if the file cannot
   * be located by its specified name, then the mime-type is detected from the file extension.
   *
   * @param fileName the name of the file with its path.
   * @return the mime-type of the specified file.
   */
  public static String getMimeType(final String fileName) {

    // Request caching in order to increase significantly the performance about file parsing
    String cacheKey = MIME_TYPE_CACHE_KEY_PREFIX + fileName;
    String cachedMimeType = getRequestCacheService().getCache().get(cacheKey, String.class);
    if (StringUtil.isDefined(cachedMimeType)) {
      return cachedMimeType;
    }

    String mimeType = computeMimeType(fileName);

    // The computed mime type is put into the request cache (performance)
    getRequestCacheService().getCache().put(cacheKey, mimeType);
    return mimeType;
  }

  @NotNull
  private static String computeMimeType(final String fileName) {
    String mimeType = null;
    final String fileExtension = FileRepositoryManager.getFileExtension(fileName).toLowerCase();
    File file = new File(fileName);
    if (file.exists()) {
      mimeType = getMimeTypeByMetadata(file, mimeType);
    }
    if (!StringUtil.isDefined(mimeType) && MIME_TYPES_EXTENSIONS != null &&
        !fileExtension.isEmpty()) {
      mimeType = getMimeTypeByFileExtension(fileExtension, mimeType);
    }
    if (!StringUtil.isDefined(mimeType)) {
      mimeType = MIME_TYPES.getContentType(fileName);
    }
    mimeType = getPeculiarChildMimeType(mimeType, fileExtension);
    if (mimeType == null) {
      mimeType = DEFAULT_MIME_TYPE;
    }
    return mimeType;
  }

  @Nullable
  private static String getPeculiarChildMimeType(String parentMimeType, final String fileExtension) {
    String mimeType = parentMimeType;
    // if the mime type is application/xhml+xml or text/html whereas the file is a JSP or PHP script
    if (XHTML_MIME_TYPE.equalsIgnoreCase(parentMimeType) || HTML_MIME_TYPE.equalsIgnoreCase(parentMimeType)) {
      if (fileExtension.contains(JSP_EXTENSION)) {
        mimeType = JSP_MIME_TYPE;
      } else if (fileExtension.contains(PHP_EXTENSION)) {
        mimeType = PHP_MIME_TYPE;
      }
      // if the mime type refers a ZIP archive, checks if it is an archive of the java platform
    } else if (ARCHIVE_MIME_TYPE.equalsIgnoreCase(parentMimeType) || SHORT_ARCHIVE_MIME_TYPE.
        equalsIgnoreCase(parentMimeType)) {
      if (JAR_EXTENSION.equalsIgnoreCase(fileExtension) || WAR_EXTENSION.equalsIgnoreCase(
          fileExtension) || EAR_EXTENSION.equalsIgnoreCase(fileExtension)) {
        mimeType = JAVA_ARCHIVE_MIME_TYPE;
      } else if ("3D".equalsIgnoreCase(fileExtension)) {
        mimeType = SPINFIRE_MIME_TYPE;
      }
    }
    return mimeType;
  }

  private static String getMimeTypeByFileExtension(final String fileExtension, final String defaultMimeType) {
    String mimeType = defaultMimeType;
    try {
      mimeType = MIME_TYPES_EXTENSIONS.getString(fileExtension);
    } catch (final MissingResourceException e) {
      SilverLogger.getLogger(FileUtil.class).warn("Unknown mime-type: {0}", e.getMessage(), e);
    }
    return mimeType;
  }

  private static String getMimeTypeByMetadata(final File file, final String defaultMimeType) {
    String mimeType = defaultMimeType;
    try {
      mimeType = MetadataExtractor.get().detectMimeType(file);
    } catch (Exception ex) {
      SilverLogger.getLogger(FileUtil.class)
          .warn("File exists ({0}), but mime-type has been detected: {1}", file.getName(),
              ex.getMessage(), ex);
    }
    return mimeType;
  }

  /**
   * Create the array of strings this array represents the repertories where the files must be
   * stored.
   */
  public static String[] getAttachmentContext(final String context) {
    if (!StringUtil.isDefined(context)) {
      return new String[]{BASE_CONTEXT};
    }
    final StringTokenizer strToken = new StringTokenizer(context, CONTEXT_TOKEN);
    final List<String> folders = new ArrayList<>(10);
    folders.add(BASE_CONTEXT);
    while (strToken.hasMoreElements()) {
      folders.add(strToken.nextToken().trim());
    }
    return folders.toArray(new String[folders.size()]);
  }

  /**
   * Read the content of a file as text (the text is supposed to be in the UTF-8 charset).
   * Instead of using this method, prefer to use the following Java > 7 statement:<br/>
   * <pre>{@code new String(Files.readAllBytes(file.toPath()), Charset.defaultCharset());}</pre>
   *
   * @param file the file to read.
   * @return the file content as a String.
   * @throws IOException if an error occurs while reading the file.
   */
  public static String readFileToString(final File file) throws IOException {
    return new String(Files.readAllBytes(file.toPath()), Charset.defaultCharset());
  }

  /**
   * Indicates if the OS is from the Microsoft Windows familly
   *
   * @return true if the OS is from the Microsoft Windows familly - false otherwise.
   */
  public static boolean isWindows() {
    return OsEnum.getOS().isWindows();
  }

  /**
   * If 3D document.
   *
   * @param filename the name of the file.
   * @return true or false
   */
  public static boolean isSpinfireDocument(String filename) {
    return SPINFIRE_MIME_TYPE.equals(getMimeType(filename));
  }

  /**
   * Indicates if the current file is of type archive.
   *
   * @param filename the name of the file.
   * @return true is the file s of type archive - false otherwise.
   */
  public static boolean isArchive(final String filename) {
    return ARCHIVE_MIME_TYPES.contains(getMimeType(filename));
  }

  /**
   * Indicates if the current file is of type image.
   *
   * @param filename the name of the file.
   * @return true is the file is of type image - false otherwise.
   */
  public static boolean isImage(final String filename) {
    String mimeType = getMimeType(filename);
    if (DEFAULT_MIME_TYPE.equals(mimeType)) {
      return FilenameUtils.isExtension(filename.toLowerCase(), ImageUtil.IMAGE_EXTENTIONS);
    } else {
      return mimeType.startsWith("image");
    }
  }

  /**
   * Indicates if the current file is of type mail.
   *
   * @param filename the name of the file.
   * @return true is the file is of type mail - false otherwise.
   */
  public static boolean isMail(final String filename) {
    return FilenameUtils.isExtension(filename, Mail.MAIL_EXTENTIONS);
  }

  /**
   * Indicates if the current file is of type PDF.
   *
   * @param filename the name of the file.
   * @return true is the file s of type archive - false otherwise.
   */
  public static boolean isPdf(final String filename) {
    final String mimeType = getMimeType(filename);
    return PDF_MIME_TYPE.equals(mimeType);
  }

  public static boolean isOpenOfficeCompatible(final String filename) {
    final String mimeType = getMimeType(filename);
    return OPEN_OFFICE_MIME_TYPES.contains(mimeType) || isMsOfficeExtension(mimeType);
  }

  private static boolean isMsOfficeExtension(final String mimeType) {
    return mimeType.startsWith(WORD_2007_EXTENSION) || mimeType.startsWith(EXCEL_2007_EXTENSION) ||
        mimeType.startsWith(POWERPOINT_2007_EXTENSION) || mimeType.startsWith(MSPROJECT_MIME_TYPE);

  }

  /**
   * Asserts that the path doesn't contain relative navigation between pathes.
   *
   * @param path the path to check
   * @throws RelativeFileAccessException when a relative path is detected.
   */
  public static void assertPathNotRelative(String path) throws RelativeFileAccessException {
    String unixPath = FilenameUtils.separatorsToUnix(path);
    if (unixPath != null && (unixPath.contains("../") || unixPath.contains("/.."))) {
      throw new RelativeFileAccessException(
          SilverpeasExceptionMessages.failureOnGetting("path with relative parts", path));
    }
  }

  /**
   * Forces the deletion of the specified file. If the write property of the file to delete isn't
   * set, this property is then set before deleting.
   *
   * @param fileToDelete file to delete.
   * @throws IOException if the deletion failed or if the file doesn't exist.
   */
  public static void forceDeletion(File fileToDelete) throws IOException {
    if (fileToDelete.exists() && !fileToDelete.canWrite()) {
      fileToDelete.setWritable(true);
    }
    try(Stream<Path> paths = Files.walk(fileToDelete.toPath())) {
      paths.sorted(Comparator.reverseOrder())
          .map(Path::toFile)
          .forEach(File::delete);
    }
  }

  /**
   * Moves the specified source file to the specified destination. If the destination exists, it is
   * then replaced by the source; if the destination is a directory, then it is deleted with all of
   * its contain.
   *
   * @param source the file to move.
   * @param destination the destination file of the move.
   * @throws IOException if the source or the destination is invalid or if an error occurs while
   * moving the file.
   */
  public static void moveFile(File source, File destination) throws IOException {
    if (destination.exists()) {
      forceDeletion(destination);
    }
    Files.move(source.toPath(), destination.toPath());
  }

  /**
   * Copies the specified source file to the specified destination. If the destination exists, it is
   * then replaced by the source. If the destination can be overwritten, its write property is set
   * before the copy.
   *
   * @param source the file to copy.
   * @param destination the destination file of the move.
   * @throws IOException if the source or the destination is invalid or if an error occurs while
   * copying the file.
   */
  public static void copyFile(File source, File destination) throws IOException {
    if (destination.exists() && !destination.canWrite()) {
      destination.setWritable(true);
    }
    Files.copy(source.toPath(), destination.toPath(), REPLACE_EXISTING);
  }

  /*
   * Remove any \ or / from the filename thus avoiding conflicts on the server.
   *
   * @param fileName
   * @return
   */
  public static String getFilename(String fileName) {
    if (!StringUtil.isDefined(fileName)) {
      return "";
    }
    return FilenameUtils.getName(fileName);
  }

  /**
   * Convert a path to the current OS path format.
   *
   * @param undeterminedOsPath a path
   * @return server OS pah.
   */
  public static String convertPathToServerOS(String undeterminedOsPath) {
    if (undeterminedOsPath == null || !StringUtil.isDefined(undeterminedOsPath)) {
      return "";
    }
    String localPath = undeterminedOsPath;
    localPath = localPath.replace('\\', File.separatorChar).replace('/', File.separatorChar);
    return localPath;
  }

  public static boolean deleteEmptyDir(File directory) {
    if (directory.exists() && directory.isDirectory() && directory.list() != null && directory.
        list().length == 0) {
      try {
        Files.delete(directory.toPath());
        return true;
      } catch (IOException e) {
        SilverLogger.getLogger(FileUtil.class).warn(e);
        return false;
      }
    }
    return false;
  }

  /**
   * Moves all files from sub folders to the given root folder and deletes after all the sub
   * folders.
   * @param rootFolder the root folder from which the sub folders are retrieved and into which the
   * files will be moved if any.
   * @return an array of {@link File} that represents the found sub folders. The returned array is
   * never null.
   * @throws IOException
   */
  public static File[] moveAllFilesAtRootFolder(File rootFolder) throws IOException {
    return moveAllFilesAtRootFolder(rootFolder, true);
  }

  /**
   * Moves all files from sub folders to the given root folder.
   * @param rootFolder the root folder from which the sub folders are retrieved and into which the
   * files will be moved if any.
   * @param deleteFolders true if the sub folders must be deleted.
   * @return an array of {@link File} that represents the found sub folders. The returned array is
   * never null.
   * @throws IOException
   */
  public static File[] moveAllFilesAtRootFolder(File rootFolder, boolean deleteFolders)
      throws IOException {
    File[] foldersAtRoot = rootFolder != null ?
        rootFolder.listFiles((FileFilter) FileFilterUtils.directoryFileFilter()) : null;
    if (foldersAtRoot != null) {
      for (File folderAtRoot : foldersAtRoot) {
        for (File file : FileUtils.listFiles(folderAtRoot, FileFilterUtils.fileFileFilter(),
            FileFilterUtils.trueFileFilter())) {
          File newFilePath = new File(rootFolder, file.getName());
          if (!newFilePath.exists()) {
            FileUtils.moveFile(file, newFilePath);
          }
        }
        if (deleteFolders) {
          FileUtils.deleteQuietly(folderAtRoot);
        }
      }
    }
    return foldersAtRoot != null ? foldersAtRoot : new File[0];
  }

  /**
   * Validate that fileName given in parameter is inside extraction target directory (intendedDir)
   * @param fileName the file name to extract
   * @param intendedDir the extraction target directory
   * @return the filename if fileName is inside extraction target directory
   * @throws java.io.IOException if fileName is outside extraction target directory
   */
  public static String validateFilename(String fileName, String intendedDir)
      throws java.io.IOException {
    File f = new File(fileName);
    String canonicalPath = f.getCanonicalPath();

    File iD = new File(intendedDir);
    String canonicalID = iD.getCanonicalPath();

    if (canonicalPath.startsWith(canonicalID)) {
      return canonicalPath;
    } else {
      throw new IllegalStateException("File is outside extraction target directory (security)");
    }
  }
}
