/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.model.ContextualDesignation;
import org.silverpeas.core.workflow.api.model.ContextualDesignations;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Class managing a collection of ContextualDesigantion objects.
 */
@XmlRootElement
public class SpecificLabelListHelper implements ContextualDesignations, Serializable {
  private static final long serialVersionUID = -4580671511307866063L;
  private static final String DEFAULT = "default";

  @XmlElement(type = SpecificLabel.class)
  private List<ContextualDesignation> labels;

  /**
   * Constructor
   */
  public SpecificLabelListHelper() {
    this.labels = new ArrayList<>();
  }

  /**
   * Constructor
   */
  public SpecificLabelListHelper(List<ContextualDesignation> labels) {
    this.labels = labels;
  }

  /*
   * (non-Javadoc)
   * @see ContextualDesignations#getLabel(java. lang.String,
   * java.lang.String)
   */
  @Override
  public String getLabel(String role, String language) {
    ContextualDesignation label = getSpecificLabel(role, language);

    if (label != null) {
      return label.getContent();
    }
    label = getSpecificLabel(role, DEFAULT); //$NON-NLS-1$
    if (label != null) {
      return label.getContent();
    }
    label = getSpecificLabel(DEFAULT, language); //$NON-NLS-1$
    if (label != null) {
      return label.getContent();
    }
    //$NON-NLS-1$ //$NON-NLS-2$
    label = getSpecificLabel(DEFAULT, DEFAULT);
    if (label != null) {
      return label.getContent();
    }
    return ""; //$NON-NLS-1$
  }

  /*
   * (non-Javadoc)
   * @see ContextualDesignations#getSpecificLabel
   * (java.lang.String, java.lang.String)
   */
  @Override
  public ContextualDesignation getSpecificLabel(String role, String language) {
    SpecificLabel label = null;
    for (int l = 0; l < labels.size(); l++) {
      label = (SpecificLabel) labels.get(l);
      if (role != null && role.equals(label.getRole()) && language != null
          && language.equals(label.getLanguage())) {
        return label;
      }
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * @seecom.silverpeas.workflow.api.model.ContextualDesignations# addContextualDesignation
   * (ContextualDesignation)
   */
  @Override
  public void addContextualDesignation(
      ContextualDesignation contextualDesignation) {
    labels.add(contextualDesignation);
  }

  /*
   * (non-Javadoc)
   * @seecom.silverpeas.workflow.api.model.ContextualDesignations# createContextualDesignation()
   */
  @Override
  public ContextualDesignation createContextualDesignation() {
    return new SpecificLabel();
  }

  /*
   * (non-Javadoc)
   * @seecom.silverpeas.workflow.api.model.ContextualDesignations# iterateContextualDesignation()
   */
  @Override
  public Iterator<ContextualDesignation> iterateContextualDesignation() {
    if (labels == null) {
      return null;
    } else {
      return labels.iterator();
    }
  }

  /*
   * (non-Javadoc)
   * @seecom.silverpeas.workflow.api.model.ContextualDesignations#
   * removeContextualDesignation(java.lang.String)
   */
  @Override
  public void removeContextualDesignation(
      ContextualDesignation contextualDesignation) throws WorkflowException {
    if (labels == null) {
      return;
    }
    if (!labels.removeIf(d ->
        d.getRole().equals(contextualDesignation.getRole()) &&
        d.getLanguage().equals(contextualDesignation.getLanguage()))) {
      throw new WorkflowException("SpecificLabelListHelper.removeContextualDesignation()",
          //$NON-NLS-1$
          "workflowEngine.EX_DESIGNATION_NOT_FOUND", // $NON-NLS-1$
          contextualDesignation == null ? "<null>" : contextualDesignation.getContent());
    }
  }
}