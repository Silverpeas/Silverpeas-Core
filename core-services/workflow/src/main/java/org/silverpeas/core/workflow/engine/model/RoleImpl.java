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
import org.silverpeas.core.workflow.api.model.Role;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Class implementing the representation of the &lt;role&gt; element of a Process Model.
 **/
@XmlRootElement(name = "role")
@XmlAccessorType(XmlAccessType.NONE)
public class RoleImpl implements Role {
  private static final long serialVersionUID = 1005254939500303606L;
  @XmlAttribute
  private String name;
  @XmlElement(name = "label", type = SpecificLabel.class)
  private List<ContextualDesignation> labels;
  @XmlElement(name = "description", type = SpecificLabel.class)
  private List<ContextualDesignation> descriptions;

  /**
   * Constructor
   */
  public RoleImpl() {
    reset();
  }


  /**
   * reset attributes
   */
  private void reset() {
    labels = new ArrayList<>();
    descriptions = new ArrayList<>();
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public ContextualDesignations getLabels() {
    return new SpecificLabelListHelper(labels);
  }

  @Override
  public String getLabel(String role, String language) {
    return getLabels().getLabel(role, language);
  }

  @Override
  public void addLabel(ContextualDesignation label) {
    labels.add(label);
  }

  @Override
  public ContextualDesignations getDescriptions() {
    return new SpecificLabelListHelper(descriptions);
  }

  @Override
  public String getDescription(String role, String language) {
    return getDescriptions().getLabel(role, language);
  }

}