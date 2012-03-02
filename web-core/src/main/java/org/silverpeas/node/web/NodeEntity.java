package org.silverpeas.node.web;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import com.silverpeas.rest.Exposable;
import com.stratelia.webactiv.util.node.model.NodeDetail;

@XmlRootElement
public class NodeEntity implements Exposable {

  private static final long serialVersionUID = -5740937039604775733L;
  
  @XmlElement(defaultValue = "")
  private URI uri;
  @XmlElement(required = true)
  private String data;
  @XmlElement(required = true)
  private NodeAttrEntity attr;
  @XmlElements({@XmlElement})
  private NodeEntity[] children;
  @XmlElement(defaultValue = "")
  private URI childrenURI;
  @XmlElement(required = true)
  private String state = "closed";
  
  /**
   * Creates a new node entity from the specified node.
   * @param node the node to entitify.
   * @return the entity representing the specified node.
   */
  public static NodeEntity fromNodeDetail(final NodeDetail node, URI uri) {
    return new NodeEntity(node, uri);
  }
  
  public static NodeEntity fromNodeDetail(final NodeDetail node, String uri) {
    return fromNodeDetail(node, getURI(uri));
  }

  @Override
  public URI getURI() {
    return uri;
  }
  
  public void setURI(URI uri) {
    this.uri = uri;
  }

  private NodeEntity(final NodeDetail node, URI uri) {
    this.setData(node.getName());
    this.uri = uri;
    this.childrenURI = getChildrenURI(uri);
    this.setAttr(NodeAttrEntity.fromNodeDetail(node, uri));
    if (node.getChildrenDetails() != null) {
      List<NodeEntity> entities = new ArrayList<NodeEntity>();
      for (NodeDetail child : node.getChildrenDetails()) {
        URI childURI = getChildURI(uri, child.getNodePK().getId());
        NodeEntity childEntity = fromNodeDetail(child, childURI);
        childEntity.childrenURI = getChildrenURI(childURI);
        entities.add(childEntity);
      }
      children = entities.toArray(new NodeEntity[0]);
    }
  }
  
  private URI getChildURI(URI parentURI, String childId) {
    try {
      return new URI(parentURI + "/" + childId);
    } catch (URISyntaxException ex) {
      Logger.getLogger(NodeEntity.class.getName()).log(Level.SEVERE, null, ex);
      throw new RuntimeException(ex.getMessage(), ex);
    }
  }
  
  private static URI getURI(String uri) {
    try {
      return new URI(uri);
    } catch (URISyntaxException ex) {
      Logger.getLogger(NodeEntity.class.getName()).log(Level.SEVERE, null, ex);
      throw new RuntimeException(ex.getMessage(), ex);
    }
  }

  public void setChildren(NodeEntity[] children) {
    this.children = children;
  }

  public NodeEntity[] getChildren() {
    return children;
  }

  public void setData(String data) {
    this.data = data;
  }

  public String getData() {
    return data;
  }

  public void setAttr(NodeAttrEntity attr) {
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

  public URI getChildrenURI(URI uri) {
    try {
      return new URI(uri + "/children");
    } catch (URISyntaxException ex) {
      Logger.getLogger(NodeEntity.class.getName()).log(Level.SEVERE, null, ex);
      throw new RuntimeException(ex.getMessage(), ex);
    }
  }
  
  public URI getChildrenURI() {
    return childrenURI;
  }

}
