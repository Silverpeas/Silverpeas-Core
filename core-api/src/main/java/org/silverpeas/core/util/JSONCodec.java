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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import org.silverpeas.core.exception.DecodingException;
import org.silverpeas.core.exception.EncodingException;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * An encoder of Java bean to a JSON representation and a decoder of JSON stream into the
 * corresponding Java bean.
 * <p>
 * In order to perform the marshalling and the unmarchalling, the fields of the bean must be
 * annotated with the JAXB annotations. All null fields are by default ignored.
 * @author mmoquillon
 */
public class JSONCodec {

  private JSONCodec() {
  }

  /**
   * Encodes the specified bean into a JSON representation.
   * @param bean the bean to encode.
   * @param <T> the type of the bean.
   * @return the JSON representation of the bean in a String.
   * @throws EncodingException if an error occurs while encoding a bean in JSON.
   */
  public static <T> String encode(T bean) {
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
   * Encodes the bean dynamically built by the specified builder. This method is just a convenient
   * one to dynamically build a JSON representation of a simple bean.
   * We recommend to represent the bean to serialize as a Java object and then
   * to use the method {@code org.silverpeas.core.util.JSONCodec#encode(T bean)}.
   * @param beanBuilder a function that accepts as argument a JSONObject instance and that returns
   * the JSONObject instance enriched with the attributes set by the function.
   * @return the JSON representation of the bean in a String.
   * @throws EncodingException if an error occurs while encoding a bean in JSON.
   */
  public static String encodeObject(UnaryOperator<JSONObject> beanBuilder) {
    ObjectMapper mapper = getObjectMapper();
    StringWriter writer = new StringWriter();
    JsonNode node = mapper.createObjectNode();
    JSONObject bean = beanBuilder.apply(new JSONObject((ObjectNode) node));
    try {
      mapper.writeValue(writer, bean.getObjectNode());
    } catch (IOException ex) {
      throw new EncodingException(ex.getMessage(), ex);
    }
    return writer.toString();
  }

  /**
   * Encodes an array of beans that are dynamically built thank to the specified builder.
   * This method is just a convenient one to dynamically build a JSON representation of an array
   * of simple beans. We recommend to represent the bean to serialize as a Java object and then
   * to use the method {@code org.silverpeas.core.util.JSONCodec#encode(T bean)}.
   * @param arrayBuilder a function that accepts as argument a JSONObject instance and that returns
   * the JSONObject instance enriched with the attributes set by the function.
   * @return the JSON representation of the bean in a String.
   * @throws EncodingException if an error occurs while encoding a bean in JSON.
   */
  public static String encodeArray(UnaryOperator<JSONArray> arrayBuilder) {
    ObjectMapper mapper = getObjectMapper();
    StringWriter writer = new StringWriter();
    ArrayNode node = mapper.createArrayNode();
    JSONArray array = arrayBuilder.apply(new JSONArray(node));
    try {
      mapper.writeValue(writer, array.getArrayNode());
    } catch (IOException ex) {
      throw new EncodingException(ex.getMessage(), ex);
    }
    return writer.toString();
  }

  /**
   * Decodes the specified JSON representation into its corresponding bean.
   * @param <T> the type of the bean.
   * @param json the JSON representation of a bean to decode.
   * @param beanType the class of the bean
   * @return the bean decoded from JSON.
   * @throws EncodingException if an error occurs while decoding a JSON String into a bean.
   */
  public static <T> T decode(String json, Class<T> beanType) {
    ObjectMapper mapper = getObjectMapper();
    T bean;
    try {
      bean = mapper.readValue(json, beanType);
    } catch (IOException ex) {
      throw new DecodingException(ex.getMessage(), ex);
    }
    return bean;
  }

  /**
   * Decodes the specified JSON representation into its corresponding bean.
   * @param <T> the type of the bean.
   * @param jsonStream a stream to a JSON representation of a bean to decode.
   * @param beanType the class of the bean
   * @return the bean decoded from JSON.
   * @throws EncodingException if an error occurs while decoding a JSON stream into a bean.
   */
  public static <T> T decode(InputStream jsonStream, Class<T> beanType) {
    ObjectMapper mapper = getObjectMapper();
    T bean;
    try {
      bean = mapper.readValue(jsonStream, beanType);
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
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    return mapper;
  }

  public static class JSONObject {

    private ObjectNode objectNode;

    protected JSONObject(ObjectNode objectNode) {
      this.objectNode = objectNode;
    }

    protected ObjectNode getObjectNode() {
      return this.objectNode;
    }

    public JSONObject putNull(final String fieldName) {
      objectNode.putNull(fieldName);
      return this;
    }

    public JSONObject put(final String fieldName, final Number v) {
      if (v instanceof Short) {
        put(fieldName, (Short) v);
      } else if (v instanceof Integer) {
        put(fieldName, (Integer) v);
      } else if (v instanceof Long) {
        put(fieldName, (Long) v);
      } else if (v instanceof Float) {
        put(fieldName, (Float) v);
      } else if (v instanceof Double) {
        put(fieldName, (Double) v);
      } else if (v instanceof BigDecimal) {
        put(fieldName, (BigDecimal) v);
      } else {
        put(fieldName, encode(v));
      }
      return this;
    }

    public JSONObject put(final String fieldName, final Short v) {
      objectNode.put(fieldName, v);
      return this;
    }

    public JSONObject put(final String fieldName, final Integer v) {
      objectNode.put(fieldName, v);
      return this;
    }

    public JSONObject put(final String fieldName, final Long v) {
      objectNode.put(fieldName, v);
      return this;
    }

    public JSONObject put(final String fieldName, final Float v) {
      objectNode.put(fieldName, v);
      return this;
    }

    public JSONObject put(final String fieldName, final Double v) {
      objectNode.put(fieldName, v);
      return this;
    }

    public JSONObject put(final String fieldName, final BigDecimal v) {
      objectNode.put(fieldName, v);
      return this;
    }

    public JSONObject put(final String fieldName, final String v) {
      objectNode.put(fieldName, v);
      return this;
    }

    public JSONObject put(final String fieldName, final Boolean v) {
      objectNode.put(fieldName, v);
      return this;
    }

    public JSONObject put(final String fieldName, final byte[] v) {
      objectNode.put(fieldName, v);
      return this;
    }

    public JSONObject putJSONArray(final String fieldName, UnaryOperator<JSONArray> arrayBuilder) {
      arrayBuilder.apply(new JSONArray(objectNode.putArray(fieldName)));
      return this;
    }

    public JSONObject putJSONObject(final String fieldName,
        UnaryOperator<JSONObject> arrayBuilder) {
      arrayBuilder.apply(new JSONObject(objectNode.putObject(fieldName)));
      return this;
    }
  }

  public static class JSONArray {

    private ArrayNode arrayNode;

    protected JSONArray(ArrayNode arrayNode) {
      this.arrayNode = arrayNode;
    }

    protected ArrayNode getArrayNode() {
      return this.arrayNode;
    }

    public JSONArray add(final Number v) {
      if (v instanceof Short) {
        add((Short) v);
      } else if (v instanceof Integer) {
        add((Integer) v);
      } else if (v instanceof Long) {
        add((Long) v);
      } else if (v instanceof Float) {
        add((Float) v);
      } else if (v instanceof Double) {
        add((Double) v);
      } else if (v instanceof BigDecimal) {
        add((BigDecimal) v);
      } else {
        add(encode(v));
      }
      return this;
    }

    public JSONArray add(final Short v) {
      arrayNode.add(v);
      return this;
    }

    public JSONArray add(final Integer v) {
      arrayNode.add(v);
      return this;
    }

    public JSONArray add(final Long v) {
      arrayNode.add(v);
      return this;
    }

    public JSONArray add(final Float v) {
      arrayNode.add(v);
      return this;
    }

    public JSONArray add(final Double v) {
      arrayNode.add(v);
      return this;
    }

    public JSONArray add(final BigDecimal v) {
      arrayNode.add(v);
      return this;
    }

    public JSONArray add(final String v) {
      arrayNode.add(v);
      return this;
    }

    public JSONArray add(final Boolean v) {
      arrayNode.add(v);
      return this;
    }

    public JSONArray add(final byte[] v) {
      arrayNode.add(v);
      return this;
    }

    public JSONArray addJSONObject(UnaryOperator<JSONObject> beanBuilder) {
      ObjectNode node = arrayNode.addObject();
      beanBuilder.apply(new JSONObject(node));
      return this;
    }

    public JSONArray addJSONArray(UnaryOperator<JSONArray> arrayBuilder) {
      ArrayNode node = arrayNode.addArray();
      arrayBuilder.apply(new JSONArray(node));
      return this;
    }

    public JSONArray addJSONArray(List<String> elements) {
      for (String element : elements) {
        arrayNode.add(element);
      }
      return this;
    }
  }
}
