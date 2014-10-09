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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.util.exception.DecodingException;
import org.silverpeas.util.exception.EncodingException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Integration test on the decoding/encoding of beans from/to JSON.
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class JSONCodecIntegrationTest {

  @Deployment
  public static Archive<?> createTestArchive() {
    return ShrinkWrap.create(JavaArchive.class, "test.jar")
        .addClasses(TestSerializableBean.class,
            DecodingException.class, EncodingException.class, JSONCodec.class)
        .addAsManifestResource("META-INF/test-MANIFEST.MF", "MANIFEST.MF")
        .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
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
  public void decodeJSONIntoABeanShouldWork() {
    StringWriter json = new StringWriter();
    JsonWriter writer = Json.createWriter(json);
    JsonObject object = Json.createObjectBuilder()
        .add("id", "42")
        .add("name", "Toto Chez-les-Papoos")
        .add("date", new Date().getTime())
        .build();
    writer.writeObject(object);

    TestSerializableBean bean = JSONCodec.decode(json.toString(), TestSerializableBean.class);
    assertThat(bean, notNullValue());
    assertThat(bean.getId(), is(object.getString("id")));
    assertThat(bean.getName(), is(object.getString("name")));
    assertThat(bean.getDate().getTime(), is(object.getJsonNumber("date").longValue()));
  }
}
