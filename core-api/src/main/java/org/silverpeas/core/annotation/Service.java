/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
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
package org.silverpeas.core.annotation;

import java.lang.annotation.*;

/**
 * This annotation is to tag an object as being a business service. A business service is an object
 * whose goal is to provide transverse functionnalities wthin which can be implied one or more types
 * of business objects.
 *
 * Beans annoted with this annotation are marked to be managed by the underlying IoC container.
 *
 * The annotation is an abstraction above the IoC container used by Silverpeas so that it is can
 * possible to change the IoC container (Spring or CDI for example) by changing the wrapped
 * annnotation to those specific at this IoC implementation without impacting the annotated IoC
 * managed beans..
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Service {

  String value() default "";
}
