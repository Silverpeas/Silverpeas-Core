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
 * along withWriter this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.pdc.web;

import com.silverpeas.rest.Exposable;
import com.stratelia.webactiv.util.WAPrimaryKey;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The PdC classification entity represents the web entity of the classification of a Silverpeas's
 * resource on the classification plan (PdC).
 * The PdC classificiation is then identified in the web by its unique identifier, its URI.
 * As such, it publishes only some of its attributes
 */
@XmlRootElement
public class PdcClassificationEntity implements Exposable {
  private static final long serialVersionUID = -2217575091640675000L;
  
  @XmlElement(defaultValue = "")
  private URI uri;
  
  /**
   * Creates a non-defined PdC classification.
   * Resources that are not classified on the PdC have a such undefined classification as they have
   * no classification positions on the PdC axis.
   * @return a web entity representing an undefined PdC classification.
   */
  public static PdcClassificationEntity undefinedClassification() {
    return new PdcClassificationEntity();
  }

  @Override
  public URI getURI() {
    return this.uri;
  }
  
  /**
   * Gets the URI of this classification entity.
   * @param uri the URI identifying uniquely in the Web this classification.
   * @return the classification URI.
   */
  public PdcClassificationEntity withURI(final URI uri) {
    this.uri = uri;
    return this;
  }
  
  /**
   * Gets all the positions on the PdC axis that defines this resource classification.
   * @return a list of a Web representation of each classification positions in this classification.
   */
  public List<PdcPositionEntity> getClassificationPositions() {
    return new ArrayList<PdcPositionEntity>();
  }
}
