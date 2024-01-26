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
package org.silverpeas.core.web.util.viewgenerator.html.override;

import org.apache.ecs.xhtml.script;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.util.URLUtil;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import static org.silverpeas.core.cache.service.CacheAccessorProvider.getThreadCacheAccessor;
import static org.silverpeas.core.web.util.viewgenerator.html.JavascriptPluginInclusion
    .normalizeWebResourceUrl;

public class LoadScriptTag extends TagSupport {
  private static final long serialVersionUID = 1439996118011433471L;

  private String webContext = URLUtil.getApplicationURL();
  private String src;
  private String jsPromiseScope;
  private String jsPromiseName;

  public void setWebContext(final String webContext) {
    this.webContext = webContext;
  }

  public String getWebContext() {
    return webContext;
  }

  public void setSrc(final String src) {
    this.src = src;
  }

  public String getSrc() {
    return src;
  }

  public String getJsPromiseScope() {
    return jsPromiseScope;
  }

  public void setJsPromiseScope(final String jsPromiseScope) {
    this.jsPromiseScope = jsPromiseScope;
  }

  public String getJsPromiseName() {
    return jsPromiseName;
  }

  public void setJsPromiseName(final String jsPromiseName) {
    this.jsPromiseName = jsPromiseName;
  }

  @Override
  public int doEndTag() throws JspException {
    String source = src.startsWith("/") && !src.startsWith(webContext) ? webContext + src : src;
    script dynamicLoading = new script().setType("text/javascript")
        .addElement(generateDynamicScriptLoading(source, jsPromiseName, jsPromiseScope));
    dynamicLoading.output(pageContext.getOut());
    return EVAL_PAGE;
  }

  /**
   * Centralization of dynamic script instantiation.
   * Even if several calls are done for a same HTML page, the script is loaded one time only.
   * @param src the script src.
   * @param jsPromiseName the name of a promise.
   * @param jsPromiseScope the scope of the promise.
   * @return the loading treatment.
   */
  public static String generateDynamicScriptLoading(final String src, final String jsPromiseName,
      final String jsPromiseScope) {
    String key = "$jsDynamic$script$" + src;
    if (getThreadCacheAccessor().getCache().get(key) == null) {
      getThreadCacheAccessor().getCache().put(key, true);
      if (StringUtil.isNotDefined(jsPromiseName)) {
        return "jQuery.ajax({type:'GET',url:'" + normalizeWebResourceUrl(src) + "',dataType:'script',cache:true});";
      }
      final String scope = StringUtil.isDefined(jsPromiseScope) ? (jsPromiseScope + ".") : "var ";
      return scope + jsPromiseName + "=" +
          "new Promise(function(resolve, reject){" +
          "jQuery.ajax({type:'GET',url:'" + normalizeWebResourceUrl(src) + "',dataType:'script',cache:true,success:function(){resolve();}});" +
          "});";
    } else {
      return "";
    }
  }
}
