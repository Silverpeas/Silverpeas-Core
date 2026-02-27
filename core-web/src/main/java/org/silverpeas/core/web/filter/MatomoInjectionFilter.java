/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.core.web.filter;

import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.template.SilverpeasTemplates;
import org.silverpeas.core.web.look.LookHelper;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;
import org.silverpeas.kernel.util.StringUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

/**
 * Servlet {@link Filter} responsible for injecting the Matomo tracking script
 * into generated JSP pages.
 * <p>
 * The script is dynamically built using a Silverpeas template and enriched with
 * contextual information such as the current user, space and component identifiers.
 * </p>
 * <p>
 * The injection is performed just before the closing {@code </body>} tag of the HTML response.
 * </p>
 */
public class MatomoInjectionFilter implements Filter {


    // HTML closing tag used as injection point.
    public static final String SCRIPT = "</script>";

    // Configuration settings related to Matomo integration.
    private SettingBundle settings;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
        this.settings =
                ResourceLocator.getSettingBundle("org.silverpeas.statistics.settings.matomo");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // If Matomo tracking is disabled, continue without wrapping the response.
        if (!settings.getBoolean("matomo.enable")) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpReq = (HttpServletRequest) request;
        String path = httpReq.getRequestURI();
        String bodyPartJSP = settings.getString("bodypart.jsp");

        // Only process JSP pages and ignore specific technical pages.

        TrackableContent content = getTrackageContent(httpReq);
        if (!path.contains(bodyPartJSP)) {
            if (!content.isContent()) {
                chain.doFilter(request, response);
                return;
            }
        }

        // Wrap the response to capture the generated HTML content.
        CharResponseWrapper responseWrapper = new CharResponseWrapper((HttpServletResponse) response);
        chain.doFilter(request, responseWrapper);

        // Inject the Matomo script just before the closing </body> tag if present.
        String originalContent = responseWrapper.toString();
        if (originalContent.contains(SCRIPT)) {
            String matomoScript = buildMatomoScript((HttpServletRequest) request, content);
            originalContent = originalContent.replace(SCRIPT, SCRIPT + matomoScript);
        }

        // Write the modified content back to the original response output stream.
        byte[] bytes = originalContent.getBytes(response.getCharacterEncoding());
        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);
    }

    private TrackableContent getTrackageContent(HttpServletRequest httpReq) {
        String path = httpReq.getRequestURI();
        TrackableContent content = new TrackableContent();
        content.setContent(false);

        if (path.contains("ViewPublication")) {
            content.setContentType("Publication");
            content.setContent(true);
            content.setContentId(httpReq.getParameter("PubId"));
            PublicationDetail pub = PublicationService.get().getDetail(new PublicationPK(content.getContentId()));
            content.setContentName(pub.getTitle());
            content.setPermalink(pub.getPermalink());
        } else if (path.contains("/Rquickinfo/") && path.contains("/View")) {
            content.setContentType("News");
            content.setContent(true);
            content.setContentId(httpReq.getParameter("Id"));
            // TODO : set name and permalink if move ton Silverpeas-Component
        }

        return content;
    }

    private String buildMatomoScript(HttpServletRequest request, TrackableContent content) {

        String userId = getCurrentUserId(request);
        SilverpeasTemplate template = SilverpeasTemplates.createSilverpeasTemplateOnCore("statistics");
        template.setAttribute("matomoUrl", settings.getString("matomo.url"));
        template.setAttribute("siteId", settings.getString("matomo.siteId"));

        if (content.isContent()) {
            template.setAttribute("space", "");
            template.setAttribute("component", "");
            template.setAttribute("userId", userId);
            template.setAttribute("content", content.getContentType() + "_" + content.getContentName() + "_" + content.getContentId());
            template.setAttribute("virtualPage", content.getPermalink());

            String script = template.applyFileTemplate("matomo");
            script = script.replace("<script>", "<script>if (!document.body.dataset.matomoExecuted) {document.body.dataset.matomoExecuted = 'true';");
            script = script.replace("</script>", "}</script>");
            return script;
        } else {
            String spaceId = request.getParameter("SpaceId");
            String componentId = request.getParameter("ComponentId");
            String spaceName = "";
            if (StringUtil.isDefined(spaceId)) {
                try {
                    spaceName = Administration.get().getSpaceInstLightById(spaceId).getName();
                } catch (Exception e) {
                }
            }
            template.setAttribute("space", spaceName + "_" + componentId);
            String componentName = "";
            if (StringUtil.isDefined(componentId)) {
                try {
                    componentName = Administration.get().getComponentInstLight(componentId).getName();
                } catch (Exception e) {
                }
            }
            template.setAttribute("component", componentName + "_" + componentId);
            template.setAttribute("userId", userId);
            template.setAttribute("content", "");

            if (StringUtil.isDefined(componentId)) {
                template.setAttribute("virtualPage", "/silverpeas/Component/" + componentId);
            } else if (StringUtil.isDefined(spaceId)) {
                template.setAttribute("virtualPage", "/silverpeas/Space/" + componentId);
            }
                return template.applyFileTemplate("matomo");
        }
    }

    public String getCurrentUserId(HttpServletRequest request) {
        String userId = "";
        LookHelper helper = getLookHelper(request);
        if (helper != null) userId = helper.getUserId();
        return userId;
    }
    private LookHelper getLookHelper(HttpServletRequest request) {
        if (request == null) return null;
        HttpSession session = request.getSession(false);
        if (session == null) return null;
        Object attr = session.getAttribute("Silverpeas_LookHelper");
        if (attr == null) return  null;
        return (LookHelper) attr;
    }

    /**
     * Wrapper used to capture the response output as a character stream
     * in order to modify it before sending it to the client.
     */
    private static class CharResponseWrapper extends HttpServletResponseWrapper {
        private final CharArrayWriter charWriter = new CharArrayWriter();
        private PrintWriter writer;

        public CharResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public PrintWriter getWriter() {
            if (writer == null) {
                writer = new PrintWriter(charWriter);
            }
            return writer;
        }

        @Override
        public String toString() {
            return charWriter.toString();
        }
    }

    private class TrackableContent {
        private boolean content;
        private String contentId;
        private String contentName;
        private String contentType;

        private String permalink;

        public boolean isContent() {
            return content;
        }

        public void setContent(boolean content) {
            this.content = content;
        }

        public String getContentId() {
            return contentId;
        }

        public void setContentId(String contentId) {
            this.contentId = contentId;
        }

        public String getContentName() {
            return contentName;
        }

        public void setContentName(String contentName) {
            this.contentName = contentName;
        }

        public String getContentType() {
            return contentType;
        }

        public String getPermalink() {
            return permalink;
        }

        public void setPermalink(String permalink) {
            this.permalink = permalink;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }
    }
}
