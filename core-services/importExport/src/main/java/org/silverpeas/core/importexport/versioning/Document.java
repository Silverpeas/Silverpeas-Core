/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.importexport.versioning;

import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.WAPrimaryKey;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Document implements java.io.Serializable, Cloneable {

  private static final long serialVersionUID = 1L;

  private DocumentPK pk;
  private WAPrimaryKey foreignKey;
  @XmlElement(name = "name")
  private String name;
  @XmlElement(name = "description")
  private String description;
  private int status;
  private String instanceId;

  @XmlElementWrapper(name = "versions")
  @XmlElement(name = "version", namespace = "http://www.silverpeas.org/exchange")
  private List<DocumentVersion> versionsType;

  public DocumentPK getPk() {
    return pk;
  }

  public void setPk(DocumentPK pk) {
    this.pk = pk;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  /**
   * Overriden toString method for debug/trace purposes
   */
  public String toString() {
    return "Worker object : [ pk = " + pk + ", foreignKey = " + foreignKey
        + ", name = " + name + ", description = " + description + ", status = "
        + status + ", instanceId = " + instanceId + " ];";
  }

  /**
   * Support Cloneable Interface
   */
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      // this should never happened
      SilverLogger.getLogger(this).silent(e);
      throw new SilverpeasRuntimeException(e);
    }
  }

  public List<DocumentVersion> getVersionsType() {
    return versionsType;
  }

  public void setVersionsType(List<DocumentVersion> versions) {
    versionsType = versions;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Document other = (Document) obj;
    if (this.pk != other.pk && (this.pk == null || !this.pk.equals(other.pk))) {
      return false;
    }
    if (this.foreignKey != other.foreignKey &&
        (this.foreignKey == null || !this.foreignKey.equals(other.foreignKey))) {
      return false;
    }
    if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
      return false;
    }
    if ((this.description == null) ? (other.description != null) : !this.description
        .equals(other.description)) {
      return false;
    }
    if (this.status != other.status) {
      return false;
    }
    return this.instanceId == null ? other.instanceId == null :
        this.instanceId.equals(other.instanceId);
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 41 * hash + (this.pk != null ? this.pk.hashCode() : 0);
    hash = 41 * hash + (this.foreignKey != null ? this.foreignKey.hashCode() : 0);
    hash = 41 * hash + (this.name != null ? this.name.hashCode() : 0);
    hash = 41 * hash + (this.description != null ? this.description.hashCode() : 0);
    hash = 41 * hash + this.status;
    hash = 41 * hash + (this.instanceId != null ? this.instanceId.hashCode() : 0);
    return hash;
  }

}