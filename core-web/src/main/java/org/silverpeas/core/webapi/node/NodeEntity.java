/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.core.webapi.node;

import org.silverpeas.core.webapi.base.WebEntity;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodeI18NDetail;
import org.silverpeas.core.node.model.NodePK;
import org.owasp.encoder.Encode;
import org.silverpeas.core.i18n.Translation;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@XmlRootElement
public class NodeEntity implements WebEntity {

  private static final long serialVersionUID = -5740937039604775733L;
  @XmlElement(defaultValue = "")
  private URI uri;
  @XmlElement(required = true)
  private String data;
  @XmlElement(required = true)
  private NodeAttrEntity attr;
  @XmlElement
  private NodeEntity[] children;
  @XmlElement(defaultValue = "")
  private URI childrenURI;
  @XmlElement
  private NodeTranslationEntity[] translations;
  @XmlElement(required = true)
  private String state = "closed";

  public NodeEntity() {
  }

  /**
   * Creates a new node entity from the specified node.
   *
   * @param node the node to entitify.
   * @return the entity representing the specified node.
   */
  public static NodeEntity fromNodeDetail(final NodeDetail node, URI uri) {
    return new NodeEntity(node, uri, null);
  }

  public static NodeEntity fromNodeDetail(final NodeDetail node, URI uri, String lang) {
    return new NodeEntity(node, uri, lang);
  }

  public static NodeEntity fromNodeDetail(final NodeDetail node, String uri) {
    return fromNodeDetail(node, getURI(uri), null);
  }

  public static NodeEntity fromNodeDetail(final NodeDetail node, String uri, String lang) {
    return fromNodeDetail(node, getURI(uri), lang);
  }

  @Override
  public URI getURI() {
    return uri;
  }

  public void setURI(URI uri) {
    this.uri = uri;
  }

  private NodeEntity(final NodeDetail node, URI uri, String lang) {
    this.data = Encode.forHtml(node.getName(lang));
    this.uri = uri;
    this.attr = NodeAttrEntity.fromNodeDetail(node, uri, lang);

    // set translations
    Map<String, NodeI18NDetail> theTranslations = node.getTranslations();
    List<NodeTranslationEntity> translationEntities = new ArrayList<NodeTranslationEntity>();
    for (Translation translation : theTranslations.values()) {
      NodeTranslationEntity translationEntity =
          new NodeTranslationEntity(translation.getId(), translation.getLanguage(), node);
      translationEntities.add(translationEntity);
    }
    this.translations = translationEntities.toArray(new NodeTranslationEntity[0]);

    // set children data
    setChildrenURI(getChildrenURI(uri));
    if (node.getChildrenDetails() != null) {
      List<NodeEntity> entities = new ArrayList<NodeEntity>();
      for (NodeDetail child : node.getChildrenDetails()) {
        URI childURI = getChildURI(uri, child.getNodePK().getId());
        NodeEntity childEntity = fromNodeDetail(child, childURI, lang);
        childEntity.setChildrenURI(getChildrenURI(childURI));
        entities.add(childEntity);
      }
      children = entities.toArray(new NodeEntity[0]);
    }
  }

  private URI getChildURI(URI parentURI, String childId) {
    try {
      return new URI(parentURI + "/" + childId);
    } catch (URISyntaxException ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
      throw new RuntimeException(ex.getMessage(), ex);
    }
  }

  private static URI getURI(String uri) {
    try {
      return new URI(uri);
    } catch (URISyntaxException ex) {
      SilverLogger.getLogger(NodeEntity.class).error(ex.getMessage(), ex);
      throw new RuntimeException(ex.getMessage(), ex);
    }
  }

  public void setChildren(NodeEntity[] children) {
    this.children = children.clone();
  }

  public NodeEntity[] getChildren() {
    return children;
  }

  public final void setData(String data) {
    this.data = data;
  }

  public String getData() {
    return data;
  }

  public final void setAttr(NodeAttrEntity attr) {
    this.attr = attr;
  }

  public NodeAttrEntity getAttr() {
    return attr;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getState() {
    return state;
  }

  public final void setChildrenURI(URI childrenURI) {
    this.childrenURI = childrenURI;
    this.attr.setChildrenURI(childrenURI);
  }

  public final URI getChildrenURI(URI uri) {
    try {
      return new URI(uri + "/children");
    } catch (URISyntaxException ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
      throw new RuntimeException(ex.getMessage(), ex);
    }
  }

  public URI getChildrenURI() {
    return childrenURI;
  }

  public final void setTranslations(NodeTranslationEntity[] translations) {
    this.translations = (translations != null ? translations.clone() : null);
  }

  public NodeTranslationEntity[] getTranslations() {
    return translations;
  }

  /**
   * Gets the node pk objet that this entity represents.
   *
   * @return a node PK.
   */
  public NodePK toNodePK() {
    NodePK nodePk = new NodePK(this.attr.getId(), this.attr.getComponentId());
    return nodePk;
  }
}
