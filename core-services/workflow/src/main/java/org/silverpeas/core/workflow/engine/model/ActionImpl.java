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

import java.util.ArrayList;
import java.util.List;

import org.silverpeas.core.workflow.api.model.Action;
import org.silverpeas.core.workflow.api.model.Consequences;
import org.silverpeas.core.workflow.api.model.ContextualDesignation;
import org.silverpeas.core.workflow.api.model.ContextualDesignations;
import org.silverpeas.core.workflow.api.model.Form;
import org.silverpeas.core.workflow.api.model.QualifiedUsers;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class implementing the representation of the &lt;action&gt; element of a Process Model.
 **/
@XmlRootElement(name = "action")
@XmlAccessorType(XmlAccessType.NONE)
public class ActionImpl implements Action {
  private static final long serialVersionUID = -6984785710903135661L;
  @XmlID
  @XmlAttribute
  private String name;
  @XmlAttribute
  private String kind;
  @XmlElement(name = "label", type = SpecificLabel.class)
  private List<ContextualDesignation> labels;
  @XmlElement(name = "description", type = SpecificLabel.class)
  private List<ContextualDesignation> descriptions;
  @XmlElement(type = QualifiedUsersImpl.class)
  private QualifiedUsers allowedUsers;
  @XmlIDREF
  @XmlAttribute
  private FormImpl form;
  @XmlElement(type = ConsequencesImpl.class)
  private Consequences consequences;

  /**
   * Constructor
   */
  public ActionImpl() {
    reset();
  }

  /**
   * reset attributes
   */
  private void reset() {
    labels = new ArrayList<>();
    descriptions = new ArrayList<>();
    kind = "update";
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
  public QualifiedUsers getAllowedUsers() {
    return allowedUsers;
  }

  @Override
  public Consequences getConsequences() {
    return consequences;
  }

  @Override
  public Form getForm() {
    return form;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getKind() {
    return kind;
  }

  @Override
  public ContextualDesignations getDescriptions() {
    return new SpecificLabelListHelper(descriptions);
  }

  @Override
  public String getDescription(String role, String language) {
    return getDescriptions().getLabel(role, language);
  }

  @Override
  public void setAllowedUsers(QualifiedUsers allowedUsers) {
    this.allowedUsers = allowedUsers;
  }

  @Override
  public Consequences createConsequences() {
    return new ConsequencesImpl();
  }

  @Override
  public void setConsequences(Consequences consequences) {
    this.consequences = consequences;
  }

  @Override
  public void setForm(Form form) {
    this.form = (FormImpl) form;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public void setKind(String kind) {
    this.kind = kind;
  }

  /**
   * Get the unique key, used by equals method
   * @return unique key
   */
  public String getKey() {
    return getName();
  }

}
