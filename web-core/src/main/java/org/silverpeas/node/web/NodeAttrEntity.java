package org.silverpeas.node.web;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.stratelia.webactiv.util.node.model.NodeDetail;

@XmlRootElement
public class NodeAttrEntity {
  
  @XmlElement(defaultValue = "")
  private URI uri;
  @XmlElement(defaultValue = "")
  private String id;
  @XmlElement(required = true)
  private String componentId;
  
  /**
   * Creates a new node entity from the specified node.
   * @param node the node to entitify.
   * @return the entity representing the specified node.
   */
  public static NodeAttrEntity fromNodeDetail(final NodeDetail node, URI uri) {
    return new NodeAttrEntity(node, uri);
  }
  
  public static NodeAttrEntity fromNodeDetail(final NodeDetail node, String uri) {
    return fromNodeDetail(node, getURI(uri));
  }
  
  private NodeAttrEntity(final NodeDetail node, URI uri) {
    this.setComponentId(node.getNodePK().getInstanceId());
    this.setId(node.getNodePK().getId());
    this.setUri(uri);
  }
  
  private static URI getURI(String uri) {
    try {
      return new URI(uri);
    } catch (URISyntaxException ex) {
      Logger.getLogger(NodeAttrEntity.class.getName()).log(Level.SEVERE, null, ex);
      throw new RuntimeException(ex.getMessage(), ex);
    }
  }

  public void setUri(URI uri) {
    this.uri = uri;
  }

  public URI getUri() {
    return uri;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public void setComponentId(String componentId) {
    this.componentId = componentId;
  }

  public String getComponentId() {
    return componentId;
  }
}