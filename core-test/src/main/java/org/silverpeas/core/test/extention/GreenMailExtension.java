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

package org.silverpeas.core.test.extention;

import com.icegreen.greenmail.base.GreenMailOperations;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailProxy;
import com.icegreen.greenmail.util.ServerSetup;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.silverpeas.core.SilverpeasException;

import java.io.IOException;
import java.util.Properties;

/**
 * A replacement of {@link com.icegreen.greenmail.junit.GreenMailRule} to use GreenMail in JUnit 5
 * based unit tests.
 * @author mmoquillon
 */
public class GreenMailExtension
    implements ParameterResolver, BeforeAllCallback, BeforeEachCallback, AfterEachCallback {

  private static final String MAIL_SERVER = "GreenMailServer";

  private ServerSetup[] setups;

  public GreenMailExtension(final ServerSetup... serverSetups) {
    setups = serverSetups;
  }

  public GreenMailExtension() {
    this(ServerSetupTest.ALL);
  }

  @Override
  public void beforeAll(final ExtensionContext context) throws Exception {
    SmtpConfig config = context.getRequiredTestClass().getAnnotation(SmtpConfig.class);
    if (config != null) {
      Properties configuration = new Properties();
      try {
        configuration.load(context.getRequiredTestClass().getResourceAsStream(config.value()));
      } catch (IOException ex) {
        throw new SilverpeasException(ex);
      }
      int port = Integer.parseInt(configuration.getProperty("SMTPPort", "25"));
      setups = new ServerSetup[]{new ServerSetup(port, null, ServerSetup.PROTOCOL_SMTP)};
    }
  }

  @Override
  public boolean supportsParameter(final ParameterContext parameterContext,
      final ExtensionContext extensionContext) {
    return parameterContext.getParameter().getType().equals(GreenMailOperations.class);
  }

  @Override
  public Object resolveParameter(final ParameterContext parameterContext,
      final ExtensionContext extensionContext) {
    return store(extensionContext).get(MAIL_SERVER);
  }

  @Override
  public void beforeEach(final ExtensionContext context) throws Exception {
    final GreenMailServer server = new GreenMailServer(setups);
    store(context).put(MAIL_SERVER, server);
    server.start();
  }

  @Override
  public void afterEach(final ExtensionContext context) throws Exception {
    final GreenMailServer server = store(context).remove(MAIL_SERVER, GreenMailServer.class);
    server.stop();
  }

  private ExtensionContext.Store store(final ExtensionContext context) {
    final ExtensionContext.Namespace storeNs =
        ExtensionContext.Namespace.create(getClass(), context.getRequiredTestMethod());
    return context.getStore(storeNs);
  }

  private class GreenMailServer extends GreenMailProxy {

    private final GreenMail greenMail;

    GreenMailServer(final ServerSetup[] setups) {
      this.greenMail = new GreenMail(setups);
    }

    @Override
    protected GreenMail getGreenMail() {
      return this.greenMail;
    }

  }
}
  