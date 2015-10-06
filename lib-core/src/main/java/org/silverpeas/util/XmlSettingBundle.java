/**
 * Copyright (C) 2000 - 2015 Silverpeas
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.util;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A bundle of settings in XML used to configure some features in Silverpeas or the behaviour of an
 * application.
 * </p>
 * The settings in a such bundle are structured as a tree and are schema or DTD free. The only
 * constrain is how each setting is defined: by the XML element <code>param</code> that is made
 * up as following
 * <pre>
 * param = param-name param-description? param-value+
 * </pre>
 * with <code>?</code> meaning zero or more and <code>+</code> meaning at least one.
 * </p>
 * The content of an XML bundle is cached but there is no expiration-based mechanism of this cache.
 *
 * @author miguel
 */
public class XmlSettingBundle implements SilverpeasBundle {

  private static final String PARAM = "param";
  private static final String PARAM_NAME = "param-name";
  private static final String PARAM_DESCRIPTION = "param-description";
  private static final String PARAM_VALUE = "param-value";


  private final Function<String, InputStream> loader;
  private final String name;
  private Document dom;

  protected XmlSettingBundle(final String name, Function<String, InputStream> loader) {
    this.name = name;
    this.loader = loader;
  }

  /**
   * Gets a set of all the parameter names defined in this bundle. Each parameter name is fully
   * qualified: it is made up of the complete path from the root node down to the parameter, each
   * node name separated by a dot. For example, if a param named <code>driver</code> is defined in
   * a node <code>foo</code> itself defined in the root node <code>configuration</code>, then the
   * fully qualified parameter will be <code>configuration.toto.driver</code>.
   * @return a set of keys.
   */
  @Override
  public Set<String> keySet() {
    Document document = getXMLDocument();
    Set<String> keys = new LinkedHashSet<>(10);
    List<Node> nodes = findAllNodes(document, PARAM_NAME);
    for (Node node : nodes) {
      String nodeFullName = node.getNodeValue();
      Node parent = node.getParentNode();
      if (PARAM.equals(parent.getLocalName())) {
        while ((parent = parent.getParentNode()) != null) {
          nodeFullName = parent.getNodeName() + "." + nodeFullName;
        }
        keys.add(nodeFullName);
      }
    }
    return keys;
  }

  /**
   * Is this bundle contains the specified parameter.
   * @param key fully qualified name of the parameter.
   * @return true if this bundle has the specified parameter, false otherwise.
   */
  @Override
  public boolean containsKey(final String key) {
    return keySet().contains(key);
  }

  /**
   * What is the fully qualified name of this bundle.
   * @return the base bundle name (that is to say without the hierarchy path name that is handled
   * by {@code org.silverpeas.util.ResourceLocator}).
   */
  @Override
  public String getBaseBundleName() {
    return this.name;
  }

  /**
   * Gets the value as a String of the parameter identified by the specified key. The key is the
   * fully qualified name of a parameter and should exist in the bundle otherwise a
   * {@code java.util.MissingResourceException} exception is thrown.
   * @param key the unique name of the parameter in this bundle. It is made up of the complete path
   * from the root node down to the parameter, each node name separated by a dot. For example, if a
   * param named <code>driver</code> is defined in a node <code>foo</code> itself defined in the
   * root node <code>configuration</code>, then the unique name will be
   * <code>configuration.toto.driver</code>.
   * @return the value of the data as a string of characters. If several values are defined for
   * the specified parameter, then returns null. In that case, use the
   * {@code org.silverpeas.util.XmlSettingBundle#getStringArray} method.
   * @throws MissingResourceException if either the bundle doesn't exist or the key isn't defined
   * in the bundle.
   */
  @Override
  public String getString(final String key) throws MissingResourceException {
    Node param = findParameter(key);
    String value = null;
    List<Node> values = findAllNodes(param, PARAM_VALUE);
    if (values.size() == 1) {
      value = values.get(0).getNodeValue();
    }
    return value;
  }

  /**
   * Gets all the values as String of the parameter identified by the specified key. The key is the
   * fully qualified name of a parameter and should exist in the bundle otherwise a
   * {@code java.util.MissingResourceException} exception is thrown.
   * @param key the unique name of the parameter in this bundle. It is made up of the complete path
   * from the root node down to the parameter, each node name separated by a dot. For example, if a
   * param named <code>driver</code> is defined in a node <code>foo</code> itself defined in the
   * root node <code>configuration</code>, then the unique name will be
   * <code>configuration.toto.driver</code>.
   * @return the value of the data as a string of characters. If several values are defined for
   * the specified parameter, then returns null.
   * @throws MissingResourceException if either the bundle doesn't exist or the key isn't defined
   * in the bundle.
   */
  public String[] getStringArray(final String key) throws MissingResourceException {
    Node param = findParameter(key);
    List<Node> valueNodes = findAllNodes(param, PARAM_VALUE);
    List<String> values = valueNodes.stream().map(Node::getNodeValue).collect(Collectors.toList());
    return values.toArray(new String[values.size()]);
  }

  /**
   * Is this bundle exists?
   * @return true if this bundle exists, false otherwise.
   */
  @Override
  public boolean exists() {
    return false;
  }

  private Document getXMLDocument() {
    if (dom == null) {
      try {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
        InputSource inputSource = new InputSource(this.loader.apply(this.name));
        dom = documentBuilder.parse(inputSource);
        dom.normalize();
      } catch (ParserConfigurationException e) {
        e.printStackTrace();
      } catch (SAXException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return dom;
  }

  private Node findParameter(String key) {
    String[] nodeNames = key.split(",");
    Node currentNode = getXMLDocument();
    for (int i = 0; i < nodeNames.length -2 && currentNode != null;) {
      if (currentNode.getNodeName().equals(nodeNames[i])) {
        if (currentNode.hasChildNodes() && i < nodeNames.length - 2) {
          Node nextNode = null;
          NodeList childNodes = currentNode.getChildNodes();
          for (int j = 0; j < childNodes.getLength(); j++) {
            Node childNode = childNodes.item(i);
            if (childNode.getNodeName().equals(nodeNames[++i])) {
              nextNode = childNode;
            }
          }
          currentNode = nextNode;
        }
      } else {
        currentNode = null;
      }
    }
    if (currentNode != null) {
      List<Node> params = findAllNodes(currentNode, PARAM);
      for (Node param: params) {
        Node paramName = findNode(param, PARAM_NAME);
        if (paramName.getNodeValue().equals(nodeNames[nodeNames.length - 1])) {
          return param;
        }
      }
    }
    throw new MissingResourceException(
        "Can't find resource for bundle " + this.name + ", key " + key, this.name, key);
  }

  private Node findNode(Node node, String name) {
    if (node.getNodeName().equals(name)) {
      return node;
    }
    if (node.hasChildNodes()) {
      NodeList list = node.getChildNodes();
      int size = list.getLength();
      for (int i = 0; i < size; i++) {
        Node found = findNode(list.item(i), name);
        if (found != null) {
          return found;
        }
      }
    }
    return null;
  }

  private List<Node> findAllNodes(Node node, String name) {
    List<Node> v = new ArrayList<>(10);
    if (node.getNodeName().equals(name)) {
      v.add(node);
    } else if (node.hasChildNodes()) {
      NodeList list = node.getChildNodes();
      int size = list.getLength();
      for (int i = 0; i < size; i++) {
        Node found = findNode(list.item(i), name);
        if (found != null) {
          v.add(found);
        }
      }
    }
    return v;
  }
}
