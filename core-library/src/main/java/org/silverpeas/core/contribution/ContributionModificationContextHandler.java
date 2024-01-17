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
package org.silverpeas.core.contribution;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.annotation.Technical;
import org.silverpeas.core.util.JSONCodec;
import org.silverpeas.core.util.ServiceProvider;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Optional;

import static org.silverpeas.core.cache.service.CacheServiceProvider.getRequestCacheService;
import static org.silverpeas.core.util.StringUtil.fromBase64;
import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * This class permits to handle a contribution modification context.
 * <p>
 * A such context can give additional information to adopt different kind of behaviors. For example,
 * it can indicates if the user modification is a minor one which permits the contribution services
 * to adopt a different behavior against a normal one.
 * </p>
 * @author silveryocha
 */
@Technical
@Service
public class ContributionModificationContextHandler
    implements ContributionOperationContextPropertyHandler {

  /**
   * HTTP parameter from which the contribution modification context is retrieved.
   */
  private static final String HTTP_PARAM = "CONTRIBUTION_MODIFICATION_CONTEXT";

  private static final String CACHE_KEY =
      ContributionModificationContextHandler.class.getName() + "#CACHE_KEY";

  public static ContributionModificationContextHandler get() {
    return ServiceProvider.getService(ContributionModificationContextHandler.class);
  }

  /**
   * Hidden constructor.
   */
  protected ContributionModificationContextHandler() {
  }

  /**
   * Verifies from a request if it exists a contribution modification context.
   * <p>
   * This context could give additional information to adopt a behavior or an other.
   * </p>
   * @param request the current HTTP request.
   */
  @Override
  public void parseForProperty(HttpServletRequest request) {
    final String parameter = request.getParameter(HTTP_PARAM);
    final String header = request.getHeader(HTTP_PARAM);
    final Context context = getMergedContext(parameter, header);
    getRequestCacheService().getCache().put(CACHE_KEY, context);
  }

  /**
   * Forces the context by indicating a minor modification.
   * <p>
   * This method permits to indicate a such context by bypassing an HTTP request decoding.
   * </p>
   * <p>
   * The context is also registered into a thread cache, like it is done with the HTTP request
   * decoding.
   * </p>
   */
  public void setMinorModification() {
    getOrCreateContext().isMinor = true;
  }

  /**
   * Indicates from current request if the current user made a minor modification.
   * @return true if minor, false otherwise.
   */
  public Optional<Boolean> isMinorModification() {
    return getContext().map(Context::isMinor);
  }

  private Context getMergedContext(final String parameter, final String header) {
    Context fromParameters = decodeContext(parameter);
    Context fromHeaders = decodeContext(header);
    final Context mergedContext = new Context();
    if (fromParameters.isMinor != null) {
      mergedContext.isMinor = fromParameters.isMinor;
    }
    if (mergedContext.isMinor == null || (!mergedContext.isMinor && fromHeaders.isMinor != null)) {
      mergedContext.isMinor = fromHeaders.isMinor;
    }
    return mergedContext;
  }

  private Context decodeContext(final String value) {
    final String decodedValue;
    if (isDefined(value)) {
      if (value.startsWith("{")) {
        decodedValue = value;
      } else {
        decodedValue = new String(fromBase64(value));
      }
    } else {
      decodedValue = "{}";
    }
    return JSONCodec.decode(decodedValue, Context.class);
  }

  private Optional<Context> getContext() {
    return Optional.ofNullable(getRequestCacheService().getCache().get(CACHE_KEY, Context.class));
  }

  private Context getOrCreateContext() {
    return getRequestCacheService().getCache()
        .computeIfAbsent(CACHE_KEY, Context.class, Context::new);
  }

  @XmlRootElement
  @XmlAccessorType(XmlAccessType.PROPERTY)
  @JsonIgnoreProperties(ignoreUnknown = true)
  private static class Context implements Serializable {
    private static final long serialVersionUID = -7623666268435749654L;

    @XmlElement
    private Boolean isMinor = null;

    Boolean isMinor() {
      return isMinor;
    }
  }
}
