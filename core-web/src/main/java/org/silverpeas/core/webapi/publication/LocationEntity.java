package org.silverpeas.core.webapi.publication;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.publication.model.Location;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.model.NodePath;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.web.rs.WebEntity;
import org.silverpeas.core.webapi.util.UserEntity;

import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.annotation.XmlElement;
import java.net.URI;
import java.util.Date;

/**
 * @author Nicolas
 */
public class LocationEntity implements WebEntity {

  @XmlElement(defaultValue = "")
  private URI uri;

  @XmlElement
  private Date date;

  @XmlElement
  private boolean alias = false;

  @XmlElement
  private UserEntity user;

  @XmlElement
  private String path;

  @XmlElement
  private String id;

  @XmlElement
  private String componentId;

  public LocationEntity() {

  }

  public static LocationEntity fromLocation(Location location, URI uri, String lang) {
    return new LocationEntity(location, uri, lang);
  }

  private LocationEntity(final Location location, URI uri, String lang) {
    if (location.isAlias()) {
      this.alias = true;
      this.date = location.getAlias().getDate();
      this.user = new UserEntity(User.getById(location.getAlias().getUserId()));
    }
    this.id = location.getId();
    this.componentId = location.getInstanceId();
    this.uri = UriBuilder.fromUri(uri).path("locations").path(id+"-"+componentId).build();

    NodePath nodePath = NodeService.get().getPath(new NodePK(id, componentId));
    this.path = nodePath.format(lang, true);
  }

  @Override
  public URI getURI() {
    return uri;
  }



}
