package com.stratelia.webactiv.util.attachment.model.jcr.impl;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;

import org.apache.jackrabbit.JcrConstants;

import com.silverpeas.jcrutil.model.impl.AbstractJcrRegisteringTestCase;

public abstract class AbstractJcrTestCase extends AbstractJcrRegisteringTestCase {

  public AbstractJcrTestCase() {
    super();
  }

  public AbstractJcrTestCase(String name) {
    super(name);
  }

  protected String readFile(String path) throws IOException {
    CharArrayWriter writer = null;
    InputStream in = null;
    Reader reader = null;
    try {
      in = new FileInputStream(path);
      writer = new CharArrayWriter();
      reader = new InputStreamReader(in);
      char[] buffer = new char[8];
      int c = 0;
      while ((c = reader.read(buffer)) != -1) {
        writer.write(buffer, 0, c);
      }
      return new String(writer.toCharArray());
    } catch (IOException ioex) {
      return null;
    } finally {
      if (reader != null) {
        reader.close();
      }
      if (in != null) {
        in.close();
      }
      if (writer != null) {
        writer.close();
      }
    }
  }

  protected void createTempFile(String path, String content) throws IOException {
    File attachmentFile = new File(path);
    attachmentFile.deleteOnExit();
    FileOutputStream out = null;
    Writer writer = null;
    try {
      out = new FileOutputStream(attachmentFile);
      writer = new OutputStreamWriter(out);
      writer.write(content);
    } finally {
      if (writer != null) {
        writer.close();
      }
      if (out != null) {
        out.close();
      }
    }
  }

  protected String readFileFromNode(Node fileNode) throws IOException,
      ValueFormatException, PathNotFoundException, RepositoryException {
    CharArrayWriter writer = null;
    InputStream in = null;
    Reader reader = null;
    try {
      in = fileNode.getNode(JcrConstants.JCR_CONTENT).getProperty(
          JcrConstants.JCR_DATA).getStream();
      writer = new CharArrayWriter();
      reader = new InputStreamReader(in);
      char[] buffer = new char[8];
      int c = 0;
      while ((c = reader.read(buffer)) != -1) {
        writer.write(buffer, 0, c);
      }
      return new String(writer.toCharArray());
    } catch (IOException ioex) {
      return null;
    } finally {
      if (reader != null) {
        reader.close();
      }
      if (in != null) {
        in.close();
      }
      if (writer != null) {
        writer.close();
      }
    }
  }

  protected String[] getConfigLocations() {
    return new String[] { "spring-in-memory-jcr.xml" };
  }
}
