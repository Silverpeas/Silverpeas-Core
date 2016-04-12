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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.core.webapi.admin;

import static org.silverpeas.core.webapi.admin.AdminResourceURIs.COMPONENTS_BASE_URI;
import static org.silverpeas.core.webapi.admin.AdminResourceURIs.SPACES_BASE_URI;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.admin.component.model.ComponentInstLight;

/**
 * The component instance light entity is a ComponentInstLight object that is exposed in the web as
 * an entity (web entity). As such, it publishes only some of its attributes. It represents a
 * ComponentInstLight in Silverpeas plus some additional information such as the URI for accessing
 * it.
 * @author Yohann Chastagnier
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ComponentEntity extends StructureElementEntity<ComponentEntity> {

  public static final String TYPE = "component";

  private static final long serialVersionUID = 6763670746538050422L;

  @XmlElement(required = true)
  @NotNull
  @Size(min = 1)
  private String name;

  @XmlElement(required = true)
  @NotNull
  private String url;

  /**
   * Creates a new ComponentInstLight entity from the specified ComponentInstLight.
   * @param component the ComponentInstLight to entitify.
   * @param language the current language.
   * @return the entity representing the specified ComponentInstLight.
   */
  public static ComponentEntity createFrom(final ComponentInstLight component, final String language) {
    return new ComponentEntity(component, language);
  }

  /**
   * Gets the name of the component.
   * @return the name of the component.
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the url of the component.
   * @return the url of the component.
   */
  public String getUrl() {
    return url;
  }

  /**
   * Instantiating a new web entity from the corresponding data
   * @param component
   */
  private ComponentEntity(final ComponentInstLight component, final String language) {
    super(TYPE, (component.getId() == null ? "" : component.getId().replaceFirst(
        component.getName(), "")), component.getDomainFatherId(), component.getLabel(language),
        component.getDescription(language), component.getStatus(), component.getOrderNum(),
        component.isInheritanceBlocked());
    name = component.getName() == null ? "" : component.getName();
    url = URLUtil.getURL(component.getName(), null, component.getId()) + "Main";
  }

  protected ComponentEntity() {
    super();
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.core.webapi.admin.StructureElementEntity#getStringBaseURI()
   */
  @Override
  protected String getStringBaseURI() {
    return getURI().toString().replaceFirst(COMPONENTS_BASE_URI + "/[0-9]+", COMPONENTS_BASE_URI);
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.core.webapi.admin.StructureElementEntity#getStringParentBaseURI()
   */
  @Override
  protected String getStringParentBaseURI() {
    return getURI().toString().replaceFirst(COMPONENTS_BASE_URI + "/[0-9]+", SPACES_BASE_URI);
  }
}
