package org.silverpeas.core.webapi.preferences;

import org.silverpeas.core.webapi.base.WebEntity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

/**
 * @author mmoquillon
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class UserPreferencesEntity implements WebEntity {

  @XmlElement
  private URI uri;

  @XmlElement(nillable = true)
  private String language;

  @Override
  public URI getURI() {
    return uri;
  }

  public String getLanguage() {
    return this.language;
  }

  protected void setURI(final URI uri) {
    this.uri = uri;
  }
}
