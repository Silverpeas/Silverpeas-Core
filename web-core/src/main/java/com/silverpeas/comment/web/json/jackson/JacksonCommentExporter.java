/*
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

package com.silverpeas.comment.web.json.jackson;

import com.silverpeas.comment.model.Comment;
import com.silverpeas.comment.web.json.JSONCommentExporter;
import com.silverpeas.export.ExportDescriptor;
import com.silverpeas.export.ExportException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import javax.inject.Named;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import static com.silverpeas.comment.web.json.JSONCommentFields.*;

/**
 * An exporter of comments in the JSON format by using the Jackson API.
 */
@Named("jsonCommentExporter")
public class JacksonCommentExporter implements JSONCommentExporter {

  @Override
  public void export(ExportDescriptor descriptor, Comment... exportables) throws ExportException {
    export(descriptor, Arrays.asList(exportables));
  }

  @Override
  public void export(ExportDescriptor descriptor,
      List<Comment> exportables) throws ExportException {
    try {
      Writer output = descriptor.getWriter();
      JsonFactory factory = new JsonFactory();
      JsonGenerator generator = factory.createJsonGenerator(output);
      if (exportables.size() > 1) {
        generator.writeStartArray();
      }
      for (Comment comment : exportables) {
        generator.writeStartObject();
        generator.writeStringField(COMMENT_ID, comment.getCommentPK().getId());
        generator.writeStringField(COMPONENT_ID, comment.getCommentPK().getInstanceId());
        generator.writeStringField(RESOURCE_ID, comment.getForeignKey().getId());
        generator.writeStringField(TEXT, comment.getMessage());
        generator.writeObjectFieldStart(WRITER);
        generator.writeStringField(WRITER_ID, comment.getOwnerDetail().getId());
        generator.writeStringField(WRITER_NAME, comment.getOwnerDetail().getDisplayedName());
        generator.writeStringField(WRITER_AVATAR, comment.getOwnerDetail().getAvatar());
        generator.writeEndObject();
        generator.writeStringField(CREATION_DATE, comment.getCreationDate());
        generator.writeStringField(MODIFICATION_DATE, comment.getModificationDate());
        generator.writeEndObject();
      }
      if (exportables.size() > 1) {
        generator.writeEndArray();
      }
      generator.close();
    } catch (Exception ex) {
      throw new ExportException(ex.getMessage(), ex);
    }
  }

}
