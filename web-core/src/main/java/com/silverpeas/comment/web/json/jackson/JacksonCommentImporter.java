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
import com.silverpeas.comment.model.CommentPK;
import com.silverpeas.comment.web.json.JSONCommentImporter;
import com.silverpeas.export.ImportDescriptor;
import com.silverpeas.export.ImportException;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Named;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import static com.silverpeas.comment.web.json.JSONCommentFields.*;

/**
 * An importer of comments in the JSON format by using the Jackson API.
 */
@Named("jsonCommentImporter")
public class JacksonCommentImporter implements JSONCommentImporter {

  private static final String ERROR_FIELD_MSG = "Unexpected field in the JSON stream: ";
  private static final String ERROR_START_TOKEN_MSG = "Unexpected start token in the JSON stream: ";

  @Override
  public List<Comment> importFrom(ImportDescriptor descriptor) throws ImportException {
    try {
      List<Comment> comments = new ArrayList<Comment>();

      JsonFactory jsonFactory = new JsonFactory();
      JsonParser parser = jsonFactory.createJsonParser(descriptor.getReader());
      JsonToken parsingEndToken = getParsingEndToken(parser);
      if (parsingEndToken == JsonToken.END_OBJECT) {
        comments.add(parseACommentWith(parser));
      } else {
        while (parser.nextToken() != JsonToken.END_ARRAY) {
          comments.add(parseACommentWith(parser));
        }
      }

      return comments;
    } catch (ImportException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new ImportException(ex.getMessage(), ex);
    }
  }

  private JsonToken getParsingEndToken(final JsonParser parser) throws ImportException, IOException {
    JsonToken token = parser.nextToken();
    switch (token) {
      case START_ARRAY:
        token = JsonToken.END_ARRAY;
        break;
      case START_OBJECT:
        token = JsonToken.END_OBJECT;
        break;
      default:
        throw new ImportException(ERROR_START_TOKEN_MSG + token.name());
    }
    return token;
  }

  private Comment parseACommentWith(JsonParser parser) throws IOException, ImportException {
    String text = "";
    String id = "";
    String resourceId = "";
    String creationDate = "";
    String modificationDate = "";
    String componentId = "";
    UserDetail user = new UserDetail();
    while (parser.nextToken() != JsonToken.END_OBJECT) {

      String fieldName = parser.getCurrentName();
      if (COMMENT_ID.equals(fieldName)) {
        id = parser.getText();
      } else if (COMPONENT_ID.equals(fieldName)) {
        componentId = parser.getText();
      } else if (RESOURCE_ID.equals(fieldName)) {
        resourceId = parser.getText();
      } else if (CREATION_DATE.equals(fieldName)) {
        creationDate = parser.getText();
      } else if (MODIFICATION_DATE.equals(fieldName)) {
        modificationDate = parser.getText();
      } else if (TEXT.equals(fieldName)) {
        text = parser.getText();
      } else if (WRITER.equals(fieldName)) {
        if (parser.nextToken() != JsonToken.START_OBJECT) {
          throw new ImportException(ERROR_FIELD_MSG + parser.getCurrentName() +
              ". Expected: " + JsonToken.START_OBJECT.name());
        }
        while (parser.nextToken() != JsonToken.END_OBJECT) {
          fieldName = parser.getCurrentName();
          if (WRITER_ID.equals(fieldName)) {
            user.setId(parser.getText());
          } else if (!WRITER_AVATAR.equals(fieldName) && !WRITER_NAME.equals(fieldName)) {
            throw new ImportException(ERROR_FIELD_MSG + fieldName);
          }
        }
      } else {
        throw new ImportException(ERROR_FIELD_MSG + fieldName);
      }
    }
    Comment comment = new Comment(new CommentPK(id, componentId), new PublicationPK(resourceId,
        componentId), Integer.valueOf(user.getId()), "", text, creationDate, modificationDate);

    return comment;
  }
}
