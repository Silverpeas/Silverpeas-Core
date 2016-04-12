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
package org.silverpeas.core.webapi.pdc;

import org.silverpeas.core.pdc.thesaurus.model.ThesaurusException;
import org.silverpeas.core.webapi.base.WebEntity;
import org.silverpeas.core.pdc.pdc.model.Axis;
import org.silverpeas.core.pdc.pdc.model.UsedAxis;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A web entity representing the classification plan (named PdC). The classification plan can be
 * either the one modeled for the Silverpeas system or the one parametrized for a given Silverpeas
 * component instance. It defines the axis that can be used in the classification of a resource
 * managed by a component instance.
 */
@XmlRootElement
public class PdcEntity implements WebEntity {

  private static final long serialVersionUID = 6798294501268706300L;
  /**
   * The value representing an absence of thesaurus.
   */
  public static final UserThesaurusHolder NoThesaurus = null;
  @XmlElement(defaultValue = "")
  private URI uri;
  @XmlElement
  private List<PdcAxis> axis = new ArrayList<PdcAxis>();

  /**
   * Creates a new web entity of a PdC containing the specified axis whose terms are expressed in
   * the specified language and with the specified user thesaurus. The web representation of the PdC
   * is identified at the specified URI.
   *
   * @param usedAxis the axis to use in the PdC.
   * @param inLanguage the language in which the PdC's terms should be translated.
   * @param atURI the URI at which the PdC can be found.
   * @param withThesaurus the thesaurus to use with this PdC. The thesaurus will be use to set the
   * synonyms of each value of the different PdC's axis. NoThesaurus if the user has no thesaurus
   * configured.
   * @return the web representation of a PdC.
   * @throws ThesaurusException if an error occurs while using the thesaurus to find the synonyms of
   * values of the different PdC's axis.
   */
  public static PdcEntity aPdcEntityWithUsedAxis(final List<UsedAxis> usedAxis, String inLanguage,
      final URI atURI, final UserThesaurusHolder withThesaurus) throws ThesaurusException {
    return new PdcEntity(atURI).withAsAxis(fromUsedAxis(usedAxis, inLanguage, withThesaurus));
  }

  public static PdcEntity aPdcEntityWithAxis(final List<Axis> usedAxis, String inLanguage,
      final URI atURI, final UserThesaurusHolder withThesaurus) throws ThesaurusException {
    return new PdcEntity(atURI).withAsAxis(fromAxis(usedAxis, inLanguage, withThesaurus));
  }

  /**
   * A convenient method to enhance the readability of the method calls in which the axis are passed
   * as argument.
   *
   * @param axis the axis of the PdC.
   * @return the specified axis.
   */
  public static List<UsedAxis> withAxis(final List<UsedAxis> axis) {
    return axis;
  }

  /**
   * A convenient method to enhance the readability of the method calls that expect the language as
   * argument.
   *
   * @param language the language of a user.
   * @return the language.
   */
  public static String inLanguage(String language) {
    return language;
  }

  /**
   * Gets the URI at which this resource is published and can be accessed.
   *
   * @return the web resource URI.
   */
  @Override
  public URI getURI() {
    return uri;
  }

  /**
   * Gets the axis of this PdC.
   *
   * @return an unmodifiable list of PdC axis.
   */
  public List<PdcAxis> getAxis() {
    return Collections.unmodifiableList(axis);
  }

  /**
   * Adds the specified PdC axis among the others axis of this PdC.
   *
   * @param axis the axis of this PdC.
   * @return itself.
   */
  public PdcEntity withAsAxis(final List<PdcAxis> axis) {
    this.axis.addAll(axis);
    return this;
  }

  /**
   * A convenient method to enhance the readability of creators.
   *
   * @param uri the URI at which the classification is published.
   * @return the classification URI.
   */
  public static URI atURI(final URI uri) {
    return uri;
  }

  protected PdcEntity() {
  }

  private static List<PdcAxis> fromUsedAxis(final List<UsedAxis> theAxisToUse, String inLanguage,
      final UserThesaurusHolder usingThesaurus) throws ThesaurusException {
    List<PdcAxis> axis = new ArrayList<PdcAxis>();
    for (UsedAxis usedAxis : theAxisToUse) {
      axis.add(PdcAxis.fromTheUsedAxis(usedAxis, inLanguage, usingThesaurus));
    }
    return axis;
  }

  private static List<PdcAxis> fromAxis(final List<Axis> theAxis, String inLanguage,
      final UserThesaurusHolder usingThesaurus) throws ThesaurusException {
    List<PdcAxis> axis = new ArrayList<PdcAxis>();
    for (Axis anAxis : theAxis) {
      axis.add(PdcAxis.fromTheAxis(anAxis, inLanguage, usingThesaurus));
    }
    return axis;
  }

  private PdcEntity(final URI atURI) {
    this.uri = atURI;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final PdcEntity other = (PdcEntity) obj;
    if (this.uri != other.uri && (this.uri == null || !this.uri.equals(other.uri))) {
      return false;
    }
    return !(this.axis != other.axis && (this.axis == null || !this.axis.equals(other.axis)));
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 47 * hash + (this.uri != null ? this.uri.hashCode() : 0);
    hash = 47 * hash + (this.axis != null ? this.axis.hashCode() : 0);
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder axisArray = new StringBuilder("[");
    for (PdcAxis anAxis : getAxis()) {
      axisArray.append(anAxis.toString()).append(", ");
    }
    if (axisArray.length() > 1) {
      axisArray.replace(axisArray.length() - 2, axisArray.length(), "]");
    } else {
      axisArray.append("]");
    }
    return "PdcEntity{uri=" + getURI() + ", axis=" + axisArray.toString() + '}';
  }
}
