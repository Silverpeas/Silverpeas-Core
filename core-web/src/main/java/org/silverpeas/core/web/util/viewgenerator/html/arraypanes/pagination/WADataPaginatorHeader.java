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
 * WADataPaginatorHeader.java
 *
 * Created on 26 mars 2001, 17:08
 */

package org.silverpeas.core.web.util.viewgenerator.html.arraypanes.pagination;

import java.util.Vector;

/**
 * The WADataPaginatorHeader is used by presentation components such as the ViewGenerator to help
 * them manage their user interface. The hold all information that are needed by the rendering
 * object to present table headers, namely: <br>
 * <li>Flags to help sorting data <li>Default sork jey <li>Routing address, that is, script code to
 * which the column headers of data displayed are bound to
 * <p>
 * This object is actually a collection of {@link FieldDescriptor} objects. One thus first creates
 * an empty WADataPaginatorHeader, and then call the {@link #addField} method as many times as there
 * are columns to display. <br>
 * The rendering process, on the other hand, will get this list of fields back, and will, depending
 * on the flags returned by the {@link #getFlags()} routine, output the header information of each
 * field.
 * @author jpouyadou
 * @version 1.0
 */
public class WADataPaginatorHeader {
  /** sortable flag. This flag is set if the given column is sortable. */
  public static final int FLAGS_SORTABLE = 0x0001;
  /**
   * ascending flag. If the FLAGS_SORTABLE is set, this is set also if the column data is sorted in
   * ascending order, and unset otherwise. THis flags must be preset by the creator or the object,
   * and could be toggled by the rendering process
   */
  public static final int FLAGS_ASCENDING = 0x0002;
  /**
   * sort key. This is the current sort key for the table
   */
  public static final int FLAGS_SORT_KEY = 0x0004;

  Vector<FieldDescriptor> m_Fields = new Vector<FieldDescriptor>(10);
  String m_SortKey = null;
  int m_SortKeyIndex = -1;
  String m_RoutingAddress = null;

  public WADataPaginatorHeader() {
  }

  /**
   * This method retuns the count of fields in this header. It should match the number of times the
   * {@link #addField} succeeded
   * @see #addField()
   */
  public int getFieldCount() {
    return (m_Fields.size());
  }

  /**
   * This method adds a field at the end of list of fields for this header
   * @param field The internal field name, presumably meaningful to the DataPaginator.
   * @param displayName The display name of the field, that would typically be output on the
   * rendering device by presentation objects (in short, thisis the label of the column)
   * @param flags A combination of flags suchs as {@link #FLAGS_SORTABLE}, {@link #FLAGS_ASCENDING}
   * and so on.
   * @param routingAddress A specification of the callback method used when the user selects this
   * field If this value is NULL, the rendering device uses whatever is suitable for its default
   * behaviour. If this value is empty, the rendering device should NOT associate a routing address
   * to this field. In other words, this field will not be 'clickable'. In all other cases, the
   * rendering process will use the value. Typically, one would use an URL as a routingAddress.
   */

  public void addField(String field, String displayName, int flags,
      String routingAddress) {
    m_Fields
        .add(new FieldDescriptor(field, displayName, flags, routingAddress));
    if ((flags & FLAGS_SORT_KEY) != 0) {
      m_SortKeyIndex = m_Fields.size() - 1;
      m_SortKey = field;
    }
  }

  public void toggleFieldSortOrder(int idx) {
    if (idx < 0 || idx >= m_Fields.size()) {
      return;
    }
    if (isFieldAscending(idx)) {
      setFieldDescending(idx);
    } else {
      setFieldAscending(idx);
    }
  }

  private void setFieldDescending(int idx) {
    FieldDescriptor fd = m_Fields.get(idx);
    int f = fd.getFlags();
    f &= ~FLAGS_ASCENDING;
    fd.setFlags(f);
  }

  private void setFieldAscending(int idx) {
    FieldDescriptor fd = m_Fields.get(idx);
    int f = fd.getFlags();
    f |= FLAGS_ASCENDING;
    fd.setFlags(f);
  }

  public boolean isFieldSortable(int idx) {
    if (idx < 0 || idx >= m_Fields.size()) {
      return (false);
    }
    return (m_Fields.get(idx).isSortable());
  }

  public boolean isFieldAscending(int idx) {
    if (idx < 0 || idx >= m_Fields.size()) {
      return (false);
    }
    return (m_Fields.get(idx).isAscending());
  }

  public boolean isFieldDescending(int idx) {
    if (idx < 0 || idx >= m_Fields.size()) {
      return (false);
    }
    return (m_Fields.get(idx).isDescending());
  }

  public String getFieldRoutingAddress(int idx) {
    if (idx < 0 || idx >= m_Fields.size()) {
      return (null);
    }
    return (m_Fields.get(idx).getRoutingAddress());
  }

  public String getSortKey() {
    return (m_SortKey);
  }

  public int getSortKeyIndex() {
    return (m_SortKeyIndex);
  }

  public String getFieldDisplayName(int idx) {
    if (idx < 0 || idx >= m_Fields.size()) {
      return (null);
    }
    return (m_Fields.get(idx).getDisplayName());
  }

  public String getFieldName(int idx) {
    if (idx < 0 || idx >= m_Fields.size()) {
      return (null);
    }
    return (m_Fields.get(idx).getName());
  }

  class FieldDescriptor {
    String m_Name = null;
    String m_DisplayName = null;
    int m_Flags;
    String m_RoutingAddress = null;

    FieldDescriptor(String name, String dn, int flags, String routingAddress) {
      m_Name = name;
      m_DisplayName = dn;
      m_Flags = flags;
      m_RoutingAddress = routingAddress;
    }

    public void setRoutingAddress(String address) {
      m_RoutingAddress = address;
    }

    public String getRoutingAddress() {
      return (m_RoutingAddress);
    }

    public void setFlags(int value) {
      m_Flags = value;
    }

    public String getName() {
      return (m_Name);
    }

    public String getDisplayName() {
      return (m_DisplayName);
    }

    public int getFlags() {
      return (m_Flags);
    }

    public boolean isSortable() {
      return ((m_Flags & FLAGS_SORTABLE) != 0);
    }

    public boolean isAscending() {
      if (isSortable()) {
        return ((m_Flags & FLAGS_ASCENDING) != 0);
      } else {
        return (false);
      }
    }

    public boolean isDescending() {
      if (isSortable()) {
        return ((m_Flags & FLAGS_ASCENDING) == 0);
      } else {
        return (false);
      }
    }
  }
}