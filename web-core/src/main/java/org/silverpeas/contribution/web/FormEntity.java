/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.contribution.web;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Yohann Chastagnier
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class FormEntity extends AbstractContentEntity {
  private static final long serialVersionUID = -8723262348052334532L;

  /* The name fo the form */
  @XmlElement(defaultValue = "")
  private String formId = "";

  /* List of form fields indexed by their names */
  @XmlElement(defaultValue = "")
  private Map<String, FormFieldEntity> fields = new LinkedHashMap<String, FormFieldEntity>();

  /**
   * Creates a new form entity from the specified form.
   *
   * @param formId
   * @return the entity representing the specified form.
   */
  public static FormEntity createFrom(final String formId) {
    return new FormEntity(formId);
  }

  /**
   * Default hidden constructor.
   */
  private FormEntity(final String formId) {
    super("form");
    this.formId = formId;
  }

  protected FormEntity() {
  }

  protected String getFormId() {
    return formId;
  }

  protected Map<String, FormFieldEntity> getFields() {
    return fields;
  }

  public FormEntity addFormField(FormFieldEntity formField) {
    fields.put(formField.getName(), formField);
    return this;
  }
}
