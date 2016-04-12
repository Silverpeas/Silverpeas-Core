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

package org.silverpeas.core.util;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.test.BasicWarBuilder;
import org.silverpeas.core.exception.DecodingException;
import org.silverpeas.core.exception.EncodingException;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Integration test on the decoding/encoding of beans from/to JSON.
 * This test is an integration test instead of being a unit test in order to access the javax.json
 * API of JEE to assert the encoding and decoding work fine.
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class JSONCodecIntegrationTest {

  @Deployment
  public static Archive<?> createTestArchive() {
    return BasicWarBuilder.onWarForTestClass(JSONCodecIntegrationTest.class).testFocusedOn(
        war -> war.addClasses(TestSerializableBean.class, TestBean.class, DecodingException.class,
            EncodingException.class, JSONCodec.class))
        .addAsResource("META-INF/test-MANIFEST.MF", "META-INF/MANIFEST.MF").build();
  }

  @Test
  public void emptyTest() {
    // just to test the deployment into wildfly works fine.
  }

  @Test
  public void encodeABeanInJSONShouldWork() {
    TestSerializableBean bean = new TestSerializableBean("42", "Toto Chez-les-Papoos", new Date());

    String json = JSONCodec.encode(bean);

    assertThat(json, notNullValue());
    assertThat(json.isEmpty(), is(false));
    JsonReader reader = Json.createReader(new StringReader(json));
    JsonObject object = reader.readObject();
    assertThat(object.getString("id"), is(bean.getId()));
    assertThat(object.getString("name"), is(bean.getName()));
    Date date = new Date(object.getJsonNumber("date").longValue());
    assertThat(date, is(bean.getDate()));
  }

  @Test
  public void encodeAnUnannotatedBeanInJSONShouldWork() {
    TestBean bean = new TestBean("42", "Toto Chez-les-Papoos", new Date());

    String json = JSONCodec.encode(bean);

    assertThat(json, notNullValue());
    assertThat(json.isEmpty(), is(false));
    JsonReader reader = Json.createReader(new StringReader(json));
    JsonObject object = reader.readObject();
    assertThat(object.getString("id"), is(bean.getId()));
    assertThat(object.getString("name"), is(bean.getName()));
    Date date = new Date(object.getJsonNumber("date").longValue());
    assertThat(date, is(bean.getDate()));
  }

  @Test
  public void encodeAListOfBeansInJSONShouldWork() {
    TestSerializableBean bean1 = new TestSerializableBean("42", "Toto Chez-les-Papoos", new Date());
    TestSerializableBean bean2 = new TestSerializableBean("24", "Titi Gros-Minet", new Date());

    String json = JSONCodec.encode(Arrays.asList(bean1, bean2));

    assertThat(json, notNullValue());
    assertThat(json.isEmpty(), is(false));
    JsonReader reader = Json.createReader(new StringReader(json));
    JsonArray array = reader.readArray();
    assertThat(array.size(), is(2));
    assertThat(array.getJsonObject(0).getString("id"), is("42"));
    assertThat(array.getJsonObject(0).getString("name"), is("Toto Chez-les-Papoos"));
    Date date = new Date(array.getJsonObject(0).getJsonNumber("date").longValue());
    assertThat(date, is(bean1.getDate()));

    assertThat(array.getJsonObject(1).getString("id"), is("24"));
    assertThat(array.getJsonObject(1).getString("name"), is("Titi Gros-Minet"));
    date = new Date(array.getJsonObject(1).getJsonNumber("date").longValue());
    assertThat(date, is(bean2.getDate()));
  }

  @Test
  public void decodeJSONObjectIntoABeanShouldWork() {
    StringWriter json = new StringWriter();
    JsonWriter writer = Json.createWriter(json);
    JsonObject object =
        Json.createObjectBuilder().add("id", "42").add("name", "Toto Chez-les-Papoos")
            .add("date", new Date().getTime()).build();
    writer.writeObject(object);

    TestSerializableBean bean = JSONCodec.decode(json.toString(), TestSerializableBean.class);
    assertThat(bean, notNullValue());
    assertThat(bean.getId(), is(object.getString("id")));
    assertThat(bean.getName(), is(object.getString("name")));
    assertThat(bean.getDate().getTime(), is(object.getJsonNumber("date").longValue()));
  }

  @Test
  public void decodeJSONObjectIntoAnUnannotatedBeanShouldWork() {
    StringWriter json = new StringWriter();
    JsonWriter writer = Json.createWriter(json);
    JsonObject object =
        Json.createObjectBuilder().add("id", "42").add("name", "Toto Chez-les-Papoos")
            .add("date", new Date().getTime()).build();
    writer.writeObject(object);

    TestBean bean = JSONCodec.decode(json.toString(), TestBean.class);
    assertThat(bean, notNullValue());
    assertThat(bean.getId(), is(object.getString("id")));
    assertThat(bean.getName(), is(object.getString("name")));
    assertThat(bean.getDate().getTime(), is(object.getJsonNumber("date").longValue()));
  }

  @Test
  public void decodeJSONStreamIntoABeanShouldWork() {
    InputStream jsonStream = getClass().getResourceAsStream("bean.json");
    TestSerializableBean bean = JSONCodec.decode(jsonStream, TestSerializableBean.class);
    assertThat(bean, notNullValue());
    assertThat(bean.getId(), is("42"));
    assertThat(bean.getName(), is("Toto Chez-les-Papoos"));
    assertThat(bean.getDate().getTime(), is(1416580107074l));
  }

  @Test
  public void decodeJSONArrayIntoAnArrayOfBeansShouldWork() {
    StringWriter json = new StringWriter();
    JsonWriter writer = Json.createWriter(json);
    JsonArray array = Json.createArrayBuilder().add(
        Json.createObjectBuilder().add("id", "42").add("name", "Toto Chez-les-Papoos")
            .add("date", new Date().getTime())).add(
        Json.createObjectBuilder().add("id", "24").add("name", "Titi Gros-Minet")
            .add("date", new Date().getTime())).build();
    writer.writeArray(array);

    TestSerializableBean[] beans = JSONCodec.decode(json.toString(), TestSerializableBean[].class);
    assertThat(beans, notNullValue());
    assertThat(beans.length, is(array.size()));
    assertThat(beans[0].getId(), is(array.getJsonObject(0).getString("id")));
    assertThat(beans[0].getName(), is(array.getJsonObject(0).getString("name")));
    assertThat(beans[0].getDate().getTime(),
        is(array.getJsonObject(0).getJsonNumber("date").longValue()));
    assertThat(beans[1].getId(), is(array.getJsonObject(1).getString("id")));
    assertThat(beans[1].getName(), is(array.getJsonObject(1).getString("name")));
    assertThat(beans[1].getDate().getTime(),
        is(array.getJsonObject(1).getJsonNumber("date").longValue()));
  }

  @Test
  public void decodeJSONStreamIntoAnArrayOfBeansShouldWork() {
    InputStream jsonStream = getClass().getResourceAsStream("beanArray.json");
    TestSerializableBean[] beans = JSONCodec.decode(jsonStream, TestSerializableBean[].class);
    assertThat(beans[0], notNullValue());
    assertThat(beans.length, is(2));
    assertThat(beans[0].getId(), is("42"));
    assertThat(beans[0].getName(), is("Toto Chez-les-Papoos"));
    assertThat(beans[0].getDate().getTime(), is(1416580107074l));
    assertThat(beans[1], notNullValue());
    assertThat(beans[1].getId(), is("24"));
    assertThat(beans[1].getName(), is("Titi Gros-Minet"));
    assertThat(beans[1].getDate().getTime(), is(1416580107074l));
  }

  @Test
  public void encodeDynamicallyAJSONObjectShouldWork() {
    String json = JSONCodec.encodeObject(o -> o.put("name", "Toto Chez-les-Papoos").put("age", 42));

    assertThat(json, notNullValue());
    assertThat(json.isEmpty(), is(false));
    JsonReader reader = Json.createReader(new StringReader(json));
    JsonObject object = reader.readObject();
    assertThat(object.getInt("age"), is(42));
    assertThat(object.getString("name"), is("Toto Chez-les-Papoos"));
  }

  @Test
  public void encodeDynamicallyAnEmptyJSONObjectShouldWork() {
    String json = JSONCodec.encodeObject(o -> o);

    assertThat(json, notNullValue());
    assertThat(json.isEmpty(), is(false));
    assertThat(json, is("{}"));
  }

  @Test
  public void encodeDynamicallyAJSONArrayShouldWork() {
    String json = JSONCodec.encodeArray(
        a -> a.addJSONObject(o -> o.put("name", "Toto Chez-les-Papoos").put("age", 42))
            .addJSONObject(o -> o.put("name", "Titi Gros-Minet").put("age", 24)));

    assertThat(json, notNullValue());
    assertThat(json.isEmpty(), is(false));
    JsonReader reader = Json.createReader(new StringReader(json));
    JsonArray array = reader.readArray();
    assertThat(array.size(), is(2));
    assertThat(array.getJsonObject(0).getInt("age"), is(42));
    assertThat(array.getJsonObject(0).getString("name"), is("Toto Chez-les-Papoos"));

    assertThat(array.getJsonObject(1).getInt("age"), is(24));
    assertThat(array.getJsonObject(1).getString("name"), is("Titi Gros-Minet"));
  }

  @Test
  public void encodeDynamicallyAnEmptyJSONArrayShouldWork() {
    String json = JSONCodec.encodeArray(a -> a);

    assertThat(json, notNullValue());
    assertThat(json.isEmpty(), is(false));
    assertThat(json, is("[]"));
  }

  @Test
  public void encodeListAsJSONArrayShouldWork() {
    List<String> elements = new ArrayList<>();
    elements.add("one");
    elements.add("two");
    elements.add("three");

    String result = JSONCodec.encodeArray(jsonArray -> {
      jsonArray.addJSONArray(elements);
      return jsonArray;
    });
    assertThat(result, notNullValue());
    assertThat(result, is("[\"one\",\"two\",\"three\"]"));
  }

}
