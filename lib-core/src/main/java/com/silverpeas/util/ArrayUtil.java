/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.silverpeas.util;

import com.novell.ldap.LDAPEntry;
import com.silverpeas.form.FieldTemplate;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.apache.commons.lang3.ArrayUtils;

public class ArrayUtil extends ArrayUtils {

  public static final Group[] EMPTY_GROUP_ARRAY = new Group[0];
  public static final UserDetail[] EMPTY_USER_DETAIL_ARRAY = new UserDetail[0];
  public static final FieldTemplate[] EMPTY_FIELD_TEMPLATE_ARRAY = new FieldTemplate[0];
  public static final LDAPEntry[] EMPTY_LDAP_ENTRY_ARRAY = new LDAPEntry[0];
  public static final ComponentInst[] EMPTY_COMPONENT_INSTANCE_ARRAY = new ComponentInst[0];
}
