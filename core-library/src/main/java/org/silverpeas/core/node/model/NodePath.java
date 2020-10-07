/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

package org.silverpeas.core.node.model;

import org.silverpeas.core.util.ResourcePath;

import java.util.Collection;

/**
 * List of {@link NodeDetail} which represents a path.
 * @author silveryocha
 */
public class NodePath extends ResourcePath<NodeDetail> {
  private static final long serialVersionUID = -2389557818767894656L;

  public NodePath() {
    super();
  }

  public NodePath(final int initialCapacity) {
    super(initialCapacity);
  }

  public NodePath(final Collection<? extends NodeDetail> c) {
    super(c);
  }

  @Override
  protected String getInstanceId(final NodeDetail node) {
    return node.getNodePK().getInstanceId();
  }

  @Override
  protected String getId(final NodeDetail node) {
    return node.getId();
  }

  @Override
  protected boolean isRoot(final NodeDetail node) {
    return node.isRoot();
  }

  @Override
  protected boolean rootIsComponentInstance() {
    return true;
  }

  @Override
  protected String getLabel(final NodeDetail node, final String language) {
    return node.getName(language);
  }
}
