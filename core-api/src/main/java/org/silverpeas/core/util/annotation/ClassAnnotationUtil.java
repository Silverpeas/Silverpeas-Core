/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.util.annotation;

import org.apache.commons.lang3.NotImplementedException;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author Yohann Chastagnier
 */
public class ClassAnnotationUtil {

  /**
   * This method is awesome.
   * It permits to retrieve the class of a parametrized type, even if this type is parametrized
   * into a super class or interface from a hierarchy of classes.
   * <p>
   * Example (it is stupid, but it is for understanding):
   * <pre>
   *   {@code
   *   public interface Car<? extends Model, ? extends Engine> {...}
   *
   *   public abstract SedanElectricCar extends Car<Sedan, Electric> {...}
   *
   *   public class Wikispeed implements SedanElectricCar {...}
   *   }
   * </pre>
   * When trying to get some parametrized type information from an instance of Wikispeed,
   * perform this operation:
   * <pre>
   *   {@code
   *   Class<? extends Engine> engineClassOfCar =
   *     AnnotationUtil.searchParameterizedTypeFrom(Engine.class, Wikispeed.class)
   *   }
   * </pre>
   * Some explanations:
   * <ul>
   * <li>Engine.class is the superclass of the searched class from parametrized type</li>
   * <li>Wikispeed.class is the instance from which the parametrized type is searched</li>
   * </ul>
   * @param <T> the class type.
   * @param searchedParametrizedTypeClass the superclass of the searched parametrized type.
   * @param fromClass the instance from which the parametrized type is searched.
   * @return the class of the parametrized type if any, null otherwise.
   */
  @SuppressWarnings("unchecked")
  public static <T> Class<T> searchParameterizedTypeFrom(Class<?> searchedParametrizedTypeClass,
      Class<?> fromClass) {
    return (Class) searchParameterizedTypeFrom(searchedParametrizedTypeClass, (Type) fromClass);
  }

  @SuppressWarnings("unchecked")
  private static Type searchParameterizedTypeFrom(Class<?> searchedParametrizedTypeClass,
      Type fromClassType) {
    if (fromClassType instanceof ParameterizedType) {
      for (Type classType : ((ParameterizedType) fromClassType).getActualTypeArguments()) {
        if (classType instanceof Class &&
            searchedParametrizedTypeClass.isAssignableFrom((Class) classType)) {
          return classType;
        }
      }
      return searchParameterizedTypeFrom(searchedParametrizedTypeClass,
          ((ParameterizedType) fromClassType).getRawType());
    } else if (fromClassType != null) {
      Type result = searchParameterizedTypeFrom(searchedParametrizedTypeClass,
          ((Class) fromClassType).getGenericSuperclass());
      if (result != null) {
        return result;
      }
      for (Type interfaceType : ((Class) fromClassType).getGenericInterfaces()) {
        result = searchParameterizedTypeFrom(searchedParametrizedTypeClass, interfaceType);
        if (result != null) {
          return result;
        }
      }
    }
    return null;
  }

  /**
   * This method is awesome.
   * It permits to retrieve the class from a class hierarchy the one that declares the given
   * annotation.
   * @param <T> the class type.
   * @param searchedAnnotationClass the class of the searched annotation.
   * @param fromClass the class from which the annotation is searched.
   * @return the class that declares the given annotation, null otherwise.
   */
  @SuppressWarnings("unchecked")
  public static <T> Class<T> searchClassThatDeclaresAnnotation(
      Class<? extends Annotation> searchedAnnotationClass, Class<?> fromClass) {
    return searchClassThatDeclaresAnnotation(searchedAnnotationClass, (Type) fromClass);
  }

  @SuppressWarnings("unchecked")
  private static <T> Class<T> searchClassThatDeclaresAnnotation(
      Class<? extends Annotation> searchedAnnotationClass, Type fromClassType) {
    if (fromClassType != null) {
      if (fromClassType instanceof Class) {
        if (((Class) fromClassType).getAnnotation(searchedAnnotationClass) != null) {
          return (Class) fromClassType;
        }
        return searchClassThatDeclaresAnnotation(searchedAnnotationClass,
            ((Class) fromClassType).getGenericSuperclass());
      } else if (fromClassType instanceof ParameterizedType) {
        return searchClassThatDeclaresAnnotation(searchedAnnotationClass,
            ((ParameterizedType) fromClassType).getRawType());
      } else {
        throw new NotImplementedException("");
      }
    }
    return null;
  }
}