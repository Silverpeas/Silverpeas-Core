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
package org.silverpeas.web.pdc;

import org.apache.commons.fileupload.FileItem;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.content.form.*;
import org.silverpeas.core.contribution.content.form.form.XmlSearchForm;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateException;
import org.silverpeas.core.index.indexing.model.FieldDescription;
import org.silverpeas.core.index.search.model.QueryDescription;
import org.silverpeas.core.pdc.form.fieldtype.PdcField;
import org.silverpeas.core.util.file.FileUploadUtil;
import org.silverpeas.kernel.annotation.NonNull;
import org.silverpeas.kernel.annotation.Nullable;
import org.silverpeas.kernel.util.StringUtil;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class QueryParameters implements java.io.Serializable {

  public static final String PARAM_FOLDER = "folderSearch";

  private static final long serialVersionUID = -5191736720955151540L;
  private String keywords = null;
  private String spaceId = null;
  private String instanceId = null;
  private String creatorId = null;
  private LocalDate afterdate = null;
  private LocalDate beforedate = null;
  private LocalDate afterupdatedate = null;
  private LocalDate beforeupdatedate = null;
  private Map<String, String> xmlQuery = null;
  private String folder = null;

  // attributes below are used only to display info in the search page
  private UserDetail creatorDetail = null;

  public void clear() {
    this.keywords = null;
    this.spaceId = null;
    this.instanceId = null;
    this.creatorId = null;
    this.afterdate = null;
    this.beforedate = null;
    this.afterupdatedate = null;
    this.beforeupdatedate = null;
    this.creatorDetail = null;
    this.xmlQuery = null;
  }

  public String getKeywords() {
    return keywords;
  }

  public void setKeywords(String keywords) {
    this.keywords = keywords;
  }

  public String getSpaceId() {
    return spaceId;
  }

  /**
   * By using this method the spaceId filter is set.
   * The instanceId (if any) is cleared.
   * @param spaceId the unique identifier of a space
   */
  public void setSpaceId(String spaceId) {
    setSpaceIdAndInstanceId(spaceId, null);
  }

  public String getInstanceId() {
    return instanceId;
  }

  /**
   * Setting an instanceId while the spaceId is not defined makes no sense here.
   * That's why a spaceId must be passed to the method, if it is null, empty or "*",
   * then no instanceId is set.
   * @param spaceId the unique identifier of a space
   * @param instanceId the unique identifier of a component instance
   */
  public void setSpaceIdAndInstanceId(String spaceId, String instanceId) {
    this.spaceId = null;
    this.instanceId = null;
    if (StringUtil.isDefined(spaceId) && !"*".equals(spaceId)) {
      this.spaceId = spaceId;
      if (StringUtil.isDefined(instanceId) && !"*".equals(instanceId)) {
        this.instanceId = instanceId;
      }
    }
  }

  public String getCreatorId() {
    return creatorId;
  }

  public void setCreatorId(String creatorId) {
    if (!StringUtil.isDefined(creatorId) || "*".equals(creatorId)) {
      this.creatorId = null;
      this.creatorDetail = null;
    } else {
      this.creatorId = creatorId;
    }
  }

  public LocalDate getAfterDate() {
    return afterdate;
  }

  public void setAfterDate(LocalDate afterdate) {
    this.afterdate = afterdate;
  }

  public LocalDate getBeforeDate() {
    return beforedate;
  }

  public void setBeforeDate(LocalDate beforedate) {
    this.beforedate = beforedate;
  }

  public LocalDate getAfterUpdateDate() {
    return afterupdatedate;
  }

  public void setAfterUpdateDate(LocalDate afterdate) {
    this.afterupdatedate = afterdate;
  }

  public LocalDate getBeforeUpdateDate() {
    return beforeupdatedate;
  }

  public void setBeforeUpdateDate(LocalDate beforedate) {
    this.beforeupdatedate = beforedate;
  }

  public void addXmlSubQuery(String field, String query) {
    if (xmlQuery == null) {
      xmlQuery = new HashMap<>();
    }

    xmlQuery.put(field, query);
  }

  public void clearXmlQuery() {
    xmlQuery = null;
  }

  /**
   * Sets this query parameters by using the search form of the specified publication template and
   * from the given data to search.
   * <p>
   * A data record is got from the specified template and then filled with the file items
   * provided by the user. Only the items matching the field of the search form are taken into
   * account to fill the data record. Each field in the data record is named by their counterpart
   * in the search form and prefixed by the specified namespace (or by the template filename if it's
   * not set). This query parameters is then set by invoking the
   * {@link QueryParameters#addXmlSubQuery(String, String)} method for each field value of the data
   * record.
   * </p>
   * @param template a publication template providing the search form to use to define the data
   * to query
   * @param fieldsNamespace the namespace of the fields in the search form. The namespace is used
   * to distinguish the fields of similar name between different forms. By default, if not set, the
   * template filename is used as namespace.
   * @param items a list of data to search uploaded by the user as file items.
   * @param context the web context within which the search is asked by the user.
   * @return a record of data from which the search has to be performed.
   * @throws FormException if an error occurs while getting the search form template.
   * @throws PublicationTemplateException if an error occurs while getting the search form.
   */
  public DataRecord setByXmlSearchForm(@NonNull PublicationTemplate template,
      @Nullable String fieldsNamespace,
      @NonNull List<FileItem> items,
      PagesContext context)
      throws FormException, PublicationTemplateException {
    Objects.requireNonNull(template);

    RecordTemplate searchTemplate = template.getSearchTemplate();
    DataRecord data = searchTemplate.getEmptyRecord();
    XmlSearchForm searchForm = (XmlSearchForm) template.getSearchForm();
    searchForm.update(items, data, context);

    // build the xmlSubQuery according to the data record object
    String namespace = StringUtil.isDefined(fieldsNamespace) ? fieldsNamespace :
        template.getFileName();
    String templateName = namespace.substring(0, namespace.lastIndexOf('.'));
    if (data != null) {
      for (String fieldName : data.getFieldNames()) {
        Field field = data.getField(fieldName);
        String fieldValue = field.getStringValue();
        if (fieldValue != null && !fieldValue.trim().isEmpty()) {
          String fieldQuery = fieldValue.trim();
          if (fieldValue.contains("##")) {
            String operator = FileUploadUtil.getParameter(items,fieldName+"Operator");
            context.setSearchOperator(fieldName, operator);
            fieldQuery = fieldQuery.replace("##", " "+operator+" ");
          } else if (field instanceof PdcField) {
            // search on the PdC by a form: in this case the search will be performed against the
            // index and as such, we need to use a regexp to select all the resources having at least
            // the queried position(s) on the PdC
            fieldQuery = "Regexp:([0-9,;]+\\.)*" + fieldQuery + "(\\.[0-9,;]+)*";
          }
          addXmlSubQuery(templateName + "$$" + fieldName, fieldQuery);
        }
      }
    }
    return data;
  }

  public QueryDescription getQueryDescription(String searchingUser, String searchingLanguage) {
    QueryDescription query = new QueryDescription(getKeywords());
    query.setSearchingUser(searchingUser);
    query.setRequestedLanguage(searchingLanguage);

    if (StringUtil.isDefined(getCreatorId())) {
      query.setRequestedAuthor(getCreatorId());
    } else {
      query.setRequestedAuthor(null);
    }

    query.setRequestedCreatedAfter(getAfterDate());
    query.setRequestedCreatedBefore(getBeforeDate());
    query.setRequestedUpdatedAfter(getAfterUpdateDate());
    query.setRequestedUpdatedBefore(getBeforeUpdateDate());

    if (xmlQuery != null) {
      for (var q : xmlQuery.entrySet()) {
        query.addFieldQuery(new FieldDescription(q.getKey(), q.getValue(), searchingLanguage));
      }
    }

    return query;
  }

  public void setCreatorDetail(UserDetail userDetail) {
    this.creatorDetail = userDetail;
  }

  public UserDetail getCreatorDetail() {
    return this.creatorDetail;
  }

  public boolean isDefined() {
    return StringUtil.isDefined(keywords) || afterdate != null || beforedate != null || StringUtil
        .isDefined(creatorId);
  }

  public void setFolder(String folder) {
    this.folder = folder;
  }

  public String getFolder() {
    return folder;
  }
}