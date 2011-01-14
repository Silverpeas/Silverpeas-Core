/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.workflow.engine.model;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import com.silverpeas.workflow.api.model.Trigger;
import com.silverpeas.workflow.api.model.Triggers;
import java.util.ArrayList;

/**
 * Class implementing the representation of the &lt;triggers&gt; element of a Process Model.
 **/
public class TriggersImpl implements Serializable, Triggers {
  private static final long serialVersionUID = -2251572849084710965L;
  private List<Trigger> triggerList;

  /**
   * Constructor
   */
  public TriggersImpl() {
    super();
    triggerList = new ArrayList<Trigger>();
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.engine.model.Columns#getItemRefList()
   */
  @Override
  public List<Trigger> getTriggerList() {
    return triggerList;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Columns#addColumn(com.silverpeas.workflow
   * .api.model.Column)
   */
  @Override
  public void addTrigger(Trigger trigger) {
    triggerList.add(trigger);
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Columns#createColumn()
   */
  @Override
  public Trigger createTrigger() {
    return new TriggerImpl();
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Columns#iterateColumn()
   */
  @Override
  public Iterator<Trigger> iterateTrigger() {
    return triggerList.iterator();
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Columns#removeAllColumns()
   */
  @Override
  public void removeAllTriggers() {
    triggerList.clear();
  }
}
