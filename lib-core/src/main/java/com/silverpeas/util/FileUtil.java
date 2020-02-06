/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
package com.silverpeas.util;

import com.silverpeas.util.exception.RelativeFileAccessException;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.ResourceLocator;
import org.apache.commons.exec.util.StringUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.silverpeas.util.mail.Mail;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import static org.silverpeas.cache.service.CacheServiceFactory.getRequestCacheService;

public class FileUtil implements MimeTypes {

  private static final ResourceLocator MIME_TYPES_EXTENSIONS = new ResourceLocator(
      "org.silverpeas.util.attachment.mime_types", "");
  public static final String CONTEXT_TOKEN = ",";
  public static final String BASE_CONTEXT = "Attachment";
  private static final MimetypesFileTypeMap MIME_TYPES = new MimetypesFileTypeMap();
  private static final String MIME_TYPE_CACHE_KEY_PREFIX = "FileUtil.getMimeType$$";
  private static final ClassLoader loader = java.security.AccessController.doPrivileged(
      new java.security.PrivilegedAction<ConfigurationClassLoader>() {
        @Override
        public ConfigurationClassLoader run() {
          return new ConfigurationClassLoader(FileUtil.class.getClassLoader());
        }
      });

  /**
   * Utility method for migration of Silverpeas configuration from : com.silverpeas,
   * com.stratelia.silverpeas, com.stratelia.webactiv to org.silverpeas
   *
   * @param bundle the name of the bundle.
   * @return the name of the migrated bundle.
   */
  public static String convertBundleName(final String bundle) {
    return bundle.replace("com.silverpeas", "org.silverpeas").replace(
        "com.stratelia.silverpeas", "org.silverpeas").replace("com.stratelia.webactiv",
            "org.silverpeas");
  }

  /**
   * Utility method for migration of Silverpeas configuration from : com/silverpeas,
   * com/stratelia/silverpeas, com/stratelia/webactiv to org/silverpeas
   *
   * @param resource the name of the resource.
   * @return the name of the migrated resource.
   */
  public static String convertResourceName(final String resource) {
    return resource.replace("com/silverpeas", "org/silverpeas")
        .replace("com/stratelia/silverpeas", "org/silverpeas")
        .replace("com/stratelia/webactiv", "org/silverpeas");
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
    String cachedMimeType = getRequestCacheService().get(cacheKey, String.class);
    if (StringUtil.isDefined(cachedMimeType)) {
      return cachedMimeType;
    }

    String mimeType = null;
    final String fileExtension = FileRepositoryManager.getFileExtension(fileName).toLowerCase();
    File file = new File(fileName);
    if (file.exists()) {
      try {
        mimeType = MetadataExtractor.getInstance().detectMimeType(file);
      } catch (Exception ex) {
        SilverTrace.warn("attachment", "FileUtil",
            "attachment.MSG_MISSING_MIME_TYPES_PROPERTIES", ex.getMessage(), ex);
      }
    }
    if (!StringUtil.isDefined(mimeType)) {
      try {
        if (MIME_TYPES_EXTENSIONS != null) {
          if (!fileExtension.isEmpty()) {
            mimeType = MIME_TYPES_EXTENSIONS.getString(fileExtension);
          }
        }
      } catch (final MissingResourceException e) {
        SilverTrace.warn("attachment", "FileUtil",
            "attachment.MSG_MISSING_MIME_TYPES_PROPERTIES", null, e);
      }
    }
    if (!StringUtil.isDefined(mimeType)) {
      mimeType = MIME_TYPES.getContentType(fileName);
    }
    // if the mime type is application/xhml+xml or text/html whereas the file is a JSP or PHP script
    if (XHTML_MIME_TYPE.equalsIgnoreCase(mimeType) || HTML_MIME_TYPE.equalsIgnoreCase(mimeType)) {
      if (fileExtension.contains(JSP_EXTENSION)) {
        mimeType = JSP_MIME_TYPE;
      } else if (fileExtension.contains(PHP_EXTENSION)) {
        mimeType = PHP_MIME_TYPE;
      }
      // if the mime type refers a ZIP archive, checks if it is an archive of the java platform
    } else if (ARCHIVE_MIME_TYPE.equalsIgnoreCase(mimeType) || SHORT_ARCHIVE_MIME_TYPE.
        equalsIgnoreCase(
            mimeType)) {
      if (JAR_EXTENSION.equalsIgnoreCase(fileExtension) || WAR_EXTENSION.equalsIgnoreCase(
          fileExtension) || EAR_EXTENSION.equalsIgnoreCase(fileExtension)) {
        mimeType = JAVA_ARCHIVE_MIME_TYPE;
      } else if ("3D".equalsIgnoreCase(fileExtension)) {
        mimeType = SPINFIRE_MIME_TYPE;
      }
    }
    if (mimeType == null) {
      mimeType = DEFAULT_MIME_TYPE;
    }

    // The computed mime type is put into the request cache (performance)
    getRequestCacheService().put(cacheKey, mimeType);
    return mimeType;
  }

  /**
   * Create the array of strings this array represents the repertories where the files must be
   * stored.
   *
   * @param context
   * @return
   */
  public static String[] getAttachmentContext(final String context) {
    if (!StringUtil.isDefined(context)) {
      return new String[]{BASE_CONTEXT};
    }
    final StringTokenizer strToken = new StringTokenizer(context, CONTEXT_TOKEN);
    final List<String> folders = new ArrayList<String>(10);
    folders.add(BASE_CONTEXT);
    while (strToken.hasMoreElements()) {
      folders.add(strToken.nextToken().trim());
    }
    return folders.toArray(new String[folders.size()]);
  }

  /**
   * Read the content of a file in a byte array.
   *
   * @param file the file to be read.
   * @return the bytes array containing the content of the file.
   * @throws IOException
   */
  public static byte[] readFile(final File file) throws IOException {
    return FileUtils.readFileToByteArray(file);
  }

  /**
   * Read the content of a file as text (the text is supposed to be in the UTF-8 charset).
   *
   * @param file the file to read.
   * @return the file content as a String.
   * @throws IOException if an error occurs while reading the file.
   */
  public static String readFileToString(final File file) throws IOException {
    return FileUtils.readFileToString(file);
  }

  /**
   * Write a stream into a file.
   *
   * @param file the file to be written.
   * @param data the data to be written.
   * @throws IOException
   */
  public static void writeFile(final File file, final InputStream data) throws IOException {
    FileOutputStream out = null;
    try {
      out = new FileOutputStream(file);
      IOUtils.copy(data, out);
    } finally {
      if (out != null) {
        out.close();
      }
    }
  }

  /**
   * Write a stream into a file.
   *
   * @param file the file to be written.
   * @param data the data to be written.
   * @throws IOException
   */
  public static void writeFile(final File file, final Reader data) throws IOException {
    FileWriter out = new FileWriter(file);
    try {
      IOUtils.copy(data, out);
    } finally {
      IOUtils.closeQuietly(out);
    }
  }

  /**
   * Loads a ResourceBundle from the Silverpeas configuration directory.
   *
   * @param bundleName the name of the bundle.
   * @param locale the locale of the bundle.
   * @return the corresponding ResourceBundle if it exists - null otherwise.
   */
  public static ResourceBundle loadBundle(final String bundleName, final Locale locale) {
    String name = convertBundleName(bundleName);
    ResourceBundle bundle;
    Locale loc = locale;
    if (loc == null) {
      loc = Locale.ROOT;
    }
    try {
      bundle = ResourceBundle.getBundle(name, loc, loader, new ConfigurationControl());
      if (bundle == null) {
        bundle = ResourceBundle.getBundle(bundleName, loc, loader, new ConfigurationControl());
      }
    } catch (MissingResourceException mex) {
      //Let's try with the real name
      bundle = ResourceBundle.getBundle(bundleName, loc, loader, new ConfigurationControl());
    }
    return bundle;
  }

  /**
   * Loads loads the resource into the specified properties.
   *
   * @param properties the properties to be loaded with the resource.
   * @param resourceName the name of the resource.
   * @throws IOException
   */
  public static void loadProperties(final Properties properties, final String resourceName) throws
      IOException {
    if (StringUtil.isDefined(resourceName) && properties != null) {
      String name = convertResourceName(resourceName);
      InputStream in = loader.getResourceAsStream(name);
      try {
        properties.load(in);
      } finally {
        IOUtils.closeQuietly(in);
      }
    }
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

  static boolean isMsOfficeExtension(final String mimeType) {
    return mimeType.startsWith("application/vnd.ms-word") ||
        mimeType.startsWith("application/vnd.ms-excel") ||
        mimeType.startsWith("application/vnd.ms-powerpoint") ||
        mimeType.startsWith("application/vnd.ms-project");
  }

  /**
   * Checking that the path doesn't contain relative navigation between pathes.
   *
   * @param path
   * @throws RelativeFileAccessException
   */
  public static void checkPathNotRelative(String path) throws RelativeFileAccessException {
    String unixPath = FilenameUtils.separatorsToUnix(path);
    if (unixPath != null && (unixPath.contains("../") || unixPath.contains("/.."))) {
      throw new RelativeFileAccessException();
    }
  }

  public static Collection<File> listFiles(File directory, String[] extensions, boolean recursive) {
    return FileUtils.listFiles(directory, extensions, recursive);
  }

  public static Collection<File> listFiles(File directory, String[] extensions,
      boolean caseSensitive, boolean recursive) {
    if (caseSensitive) {
      return listFiles(directory, extensions, recursive);
    }
    IOFileFilter filter;
    if (extensions == null) {
      filter = TrueFileFilter.INSTANCE;
    } else {
      String[] suffixes = new String[extensions.length];
      for (int i = 0; i < extensions.length; i++) {
        suffixes[i] = "." + extensions[i];
      }
      filter = new SuffixFileFilter(suffixes, IOCase.INSENSITIVE);
    }
    return FileUtils.listFiles(directory, filter, (recursive ? TrueFileFilter.INSTANCE
        : FalseFileFilter.INSTANCE));
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
    FileUtils.forceDelete(fileToDelete);
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
      FileUtils.forceDelete(destination);
    }
    FileUtils.moveFile(source, destination);
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
    FileUtils.copyFile(source, destination);
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

  private FileUtil() {
  }

  /**
   * Convert a path to the current OS path format.
   *
   * @param undeterminedOsPath
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

  public static String convertFilePath(File file) {
    if (OsEnum.getOS().isWindows()) {
      return StringUtils.quoteArgument(file.getAbsolutePath());
    }
    String path = file.getAbsolutePath();
    path = path.replaceAll("\\\\", "\\\\\\\\");
    path = path.replaceAll("\\s", "\\\\ ");
    path = path.replaceAll("<", "\\\\<");
    path = path.replaceAll(">", "\\\\>");
    path = path.replaceAll("'", "\\\\'");
    path = path.replaceAll("\"", "\\\\\"");
    path = path.replaceAll("\\{", "\\\\{");
    path = path.replaceAll("}", "\\\\}");
    path = path.replaceAll("\\(", "\\\\(");
    path = path.replaceAll("\\)", "\\\\)");
    path = path.replaceAll("\\[", "\\\\[");
    path = path.replaceAll("\\]", "\\\\]");
    path = path.replaceAll("\\&", "\\\\&");
    path = path.replaceAll("\\|", "\\\\|");
    return path;
  }

  public static boolean deleteEmptyDir(File directory) {
    if (directory.exists() && directory.isDirectory() && directory.list() != null && directory.
        list().length == 0) {
      return directory.delete();
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

  public static boolean isFileSecure(String fileName, String intendedDir) {
    boolean result = false;
    try {
      validateFilename(fileName, intendedDir);
      result = true;
    } catch (Exception e) {
      SilverTrace.warn("fileutil", "FileUtil.isFileSecure", "Security alert on " + fileName);
    }
    return result;
  }

}
