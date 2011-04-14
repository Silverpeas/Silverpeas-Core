/*
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * along withWriter this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.comment.web.json.jackson;

import com.silverpeas.comment.web.CommentEntity;
import com.silverpeas.comment.web.json.JSONCommentExporter;
import com.silverpeas.export.ExportDescriptor;
import com.silverpeas.export.ExportException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import javax.inject.Named;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import static com.silverpeas.comment.web.json.JSONCommentFields.*;

/**
 * An exporter of comments in the JSON format by using the Jackson API.
 */
@Named("jsonCommentExporter")
public class JacksonCommentExporter implements JSONCommentExporter {

  private ObjectMapper mapper = new ObjectMapper();

  @Override
  public void export(ExportDescriptor descriptor, CommentEntity... exportables) throws ExportException {
    export(descriptor, Arrays.asList(exportables));
  }

  @Override
  public void export(ExportDescriptor descriptor,
      List<CommentEntity> exportables) throws ExportException {
    try {
      Writer output = descriptor.getWriter();
      if (exportables.size() == 1) {
        mapper.writeValue(output, exportables.get(0));
      } else {
        mapper.writeValue(output, exportables);
      }
//      JsonFactory factory = new JsonFactory();
//      JsonGenerator generator = factory.createJsonGenerator(output);
//      if (exportables.size() > 1) {
//        generator.writeStartArray();
//      }
//      for (CommentEntity comment : exportables) {
//        generator.writeStartObject();
//        generator.writeStringField(COMMENT_URI_FIELD, comment.getURI().toString());
//        generator.writeStringField(COMMENT_ID_FIELD, comment.getId());
//        generator.writeStringField(COMPONENT_ID_FIELD, comment.getComponentId());
//        generator.writeStringField(RESOURCE_ID_FIELD, comment.getResourceId());
//        generator.writeStringField(TEXT_FIELD, comment.getText());
//        generator.writeObjectFieldStart(AUTHOR_FIELD);
//        generator.writeStringField(AUTHOR_ID_FIELD, comment.getWriter().getId());
//        generator.writeStringField(AUTHOR_NAME_FIELD, comment.getWriter().getFullName());
//        generator.writeStringField(AUTHOR_AVATAR_FIELD, comment.getWriter().getAvatar());
//        generator.writeEndObject();
//        generator.writeStringField(CREATION_DATE_FIELD, comment.getCreationDate());
//        generator.writeStringField(MODIFICATION_DATE_FIELD, comment.getModificationDate());
//        generator.writeEndObject();
//      }
//      if (exportables.size() > 1) {
//        generator.writeEndArray();
//      }
//      generator.close();
    } catch (Exception ex) {
      throw new ExportException(ex.getMessage(), ex);
    }
  }

}
