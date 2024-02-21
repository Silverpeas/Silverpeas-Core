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

package org.silverpeas.core.admin.domain.driver.googledriver;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.directory.Directory;
import com.google.api.services.directory.DirectoryScopes;
import com.google.api.services.directory.model.User;
import com.google.api.services.directory.model.Users;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.kernel.logging.SilverLogger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author silveryocha
 */
public class GoogleDirectoryRequester {
  private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
  private static final int QUERY_MAX_RESULTS = 500;
  private static final String MY_CUSTOMER = "my_customer";
  private final String serviceAccountUser;
  private final String jsonKeyPath;
  private final String filter;

  /**
   * Global instance of the scopes required by this quickstart.
   * If modifying these scopes, delete your previously saved tokens/ folder.
   */
  private static final List<String> SCOPES = Collections
      .singletonList(DirectoryScopes.ADMIN_DIRECTORY_USER_READONLY);
  private static final String APPLICATION_NAME = "Silverpeas Google Domain Fetcher";

  GoogleDirectoryRequester(final String serviceAccountUser, final String jsonKeyPath,
      final String filter) {
    this.serviceAccountUser = serviceAccountUser;
    this.jsonKeyPath = jsonKeyPath;
    this.filter = filter;
  }

  /**
   * Creates an authorized Credential object.
   * @return An authorized Credential object.
   * @throws IOException If the credentials.json file cannot be found.
   */
  private GoogleCredentials getGoogleCredentials()
      throws IOException {
    final ServiceAccountCredentials credentials;
    try (InputStream is = new FileInputStream(jsonKeyPath)) {
      credentials = ServiceAccountCredentials.fromStream(is);
    }
    return credentials.toBuilder()
        .setScopes(SCOPES)
        .setServiceAccountUser(serviceAccountUser)
        .build();
  }

  private Directory getDirectoryService() throws AdminException {
    try {
      final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
      final HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(getGoogleCredentials());
      return new Directory.Builder(httpTransport, JSON_FACTORY, requestInitializer)
          .setApplicationName(APPLICATION_NAME)
          .build();
    } catch (Exception e) {
      throw new AdminException(e);
    }
  }

  public List<User> users() throws AdminException {
    try {
      List<User> result = new LinkedList<>();
      final long start = System.currentTimeMillis();
      final Directory.Users.List users = getDirectoryService().users().list()
          .setMaxResults(QUERY_MAX_RESULTS).setCustomer(MY_CUSTOMER)
          .setProjection("full");
      String pageToken = null;
      while (true) {
        final Users currentUsers = users.setPageToken(pageToken).execute();
        pageToken = currentUsers.getNextPageToken();
        final List<User> currentResult = currentUsers.getUsers();
        result.addAll(currentResult);
        if (currentResult.size() < QUERY_MAX_RESULTS || pageToken == null) {
          break;
        }
      }
      result = applyFilter(result);
      result.sort(Comparator
          .comparing((User g) -> g.getName().getFamilyName().toLowerCase())
          .thenComparing(g -> g.getName().getGivenName().toLowerCase()));
      final long end = System.currentTimeMillis();
      SilverLogger.getLogger(this).debug(() -> MessageFormat
          .format("Getting accounts in {0}", DurationFormatUtils.formatDurationHMS(end - start)));
      return result;
    } catch (IOException e) {
      throw new AdminException(e);
    }
  }

  private List<User> applyFilter(final List<User> result) {
    return new GoogleUserFilter<>(result, filter).apply();
  }

  public User user(final String id) throws AdminException {
    final long start = System.currentTimeMillis();
    try {
      return getDirectoryService().users().get(id).execute();
    } catch (IOException e) {
      throw new AdminException(e);
    } finally {
      final long end = System.currentTimeMillis();
      SilverLogger.getLogger(this).debug(() -> MessageFormat
          .format("Getting account {0} in {1}", id,
              DurationFormatUtils.formatDurationHMS(end - start)));
    }
  }
}