/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.workflow.engine.model;

import org.silverpeas.core.workflow.api.model.ContextualDesignation;
import org.silverpeas.core.workflow.api.model.ContextualDesignations;
import org.silverpeas.core.workflow.api.model.Participant;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Class implementing the representation of the &lt;participant&gt; element of a Process Model.
 **/
@XmlRootElement(name = "participant")
@XmlAccessorType(XmlAccessType.NONE)
public class ParticipantImpl implements Participant {
  private static final long serialVersionUID = -6272061474366848845L;
  @XmlID
  @XmlAttribute
  private String name;
  @XmlAttribute
  private String resolvedState;
  @XmlElement(name = "label", type = SpecificLabel.class)
  private List<ContextualDesignation> labels;
  @XmlElement(name = "description", type = SpecificLabel.class)
  private List<ContextualDesignation> descriptions;

  /**
   * Constructor
   */
  public ParticipantImpl() {
    reset();
  }

  /**
   * reset attributes
   */
  private void reset() {
    labels = new ArrayList<>();
    descriptions = new ArrayList<>();
  }

  /**
   * Get description in specific language for the given role
   * @param language description's language
   * @param role role for which the description is
   * @return wanted description as a String object. If description is not found, search description
   * with given role and default language, if not found again, return the default description in
   * given language, if not found again, return the default description in default language, if not
   * found again, return empty string.
   */
  @Override
  public String getDescription(String role, String language) {
    return getDescriptions().getLabel(role, language);
  }

  @Override
  public ContextualDesignations getDescriptions() {
    return new SpecificLabelListHelper(descriptions);
  }

  /**
   * Get label in specific language for the given role
   * @param language label's language
   * @param role role for which the label is
   * @return wanted label as a String object. If label is not found, search label with given role
   * and default language, if not found again, return the default label in given language, if not
   * found again, return the default label in default language, if not found again, return empty
   * string.
   */
  @Override
  public String getLabel(String role, String language) {
    return getLabels().getLabel(role, language);
  }

  @Override
  public ContextualDesignations getLabels() {
    return new SpecificLabelListHelper(labels);
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public String getResolvedState() {
    return this.resolvedState;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public void setResolvedState(String resolvedState) {
    this.resolvedState = resolvedState;
  }

}