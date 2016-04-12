/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.core.util;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
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
import java.util.Arrays;
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
 * The settings in a such bundle are structured into a tree of setting sections and are schema or
 * DTD free. Each non-leave node defines a setting section and the only
 * constrain is how each setting is defined: by the XML element <code>param</code> that must be
 * made up of the following leaf XML elements.
 * <pre>
 * param = param-name param-description? param-value+
 * </pre>
 * with <code>?</code> meaning zero element or more and <code>+</code> meaning at least one element.
 * </p>
 * The content of an XML bundle is cached but there is no expiration-based mechanism of this cache.
 * </p>
 * If the XML content of the bundle is malformed, then a {@code java.util.MissingResourceException}
 * exception is thrown.
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
   * a section <code>foo</code> itself defined in the root node <code>configuration</code>, then
   * the fully qualified parameter will be <code>configuration.toto.driver</code>.
   * @return a set of keys.
   */
  @Override
  public Set<String> keySet() {
    Node rootNode = getCurrentRootNode();
    List<Node> nodes = findAllNodes(rootNode, PARAM_NAME, true);
    Set<String> keys = new LinkedHashSet<>(nodes.size());
    for (Node node : nodes) {
      String nodeFullName = node.getTextContent();
      Node parent = node.getParentNode();
      if (PARAM.equals(parent.getNodeName())) {
        while ((parent = parent.getParentNode()).getNodeName().equals(rootNode.getNodeName())) {
          nodeFullName = parent.getNodeName() + "." + nodeFullName;
        }
        keys.add(rootNode.getNodeName() + "." + nodeFullName);
      }
    }
    return keys;
  }

  /**
   * Is this bundle contains the specified parameter?
   * @param key the absolute or relative fully qualified name of the parameter. It is made up of
   * the path from the root node down to the parameter with the given name, each node separated by
   * a dot. For example, if a parameter named <code>driver</code> is defined in a section
   * <code>foo</code> itself defined in the current root node <code>configuration</code>, then the
   * unique name will be either the absolute one <code>configuration.toto.driver</code> or the
   * relative one <code>toto.driver</code>.
   * @return true if this bundle has the specified parameter, false otherwise.
   */
  @Override
  public boolean containsKey(final String key) {
    String path = key;
    Node rootNode = getCurrentRootNode();
    if (!key.startsWith(rootNode.getNodeName())) {
      path = rootNode.getNodeName() + "." + key;
    }
    return keySet().contains(path);
  }

  /**
   * What is the fully qualified name of this bundle.
   * @return the base bundle name (that is to say without the hierarchy path name that is handled
   * by {@code org.silverpeas.core.util.ResourceLocator}).
   */
  @Override
  public String getBaseBundleName() {
    return this.name;
  }

  /**
   * Gets the value as a String of the parameter identified by the specified key. The key is the
   * fully qualified name of a parameter and should exist in the bundle otherwise a
   * {@code java.util.MissingResourceException} exception is thrown.
   * @param key the unique name of the parameter in this bundle. It is made up of the absolute or
   * relative path from the root node down to the parameter, each node name separated by a dot.
   * For example, if a parameter named <code>driver</code> is defined in a section <code>foo</code>
   * itself defined in the root node <code>configuration</code>, then the unique name will be
   * either the absolute one <code>configuration.toto.driver</code> or the relative one
   * <code>toto.driver</code>.
   * @return the value of the data as a string of characters. If several values are defined for
   * the specified parameter, then returns null. In that case, use the
   * {@code org.silverpeas.core.util.XmlSettingBundle#getStringArray} method.
   * @throws MissingResourceException if either the bundle doesn't exist or the key isn't defined
   * in the bundle.
   */
  @Override
  public String getString(final String key) throws MissingResourceException {
    Node param = getParameter(key);
    String value = null;
    List<Node> values = findAllNodes(param, PARAM_VALUE, false);
    if (values.size() == 1) {
      value = values.get(0).getTextContent();
    }
    return value;
  }

  /**
   * Gets all the values as a String of the parameter identified by the specified key. The key is
   * the fully qualified name of a parameter and should exist in the bundle otherwise a
   * {@code java.util.MissingResourceException} exception is thrown.
   * @param key the unique name of the parameter in this bundle. It is made up of the absolute or
   * relative path from the root node down to the parameter, each node name separated by a dot.
   * For example, if a parameter named <code>driver</code> is defined in a section <code>foo</code>
   * itself defined in the root node <code>configuration</code>, then the unique name will be
   * either the absolute one <code>configuration.toto.driver</code> or the relative one
   * <code>toto.driver</code>.
   * @return the value of the data as a string of characters. If several values are defined for
   * the specified parameter, then returns null.
   * @throws MissingResourceException if either the bundle doesn't exist or the key isn't defined
   * in the bundle.
   */
  public String[] getStringArray(final String key) throws MissingResourceException {
    Node param = getParameter(key);
    List<Node> valueNodes = findAllNodes(param, PARAM_VALUE, false);
    List<String> values =
        valueNodes.stream().map(Node::getTextContent).collect(Collectors.toList());
    return values.toArray(new String[values.size()]);
  }

  /**
   * Is this bundle exists?
   * @return true if this bundle exists, false otherwise.
   */
  @Override
  public boolean exists() {
    try {
      Document document = getXMLDocument();
      return document != null;
    } catch (MissingResourceException e) {
      return false;
    }
  }

  /**
   * Gets the first setting section in this XML setting bundle that is identified by the specified
   * path. The section is itself an XML setting bundle whose the root node is set to the node
   * located at the specified path.
   * All the parameter queries will be then done from this new root node (id est from the section).
   * The path must identify a section and not a parameter otherwise a
   * {@code java.util.MissingResourceException} exception could be thrown.
   * @param path the path of an XML node that represents a configuration section that can contain
   * subsections and parameters. The path is either absolute or relative to the current root node
   * down to the node to return, each node name separated by a dot. For example,
   * to get a section <code>services</code> in a node <code>foo</code> itself defined in the
   * current root node <code>configuration</code>, then path could be either the absolute one
   * <code>configuration.toto.services</code> or the relative one <code>toto.services</code>.
   * @return the asked XML setting section.
   * @throws MissingResourceException if the specified node doesn't exist in the XML setting
   * bundle or if the specified node represents a parameter and not a section.
   */
  public SettingSection getSettingSection(final String path) throws MissingResourceException {
    Node rootNode = getCurrentRootNode();
    String absolutePath = path;
    if (!path.startsWith(rootNode.getNodeName())) {
      absolutePath = rootNode.getNodeName() + "." + path;
    }
    String[] nodePath = absolutePath.split("\\.");
    Node currentNode = getNodeAt(nodePath);
    return new SettingSection(this.name, currentNode);
  }

  /**
   * Gets all the setting sections in this XML setting bundle that are all located at the specified
   * path. The sections are themselves an XML setting bundle whose the root node is set to a node
   * located at the specified path.
   * All the parameter queries will be then done from this new root node (id est from the section).
   * The path must identify a section and not a parameter otherwise a
   * {@code java.util.MissingResourceException} exception could be thrown.
   * @param path the path of the XML nodes that represent each of them a configuration section that
   * can contain subsections and parameters. The path is either absolute or relative to the current
   * root node down to the nodes to return, each node name separated by a dot. For example,
   * to get all the sections <code>service</code> in a node <code>services</code> itself defined in
   * the current root node <code>configuration</code>, then the path could be either the absolute
   * one <code>configuration.services.service</code> or the relative one
   * <code>services.service</code>.
   * @return a list of the asked XML setting sections.
   * @throws MissingResourceException if no nodes at the specified path are found in the XML
   * setting bundle or if the specified node represents a parameter and not a section.
   */
  public List<SettingSection> getAllSettingSection(final String path)
      throws MissingResourceException {
    Node rootNode = getCurrentRootNode();
    String absolutePath = path;
    if (!path.startsWith(rootNode.getNodeName())) {
      absolutePath = rootNode.getNodeName() + "." + path;
    }
    String[] nodePath = absolutePath.split("\\.");
    Node parentNode = getNodeAt(Arrays.copyOf(nodePath, nodePath.length - 1));
    List<Node> nodes = findAllNodes(parentNode, nodePath[nodePath.length - 1], false);
    if (nodes.isEmpty()) {
      throw new MissingResourceException(
          "Can't find resource for bundle " + this.name + ", key " + path, this.name, path);
    }
    return nodes.stream().map(n -> new SettingSection(this.name, n)).collect(Collectors.toList());
  }

  protected Node getCurrentRootNode() {
    return getXMLDocument().getDocumentElement();
  }

  private Document getXMLDocument() {
    if (dom == null) {
      String name = this.name.replaceAll("\\.", "/");
      if (!name.toLowerCase().endsWith(".xml")) {
        name += ".xml";
      }
      try (InputStream stream = this.loader.apply(name)) {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
        InputSource inputSource = new InputSource(stream);
        dom = documentBuilder.parse(inputSource);
        dom.normalize();
      } catch (ParserConfigurationException | SAXException | IOException e) {
        throw new MissingResourceException(e.getMessage() + this.name, this.name, "");
      }
    }
    return dom;
  }

  /**
   * Gets the node representing the parameter with the specified key.
   * @param key the fully qualified name of the parameter, that is to say the relative or absolute
   * path of the node defining the parameter from the current root node node.
   * @return the XML node representing the parameter.
   * @throws MissingResourceException if no such parameter, relative to the current node, is
   * defined into the bundle.
   */
  private Node getParameter(final String key) throws MissingResourceException {
    Node rootNode = getCurrentRootNode();
    String path = key;
    if (!key.startsWith(rootNode.getNodeName())) {
      path = rootNode.getNodeName() + "." + key;
    }
    String[] nodePath = path.split("\\.");
    Node currentNode = getNodeAt(Arrays.copyOf(nodePath, nodePath.length - 1));
    List<Node> params = findAllNodes(currentNode, PARAM, false);
    for (Node param : params) {
      Node paramName = findNode(param, PARAM_NAME, true);
      if (paramName.getTextContent().equals(nodePath[nodePath.length - 1])) {
        return param;
      }
    }
    throw new MissingResourceException(
        "Can't find resource for bundle " + this.name + ", key " + path, this.name, path);
  }

  /**
   * Gets the node at the specified path relative to the current node.
   * @param nodePath the absolute path of the node from to the current root node.
   * @return the node at the given path.
   * @throws MissingResourceException if no such node exists at the given path.
   */
  private Node getNodeAt(String[] nodePath) throws MissingResourceException {
    Node currentNode = getCurrentRootNode();
    for (int i = 0; i < nodePath.length - 1 && currentNode != null; ) {
      if (currentNode.getNodeName().equals(nodePath[i])) {
        if (currentNode.hasChildNodes() && i < nodePath.length - 1) {
          Node nextNode = null;
          NodeList childNodes = currentNode.getChildNodes();
          i = i + 1;
          for (int j = 0; j < childNodes.getLength() && nextNode == null; j++) {
            Node childNode = childNodes.item(j);
            if (childNode.getNodeName().equals(nodePath[i])) {
              nextNode = childNode;
            }
          }
          currentNode = nextNode;
        }
      } else {
        currentNode = null;
      }
    }
    if (currentNode == null ||
        (nodePath.length > 0 && !currentNode.getNodeName().equals(nodePath[nodePath.length - 1]))) {
      String key = Arrays.toString(nodePath);
      throw new MissingResourceException(
          "Can't find resource for bundle " + this.name + ", key " + key, this.name, key);
    }
    return currentNode;
  }

  /**
   * Finds the first node with the specified name. The specified node is checked first before
   * continuing further forward among the children (in the case <code>recurse</code> is true).
   * @param node the node from which a node of the specified name is looked for.
   * @param name the name of the node to find.
   * @param recurse if the seek must be done up to find a node with the specified name.
   * @return either one node having the given name or null if no node was found with this name.
   */
  private static Node findNode(Node node, String name, boolean recurse) {
    if (node.getNodeName().equals(name)) {
      return node;
    }
    if (node.hasChildNodes() && recurse) {
      NodeList list = node.getChildNodes();
      for (int i = 0; i < list.getLength(); i++) {
        Node found = findNode(list.item(i), name, recurse);
        if (found != null) {
          return found;
        }
      }
    }
    return null;
  }

  /**
   * Finds from the specified node, all the children nodes with the specified node name. If
   * <code>recurse</code> is true, then the seek is recursively performed among all the children.
   * @param node the node from which all the nodes with the specified name is looked for.
   * @param name the name of the nodes to find.
   * @param recurse if the seek must be done all along the tree from the specified root down to
   * the leave nodes.
   * @return a list of nodes having the specified name.
   */
  private static List<Node> findAllNodes(Node node, String name, boolean recurse) {
    List<Node> v = new ArrayList<>(10);
    NodeList list = node.getChildNodes();
    for (int i = 0; i < list.getLength(); i++) {
      Node found = findNode(list.item(i), name, recurse);
      if (found != null) {
        v.add(found);
      }
    }
    return v;
  }

  public static class SettingSection extends XmlSettingBundle {
    private Node node;

    private SettingSection(String bundleName, Node node) {
      super(bundleName, null);
      this.node = node;
    }

    /**
     * Gets the name of this section.
     * @return the section name.
     */
    public String getName() {
      return node.getNodeName();
    }

    /**
     * Gets the value of the specified section's attribute.
     * @param attribute the attribute identifier.
     * @return the value of the given attribute or null if no such attribute exists.
     */
    public String getAttribute(String attribute) {
      String value = null;
      Node attributeNode = node.getAttributes().getNamedItem(attribute);
      if (attributeNode != null) {
        value = attributeNode.getNodeValue();
      }
      return value;
    }

    /**
     * Gets all the names of the attribute of this section.
     * @return a set of attribute names.
     */
    public Set<String> attributeSet() {
      NamedNodeMap attributeNodes = node.getAttributes();
      Set<String> attributes = new LinkedHashSet<>(attributeNodes.getLength());
      for (int i = 0; i < attributeNodes.getLength(); i++) {
        attributes.add(attributeNodes.item(i).getNodeName());
      }
      return attributes;
    }

    @Override
    protected Node getCurrentRootNode() {
      return node;
    }

  }
}
