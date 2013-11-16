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
package com.stratelia.webactiv.beans.admin;

import java.util.Date;

import org.silverpeas.notification.jsondiff.Op;
import org.silverpeas.notification.jsondiff.Operation;

import com.silverpeas.admin.components.Parameter;
import com.silverpeas.admin.components.ParameterInputType;
import com.silverpeas.admin.notification.ComponentJsonPatch;

import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 *
 * @author ehugonnet
 */
public class ComponentInstTest {

  public ComponentInstTest() {
  }

  /**
   * Test of diff method, of class ComponentInst.
   */
  @Test
  public void testDiff() {
    ComponentInst component = new ComponentInst();
    component.setCreateDate(new Date());
    component.setCreator(new UserDetail());
    component.setCreatorUserId("10");
    component.setDescription("Description");
    component.setDomainFatherId("domain20");
    component.setHidden(false);
    component.setId("15");
    component.setInheritanceBlocked(true);
    component.setLabel("Component for test");
    component.setName("kmelia");
    component.setOrderNum(5);
    component.setPublic(true);
    component.setStatus("test");
    Parameter param = new Parameter();
    param.setName("param1");
    param.setValue("value1");
    param.setType(ParameterInputType.text.name());
    component.getParameters().add(param);
    param = new Parameter();
    param.setName("param2");
    param.setValue("value2");
    param.setType(ParameterInputType.text.name());
    component.getParameters().add(param);
    param = new Parameter();
    param.setName("param3");
    param.setValue(null);
    param.setType(ParameterInputType.text.name());
    component.getParameters().add(param);

    ComponentInst newComponent = (ComponentInst) component.clone();
    newComponent.getParameter("param2").setValue(null);
    newComponent.getParameter("param3").setValue("value3");
    newComponent.setDescription("This component is used for test only");
    newComponent.setPublic(false);
    ComponentJsonPatch result = component.diff(newComponent);

    assertThat(result, is(notNullValue()));
    assertThat(result.getComponentType(), is("kmelia"));
    assertThat(result.getOperations(), is(notNullValue()));
    assertThat(result.getOperations(), hasSize(4));
    Operation operation = result.getOperationByPath("param2");
    assertThat(operation, is(notNullValue()));
    assertThat(operation.getOp(), is(Op.remove));
    assertThat(operation.getValue(), is(""));

    operation = result.getOperationByPath("param3");
    assertThat(operation, is(notNullValue()));
    assertThat(operation.getOp(), is(Op.add));
    assertThat(operation.getValue(), is("value3"));

    operation = result.getOperationByPath("description");
    assertThat(operation, is(notNullValue()));
    assertThat(operation.getOp(), is(Op.replace));
    assertThat(operation.getValue(), is("This component is used for test only"));

    operation = result.getOperationByPath("public");
    assertThat(operation, is(notNullValue()));
    assertThat(operation.getOp(), is(Op.replace));
    assertThat(operation.getValue(), is("false"));
  }

}
