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

package org.silverpeas.core.admin.component.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author ehugonnet
 */
public class MultilangHashMapAdapter extends XmlAdapter<Multilang, HashMap<String, String>> {

  @Override
  public HashMap<String, String> unmarshal(Multilang multilang) throws Exception {
    HashMap<String, String> result = new HashMap<String, String>();
    for (Message message : multilang.getMessage()) {
      result.put(message.getLang(), message.getValue());
    }
    return result;
  }

  @Override
  public Multilang marshal(HashMap<String, String> content) throws Exception {
    List<Message> messages = new ArrayList<Message>(content.size());
    for (Map.Entry<String, String> entry : content.entrySet()) {
      Message message = new Message();
      message.setLang(entry.getKey());
      message.setValue(entry.getValue());
      messages.add(message);
    }
    Multilang multi = new Multilang();
    multi.message = messages;
    return multi;
  }
}
