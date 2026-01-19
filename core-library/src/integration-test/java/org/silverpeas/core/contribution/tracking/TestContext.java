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
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.core.contribution.tracking;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.cache.service.SessionCacheAccessor;
import org.silverpeas.core.contribution.ContributionModificationContextHandler;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.tracking.Publication.PublicationID;
import org.silverpeas.core.persistence.datasource.model.jpa.InstantAttributeConverter;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.test.stub.ComponentInstImpl;
import org.silverpeas.core.test.stub.StubbedComponentInstanceProvider;
import org.silverpeas.core.test.stub.StubbedUserProvider;
import org.silverpeas.core.util.ServiceProvider;

import java.io.BufferedReader;
import java.security.Principal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.exparity.hamcrest.date.DateMatchers.within;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * A context used in the integration tests on the contribution modification tracking. It provides
 * useful methods to access the business objects related by the tests.
 * @author mmoquillon
 */
public class TestContext {

  static final String TABLE_CREATION_SCRIPT = "create-table.sql";
  static final String DATASET_SQL_SCRIPT = "test-publication-dataset.sql";
  static final String KMELIA_ID = "kmelia200";
  private static final InstantAttributeConverter INSTANT_CONVERTER = new InstantAttributeConverter();

  public TestContext() {
    // the component instances
    StubbedComponentInstanceProvider.addComponentInstance(ComponentInstImpl.builder("100")
        .setName("A kmelia 100")
        .setSpaceId("1")
        .build());
    StubbedComponentInstanceProvider.addComponentInstance(ComponentInstImpl.builder("200")
        .setName("A kmelia 200")
        .setSpaceId("1")
        .build());
    StubbedComponentInstanceProvider.addComponentInstance(ComponentInstImpl.builder("300")
        .setName("A kmelia 300")
        .setSpaceId("1")
        .build());

    // the users
    StubbedUserProvider.addUser("0");
    StubbedUserProvider.addUser("1");
    StubbedUserProvider.addUser("2");
    StubbedUserProvider.addUser("3");
    StubbedUserProvider.addUser("100");
    StubbedUserProvider.addUser("101");
    StubbedUserProvider.addUser("200");
  }

  /**
   * Sets up a user requester of the current test.
   */
  public void setUpUserRequester() {
    SessionCacheAccessor sessionCacheAccessor =
        CacheAccessorProvider.getSessionCacheAccessor();
    User currentUser = User.getById("1");
    sessionCacheAccessor.newSessionCache(currentUser);
  }

  /**
   * Sets in the incoming request the type of the modification that has been done. Without this
   * property, the modification is considered as a simple update, meaning no discrimination is done
   * between a minor and a major update.
   * @param isMinor is the modification a minor one. Otherwise, it is a major one.
   */
  public void setUpModificationType(boolean isMinor) {
    ContributionModificationContextHandler handler =
        ServiceProvider.getService(ContributionModificationContextHandler.class);
    assertThat(handler, notNullValue());
    HttpRequest request = new HttpRequest();
    request.setHeader("{\"isMinor\": " + isMinor + "}");
    handler.parseForProperty(request);
  }

  /**
   * Asserts the specified tracking record matches the given action on the specified publication
   * performed at now.
   * @param record the tracking record
   * @param actionType the type of action
   * @param publication the publication on which the action has been executed
   */
  public void assertThatRecordMatches(TrackingEventRecord record, TrackedActionType actionType,
      Publication publication) {
    assertThat(record, notNullValue());
    assertThat(record.contrib.getLocalId(), is(publication.getIdentifier().getLocalId()));
    assertThat(record.contrib.getType(), is(publication.getContributionType()));
    assertThat(record.contrib.getComponentInstanceId(),
        is(publication.getIdentifier().getComponentInstanceId()));
    assertThat(record.action.getType(), is(actionType));
    assertThat(record.action.getUser(), is(getRelatedUser(publication, actionType)));
    if (actionType == TrackedActionType.INNER_MOVE || actionType == TrackedActionType.OUTER_MOVE) {
      assertThat(record.context, not(emptyString()));
    } else {
      assertThat(record.context, is(emptyString()));
    }
    assertThat(Date.from(record.action.getDateTime().toInstant()),
        within(1, ChronoUnit.MINUTES, new Date()));
  }

  /**
   * Gets from the database the specified publication.
   * @param pubId the unique identifier of the publication. It is expected to be in Kmelia200.
   * @return a {@link Publication} instance.
   */
  public Publication getPublication(final String pubId) {
    var id = new PublicationID(pubId, KMELIA_ID);
    PublicationService service = PublicationService.get();
    return service.getPublication(id);
  }

  /**
   * Gets the last recorded tracking event about a modification of the specified publication.
   * @param pubId the unique identifier of the publication. It is expected to be in Kmelia200.
   * @return a {@link TrackingEventRecord} instance.
   */
  public TrackingEventRecord getLastTrackingEventOn(String pubId) {
    try (Connection connection = DBUtil.openConnection()) {
      List<TrackingEventRecord> results = JdbcSqlQuery.select(
          "id, context, contrib_id, contrib_type, contrib_instanceId, action_type, action_date, " +
              "action_by")
          .from("SB_Contribution_Tracking")
          .where("contrib_id = ?", pubId)
          .and("contrib_instanceId = ?", KMELIA_ID)
          .orderBy("action_date desc")
          .executeWith(connection, row -> {
            TrackingEventRecord record = new TrackingEventRecord();
            record.id = row.getString("id");
            record.context = row.getString("context");
            String contribId = row.getString("contrib_id");
            String contribType = row.getString("contrib_type");
            String contribInstanceId = row.getString("contrib_instanceId");
            TrackedActionType actionType = TrackedActionType.valueOf(row.getString("action_type"));
            User actionUser = User.getById(row.getString("action_by"));
            Instant actionDate = INSTANT_CONVERTER.convertToEntityAttribute(row.getTimestamp("action_date"));
            record.action = new TrackedAction(actionType, actionDate, actionUser);
            record.contrib = ContributionIdentifier.from(contribInstanceId, contribId, contribType);
            return record;
          });
      return results.isEmpty() ? null : results.get(0);
    } catch (SQLException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  private User getRelatedUser(Publication publication, TrackedActionType action) {
    User user = User.getCurrentRequester();
    if (user == null) {
      switch (action) {
        case UPDATE:
        case MINOR_UPDATE:
        case MAJOR_UPDATE:
          user = publication.getLastUpdater();
          if (user == null) {
            user = User.getSystemUser();
          }
          break;
        case CREATION:
          user =
              publication.getCreatorId() == null ? null : User.getById(publication.getCreatorId());
          if (user == null) {
            user = User.getSystemUser();
          }
          break;
        default:
          user = User.getSystemUser();
          break;
      }
    }
    return user;
  }

  public static class TrackingEventRecord {
    public String id;
    public TrackedAction action;
    public ContributionIdentifier contrib;
    public String context;
  }

  private static class HttpRequest implements HttpServletRequest {

    private final Map<String, Object> headers = new HashMap<>();

    protected void setHeader(String value) {
      this.headers.put("CONTRIBUTION_MODIFICATION_CONTEXT", value);
    }

    @Override
    public String getAuthType() {
      return null;
    }

    @Override
    public Cookie[] getCookies() {
      return new Cookie[0];
    }

    @Override
    public long getDateHeader(final String name) {
      try {
        String date = getHeader(name);
        return Long.parseLong(date);
      } catch (NumberFormatException e) {
        return 0;
      }
    }

    @Override
    public String getHeader(final String name) {
      return (String) headers.get(name);
    }

    @Override
    public Enumeration<String> getHeaders(final String name) {
      //noinspection unchecked
      return new HeaderEnumeration((Collection<String>) headers.get(name));
    }

    @Override
    public Enumeration<String> getHeaderNames() {
      return new HeaderEnumeration(headers.keySet());
    }

    @Override
    public int getIntHeader(final String name) {
      try {
        String val = getHeader(name);
        return Integer.parseInt(val);
      } catch (NumberFormatException e) {
        return 0;
      }
    }

    @Override
    public String getMethod() {
      return null;
    }

    @Override
    public String getPathInfo() {
      return null;
    }

    @Override
    public String getPathTranslated() {
      return null;
    }

    @Override
    public String getContextPath() {
      return null;
    }

    @Override
    public String getQueryString() {
      return null;
    }

    @Override
    public String getRemoteUser() {
      return null;
    }

    @Override
    public boolean isUserInRole(final String role) {
      return false;
    }

    @Override
    public Principal getUserPrincipal() {
      return null;
    }

    @Override
    public String getRequestedSessionId() {
      return null;
    }

    @Override
    public String getRequestURI() {
      return null;
    }

    @Override
    public StringBuffer getRequestURL() {
      return null;
    }

    @Override
    public String getServletPath() {
      return null;
    }

    @Override
    public HttpSession getSession(final boolean create) {
      return null;
    }

    @Override
    public HttpSession getSession() {
      return null;
    }

    @Override
    public String changeSessionId() {
      return null;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
      return false;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
      return false;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
      return false;
    }

    @Override
    public boolean authenticate(final HttpServletResponse response) {
      return false;
    }

    @Override
    public void login(final String username, final String password) {

    }

    @Override
    public void logout() {

    }

    @Override
    public Collection<Part> getParts() {
      return null;
    }

    @Override
    public Part getPart(final String name) {
      return null;
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(final Class<T> handlerClass) {
      return null;
    }

    @Override
    public Object getAttribute(final String name) {
      return null;
    }

    @Override
    public Enumeration<String> getAttributeNames() {
      return null;
    }

    @Override
    public String getCharacterEncoding() {
      return null;
    }

    @Override
    public void setCharacterEncoding(final String env) {

    }

    @Override
    public int getContentLength() {
      return 0;
    }

    @Override
    public long getContentLengthLong() {
      return 0;
    }

    @Override
    public String getContentType() {
      return null;
    }

    @Override
    public ServletInputStream getInputStream() {
      return null;
    }

    @Override
    public String getParameter(final String name) {
      return null;
    }

    @Override
    public Enumeration<String> getParameterNames() {
      return null;
    }

    @Override
    public String[] getParameterValues(final String name) {
      return new String[0];
    }

    @Override
    public Map<String, String[]> getParameterMap() {
      return null;
    }

    @Override
    public String getProtocol() {
      return null;
    }

    @Override
    public String getScheme() {
      return null;
    }

    @Override
    public String getServerName() {
      return null;
    }

    @Override
    public int getServerPort() {
      return 0;
    }

    @Override
    public BufferedReader getReader() {
      return null;
    }

    @Override
    public String getRemoteAddr() {
      return null;
    }

    @Override
    public String getRemoteHost() {
      return null;
    }

    @Override
    public void setAttribute(final String name, final Object o) {

    }

    @Override
    public void removeAttribute(final String name) {

    }

    @Override
    public Locale getLocale() {
      return null;
    }

    @Override
    public Enumeration<Locale> getLocales() {
      return null;
    }

    @Override
    public boolean isSecure() {
      return false;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(final String path) {
      return null;
    }

    @Override
    public int getRemotePort() {
      return 0;
    }

    @Override
    public String getLocalName() {
      return null;
    }

    @Override
    public String getLocalAddr() {
      return null;
    }

    @Override
    public int getLocalPort() {
      return 0;
    }

    @Override
    public ServletContext getServletContext() {
      return null;
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
      return null;
    }

    @Override
    public AsyncContext startAsync(final ServletRequest servletRequest,
        final ServletResponse servletResponse) throws IllegalStateException {
      return null;
    }

    @Override
    public boolean isAsyncStarted() {
      return false;
    }

    @Override
    public boolean isAsyncSupported() {
      return false;
    }

    @Override
    public AsyncContext getAsyncContext() {
      return null;
    }

    @Override
    public DispatcherType getDispatcherType() {
      return null;
    }

    @Override
    public String getRequestId() {
      return null;
    }

    @Override
    public String getProtocolRequestId() {
      return null;
    }

    @Override
    public ServletConnection getServletConnection() {
      return null;
    }

    private static class HeaderEnumeration implements Enumeration<String> {
      private final List<String> items;
      private int cursor = -1;

      public HeaderEnumeration(final Collection<String> items) {
        this.items = new ArrayList<>(items);
      }

      @Override
      public boolean hasMoreElements() {
        return ++cursor < items.size();
      }

      @Override
      public String nextElement() {
        return items.get(cursor);
      }
    }
  }
}
