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

import com.silverpeas.pdc.PdcServiceFactory;
import com.stratelia.silverpeas.pdc.control.PdcBm;
import com.stratelia.silverpeas.pdc.control.PdcBmImpl;
import java.util.Collection;
import com.stratelia.silverpeas.pdc.model.ClassifyValue;
import com.stratelia.silverpeas.pdc.model.Value;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import static com.silverpeas.util.StringUtil.*;

/**
 * A PdC position value is a value of a position of a resource content on a given semantic
 * axis of the PdC.
 * 
 * A PdC position value can be a single term or a path of terms in an hierarchical tree carrying a
 * deeper exactness about the value. For example, in a geographic axis, the value France can be
 * a tree in which it is splited into regions, departments, towns, and so on. So, a more accurate
 * PdC position value can be made with the path France/Isère/Grenoble instead of only France.
 * 
 * In an international context, the terms require to be translated according to the language of the
 * user that requested the position's value. So, the values of positions are actually represented
 * by an identifier that is used as a key to get the correct translated term. Thus, in an
 * hierarchical semantic tree, the path of a term is in fact represented by a path of identifiers
 * from which a path of translated terms can be easily retrieved.
 */
@XmlRootElement
public class PdcPositionValue implements Serializable {

  private static final long serialVersionUID = -6826039385078009600L;
  
  private static PdcBm pdcService = new PdcBmImpl();
  
  @XmlElement(required = true)
  private String id;
  @XmlElement(required = true)
  private int axisId;
  @XmlElement(defaultValue="")
  private String treeId = "";
  @XmlElement(required = true)
  private String path;
  @XmlElement
  private List<String> synonyms = new ArrayList<String>();
  
  /**
   * Creates a new PdC position value fom the specified PdC classification value.
   * @param value a value of a PdC classification position.
   * @param inLanguage  the language in which the terms of the value should be translated.
   * @return a representation of the PdC position value.
   */
  public static PdcPositionValue fromClassifiyValue(final ClassifyValue value,
          String inLanguage) {
    List<Value> termPath = value.getFullPath();
    Value term = value.getFullPath().get(termPath.size() - 1);
    PdcPositionValue positionValue = new PdcPositionValue(
            withId(value.getValue()),
            inAxis(value.getAxisId()),
            withLocalizedPathOf(value, inLanguage));
    String treeId = term.getTreeId();
    if (isDefined(treeId) && Integer.valueOf(treeId) >= 0) {
      positionValue.setTreeId(treeId);
    }
    return positionValue;
  }
  
  /**
   * Gets the business classification position's value that is represented by this PdC position
   * value.
   * @return a ClassifyValue instance.
   */
  public ClassifyValue toClassifyValue() {
    ClassifyValue value = new ClassifyValue(axisId, id);
    
    return value;
  }

  /**
   * Gets the unique identifier of this position.
   * @return the value identifier.
   */
  public String getId() {
    return id;
  }
  
  /**
   * Gets the unique identifier of the term of this value. If the term belongs to a semantic tree
   * (not a single term), it is the one of the last term of the path in the tree.
   * @return the term identifier.
   */
  @XmlTransient
  public String getTermId() {
    String[] idNodes = id.split("/");
    return idNodes[idNodes.length - 1];
  }

  /**
   * Gets the unique identifier of the PdC's axis related by this value.
   * @return the PdC's axis identifier.
   */
  public int getAxisId() {
    return axisId;
  }

  /**
   * Gets the unique identifier of the tree to which this value belongs.
   * @return the tree identifier or an empty identifier if the value is a single term (and then
   * doesn't belong to an hierachical tree of terms)
   */
  public String getTreeId() {
    return treeId;
  }
  
  /**
   * Is this value is a node in an hierachical semantic tree?
   * The value belong to a tree when it defines a more exactness meaning for a given position value.
   * @return true if the value is a node in an hierarchical semantic tree, false otherwise.
   */
  public boolean belongToATree() {
    return isDefined(this.treeId) && !this.treeId.isEmpty();
  }

  /**
   * Gets the complete path of this value in the hierarchical tree of terms. Each term in the path
   * is separated by a '/' character.
   * If the value doesn't belong to a path, then the single term is returned.
   * @return the path of this value in an hierarchical tree of terms or the single term.
   */
  public String getPath() {
    return path;
  }

  /**
   * Gets the synonyms of this value according to a given thesaurus.
   * @return an unmodifiable list of synonyms to this value.
   */
  public List<String> getSynonyms() {
    return Collections.unmodifiableList(synonyms);
  }

  public void setSynonyms(final Collection<String> synonyms) {
    this.synonyms.clear();
    this.synonyms.addAll(synonyms);
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
    if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
      return false;
    }
    if (this.axisId != other.axisId) {
      return false;
    }
    if ((this.treeId == null) ? (other.treeId != null) : !this.treeId.equals(other.treeId)) {
      return false;
    }
    if ((this.path == null) ? (other.path != null) : !this.path.equals(other.path)) {
      return false;
    }
    if (this.synonyms != other.synonyms && (this.synonyms == null || !this.synonyms.equals(
            other.synonyms))) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 59 * hash + (this.id != null ? this.id.hashCode() : 0);
    hash = 59 * hash + this.axisId;
    hash = 59 * hash + (this.treeId != null ? this.treeId.hashCode() : 0);
    hash = 59 * hash + (this.path != null ? this.path.hashCode() : 0);
    hash = 59 * hash + (this.synonyms != null ? this.synonyms.hashCode() : 0);
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
    return "PdcPositionValue{id=" + getId() + ", axisId=" + getAxisId() + ", treeId=" + getTreeId() + ", path=" + getPath() +
            ", synonyms=" + synonymArray.toString() + '}';
  }
  
  private static String withId(String valueId) {
    return valueId;
  }
  
  private static int inAxis(int axisId) {
    return axisId;
  }

  private static String withLocalizedPathOf(final ClassifyValue value, String inLanguage) {
    LocalizedClassifyValue localizedValue = LocalizedClassifyValue.decorate(value, inLanguage);
    return localizedValue.getLocalizedPath();
  }

  protected PdcPositionValue() {
  }
  
  protected void setTreeId(String treeId) {
    this.treeId = treeId;
  }

  private PdcPositionValue(String id, int axisId, String path) {
    this.id = id;
    this.axisId = axisId;
    this.path = path;
  }
  
  private PdcBm getPdcManager() {
    PdcServiceFactory factory = PdcServiceFactory.getFactory();
    return factory.getPdcManager();
  }
}
