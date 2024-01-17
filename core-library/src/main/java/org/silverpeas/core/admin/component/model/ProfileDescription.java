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
package org.silverpeas.core.admin.component.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.HashMap;
import java.util.Map;

import static org.silverpeas.core.i18n.I18NHelper.checkLanguage;

/**
 * Description of a user profile. It defines the common properties of such profile. A profile
 * defines the access rights of a user in an application instance according to the role he plays.
 * The user profile is then a mix between a role and some privileges. The profile name is the role
 * name.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProfileDescriptionType", propOrder = { "label", "help", "spaceProfileMapping" })
@XmlSeeAlso( { Profile.class })
public class ProfileDescription {

  @XmlElement(required = true)
  @XmlJavaTypeAdapter(MultilangHashMapAdapter.class)
  protected Map<String, String> label;
  @XmlElement(required = true)
  @XmlJavaTypeAdapter(MultilangHashMapAdapter.class)
  protected Map<String, String> help;
  @XmlElement(name = "spaceMapping", nillable = true)
  protected ComponentSpaceProfileMapping spaceProfileMapping;

  /**
   * Gets the value of the label property.
   * @return possible object is {@link Multilang }
   */
  protected Map<String, String> getLabel() {
    if (label == null) {
      label = new HashMap<>();
    }
    return label;
  }

  /**
   * Puts a localized label directly linked to the {@link ProfileDescription} instance.
   * @param language the language the label is localized into.
   * @param label a localized label.
   */
  public void putLabel(final String language, final String label) {
    getLabel().put(checkLanguage(language), label);
  }

  /**
   * Gets the value of the help property.
   * @return possible object is {@link Multilang }
   */
  protected Map<String, String> getHelp() {
    if (help == null) {
      help = new HashMap<>();
    }
    return help;
  }

  /**
   * Puts a localized help directly linked to the {@link ProfileDescription} instance.
   * @param language the language the help is localized into.
   * @param help a localized help.
   */
  public void putHelp(final String language, final String help) {
    getHelp().put(checkLanguage(language), help);
  }

  /**
   * Gets the value of the spaceMapping property.
   * @return possible object is {@link ComponentSpaceProfileMapping} or null if no mapping is
   * defined.
   */
  public ComponentSpaceProfileMapping getSpaceProfileMapping() {
    return spaceProfileMapping;
  }

  /**
   * Gets the value of the spaceMapping property.
   * @param value allowed object is {@link ComponentSpaceProfileMapping }
   */
  public void setSpaceProfileMapping(final ComponentSpaceProfileMapping value) {
    this.spaceProfileMapping = value;
  }
}
