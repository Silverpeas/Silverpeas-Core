/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.admin.component.model;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each Java content interface and Java element interface
 * generated in the org.silverpeas.core.admin.component.model package.
 * <p>
 * An ObjectFactory allows you to programatically construct new instances of the Java representation
 * for XML content. The Java representation of XML content can consist of schema derived interfaces
 * and classes representing the binding of schema type definitions, element declarations and model
 * groups. Factory methods for each of these are provided in this class.
 */
@XmlRegistry
public class ObjectFactory {

  private final static QName _WAComponent_QNAME =
      new QName("http://silverpeas.org/xml/ns/component",
      "WAComponent");

  /**
   * Create a new ObjectFactory that can be used to create new instances of schema derived classes
   * for package: com.silverpeas.admin.components
   */
  public ObjectFactory() {
  }

  /**
   * Create an instance of {@link Profile }
   */
  public Profile createProfile() {
    return new Profile();
  }

  /**
   * Create an instance of {@link Option }
   */
  public Option createOption() {
    return new Option();
  }

  /**
   * Create an instance of {@link WAComponent }
   */
  public WAComponent createWAComponent() {
    return new WAComponent();
  }

  /**
   * Create an instance of {@link Parameter }
   */
  public Parameter createParameter() {
    return new Parameter();
  }

  /**
   * Create an instance of {@link Message }
   */
  public Message createMessage() {
    return new Message();
  }

  /**
   * Create an instance of {@link ProfileDescription }
   */
  public ProfileDescription createProfileDescription() {
    return new ProfileDescription();
  }

  /**
   * Create an instance of {@link Multilang }
   */
  public Multilang createMultilang() {
    return new Multilang();
  }

  /**
   * Create an instance of {@link JAXBElement }{@code <}{@link WAComponent }{@code >}
   */
  @XmlElementDecl(namespace = "http://silverpeas.org/xml/ns/component", name = "WAComponent")
  public JAXBElement<WAComponent> createWAComponent(WAComponent value) {
    return new JAXBElement<WAComponent>(_WAComponent_QNAME, WAComponent.class, null, value);
  }
}
