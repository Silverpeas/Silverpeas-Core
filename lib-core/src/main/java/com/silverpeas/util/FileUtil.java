/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

import java.io.File;
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

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.ResourceLocator;

public class FileUtil implements MimeTypes {

  private static final ResourceLocator MIME_TYPES_EXTENSIONS = new ResourceLocator(
      "org.silverpeas.util.attachment.mime_types", "");
  public static final String CONTEXT_TOKEN = ",";
  public static final String BASE_CONTEXT = "Attachment";
  private static final MimetypesFileTypeMap MIME_TYPES = new MimetypesFileTypeMap();
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
    return resource.replace("com/silverpeas", "org/silverpeas").replace(
        "com/stratelia/silverpeas", "org/silverpeas").replace("com/stratelia/webactiv",
        "org/silverpeas");
  }

  /**
   * Extract the mime-type from the file name.
   *
   * @param fileName the name of the file.
   * @return the mime-type as a String.
   */
  public static String getMimeType(final String fileName) {
    String mimeType = null;
    final String fileExtension = FileRepositoryManager.getFileExtension(fileName).toLowerCase();
    try {
      if (MIME_TYPES_EXTENSIONS != null) {
        mimeType = MIME_TYPES_EXTENSIONS.getString(fileExtension);
      }
    } catch (final MissingResourceException e) {
      SilverTrace.warn("attachment", "AttachmentController",
          "attachment.MSG_MISSING_MIME_TYPES_PROPERTIES", null, e);
    }
    if (mimeType == null) {
      mimeType = MIME_TYPES.getContentType(fileName);
    }
    if (ARCHIVE_MIME_TYPE.equalsIgnoreCase(mimeType) || SHORT_ARCHIVE_MIME_TYPE.equalsIgnoreCase(
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
   * Indicates if the current file is of type archive.
   *
   * @param filename the name of the file.
   * @return true is the file s of type archive - false otherwise.
   */
  public static boolean isArchive(final String filename) {
    return ARCHIVE_MIME_TYPES.contains(getMimeType(filename));
  }

  /**
   * Indicates if the current file is of type archive.
   *
   * @param filename the name of the file.
   * @return true is the file s of type archive - false otherwise.
   */
  public static boolean isImage(final String filename) {
    return FilenameUtils.isExtension(filename, ImageUtil.IMAGE_EXTENTIONS);
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
    return mimeType.startsWith(WORD_2007_EXTENSION) || mimeType.startsWith(EXCEL_2007_EXTENSION)
        || mimeType.startsWith(POWERPOINT_2007_EXTENSION);
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
   * Forces the deletion of the specified file.
   * @param fileToDelete file to delete.
   * @throws IOException if the deletion failed or if the file doesn't exist.
   */
  public static void forceDeletion(File fileToDelete) throws IOException {
    FileUtils.forceDelete(fileToDelete);
  }

  /**
   * Moves the specified source file to the specified destination. If the destination exists, it is
   * then replaced by the source; if the destination is a directory, then it is deleted with all of
   * its contain.
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

  private FileUtil() {
  }
}