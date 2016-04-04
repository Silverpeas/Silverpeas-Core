/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Created by IntelliJ IDEA.
 * User: mikhail_nikolaenko
 * Date: Aug 15, 2002
 * Time: 3:36:36 PM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package org.silverpeas.core.index.search.model;

import java.util.List;
import java.util.ArrayList;

public class AxisFilter {
  public static final String NAME = "NAME";
  public static final String DESCRIPTION = "DESCRIPTION";

  private List<AxisFilterNode> filter = new ArrayList<>(1);
  private int index = -1;

  public AxisFilter() {
  }

  public AxisFilter(String property, String value) {
    this(new AxisFilterNode(property, value));
  }

  public AxisFilter(AxisFilterNode filter_node) {
    filter.add(filter_node);
  }

  public void addCondition(String property, String value) {
    addCondition(new AxisFilterNode(property, value));
  }

  public void addCondition(AxisFilterNode filter_node) {
    filter.add(filter_node);
  }

  public AxisFilterNode getFirstCondition() {
    if (filter.size() > 0) {
      index = 0;
      return filter.get(0);
    } else {
      return null;
    }
  }

  public AxisFilterNode getNextCondition() {
    if (++index < filter.size()) {
      return filter.get(index);
    } else {
      return null;
    }
  }

  public int size() {
    return filter.size();
  }
}
