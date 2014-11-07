/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.util;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import org.silverpeas.util.exception.DecodingException;
import org.silverpeas.util.exception.EncodingException;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;

/**
 * An encoder of Java bean to a JSON representation and a decoder of JSON stream into the
 * corresponding Java bean.
 * <p>
 * In order to perform the marshalling and the unmarchalling, the fields of the bean must be
 * annotated with the JAXB annotations.
 * @author mmoquillon
 */
public class JSONCodec {

  /**
   * Encodes the specified bean into a JSON representation.
   * @param bean the bean to encode.
   * @param <T> the type of the bean.
   * @return the JSON representation of the bean in a String.
   * @throws EncodingException if an error occurs while encoding a bean in JSON.
   */
  public static <T extends Serializable> String encode(T bean) throws EncodingException {
    ObjectMapper mapper = getObjectMapper();
    StringWriter writer = new StringWriter();
    try {
      mapper.writeValue(writer, bean);
    } catch (IOException ex) {
      throw new EncodingException(ex.getMessage(), ex);
    }
    return writer.toString();
  }

  /**
   * Decodes the specified JSON representation into its corresponding bean.
   * @param json the JSON representation of a bean to decode.
   * @param <T> the type of the bean.
   * @return the bean decoded from JSON.
   * @throws EncodingException if an error occurs while decoding a JSON String into a bean.
   */
  public static <T extends Serializable> T decode(String json, Class<T> beanType)
      throws DecodingException {
    ObjectMapper mapper = getObjectMapper();
    T bean;
    try {
      bean = mapper.readValue(json, beanType);
    } catch (IOException ex) {
      throw new DecodingException(ex.getMessage(), ex);
    }
    return bean;
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    AnnotationIntrospector introspector = new JaxbAnnotationIntrospector(
        TypeFactory.defaultInstance());
    mapper.setAnnotationIntrospector(introspector);
    return mapper;
  }
}
