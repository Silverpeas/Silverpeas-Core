/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.util.indexEngine.parser.ooParser;

/**
 * 
 * Parser for Open Office
 * Parse the content (content.xml) and the meta datas (meta.xml)
 *
 */
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.indexEngine.parser.Parser;

public class OOParser implements Parser {
  /**
   * Constructor declaration
   */

  public OOParser() {
  }

  private String tempFolder = null;
  private final String TMP_UNZIP_DIR = "tmpUnzipOpenOffice";
  private final Namespace NS_OO = Namespace.getNamespace("office",
      "urn:oasis:names:tc:opendocument:xmlns:office:1.0");
  public final Namespace NS_DC = Namespace.getNamespace("dc",
      "http://purl.org/dc/elements/1.1/");
  private final Namespace NS_OOMETA = Namespace.getNamespace("meta",
      "urn:oasis:names:tc:opendocument:xmlns:meta:1.0");
  private final Namespace NS_OOTEXT = Namespace.getNamespace("text",
      "urn:oasis:names:tc:opendocument:xmlns:text:1.0");
  // Meta data tags
  private final String TITLE = "title";
  private final String SUBJECT = "subject";
  private final String DESCRIPTION = "description";
  private final String INITIAL_CREATOR = "initial-creator";
  private final String KEYWORD = "keyword";

  // Open Office files needed for indexing
  private final String contentFile = "content.xml";
  private final String metaFile = "meta.xml";

  public Reader getReader(String path, String encoding) {
    Reader reader = null;
    tempFolder = new Long(new Date().getTime()).toString();
    try {
      List toIndex = getFilesToIndex(path);
      String ooContents = this.parse(toIndex);
      deleteDir(new File(FileRepositoryManager.getTemporaryPath()
          + TMP_UNZIP_DIR + File.separator + tempFolder));
      reader = new StringReader(ooContents);
    } catch (Exception e) {
      SilverTrace.error("indexEngine", "OOParser.getReader()",
          "indexEngine.MSG_IO_ERROR_WHILE_READING", path, e);
    }
    return reader;
  }

  public String parse(Object file) {
    SilverTrace.debug("indexEngine", "OOParser.parse()",
        "root.MSG_PARAM_VALUE", "file=" + file.toString());
    org.jdom.Document xmlDocContent = new org.jdom.Document();
    org.jdom.Document xmlMeta = new org.jdom.Document();
    StringBuffer parsingResult = new StringBuffer();

    try {
      List files = (List) file;
      SAXBuilder builder = new SAXBuilder();
      builder.setValidation(false);
      SilverTrace.debug("indexEngine", "OOParser.parse()()",
          "root.MSG_PARAM_VALUE", "file0 = " + files.get(0));
      SilverTrace.debug("indexEngine", "OOParser.parse()()",
          "root.MSG_PARAM_VALUE", "file1 = " + files.get(1));
      xmlDocContent = builder.build(new File((String) files.get(0)));
      xmlMeta = builder.build(new File((String) files.get(1)));
      // Process Content file
      // Get only elements in <text: > tag
      Element body = xmlDocContent.getRootElement().getChild("body", NS_OO);
      if (body != null) {
        Iterator childrenElements = body.getDescendants(new ElementFilter(
            NS_OOTEXT));
        while (childrenElements.hasNext()) {
          Element currentElement = (Element) childrenElements.next();
          SilverTrace.debug("indexEngine", "OOParser.parse()",
              "Current Element = " + currentElement.getName() + " - "
              + currentElement.getText());
          parsingResult.append(" ").append(currentElement.getText());
        }
      }

      // Process Meta data file
      List children = xmlMeta.getRootElement().getChildren();
      if (children != null) {
        Iterator i = children.iterator();
        while (i.hasNext()) {
          Element currentElement = (Element) i.next();
          if (currentElement.getChild(TITLE, NS_DC) != null)
            parsingResult.append(" ").append(
                currentElement.getChild(TITLE, NS_DC).getText());
          if (currentElement.getChild(SUBJECT, NS_DC) != null)
            parsingResult.append(" ").append(
                currentElement.getChild(SUBJECT, NS_DC).getText());
          if (currentElement.getChild(DESCRIPTION, NS_DC) != null)
            parsingResult.append(" ").append(
                currentElement.getChild(DESCRIPTION, NS_DC).getText());
          if (currentElement.getChild(INITIAL_CREATOR, NS_OOMETA) != null)
            parsingResult.append(" ").append(
                currentElement.getChild(INITIAL_CREATOR, NS_OOMETA).getText());
          if (currentElement.getChild(KEYWORD, NS_OOMETA) != null)
            parsingResult.append(" ").append(
                currentElement.getChild(KEYWORD, NS_OOMETA).getText());
        }
      }
    } catch (JDOMException e) {
      SilverTrace.error("indexEngine", "OOParser.parse",
          "indexEngine.MSG_IO_ERROR_WHILE_PARSING", e);
      deleteTmp((File) file);
    } catch (IOException e) {
      SilverTrace.error("indexEngine", "OOParser.parse",
          "indexEngine.MSG_IO_ERROR_WHILE_PARSING", e);
      deleteTmp((File) file);
    }
    SilverTrace.debug("indexEngine", "OOParser.parse()", "parsingResult = "
        + parsingResult.toString());
    return parsingResult.toString();
  }

  private List<String> getFilesToIndex(String file) {
    String dest = FileRepositoryManager.getTemporaryPath() + TMP_UNZIP_DIR
        + File.separator + tempFolder;
    unzip(file, dest);
    List<String> ls = new ArrayList<String>();
    ls.add(0, dest + File.separator + contentFile);
    ls.add(1, dest + File.separator + metaFile);
    return ls;
  }

  private List unzip(String zip, String destination) {
    SilverTrace.debug("indexEngine", "OOParser.unzip()()",
        "root.MSG_PARAM_VALUE", "zip = " + zip + " destination=" + destination);
    List<String> destLs = new ArrayList<String>();
    Enumeration entries;
    ZipFile zipFile;
    File dest = new File(destination);
    try {
      SilverTrace.debug("indexEngine", "OOParser.unzip()()",
          "root.MSG_PARAM_VALUE", "tempFolder = " + tempFolder);
      dest.mkdirs();
      if (dest.isDirectory()) {
        zipFile = new ZipFile(zip);
        entries = zipFile.entries();
        while (entries.hasMoreElements()) {
          ZipEntry entry = (ZipEntry) entries.nextElement();
          if (entry.getName().equals("meta.xml")
              || entry.getName().equals("content.xml")) {
            copyInputStream(zipFile.getInputStream(entry),
                new BufferedOutputStream(new FileOutputStream(dest
                + File.separator + entry.getName())));
            destLs.add(dest.getAbsolutePath() + File.separator
                + entry.getName());
          }
        }
        zipFile.close();
      } else {
        SilverTrace.error("indexEngine", "OOParser.unzip",
            "indexEngine.MSG_IO_ERROR_WHILE_READING", dest.getAbsolutePath());
      }
    } catch (IOException e) {
      deleteDir(new File(destination));
      SilverTrace.error("indexEngine", "OOParser.unzip",
          "indexEngine.MSG_IO_ERROR_WHILE_EXTRACTING", zip, e);
    } catch (Exception e) {
      deleteDir(new File(destination));
      SilverTrace.error("indexEngine", "OOParser.unzip",
          "indexEngine.MSG_IO_ERROR_WHILE_EXTRACTING", zip, e);
    }
    return destLs;
  }

  private void copyInputStream(InputStream in, OutputStream out)
      throws IOException {
    byte[] buffer = new byte[1024];
    int len;
    while ((len = in.read(buffer)) >= 0)
      out.write(buffer, 0, len);
    in.close();
    out.close();
  }

  public boolean deleteDir(File dir) {
    if (dir.isDirectory()) {
      String[] children = dir.list();
      for (int i = 0; i < children.length; i++) {
        boolean success = deleteDir(new File(dir, children[i]));
        if (!success) {
          return false;
        }
      }
    }
    return dir.delete();
  }

  protected void deleteTmp(File file) {
    String dir = FileRepositoryManager.getTemporaryPath() + TMP_UNZIP_DIR
        + File.separator + tempFolder + File.separator + file;
    deleteDir(new File(dir));
  }
}