/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.admin.domain.driver.googledriver;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.admin.directory.Directory;
import com.google.api.services.admin.directory.DirectoryScopes;
import com.google.api.services.admin.directory.model.User;
import org.silverpeas.core.admin.service.AdminException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author silveryocha
 */
public class GoogleDirectoryRequester {
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  private static final String MY_CUSTOMER = "my_customer";
  private final String serviceAccountUser;
  private final String jsonKeyPath;

  /**
   * Global instance of the scopes required by this quickstart.
   * If modifying these scopes, delete your previously saved tokens/ folder.
   */
  private static final List<String> SCOPES = Collections
      .singletonList(DirectoryScopes.ADMIN_DIRECTORY_USER_READONLY);
  private static final String APPLICATION_NAME = "Silverpeas Google Domain Fetcher";

  GoogleDirectoryRequester(final String serviceAccountUser, final String jsonKeyPath) {
    this.serviceAccountUser = serviceAccountUser;
    this.jsonKeyPath = jsonKeyPath;
  }

  /**
   * Creates an authorized Credential object.
   * @return An authorized Credential object.
   * @throws IOException If the credentials.json file cannot be found.
   * @param httpTransport
   */
  private Credential getServiceAccountCredentials(final HttpTransport httpTransport)
      throws IOException {
    final GoogleCredential credential;
    try (InputStream is = new FileInputStream(jsonKeyPath)) {
      credential = GoogleCredential.fromStream(is);
    }
    return new GoogleCredential.Builder()
        .setTransport(httpTransport)
        .setJsonFactory(JSON_FACTORY)
        .setServiceAccountUser(serviceAccountUser)
        .setServiceAccountId(credential.getServiceAccountId())
        .setServiceAccountScopes(SCOPES)
        .setServiceAccountPrivateKey(credential.getServiceAccountPrivateKey())
        .setServiceAccountPrivateKeyId(credential.getServiceAccountPrivateKeyId())
        .setTokenServerEncodedUrl(credential.getTokenServerEncodedUrl())
        .build();
  }

  private Directory getDirectoryService() throws AdminException {
    try {
      final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
      return new Directory.Builder(httpTransport, JSON_FACTORY,
          getServiceAccountCredentials(httpTransport)).setApplicationName(APPLICATION_NAME).build();
    } catch (Exception e) {
      throw new AdminException(e);
    }
  }

  public List<User> users() throws AdminException {
    try {
      final List<User> users = getDirectoryService().users().list().setCustomer(MY_CUSTOMER)
          .execute().getUsers();
      users.sort(Comparator
          .comparing((User g) -> g.getName().getFamilyName())
          .thenComparing(g -> g.getName().getGivenName()));
      return users;
    } catch (IOException e) {
      throw new AdminException(e);
    }
  }

  public User user(final String id) throws AdminException {
    try {
      return getDirectoryService().users().get(id).execute();
    } catch (IOException e) {
      throw new AdminException(e);
    }
  }
}