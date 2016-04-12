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
package org.silverpeas.core.contribution.content.form;

import org.silverpeas.core.contribution.content.form.record.GenericFieldTemplate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silverpeas.core.contribution.content.form.record.Parameter;

/**
 * A field template implementation for testing purpose.
 */
public class MyFieldTemplate extends GenericFieldTemplate {

  private static final long serialVersionUID = 1267481858616773806L;

  private final String name;
  private final String type;
  private final String label;

  public MyFieldTemplate(final String name, final String type, final String label) {
    this.name = name;
    this.type = type;
    this.label = label;
  }

  @Override
  public String getFieldName() {
    return name;
  }

  @Override
  public String getTypeName() {
    return type;
  }

  @Override
  public String getDisplayerName() {
    return name;
  }

  @Override
  public String getLabel() {
    return label;
  }

  @Override
  public String getLabel(final String lang) {
    return label;
  }

  @Override
  public String[] getLanguages() {
    return new String[]{"fr", "en"};
  }

  @Override
  public boolean isMandatory() {
    return false;
  }

  @Override
  public boolean isReadOnly() {
    return false;
  }

  @Override
  public boolean isDisabled() {
    return false;
  }

  @Override
  public boolean isHidden() {
    return false;
  }

  @Override
  public Map<String, String> getParameters(final String language) {
    return new HashMap<String, String>();
  }

  @Override
  public List<Parameter> getParametersObj() {
    return new ArrayList<Parameter>();
  }

  @Override
  public Field getEmptyField() throws FormException {
    return null;
  }

  @Override
  public boolean isSearchable() {
    return false;
  }

  @Override
  public String getTemplateName() {
    return MyFieldTemplate.class.getSimpleName();
  }

  @Override
  public boolean isUsedAsFacet() {
    return false;
  }

  @Override
  public int getMaximumNumberOfOccurrences() {
    return 1;
  }

  @Override
  public boolean isRepeatable() {
    return false;
  }

  @Override
  public Field getEmptyField(int occurrence) throws FormException {
    return null;
  }

}
