/*
 * Copyright (C) 2000 - 2014 Silverpeas
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
package org.silverpeas.core.web.mvc.webcomponent.annotation;

import org.silverpeas.core.web.mvc.webcomponent.NavigationContext;
import org.silverpeas.core.web.mvc.webcomponent.NavigationContextListener;
import org.silverpeas.core.web.mvc.webcomponent.WebComponentController;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to specify a navigation step creation/update on a HTTP method of a {@link
 * WebComponentController}.
 * <p/>
 * When a HTTP method with this annotation is called, one of the following internal treatment is
 * performed:
 * <ul>
 *   <li>if no navigation step is referenced by {@link #identifier()},
 *   then a navigation step is created and referenced with the specified annotation identifier</li>
 *   <li>if a navigation step with specified {@link #identifier()} already exists,
 *   then the navigation step stack is reset to this</li>
 * </ul>
 * Then, in any cases, the following internal treatments are performed:
 * <ul>
 *   <li>{@link NavigationContext.NavigationStep#withFullUri(String)}: the current requested path
 *   URI (with URL parameters) is set</li>
 *   <li>{@link NavigationContextListener} necessary methods are triggered</li>
 * </ul>
 * @author Yohann Chastagnier
 */
@Inherited
@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface NavigationStep {

  /**
   * The identifier of a navigation step (several controller paths can perform different treatments
   * on same navigation step).
   * @return the above described identfifier.
   */
  String identifier();

  /**
   * The context identifier of a navigation step (several contexts can be handled into a same
   * navigation step).
   * @return the above described identfifier.
   */
  String contextIdentifier() default "unknown";
}
