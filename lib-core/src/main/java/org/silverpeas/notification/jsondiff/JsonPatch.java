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
package org.silverpeas.notification.jsondiff;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

/**
 * A list of operations.
 *
 * @author ehugonnet
 */
public class JsonPatch implements Serializable {

  private static final long serialVersionUID = 1L;
  private static final ObjectMapper mapper = new ObjectMapper();

  private Map<String, Operation> operations;

  public JsonPatch() {
    operations = new LinkedHashMap<String, Operation>(5);
  }

  public List<Operation> getOperations() {
    return new ArrayList<Operation>(this.operations.values());
  }

  public Operation getOperationByPath(String path) {
    return this.operations.get(path);
  }

  public void addOperation(Operation operation) {
    if (operation.getOp() != Op.none) {
      this.operations.put(operation.getPath(), operation);
    }
  }

  public void setOperations(List<Operation> operations) {
    this.operations.clear();
    if (operations != null) {
      for (Operation operation : operations) {
        addOperation(operation);
      }
    }
  }

  public String toJson() throws IOException {
    return mapper.writeValueAsString(this.getOperations());
  }

  public void fromJson(String json) throws IOException {
    this.setOperations(mapper.<List<Operation>>readValue(json,
        new TypeReference<List<Operation>>() {
        }));
  }
}
