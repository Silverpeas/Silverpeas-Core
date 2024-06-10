/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.chat.servers;

import com.google.api.client.util.SslUtils;
import org.silverpeas.core.chat.ChatServerException;
import org.silverpeas.core.chat.ChatSettings;
import org.silverpeas.core.util.JSONCodec;
import org.silverpeas.core.util.JSONCodec.JSONObject;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.GeneralSecurityException;
import java.util.function.UnaryOperator;

/**
 * An HTTP requester. It wraps the HTTP mechanism to communicate with a REST resource.
 * {@link HttpRequester} instances are heavy-weight objects. Initialization as well as disposal
 * may be a rather expensive operation. It is therefore advised to construct only
 * a small number of {@link HttpRequester} instances in the application. Thoses instances
 * must be {@link #close() properly closed} before being disposed to avoid leaking
 * resources.
 * @author mmoquillon
 */
public class HttpRequester implements AutoCloseable {

  private static final String USER_AGENT = "Silverpeas chat client";

  /**
   * The status code for a successful request.
   */
  public static final int STATUS_OK = 200;

  /**
   * The status code for a forbidden request.
   */
  public static final int STATUS_FORBIDDEN = 403;

  /**
   * The status code for an unauthorized request.
   */
  public static final int STATUS_UNAUTHORIZED = 401;

  /**
   * The status code for a request resulting to a conflict in the server. For example, creating a
   * resource that already exists.
   */
  public static final int STATUS_CONFLICT = 409;

  /**
   * The status code for a request resulting to the successful creation of a resource in the server.
   */
  public static final int STATUS_CREATED = 201;

  private final Client client;
  private Invocation.Builder builder;
  private final String token;

  /**
   * Constructs a new {@link HttpRequester} with the specified authentication token that will used
   * to identify and to authorize the requester to perform some tasks in the remote resource.
   * @param authenticationToken an authentication token that was provided by the REST service to
   * access its API.
   */
  public HttpRequester(final String authenticationToken) {
    try {
      this.client = ChatSettings.get().isChatServerSafeUrl()
          ? ClientBuilder.newBuilder().sslContext(SslUtils.trustAllSSLContext()).build()
          : ClientBuilder.newClient();
    } catch (GeneralSecurityException e) {
      throw new ChatServerException(e);
    }
    this.token = authenticationToken;
  }

  /**
   * Sets the specified URL of the resource to request. This method must be invoked before any
   * call to other HTTP methods.
   * @param url the URL of a REST API.
   * @param path optionally the path from the previous URL of the part of the API to request.
   * @return itself.
   */
  public HttpRequester at(String url, String... path) {
    WebTarget target = client.target(url);
    for (String aPath : path) {
      target = target.path(aPath);
    }
    builder = target.request(MediaType.APPLICATION_JSON_TYPE)
        .acceptEncoding("UTF-8")
        .header("User-Agent", USER_AGENT)
        .header("Authorization", this.token);
    return this;
  }

  /**
   * Sets an HTTP header.
   * @param name the HTTP header name.
   * @param value the value of the HTTP header.
   * @return itself.
   */
  public HttpRequester header(String name, String value) {
    builder.header(name, value);
    return this;
  }

  /**
   * Closes this {@link HttpRequester}. It frees all the resources used to perform HTTP requests.
   * So, once this method is called, this instance cannot be used again to send HTTP requests.
   * @throws Exception if the {@link HttpRequester} cannot be closed.
   */
  @Override
  public void close() throws Exception {
    client.close();
  }

  /**
   * Posts to resource referred by the set URL the data that are generated by the specified
   * builder.
   * @param entityBuilder a builder of data encoded in JSON.
   * @return the response to the POST HTTP request.
   */
  public Response post(UnaryOperator<JSONObject> entityBuilder) {
    return builder.post(
        Entity.entity(JSONCodec.encodeObject(entityBuilder), MediaType.APPLICATION_JSON_TYPE));
  }

  /**
   * Deletes the resource referred by the set URL.
   * @return the response to the DELETE HTTP request.
   */
  public Response delete() {
    return builder.delete();
  }

  /**
   * Sends a HEAD HTTP request to the set URL in order to obtain some information about the remote
   * Web resource.
   * @return the response to the HEAD HTTP request.
   */
  public Response head() {
    return builder.head();
  }
}

