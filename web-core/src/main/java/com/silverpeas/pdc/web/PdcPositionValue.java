/*
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.pdc.web;

import com.stratelia.silverpeas.pdc.model.ClassifyValue;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A PdC position value is a value of a position of a resource content on a given semantic
 * axis of the PdC.
 * 
 * A PdC position value can be a single term or a path of terms in an hierarchical tree carrying a
 * deeper exactness about the value. For example, in a geographic axis, the value France can be
 * a tree in which it is splited into regions, departments, towns, and so on. So, a more accurate
 * PdC position value can be made with the path France/Isère/Grenoble instead of only France.
 */
@XmlRootElement
public class PdcPositionValue implements Serializable {

  private static final long serialVersionUID = -6826039385078009600L;

  /**
   * Creates a new PdC position value fom the specified PdC classification value.
   * @param value a value of a PdC classification position.
   * @return a representation of the PdC position value.
   */
  public static PdcPositionValue fromClassifiyValue(final ClassifyValue value,
          String inLanguage) {
    PdcPositionValue positionValue = new PdcPositionValue(withLocalizedPathOf(value, inLanguage));
    return positionValue;
  }
  @XmlElement
  private String path;
  @XmlElement
  private List<String> synonyms = new ArrayList<String>();

  public String getPath() {
    return path;
  }

  public List<String> getSynonyms() {
    return synonyms;
  }

  public PdcPositionValue withSynonyms(final List<String> synonyms) {
    this.synonyms.clear();
    this.synonyms.addAll(synonyms);
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final PdcPositionValue other = (PdcPositionValue) obj;
    if ((this.path == null) ? (other.path != null) : !this.path.equals(other.path)) {
      return false;
    }
    if (this.synonyms != other.synonyms &&
            (this.synonyms == null || !this.synonyms.equals(other.synonyms))) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 79 * hash + (this.path != null ? this.path.hashCode() : 0);
    hash = 79 * hash + (this.synonyms != null ? this.synonyms.hashCode() : 0);
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder synonymArray = new StringBuilder("[");
    for (String synonym : synonyms) {
      synonymArray.append(synonym).append(", ");
    }
    if (synonymArray.length() > 1) {
      synonymArray.replace(synonymArray.length() - 1, synonymArray.length(), "]");
    } else {
      synonymArray.append("]");
    }
    return "PdcPositionValue{" + "path=" + path + ", synonyms=" + synonymArray.toString() + '}';
  }

  private static String withLocalizedPathOf(final ClassifyValue value, String inLanguage) {
    LocalizedClassifyValue localizedValue = LocalizedClassifyValue.decorate(value, inLanguage);
    return localizedValue.getLocalizedPath();
  }
  
  protected PdcPositionValue() {
  }

  private PdcPositionValue(String path) {
    this.path = path;
  }
}
