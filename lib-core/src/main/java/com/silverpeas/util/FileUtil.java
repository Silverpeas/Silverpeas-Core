package com.silverpeas.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.StringTokenizer;

import javax.activation.MimetypesFileTypeMap;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.ResourceLocator;

public class FileUtil implements MimeTypes {

  private static ResourceLocator MIME_TYPES_EXTENSIONS = new ResourceLocator(
      "com.stratelia.webactiv.util.attachment.mime_types", "");

  public static final String CONTEXT_TOKEN = ",";

  public static final String BASE_CONTEXT = "Attachment";

  private static final MimetypesFileTypeMap MIME_TYPES = new MimetypesFileTypeMap();

  /**
   * Extract the mime-type from the file name.
   * 
   * @param fileName
   *          the name of the file.
   * @return the mime-type as a String.
   */
  public static String getMimeType(String fileName) {
    String mimeType = null;
    String fileExtension = FileRepositoryManager.getFileExtension(fileName)
        .toLowerCase();
    try {
      if (MIME_TYPES_EXTENSIONS != null) {
        mimeType = MIME_TYPES_EXTENSIONS.getString(fileExtension);
      }
    } catch (MissingResourceException e) {
      SilverTrace.warn("attachment", "AttachmentController",
          "attachment.MSG_MISSING_MIME_TYPES_PROPERTIES", null, e);
    } catch (NullPointerException e) {
      SilverTrace.warn("attachment", "AttachmentController",
          "attachment.MSG_FILE_LOGICAL_NAME_NULL", null, e);
    }
    if (mimeType == null) {
      MIME_TYPES.getContentType(fileName);
    }
    if (ARCHIVE_MIME_TYPE.equalsIgnoreCase(mimeType)) {
      if (JAR_EXTENSION.equalsIgnoreCase(fileExtension)
          || WAR_EXTENSION.equalsIgnoreCase(fileExtension)
          || EAR_EXTENSION.equalsIgnoreCase(fileExtension)) {
        mimeType = JAVA_ARCHIVE_MIME_TYPE;
      } else if ("3D".equalsIgnoreCase(fileExtension)) {
        mimeType = SPINFIRE_MIME_TYPE;
      }
    }
    return mimeType;
  }

  /**
   * to create the array of the string this array represents the repertories
   * where the files must be stored.
   * 
   * @param str
   *          : type String: the string of repertories
   * @param token
   *          : type String: the token separating the repertories
   */
  public static String[] getAttachmentContext(String context) {
    if (context == null || "".equals(context)) {
      return new String[] { BASE_CONTEXT };
    }
    StringTokenizer strToken = new StringTokenizer(context, CONTEXT_TOKEN);
    List<String>folders = new ArrayList<String>(10);
    folders.add(BASE_CONTEXT);
    while (strToken.hasMoreElements()) {
      folders.add(strToken.nextToken().trim());
    }
    return (String[]) folders.toArray(new String[folders.size()]);
  }
  
  /**
   * Read the content of a file in a byte array.
   * @param file the file to be read.
   * @return the bytes array containing the content of the file.
   * @throws IOException
   */
  public static byte[] readFile(File file) throws IOException {
    FileInputStream in = null;
    ByteArrayOutputStream out = null;
    try {
      out = new ByteArrayOutputStream();
      in = new FileInputStream(file);
      byte[] buffer = new byte[8];
      int c = 8;
      while ((c = in.read(buffer, 0, c)) >= 0) {
        out.write(buffer, 0, c);
      }
      return out.toByteArray();
    } finally {
      if (in != null) {
        in.close();
      }
      if (out != null) {
        out.close();
      }
    }
  }
  
  /**
   * Write a stream into a file.
   * @param file the file to be written.
   * @param data the data to be written.
   * @throws IOException
   */
  public static void writeFile(File file, InputStream data) throws IOException {
    FileOutputStream out = null;
    BufferedInputStream in = null;
    try {
      out = new FileOutputStream(file);
      byte[] buffer = new byte[8];
      int c = 8;
      while ((c = data.read(buffer, 0, c)) >= 0) {
        out.write(buffer, 0, c);
      }
      out.flush();
    } finally {
      if (in != null) {
        in.close();
      }
      if (out != null) {
        out.close();
      }
    }
  }
  
  /**
   * Write a stream into a file.
   * @param file the file to be written.
   * @param data the data to be written.
   * @throws IOException
   */
  public static void writeFile(File file, Reader data) throws IOException {
    FileWriter out = null;
    BufferedReader in = null;
    try {
      out = new FileWriter(file);
      char[] buffer = new char[8];
      int c = 8;
      while ((c = data.read(buffer, 0, c)) >= 0) {
        out.write(buffer, 0, c);
      }
      out.flush();
    } finally {
      if (in != null) {
        in.close();
      }
      if (out != null) {
        out.close();
      }
    }
  }
}
