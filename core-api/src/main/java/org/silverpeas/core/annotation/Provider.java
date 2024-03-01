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
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.core.annotation;

import org.silverpeas.kernel.annotation.Managed;

import javax.enterprise.inject.Stereotype;
import javax.inject.Singleton;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is to tag a managed bean as being a provider of business objects. The way the
 * objects are provided is abstracted by such beans; they can be built by a factory or a builder or
 * by the provider itself or they can come from the IoC container.
 * <p>
 * Beans annotated with this annotation are marked to be managed by the underlying IoC container and
 * to be singleton (there is only one single instance at a given time). If the bean declare another
 * life-cycle scope, then the new scope overrides the default one. Any providers of business objects
 * that don't have to be managed by the IoC container shouldn't be annotated with this annotation.
 * </p>
 * <p>
 * The annotation is an abstraction above the IoC container used by Silverpeas so that it is can
 * possible to change the IoC container (Spring or CDI for example) by changing the wrapped
 * annotation to those specific at this IoC implementation without impacting the annotated IoC
 * managed beans.
 * </p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Managed
@Singleton
@Stereotype
public @interface Provider {
}
