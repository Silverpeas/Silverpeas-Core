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
package org.silverpeas.core.web.mvc.webcomponent.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to identify a class as a web component controller. A web component controller is the
 * main entry of the Silverpeas component's Web GUI and it takes in charge of the Web requests and
 * of the Web navigation.
 * @author Yohann Chastagnier
 */
@Inherited
@Documented
@Target(TYPE)
@Retention(RUNTIME)
public @interface WebComponentController {

  /**
   * Gets the web component controller bean identifier. This identifier will be put into each
   * requests to a resource managed by the component. The identifier is used both to check the
   * identifier of the targeted application instance and as the base folder of the web pages (JSPs).
   * For example: for an Almanach application instance, returns "almanach" and the identifier of
   * the almanach applications have to be prefixed by "almanach" and the JSP have to be
   * located into the folder <code>almanach/jsp</code>.
   * @return the web component identifier.
   */
  String value();
}
