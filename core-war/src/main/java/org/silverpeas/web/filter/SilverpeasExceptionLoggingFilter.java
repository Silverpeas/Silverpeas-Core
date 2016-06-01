package org.silverpeas.web.filter;

import org.silverpeas.core.util.logging.SilverLogger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * A filter to handle all the exception thrown from the Silverpeas application.
 * @author Yohann Chastagnier
 */
public class SilverpeasExceptionLoggingFilter implements Filter {

  @Override
  public void doFilter(final ServletRequest request, final ServletResponse response,
      final FilterChain chain) throws IOException, ServletException {
    try {
      chain.doFilter(request, response);
    } catch (Exception t) {
      SilverLogger.getLogger("silverpeas.exception.unexpected").error(t.getLocalizedMessage(), t);
      throw t;
    }
  }

  @Override
  public void init(final FilterConfig filterConfig) throws ServletException {
  }

  @Override
  public void destroy() {
  }
}
