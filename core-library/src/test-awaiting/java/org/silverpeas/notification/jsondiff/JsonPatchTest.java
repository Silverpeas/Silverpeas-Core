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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.notification.system.jsondiff;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 *
 * @author ehugonnet
 */
public class JsonPatchTest {

  /**
   * Test of toJson method, of class Patch.
   */
  @Test
  public void testToJson() throws Exception {
    JsonPatch instance = new JsonPatch();
    instance.addOperation(new Operation(Op.replace, "name", "Ged corrigée"));
    instance.addOperation(new Operation(Op.replace, "versionning", "false"));
    instance.addOperation(new Operation(Op.remove, "xmlForm", ""));
    String expResult = "[{\"op\":\"replace\",\"path\":\"name\",\"value\":\"Ged "
        + "corrigée\"},{\"op\":\"replace\",\"path\":\"versionning\",\"value\":\"false\"},{"
        + "\"op\":\"remove\",\"path\":\"xmlForm\",\"value\":\"\"}]";
    String result = instance.toJson();
    assertThat(result, is(expResult));
  }

  /**
   * Test of fromJson method, of class Patch.
   */
  @Test
  public void testFromJson() throws Exception {
    String json = "[{\"op\":\"replace\",\"path\":\"name\",\"value\":\"Ged "
        + "corrigée\"},{\"op\":\"replace\",\"path\":\"versionning\",\"value\":\"false\"},{"
        + "\"op\":\"remove\",\"path\":\"xmlForm\",\"value\":\"\"}]";
    JsonPatch instance = new JsonPatch();
    instance.fromJson(json);
    assertThat(instance, is(notNullValue()));
    assertThat(instance.getOperations(), is(notNullValue()));
    assertThat(instance.getOperations(), hasSize(3));
    Operation operation = instance.getOperationByPath("name");
    assertThat(operation, is(notNullValue()));
    assertThat(operation.getOp(), is(Op.replace));
    assertThat(operation.getValue(), is("Ged corrigée"));
    assertThat(operation.getPath(), is("name"));

    operation = instance.getOperationByPath("versionning");
    assertThat(operation, is(notNullValue()));
    assertThat(operation.getOp(), is(Op.replace));
    assertThat(operation.getValue(), is("false"));
    assertThat(operation.getPath(), is("versionning"));

    operation = instance.getOperationByPath("xmlForm");
    assertThat(operation, is(notNullValue()));
    assertThat(operation.getOp(), is(Op.remove));
    assertThat(operation.getValue(), is(""));
    assertThat(operation.getPath(), is("xmlForm"));

    operation = instance.getOperationByPath("miguel");
    assertThat(operation, is(nullValue()));
  }

  /**
   * Test of serialize an instance.
   */
  @Test
  public void testSerialize() throws Exception {
    String json = "[{\"op\":\"replace\",\"path\":\"name\",\"value\":\"Ged "
        + "corrigée\"},{\"op\":\"replace\",\"path\":\"versionning\",\"value\":\"false\"},{"
        + "\"op\":\"remove\",\"path\":\"xmlForm\",\"value\":\"\"}]";
    JsonPatch instance = new JsonPatch();
    instance.fromJson(json);
    assertThat(instance, is(notNullValue()));
    assertThat(instance.getOperations(), is(notNullValue()));
    assertThat(instance.getOperations(), hasSize(3));
    ByteArrayOutputStream buffer = new ByteArrayOutputStream(512);
    ObjectOutputStream out = new ObjectOutputStream(buffer);
    try {
      out.writeObject(instance);
    } finally {
      IOUtils.closeQuietly(out);
    }
  }

}
