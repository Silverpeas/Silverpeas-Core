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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
/*i
 * XMLConfigurationStore.java
 *
 * Created on 17 novembre 2000, 13:44
 */

package com.stratelia.webactiv.util;

//import com.sun.xml.tree.XmlDocument;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xerces.parsers.DOMParser;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * This object implements and extends the ConfigurationStore interface for XML
 * files. As a ConfigurationStore, it imposes a standard structure on the grove
 * that can be mapped 1-1 with properties, but it offers all necessary methods
 * to handle arbitrary XML structures as well.
 * <p>
 * The standard structure is the following:<br>
 * &lt;param&gt;<br>
 * &lt;param-name&gt;<br>
 * <i> parameter name </i><br>
 * &lt;/param-name&gt;<br>
 * &lt;param-value&gt;<br>
 * <i> parameter value </i><br>
 * &lt;/param-value&gt;<br>
 * &lt;param-description&gt;<br>
 * <i> parameter description </i><br>
 * &lt;/param-description&gt;<br>
 * <p>
 * The parameter description is any arbitrary string that could be used for
 * instance as a tooltip text or a as a label for a configuration tool that
 * should display the use of the given parameter.
 * <p>
 * Note that symbold such as <strong>&lt;</strong> or <strong>&amp;</strong>
 * must be escaped properly when included in the parameter name, value or
 * description.
 * 
 * @author jpouyadou
 * @version
 */
public class XMLConfigurationStore extends Object implements ConfigurationStore {
  /**
   * XML document root
   */
  private org.w3c.dom.Node m_RootNode = null;
  private Document m_XMLConfig = null;
  String m_ConfigFileName = null;

  /** Creates new XMLConfigurationStore */
  public XMLConfigurationStore(String configFileName,
      InputStream configFileInputStream, String rootString) throws Exception {
    load(configFileName, configFileInputStream, rootString);
  }

  public XMLConfigurationStore(File file, String rootString) throws Exception {
    loadFromFile(file, rootString);
  }

  public XMLConfigurationStore() throws Exception {
    try {
      javax.xml.parsers.DocumentBuilderFactory dbf = DocumentBuilderFactory
          .newInstance();
      m_XMLConfig = dbf.newDocumentBuilder().newDocument();
    } catch (javax.xml.parsers.ParserConfigurationException e) {
      throw new Exception(e.getMessage());
    }
  }

  public Node setRoot(String rootString) {
    if (rootString == null) {
      return (null);
    }
    Element el = m_XMLConfig.getDocumentElement();
    Node n = m_XMLConfig.createElement(rootString);
    if (el == null) {
      m_XMLConfig.appendChild(n);
    } else {
      el.appendChild(n);
    }
    return (n);
  }

  public void serialize() throws FileNotFoundException, IOException {
    FileOutputStream out = new FileOutputStream(new File(m_ConfigFileName));
    XMLSerializer ser = new XMLSerializer(out, new OutputFormat("xml", "UTF-8",
        false));
    ser.serialize(m_XMLConfig);
    out.close();
  }

  /**
   * This method sets the config file name. Useful when the configuration store
   * has been created empty, so that is has no associated file.
   */
  public void setConfigFileName(String configfilename) {
    m_ConfigFileName = configfilename;
  }

  private void loadFromFile(File file, String rootString) throws Exception {
    m_ConfigFileName = file.getAbsolutePath();
    load(m_ConfigFileName, null, rootString);
  }

  private void doLoad(String rootString) throws Exception, SAXParseException,
      IOException {
    if (m_XMLConfig == null) {
      throw new Exception(
          "E6000-0025:Cannot create XML document from the configuration file '"
              + m_ConfigFileName + "'");
    }
    // m_XMLConfig.getDocumentElement().normalize();
    ((org.apache.xerces.dom.NodeImpl) (m_XMLConfig)).normalize();
    m_RootNode = findNode(m_XMLConfig, rootString);
    if (m_RootNode == null) {
      throw new Exception("E6000-0023:Invalid configuration file '"
          + m_ConfigFileName + "': " + "Cannot find node '" + rootString + "j'");
    }
  }

  private void load(String configFileName, InputStream configFileInputStream,
      String rootString) throws Exception {
    m_ConfigFileName = configFileName;
    try {
      // if config file was found by the resource locator, it is an input
      // stream,
      // otherwise it is a file
      // javax.xml.parsers.DocumentBuilderFactory dbf =
      // DocumentBuilderFactory.newInstance();
      // javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
      DOMParser parser = new DOMParser();

      if (configFileInputStream == null) {
        // FileInputStream is = new FileInputStream(new File(configFileName));
        SilverTrace.debug("util", "ResourceLocator.locateResourceAsStream",
            "Parsing from file", configFileName);

        // m_XMLConfig = db.parse(is);
        try {
          String cname = "file:///" + configFileName.replace('\\', '/');
          parser.parse(cname);
        } catch (SAXException se) {
          SilverTrace.error("util", "ResourceLocator.load",
              "root.EX_XML_PARSING_FAILED", se);
          throw se;
        } catch (IOException ioe) {
          SilverTrace.error("util", "ResourceLocator.load",
              "root.EX_LOAD_IO_EXCEPTION", ioe);
          throw ioe;
        }
      } else {
        org.xml.sax.InputSource ins = new org.xml.sax.InputSource(
            configFileInputStream);
        parser.parse(ins);
      }

      m_XMLConfig = parser.getDocument();
      doLoad(rootString);
    } catch (IOException e) {
      throw new Exception("E6000-0020:Cannot open configuration file '"
          + configFileName + "': Error:" + e.getMessage());
    } catch (SAXParseException err) {
      throw new Exception("E6000-0022:Cannot parse configuration file '"
          + configFileName + "'" + ": Error at line " + err.getLineNumber()
          + ", uri '" + err.getSystemId() + "': " + err.getMessage());
    } catch (Exception e) {
      throw new Exception("E6000-0021:Cannot open configuration file '"
          + configFileName + "': Error:" + e.getMessage());
    }
  }

  public String getNodeValue(String nodename) {
    return (getXMLParamValue(null, nodename, null));
  }

  /**
   * This method returns the value of the node <strong>entry</strong>, starting
   * from the node <strong>n</strong>. It may consist of the concatenation of
   * various text en entity reference child nodes.
   * <p>
   * This method returns null if the node wasn't found
   */
  public String getXMLParamValue(Node n, String entry, String key) {
    String res = null;

    if (n == null) {
      n = m_RootNode;
    }
    // find the node, starting from the current node
    Node paramNode = findNode(n, entry);

    // node not found
    if (paramNode == null)
      return (null);

    // get all node descendants, and concatenate all their string values. --TODO
    // What about sublevels?
    NodeList paramList = paramNode.getChildNodes();
    int paramsize = paramList.getLength();
    for (int pi = 0; pi < paramsize; pi++) {
      Node psn = paramList.item(pi);
      // some parsers expand entity references, some don't. A possibility
      // encountered is that an entity
      // reference node has children whose values make up the value of the
      // reference, which in turn should
      // be grouped
      // with the rest of the text (concatenated in the res variable). This code
      // may need to be completed
      // when more parsers are put to test... Reminder: an typical entity
      // reference could be a string like
      // &amp; , whereas what we really want is the '&' character.
      if (psn instanceof EntityReference) {
        if (psn.hasChildNodes()) {
          NodeList erKids = psn.getChildNodes();
          int erKidsSize = erKids.getLength();
          for (int ii = 0; ii < erKidsSize; ii++) {
            Node entity = erKids.item(ii);
            if (entity instanceof Text) {
              String v = entity.getNodeValue();
              if (res == null) {
                res = v;
              } else {
                res = res + v;
              }
            }
          }
        }
      }
      // standard text, concatenate the value with other values
      else if (psn instanceof Text) {
        String v;
        if ((v = psn.getNodeValue()) == null)
          return (null);
        if (key != null) {
          if (v.trim().equalsIgnoreCase(key)) {
            return (key);
          }
        } else {
          if (res == null) {
            res = v.trim();
          } else {
            res = res + v.trim();
          }
        }
      }
    }
    if (res != null) {
      // if a key match was asked for, compare the value, return null if not
      // equal
      if (key != null) {
        if (res.trim().equalsIgnoreCase(key)) {
          return (key);
        }
        return (null);
      }
      return (res);
    }
    return (null);
  }

  /**
   * This method returns the values of the node <strong>entry</strong>, starting
   * from the node <strong>n</strong>. Each value may consist of the
   * concatenation of various text en entity reference child nodes.
   * <p>
   * This method returns null if the node wasn't found
   */
  public String[] getXMLParamValues(Node n, String entry, String key) {
    String res = null;
    Vector vres = new Vector(10);

    // find the node, starting from the current node
    Node paramNodes[] = findNodes(n, entry);

    // node not found
    if (paramNodes == null)
      return (null);

    // get all node descendants, and concatenate all their string values. --TODO
    // What about sublevels?
    for (int i = 0; i < paramNodes.length; i++) {
      Node paramNode = paramNodes[i];
      NodeList paramList = paramNode.getChildNodes();
      int paramsize = paramList.getLength();
      for (int pi = 0; pi < paramsize; pi++) {
        Node psn = paramList.item(pi);
        // some parsers expand entity references, some don't. A possibility
        // encountered is that an entity
        // reference node has children whose values make up the value of the
        // reference, which in turn should
        // be grouped
        // with the rest of the text (concatenated in the res variable). This
        // code may need to be completed
        // when more parsers are put to test... Reminder: an typical entity
        // reference could be a string like
        // &amp; , whereas what we really want is the '&' character.
        if (psn instanceof EntityReference) {
          if (psn.hasChildNodes()) {
            NodeList erKids = psn.getChildNodes();
            int erKidsSize = erKids.getLength();
            for (int ii = 0; ii < erKidsSize; ii++) {
              Node entity = erKids.item(ii);
              if (entity instanceof Text) {
                String v = entity.getNodeValue();
                if (res == null) {
                  res = v;
                } else {
                  res = res + v;
                }
              }
            }
          }
        }
        // standard text, concatenate the value with other values
        else if (psn instanceof Text) {
          String v;
          if ((v = psn.getNodeValue()) == null)
            return (null);
          vres.add(v.trim());
        }
      }
    }
    if (vres.size() != 0) {
      String[] r = new String[vres.size()];
      for (int i = 0; i < vres.size(); i++) {
        r[i] = (String) (vres.get(i));
      }
      return (r);
    }
    return (null);
  }

  public Node getXMLParamNode(String nodename) {
    if (m_RootNode.hasChildNodes()) {
      NodeList list = m_RootNode.getChildNodes();
      int size = list.getLength();
      for (int i = 0; i < size; i++) {
        Node n = list.item(i);
        String name = n.getNodeName();
        if (name.equalsIgnoreCase("param")) {
          // param entry found, process it
          if (getXMLParamValue(n, "param-name", nodename) != null)
            return (n);
        }
      }
    }
    return (null);
  }

  /**
   * This method returns the value of an attribute of a given node. If the
   * attribute cannot be found of if the node is null, this method returns null.
   * 
   * @param n
   *          the node where the attribute is stored
   * @param attributeName
   *          the name of the attribute. Case sensitive.
   */
  public String getAttributeValue(Node n, String attributeName) {
    if (n == null) {
      return (null);
    }
    NamedNodeMap amap = n.getAttributes();
    if (amap != null) {
      Node n2 = amap.getNamedItem(attributeName);
      if (n2 == null) {
        return (null);
      }
      String v = n2.getNodeValue();
      return (v);
    } else {
      return (null);
    }
  }

  public void putPropety(String key, String value) {
    put(key, value);
  }

  public void putProperty(String key, String value) {
    put(key, value);
  }

  public void replaceValue(Node n, String key, String value) {
    Node entry = findNode(n, key);
    if (entry != null) {
      n.removeChild(entry);
    }
    Node newElement = m_XMLConfig.createElement(key);
    Node newValue = m_XMLConfig.createTextNode(value);
    newElement.appendChild(newValue);
    n.appendChild(newElement);
  }

  public Node createElement(String key) {
    return (m_XMLConfig.createElement(key));
  }

  public Node createTextNode(String value) {
    return (m_XMLConfig.createTextNode(value));
  }

  public void appendChild(Node parent, Node child) {
    parent.appendChild(child);
  }

  public void put(String key, String value) {
    String values[] = { value };
    put(key, values);
  }

  public void put(String key, String values[]) {
    Node paramNode = getXMLParamNode(key);
    if (paramNode != null) {
      Node paramNodeParent = paramNode.getParentNode();
      if (paramNodeParent != null) {
        paramNodeParent.removeChild(paramNode);
      }
    }
    Element param = m_XMLConfig.createElement("param");
    Node name = m_XMLConfig.createElement("param-name");
    Node description = m_XMLConfig.createElement("param-description");
    Node nameValue = m_XMLConfig.createTextNode(key);
    for (int i = 0; i < values.length; i++) {
      Node vValue = m_XMLConfig.createTextNode(values[i]);
      Node v = m_XMLConfig.createElement("param-value");
      param.appendChild(v);
      v.appendChild(vValue);
    }
    m_RootNode.appendChild(param);
    param.appendChild(name);
    name.appendChild(nameValue);
    param.appendChild(description);
    m_XMLConfig.getDocumentElement().normalize();
  }

  /**
   * This method returns the value of a standard-format parameter, that is, it
   * returns the text value of the param-value element that goes with the
   * param-name for the <strong>key</strong> element. See the description of
   * standard XML resources for details <br>
   * If the key os not found, the defaultValue string is returned instead.
   */
  public String getProperty(String key, String defaultValue) {
    return (get(key, defaultValue));
  }

  public String getProperty(String key) {
    return (get(key, null));
  }

  /**
   * This method is for compatibility with the ResourceLocator
   */
  public String getString(String key) {
    return (get(key, null));
  }

  public String get(String key, String defaultValue) {
    String thisFunction = "XMLConfigurationStore.get";
    if (m_RootNode.hasChildNodes()) {
      NodeList list = m_RootNode.getChildNodes();
      int size = list.getLength();
      if (size == 0) {
        SilverTrace.debug("util", thisFunction,
            "Root node has an empty children list. Returning default value");
      }
      for (int i = 0; i < size; i++) {
        Node n = list.item(i);
        String name = n.getNodeName();
        if (name.equalsIgnoreCase("param")) {
          // param entry found, process it
          if (getXMLParamValue(n, "param-name", key) != null) {
            String v = getXMLParamValue(n, "param-value", null);
            if (v == null)
              return (defaultValue);
            else
              return (v);
          }
        }
      }
    }
    SilverTrace.debug("util", thisFunction,
        "Root node has no children. Returning default value");

    return (defaultValue);
  }

  /**
   * This method returns a long value for the given key. It throws an
   * XMLConfigurationException with the code KEY_NOT_FOUND if it cannot be
   * found, or INVALID_VALUE if it cannot be converted to a long value.
   */
  public long getLongValue(String key) throws XMLConfigurationException {
    String sv = get(key, null);
    if (sv == null) {
      throw new XMLConfigurationException(
          XMLConfigurationException.KEY_NOT_FOUND);
    }
    try {
      long l = Long.parseLong(sv);
      return (l);
    } catch (Exception x) {
      throw new XMLConfigurationException(
          XMLConfigurationException.INVALID_VALUE);
    }
  }

  /**
   * This method returns a long value for the given key. It throws an
   * XMLConfigurationException with the code KEY_NOT_FOUND if it cannot be
   * found, or INVALID_VALUE if it cannot be converted to a long value.
   */
  public int getIntValue(String key) throws XMLConfigurationException {
    String sv = get(key, null);
    if (sv == null) {
      throw new XMLConfigurationException(
          XMLConfigurationException.KEY_NOT_FOUND);
    }
    try {
      int i = Integer.parseInt(sv);
      return (i);
    } catch (Exception x) {
      throw new XMLConfigurationException(
          XMLConfigurationException.INVALID_VALUE);
    }
  }

  /**
   * This method returns all values for a multi-valued key
   */
  public String[] getValues(String key) {
    if (m_RootNode.hasChildNodes()) {
      NodeList list = m_RootNode.getChildNodes();
      int size = list.getLength();
      for (int i = 0; i < size; i++) {
        Node n = list.item(i);
        String name = n.getNodeName();
        if (name.equalsIgnoreCase("param")) {
          // param entry found, process it
          if (getXMLParamValue(n, "param-name", key) != null) {
            return (getXMLParamValues(n, "param-value", null));
          }
        }
      }
    }
    return (null);
  }

  public Node findNode(Node node, String name) {
    if (node.getNodeName().equals(name))
      return node;
    if (node.hasChildNodes()) {
      NodeList list = node.getChildNodes();
      int size = list.getLength();
      for (int i = 0; i < size; i++) {
        Node found = findNode(list.item(i), name);
        if (found != null)
          return found;
      }
    }
    return null;
  }

  public Node[] findNodes(String name) {
    return (findNodes(m_XMLConfig, name));
  }

  /**
   * This method returns all children nodes (at any depth, that is, children,
   * grazndchildren, and so on) from the node <strong>node</strong> whose name
   * matches the <strong>name</strong> string. If <strong>node</strong> is null,
   * the method returns null<br>
   * If <strong>name</strong> is null or empty, the method returns null<br>
   * If no children of the node <strong>node</strong> match the
   * <strong>name</strong> string, the method returns null<br>
   */

  public Node[] findNodes(Node node, String name) {
    if (node == null) {
      return (null);
    }
    if (name == null || name.equals("") || name.trim().equals("")) {
      return (null);
    }
    Vector v = new Vector(10);
    if (node.getNodeName().equals(name)) {
      Node[] res = new Node[1];
      res[0] = node;
      return (res);
    }
    if (node.hasChildNodes()) {
      NodeList list = node.getChildNodes();
      int size = list.getLength();
      for (int i = 0; i < size; i++) {
        Node found = findNode(list.item(i), name);
        if (found != null) {
          v.add(found);
        }
      }
    }
    if (v.size() == 0) {
      return (null);
    }
    Node[] res = new Node[v.size()];
    for (int i = 0; i < v.size(); i++) {
      res[i] = (Node) (v.get(i));
    }
    return (res);
  }

  /**
   * Returns all first level names from the configuration file
   */
  public String[] getAllNames() {
    Vector v = null;
    if (m_RootNode.hasChildNodes() == false)
      return (null);
    NodeList list = m_RootNode.getChildNodes();
    int size = list.getLength();
    if (size > 0) {
      v = new Vector(size);
      for (int i = 0; i < size; i++) {
        Node n = list.item(i);
        String name = n.getNodeName();
        if (name.equalsIgnoreCase("param")) {
          String V = null;
          if ((V = getXMLParamValue(n, "param-name", null)) != null) {
            v.add(V);
          }
        }
      }
    }
    if (v != null && v.size() > 0) {
      String result[] = new String[v.size()];
      for (int i = 0; i < v.size(); i++) {
        result[i] = (String) v.get(i);
      }
      return (result);
    } else
      return (null);
  }

  public static void main(String args[]) {
    XMLConfigurationStore C = null;
    if (args.length != 1) {
      System.err.println("ERROR:Invalid args count:" + args.length);
      System.err.println("ERROR:usage is XMLConfigurationStore filename");
      System.exit(1);
    }
    String filename = args[0];
    try {
      System.err.println("Processing XML file '" + filename);
      C = new XMLConfigurationStore(new File(filename), "testroot");
    } catch (Exception e) {
      System.err.println("Error while processing XML file '" + filename + "':"
          + e.getMessage());
      System.exit(2);
    }
    if (C == null) {
      System.err.println("could not created configuration from file '"
          + filename + "'");
      System.exit(2);
    }
    String names[] = C.getAllNames();
    if (names == null || names.length == 0) {
      System.out
          .println("no names found. Maybe the root element isn't named testroot?");
      System.exit(0);
    }
    System.out.println("Names found:");
    for (int i = 0; i < names.length; i++) {
      System.out.println(names[i]);
    }

  }
}