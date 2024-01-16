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

package org.silverpeas.core.test.extention;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to indicate that a field or a parameter has to be instantiated and then
 * registered into the bean container used in the test before executing that test. For doing, the
 * bean type must have a default constructor. Once instantiated, if the bean has a
 * {@link javax.annotation.PostConstruct} annotated method, then this method will be invoked.
 * This annotation is for classes for which an instance is preferred to a mock in unit tests.
 * The difference between such beans and those annotated with {@link TestedBean} is that the
 * injection points in the former aren't resolved; indeed, {@link TestManagedBean} annotated beans
 * are considered just like mock but with a true instance (a kind of stub then).
 * <p>
 * Any field annotated with this annotation can be explicitly instantiated, in that case it is that
 * instance that will be registered into the bean container and no
 * {@link javax.annotation.PostConstruct} annotated method will be invoked. If the test class
 * declares several annotated fields having a common type among their ancestor, then the fields
 * will be registered for that type and they could be get by using the
 * {@link org.silverpeas.core.test.TestBeanContainer#getAllBeansByType(Class, Annotation...)}
 * method. In that case, the call of
 * {@link org.silverpeas.core.test.TestBeanContainer#getBeanByType(Class, Annotation...)} method
 * with that type as parameter will throw an exception.
 * </p>
 * <p>
 * If the annotation is applied to a parameter, then a bean of the parameter type is first looking
 * for in the bean container used in the tests. If no such bean is found, then the type is
 * instantiated, the newly created bean is registered into the bean container and finally it is
 * passed as parameter value. By using this annotation with the parameters, you can get any
 * previously registered bean.
 * </p>
 * @author mmoquillon
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TestManagedBean {

}
