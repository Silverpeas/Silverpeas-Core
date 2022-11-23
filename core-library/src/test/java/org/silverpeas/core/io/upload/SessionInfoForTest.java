package org.silverpeas.core.io.upload;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.security.session.SessionInfo;

public class SessionInfoForTest extends SessionInfo {
  /**
   * Constructs a new instance about a given opened user session.
   * @param sessionId the identifier of the opened session.
   * @param user the user for which a session was opened.
   */
  public SessionInfoForTest(final String sessionId,
      final User user) {
    super(sessionId, user);
  }
}
