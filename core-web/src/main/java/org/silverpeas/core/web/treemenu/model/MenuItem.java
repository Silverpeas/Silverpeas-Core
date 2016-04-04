/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.web.treemenu.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class MenuItem implements Serializable {
  /**
   *
   */
  private static final long serialVersionUID = 8721214991615974221L;

  /**
   * the children of current menu element
   */
  private List<MenuItem> children = null;

  /**
   * the label to display for current element label must be in the correct language
   */
  @XmlElement
  private String label = null;

  /**
   * component identifier for example "kmelia1" This attribute is used only to identify the theme
   * component. Otherwise the component identifier is stored in the key attribute
   */
  @XmlElement
  private String componentId = null;
  /**
   * unique key to identify the item
   */
  @XmlElement(name = "id")
  private String key = null;
  /**
   * item level in the tree menu
   */
  @XmlElement
  private int level = -1;
  /**
   * type of node (component, space or theme)
   */
  @XmlElement(name = "nodeType")
  private NodeType type = null;

  /**
   * value of The HTML Target Attribute for Anchors (&lt;a&gt;) possible values = _blank, _self,
   * _parent, _top.
   */
  private String target = null;
  /**
   * URL to call when click on a menu item
   */
  private String url = null;

  /**
   * number of results contained in a node
   */
  @XmlElement
  private int nbObjects = -1;
  /**
   * indicates if a node is a leaf or a branch
   */
  @XmlElement(name = "isLeaf")
  private boolean leaf = false;
  /**
   * style to apply on label
   */
  @XmlElement
  private String labelStyle = null;

  /**
   * father node of current item
   */
  private MenuItem father = null;

  /**
   * if the element is a component indicates the component type.
   */
  @XmlElement
  private String componentName = null;

  /**
   * @return the componentName
   */
  public String getComponentName() {
    return componentName;
  }

  /**
   * @param componentName the componentName to set
   */
  public void setComponentName(String componentName) {
    this.componentName = componentName;
  }

  /**
   * default constructor
   */
  public MenuItem(String label) {
    this.label = label;
  }

  /**
   * full constructor allows building menu item with all his attributes
   * @param children the children of current menu element
   * @param label the label to display for current element label must be in the correct language
   * @param key unique key to identify the item
   * @param level item level in the tree menu
   * @param type type of node (component, space or theme)
   * @param target value of The HTML Target Attribute for Anchors (&lt;a&gt;) possible values =
   * _blank, _self, _parent, _top.
   * @param url URL to call when click on a menu item
   * @param nbObjects number of results contained in a node
   * @param leaf indicates if a node is a leaf or a branch
   * @param labelStyle style to apply on label
   * @param father father node of current item
   * @param componentId component identifier for example "kmelia1". This attribute is used only to
   * identify the theme component's
   */
  public MenuItem(ArrayList<MenuItem> children, String label, String key, int level,
      NodeType type, String target, String url, int nbObjects, boolean leaf, String labelStyle,
      MenuItem father, String componentId) {
    this.children = children;
    this.label = label;
    this.key = key;
    this.level = level;
    this.type = type;
    this.target = target;
    this.url = url;
    this.nbObjects = nbObjects;
    this.leaf = leaf;
    this.labelStyle = labelStyle;
    this.father = father;
    this.componentId = componentId;
  }

  /**
   * minimal constructor allows building menu item with all essential attributes
   * @param label the label to display for current element label must be in the correct language
   * @param key unique key to identify the item
   * @param level item level in the tree menu
   * @param type type of node (component, space or theme)
   * @param leaf indicates if a node is a leaf or a branch
   * @param father father node of current item
   * @param componentId component identifier for example "kmelia1". This attribute is used only to
   * identify the theme component's
   */
  public MenuItem(String label, String key, int level,
      NodeType type, boolean leaf,
      MenuItem father, String componentId) {
    this.label = label;
    this.key = key;
    this.level = level;
    this.type = type;
    this.leaf = leaf;
    this.father = father;
    this.componentId = componentId;
  }

  /**
   * @return the children
   */
  public List<MenuItem> getChildren() {
    return children;
  }

  /**
   * @param children the children to set
   */
  public void setChildren(ArrayList<MenuItem> children) {
    this.children = children;
  }

  /**
   * @return the label
   */
  public String getLabel() {
    return label;
  }

  /**
   * @param label the label to set
   */
  public void setLabel(String label) {
    this.label = label;
  }

  /**
   * @return the key
   */
  public String getKey() {
    return key;
  }

  /**
   * @param key the key to set
   */
  public void setKey(String key) {
    this.key = key;
  }

  /**
   * @return the level
   */
  public int getLevel() {
    return level;
  }

  /**
   * @param level the level to set
   */
  public void setLevel(int level) {
    this.level = level;
  }

  /**
   * @return the type
   */
  public NodeType getType() {
    return type;
  }

  /**
   * @param type the type to set
   */
  public void setType(NodeType type) {
    this.type = type;
  }

  /**
   * @return the target
   */
  public String getTarget() {
    return target;
  }

  /**
   * @param target the target to set
   */
  public void setTarget(String target) {
    this.target = target;
  }

  /**
   * @return the url
   */
  public String getUrl() {
    return url;
  }

  /**
   * @param url the url to set
   */
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * @return the nbObjects
   */
  public int getNbObjects() {
    return nbObjects;
  }

  /**
   * @param nbObjects the nbObjects to set
   */
  public void setNbObjects(int nbObjects) {
    this.nbObjects = nbObjects;
  }

  /**
   * @return the leaf
   */
  public boolean isLeaf() {
    return leaf;
  }

  /**
   * @param leaf the leaf to set
   */
  public void setLeaf(boolean leaf) {
    this.leaf = leaf;
  }

  /**
   * @return the labelStyle
   */
  public String getLabelStyle() {
    return labelStyle;
  }

  /**
   * @param labelStyle the labelStyle to set
   */
  public void setLabelStyle(String labelStyle) {
    this.labelStyle = labelStyle;
  }

  /**
   * @return the father
   */
  public MenuItem getFather() {
    return father;
  }

  /**
   * @param father the father to set
   */
  public void setFather(MenuItem father) {
    this.father = father;
  }

  /**
   * @return the componentId
   */
  public String getComponentId() {
    return componentId;
  }

  /**
   * @param componentId the componentId to set
   */
  public void setComponentId(String componentId) {
    this.componentId = componentId;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((componentId == null) ? 0 : componentId.hashCode());
    result = prime * result + ((componentName == null) ? 0 : componentName.hashCode());
    result = prime * result + ((father == null) ? 0 : father.hashCode());
    result = prime * result + ((key == null) ? 0 : key.hashCode());
    result = prime * result + ((label == null) ? 0 : label.hashCode());
    result = prime * result + ((labelStyle == null) ? 0 : labelStyle.hashCode());
    result = prime * result + (leaf ? 1231 : 1237);
    result = prime * result + level;
    result = prime * result + nbObjects;
    result = prime * result + ((target == null) ? 0 : target.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    result = prime * result + ((url == null) ? 0 : url.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof MenuItem)) {
      return false;
    }
    MenuItem other = (MenuItem) obj;
    if (componentId == null) {
      if (other.componentId != null) {
        return false;
      }
    } else if (!componentId.equals(other.componentId)) {
      return false;
    }
    if (componentName == null) {
      if (other.componentName != null) {
        return false;
      }
    } else if (!componentName.equals(other.componentName)) {
      return false;
    }
    if (father == null) {
      if (other.father != null) {
        return false;
      }
    } else if (!father.equals(other.father)) {
      return false;
    }
    if (key == null) {
      if (other.key != null) {
        return false;
      }
    } else if (!key.equals(other.key)) {
      return false;
    }
    if (label == null) {
      if (other.label != null) {
        return false;
      }
    } else if (!label.equals(other.label)) {
      return false;
    }
    if (labelStyle == null) {
      if (other.labelStyle != null) {
        return false;
      }
    } else if (!labelStyle.equals(other.labelStyle)) {
      return false;
    }
    if (leaf != other.leaf) {
      return false;
    }
    if (level != other.level) {
      return false;
    }
    if (nbObjects != other.nbObjects) {
      return false;
    }
    if (target == null) {
      if (other.target != null) {
        return false;
      }
    } else if (!target.equals(other.target)) {
      return false;
    }
    if (type == null) {
      if (other.type != null) {
        return false;
      }
    } else if (type != other.type) {
      return false;
    }
    if (url == null) {
      if (other.url != null) {
        return false;
      }
    } else if (!url.equals(other.url)) {
      return false;
    }
    return true;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("MenuItem [");
    if (children != null)
      builder.append("children=").append(children).append(", ");
    if (componentId != null)
      builder.append("componentId=").append(componentId).append(", ");
    if (componentName != null)
      builder.append("componentName=").append(componentName).append(", ");
    if (father != null)
      builder.append("father=").append(father).append(", ");
    if (key != null)
      builder.append("key=").append(key).append(", ");
    if (label != null)
      builder.append("label=").append(label).append(", ");
    if (labelStyle != null)
      builder.append("labelStyle=").append(labelStyle).append(", ");
    builder.append("leaf=").append(leaf).append(", level=").append(level).append(", nbObjects=")
        .append(nbObjects).append(", ");
    if (target != null)
      builder.append("target=").append(target).append(", ");
    if (type != null)
      builder.append("type=").append(type).append(", ");
    if (url != null)
      builder.append("url=").append(url);
    builder.append("]");
    return builder.toString();
  }

}
