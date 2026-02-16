package org.silverpeas.core.web.filter;

import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.template.SilverpeasTemplates;
import org.silverpeas.core.web.look.LookHelper;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class MatomoInjectionFilter implements Filter {

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

        if (!settings.getBoolean("matomo.enable")) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpResp = (HttpServletResponse) response;

       String path = httpReq.getRequestURI();

        if (!path.contains(".jsp") || path.endsWith("Idle.jsp")) {
            chain.doFilter(request, response);
            return;
        }

        CharResponseWrapper responseWrapper = new CharResponseWrapper((HttpServletResponse) response);
        chain.doFilter(request, responseWrapper);

        String originalContent = responseWrapper.toString();
        if (originalContent.contains("</body>")) {
            String matomoScript = buildMatomoScript((HttpServletRequest) request);
            originalContent = originalContent.replace("</body>", matomoScript + "</body>");
        }

        byte[] bytes = originalContent.getBytes(response.getCharacterEncoding());
        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);
    }

    private String buildMatomoScript(HttpServletRequest request) {

        String userId = getCurrentUserId(request);
        String spaceId = getCurrentSpaceId(request);
        String componentId = getCurrentComponentId(request);

        SilverpeasTemplate template = SilverpeasTemplates.createSilverpeasTemplateOnCore("statistics");
        template.setAttribute("spaceId", spaceId);
        template.setAttribute("componentId", componentId);
        template.setAttribute("userId", userId);
        template.setAttribute("matomoUrl", settings.getString("matomo.url"));
        template.setAttribute("siteId", settings.getString("matomo.siteId"));
        return template.applyFileTemplate("matomo");
    }

    public String getCurrentUserId(HttpServletRequest request) {
        String userId = "";
        LookHelper helper = getLookHelper(request);
        if (helper != null) userId = helper.getUserId();
        return userId;
    }

    public String getCurrentSpaceId(HttpServletRequest request) {
        String spaceId = "";
        LookHelper helper = getLookHelper(request);
        if (helper != null) spaceId = helper.getSpaceId();
        return spaceId;
    }

    private LookHelper getLookHelper(HttpServletRequest request) {
        if (request == null) return null;
        HttpSession session = request.getSession(false);
        if (session == null) return null;
        Object attr = session.getAttribute("Silverpeas_LookHelper");
        if (attr == null) return  null;
        return (LookHelper) attr;
    }

    public String getCurrentComponentId(HttpServletRequest request) {
        String componentId = "";
        LookHelper helper = getLookHelper(request);
        if (helper != null) componentId = helper.getSpaceId();
        return componentId;
    }

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

}
