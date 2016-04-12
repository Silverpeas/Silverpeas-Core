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
package org.silverpeas.core.web.jstl.settings;

import javax.servlet.jsp.JspException;

import org.silverpeas.core.web.jstl.util.AbstractSetVarTagSupport;

import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;

import org.silverpeas.core.util.ResourceLocator;

/**
 *
 * @author ehugonnet
 */
public class SettingsValueTag extends AbstractSetVarTagSupport {

  private static final long serialVersionUID = 1L;
  private String key;
  private SettingBundle settings;
  private Object defaultValue;

  public void setSettings(String file) {
    this.settings = ResourceLocator.getSettingBundle(file);
  }

  public void setDefaultValue(Object defaultValue) {
    this.defaultValue = defaultValue;
  }

  public void setKey(String key) {
    this.key = key;
  }

  @Override
  public int doEndTag() throws JspException {
    if (StringUtil.isDefined(getVar())) {
      pageContext.setAttribute(getVar(), getValue(), getScope());
    }
    return EVAL_PAGE;
  }

  private Object getValue() {
    if (defaultValue == null) {
      return settings.getString(this.key, (String) defaultValue);
    }
    if (defaultValue instanceof Boolean) {
      return settings.getBoolean(this.key, (Boolean) defaultValue);
    }
    if (defaultValue instanceof Integer) {
      return settings.getInteger(this.key, (Integer) defaultValue);
    }
    if (defaultValue instanceof Long) {
      return settings.getLong(this.key, (Long) defaultValue);
    }
    if (defaultValue instanceof Float) {
      return settings.getFloat(this.key, (Float) defaultValue);
    }
    return settings.getString(this.key, (String) defaultValue);
  }
}
