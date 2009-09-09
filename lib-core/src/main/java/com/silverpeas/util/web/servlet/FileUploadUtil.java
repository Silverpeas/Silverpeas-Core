package com.silverpeas.util.web.servlet;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.stratelia.webactiv.util.exception.UtilException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class FileUploadUtil {

  public static final boolean isRequestMultipart(HttpServletRequest request) {
    return ServletFileUpload.isMultipartContent(request);
  }

  @SuppressWarnings("unchecked")
  public static final List<FileItem> parseRequest(HttpServletRequest request)
      throws UtilException {
    try {
      // Create a factory for disk-based file items
      FileItemFactory factory = new DiskFileItemFactory();
      // Create a new file upload handler
      ServletFileUpload upload = new ServletFileUpload(factory);

      // Parse the request
      return (List<FileItem>) upload.parseRequest(request);
    } catch (FileUploadException fuex) {
      throw new UtilException("FileUploadUtil.parseRequest",
          "Error uploading files", fuex);
    }
  }

  @SuppressWarnings("unchecked")
  public static final List<FileItem> parseRequest(HttpServletRequest request, int maxSize)
      throws UtilException {
    try { // Create a factory for disk-based file items
      DiskFileItemFactory factory = new DiskFileItemFactory();
      // Set factory constraints
      factory.setSizeThreshold(maxSize);
      // Create a new file upload handler
      ServletFileUpload upload = new ServletFileUpload(factory);

      // Parse the request
      return (List<FileItem>) upload.parseRequest(request);
    } catch (FileUploadException fuex) {
      throw new UtilException("FileUploadUtil.parseRequest",
          "Error uploading files", fuex);
    }
  }

  public static String getParameter(List<FileItem> items, String parameterName) {
    Iterator<FileItem> iter = items.iterator();
    while (iter.hasNext()) {
      FileItem item = iter.next();
      if (item.isFormField() && parameterName.equals(item.getFieldName())) {
        return item.getString();
      }
    }
    return null;
  }

  public static String getOldParameter(List items, String parameterName) {
    return getParameter((List<FileItem>) items, parameterName);
  }

  public static FileItem getOldFile(List items, String parameterName) {
    return getFile((List<FileItem>) items, parameterName);
  }

  public static FileItem getFile(List<FileItem> items, String parameterName) {
    Iterator<FileItem> iter = items.iterator();
    while (iter.hasNext()) {
      FileItem item = iter.next();
      if (!item.isFormField() && parameterName.equals(item.getFieldName())) {
        return item;
      }
    }
    return null;
  }

  public static FileItem getFile(List<FileItem> items) {
    Iterator<FileItem> iter = items.iterator();
    while (iter.hasNext()) {
      FileItem item = iter.next();
      if (!item.isFormField()) {
        return item;
      }
    }
    return null;
  }

  public static FileItem getFile(HttpServletRequest request) throws UtilException {
    List<FileItem> items = FileUploadUtil.parseRequest(request);

    return FileUploadUtil.getFile(items);
  }

  public static String getFileName(FileItem file) {
    if (file == null) {
      return "";
    }

    String fullFileName = file.getName();
    if (fullFileName == null) {
      return "";
    }

    return fullFileName.substring(fullFileName.lastIndexOf(File.separator) + 1, fullFileName.length());
  }

  public static void saveToFile(File file, FileItem item) throws IOException {
    OutputStream out = FileUtils.openOutputStream(file);
    InputStream in = item.getInputStream();
    try {
      IOUtils.copy(in, out);
    } finally {
      IOUtils.closeQuietly(in);
      IOUtils.closeQuietly(out);
    }
  }
}
