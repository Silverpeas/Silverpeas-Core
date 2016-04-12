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
package org.silverpeas.core.persistence.jcr.util;

import java.util.Iterator;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

/**
 *
 * @author ehugonnet
 */
public class NodeIterable implements Iterable<Node> {

    /**
     * The node iterator being adapted.
     */
    private final NodeIterator iterator;

    /**
     * Creates an iterable adapter for the given node iterator.
     *
     * @param iterator the node iterator to be adapted
     */
    public NodeIterable(NodeIterator iterator) {
        this.iterator = iterator;
    }

    /**
     * Returns the node iterator.
     *
     * @return node iterator
     */
    @Override
    @SuppressWarnings("unchecked")
    public Iterator<Node> iterator() {
        return iterator;
    }

}
