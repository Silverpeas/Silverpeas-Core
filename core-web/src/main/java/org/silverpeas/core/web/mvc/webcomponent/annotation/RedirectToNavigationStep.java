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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.mvc.webcomponent.annotation;

import org.silverpeas.core.web.mvc.webcomponent.NavigationContext;
import org.silverpeas.core.web.mvc.webcomponent.WebComponentController;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to specify a redirection to a specified navigation step on a HTTP method of a {@link
 * WebComponentController}.
 * <p>
 * When a HTTP method with this annotation is called, the redirection is performed from
 * informations known by the navigation step returned by {@link
 * NavigationContext#findNavigationStepFrom(String)} ()} method with {@code #value()} as given
 * parameter.
 */
@Inherited
@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface RedirectToNavigationStep {

  /**
   * The identifier of the navigation step to navigate to.
   */
  String value();
}