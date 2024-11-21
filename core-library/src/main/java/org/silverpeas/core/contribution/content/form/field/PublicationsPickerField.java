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
package org.silverpeas.core.contribution.content.form.field;

import org.apache.ecs.html.A;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.content.form.AbstractField;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldDisplayer;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.kernel.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * A PublicationPickerField stores a list of publication reference
 *
 * @see Field
 * @see FieldDisplayer
 */
public class PublicationsPickerField extends AbstractField {

  private static final long serialVersionUID = -4982574221213514901L;

  /**
   * The text field type name.
   */
  public static final String TYPE = "publicationsPicker";
  private int nbPublications = 0;

  /**
   * The referenced resources.
   */
  private String rawResourceReferences = null;

  /**
   * Returns the type name.
   */
  public String getTypeName() {
    return TYPE;
  }

  /**
   * Returns the publications referenced by this field (as defined by ContributionIdentifier.ABSOLUTE_ID_FORMAT)
   */
  public String getRawResourceReferences() {
    return rawResourceReferences;
  }

  /**
   * Set the node id referenced by this field.
   */
  public void setRawResourceReferences(String rawResourceReferences) {
    StringBuilder verifiedResources = new StringBuilder();

    // check references to detect deleted publications
    if (StringUtil.isDefined(rawResourceReferences)) {
      String[] array = rawResourceReferences.split(",");
      for (String rawResourceReference : array) {
        ContributionIdentifier id = ContributionIdentifier.decode(rawResourceReference);
        PublicationDetail publi = PublicationService.get()
            .getDetail(new PublicationPK(id.getLocalId(), id.getComponentInstanceId()));
        if (publi != null) {
          if (verifiedResources.length() > 0) {
            verifiedResources.append(",");
          }
          verifiedResources.append(rawResourceReference);
        }
      }
    }

    this.rawResourceReferences = verifiedResources.toString();
  }

  /**
   * Returns true if the value is read only.
   */
  public boolean isReadOnly() {
    return false;
  }

  /**
   * Returns the string value of this field : aka the node path.
   */
  public String getValue() {
    return getValue(I18NHelper.DEFAULT_LANGUAGE);
  }

  /**
   * Returns the local value of this field. There is no local format for a user field, so the
   * language parameter is unused.
   */
  public String getValue(String language) {
    return getListOfPublications(true, language);
  }

  public String getValueAsText(String language) {
    return getListOfPublications(false, language);
  }

  public void setValue(String value) throws FormException {
    setRawResourceReferences(value);
  }

  public void setValue(String value, String language) throws FormException {
    setValue(value);
  }

  /**
   * Always returns false since a user reference can't be computed from a user name.
   */
  public boolean acceptValue(String value) {
    return false;
  }

  /**
   * Always returns false since a user reference can't be computed from a user name.
   */
  public boolean acceptValue(String value, String language) {
    return false;
  }

  /**
   * Returns the User referenced by this field.
   */
  public Object getObjectValue() {
    return getResourceReferences();
  }

  /**
   * Set node referenced by this field.
   */
  @SuppressWarnings("unchecked")
  public void setObjectValue(Object value) throws FormException {
    if (value instanceof List) {
      StringBuilder resRef = new StringBuilder();
      List<ContributionIdentifier> list = (List<ContributionIdentifier>) value;
      for (ContributionIdentifier identifier : list) {
        if (resRef.length() > 0) {
          resRef.append(",");
        }
        resRef.append(identifier.asString());
      }
      setRawResourceReferences(resRef.toString());
    } else if (value == null) {
      setRawResourceReferences(null);
    } else {
      throw new FormException("Incorrect field value type. Expected a list of contribution " +
          "identifiers");
    }
  }

  /**
   * Returns true if the value is a String and this field isn't read only.
   */
  public boolean acceptObjectValue(Object value) {
    return value instanceof UserDetail && !isReadOnly();
  }

  /**
   * Returns this field value as a normalized String
   */
  public String getStringValue() {
    return getRawResourceReferences();
  }

  /**
   * Set this field value from a normalized String
   */
  public void setStringValue(String value) {
    setRawResourceReferences(value);
  }

  /**
   * Returns true if this field isn't read only.
   */
  public boolean acceptStringValue(String value) {
    return !isReadOnly();
  }

  /**
   * Returns true if this field is not set.
   */
  public boolean isNull() {
    return CollectionUtil.isEmpty(getResourceReferences());
  }

  /**
   * Set to null this field.
   *
   * @throws FormException when the field is mandatory or when the field is read only.
   */
  public void setNull() throws FormException {
    setRawResourceReferences(null);
  }

  public int getNbPublications() {
    return nbPublications;
  }

  /**
   * Tests equality between this field and the specified field.
   */
  public boolean equals(Object o) {
    String s = getRawResourceReferences();

    if (o instanceof PublicationsPickerField) {
      String t = ((PublicationsPickerField) o).getRawResourceReferences();
      return (s == null || s.equals(t));
    } else {
      return false;
    }
  }

  /**
   * Compares this field with the specified field.
   */
  @Override
  public int compareTo(Field o) {
    String s = getValue();
    if (s == null) {
      s = "";
    }
    if (o instanceof PublicationsPickerField) {
      String t = o.getValue();
      if (t == null) {
        t = "";
      }

      if (s.equals(t)) {
        s = getRawResourceReferences();
        if (s == null) {
          s = "";
        }
        t = ((PublicationsPickerField) o).getRawResourceReferences();
        if (t == null) {
          t = "";
        }
      }
      return s.compareTo(t);
    } else {
      return -1;
    }
  }

  public int hashCode() {
    String s = getRawResourceReferences();
    return s.hashCode();
  }

  private List<ContributionIdentifier> getResourceReferences() {
    List<ContributionIdentifier> resourceReferences = new ArrayList<>();
    if (StringUtil.isDefined(getRawResourceReferences())) {
      String[] array = getRawResourceReferences().split(",");
      for (String rawResourceReference : array) {
        resourceReferences.add(ContributionIdentifier.decode(rawResourceReference));
      }
    }
    return resourceReferences;
  }

  private String getListOfPublications(boolean useLink, String language) {
    List<ContributionIdentifier> refs = getResourceReferences();
    if (CollectionUtil.isEmpty(refs)) {
      return "";
    }

    StringBuilder displayedValue = new StringBuilder();
    for (ContributionIdentifier ref : refs) {
      PublicationDetail publi = PublicationService.get()
          .getDetail(new PublicationPK(ref.getLocalId(), ref.getComponentInstanceId()));
      if (publi != null) {
        if (displayedValue.length() > 0) {
          displayedValue.append("\n");
        }
        String pubName = publi.getName(language);
        if (useLink) {
          A a = new A(publi.getPermalink(), pubName);
          a.setClass("sp-permalink");
          displayedValue.append(a);
        } else {
          displayedValue.append(pubName);
        }
        nbPublications++;
      }
    }
    return displayedValue.toString();
  }
}
