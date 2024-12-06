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

package org.silverpeas.core.webapi.documenttemplate;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.documenttemplate.DocumentTemplate;
import org.silverpeas.core.documenttemplate.DocumentTemplateRuntimeException;
import org.silverpeas.core.documenttemplate.DocumentTemplateService;
import org.silverpeas.core.io.upload.UploadedFile;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.kernel.bundle.LocalizationBundle;
import org.silverpeas.core.util.MemoizedSyncSupplier;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.viewer.service.PreviewService;
import org.silverpeas.core.viewer.service.ViewService;
import org.silverpeas.core.viewer.service.ViewerContext;
import org.silverpeas.core.web.mvc.webcomponent.WebMessager;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static javax.ws.rs.core.Response.Status.*;
import static org.apache.commons.io.FileUtils.openInputStream;
import static org.silverpeas.core.documenttemplate.DocumentTemplateSettings.isEnabled;
import static org.silverpeas.kernel.bundle.ResourceLocator.getLocalizationBundle;
import static org.silverpeas.kernel.util.StringUtil.EMPTY;
import static org.silverpeas.kernel.util.StringUtil.isNotDefined;


/**
 * Permits to centralize WEB service processing between {@link DocumentTemplateResource} and the WAR
 * controller.
 * @author silveryocha
 */
@Service
public class DocumentTemplateWebManager {

  @Inject
  private DocumentTemplateService service;

  protected DocumentTemplateWebManager() {
  }

  public static DocumentTemplateWebManager get() {
    return ServiceProvider.getService(DocumentTemplateWebManager.class);
  }

  private final MemoizedSyncSupplier<List<DocumentTemplate>> cachedList =
      new MemoizedSyncSupplier<>(() -> isEnabled() ? service.listAll() : List.of());

  /**
   * Gets the document template data from its identifier.
   * @param id the identifier of a document template.
   * @return a {@link DocumentTemplate} instance or throws exception if not found is unknown.
   */
  public DocumentTemplate getDocumentTemplate(String id) {
    if (isNotDefined(id)) {
      throw new WebApplicationException(NOT_FOUND);
    }
    return service.getById(id).orElseThrow(() -> new WebApplicationException(NOT_FOUND));
  }

  /**
   * Creates a category into Silverpeas's context.
   * @param newDocumentTemplate data of a new document template.
   * @param content the document template content (mandatory).
   */
  public void createDocumentTemplate(DocumentTemplate newDocumentTemplate,
      UploadedFile content) {
    try (final InputStream is = openInputStream(content.getFile())) {
      final DocumentTemplate created = service.put(newDocumentTemplate, is);
      clearCachedList();
      final LocalizationBundle bundle = getBundle();
      getMessager().addSuccess(bundle.getString("documentTemplate.create.success"),
          created.getName(bundle.getLocale().getLanguage()));
    } catch (DocumentTemplateRuntimeException | IOException e) {
      throw new WebApplicationException(e.getMessage(), BAD_REQUEST);
    }
  }

  /**
   * Updates a document template into Silverpeas's context.
   * @param updatedDocumentTemplate data of an updated document template.
   * @param content the document template content (mandatory).
   */
  public void updateDocumentTemplate(DocumentTemplate updatedDocumentTemplate,
      UploadedFile content) {
    try (final InputStream is = content != null ? openInputStream(content.getFile()) : null) {
      final DocumentTemplate previous = service.getById(updatedDocumentTemplate.getId())
          .orElseGet(DocumentTemplate::new);
      final DocumentTemplate updated = service.put(updatedDocumentTemplate, is);
      DisplayI18NHelper.getLanguages().forEach(l -> {
        if (!previous.getName(l).equals(updated.getName(l))) {
          final ViewerContext viewerContext = updated.getViewerContext(l);
          PreviewService.get().removePreview(viewerContext);
          ViewService.get().removeDocumentView(viewerContext);
        }
      });
      clearCachedList();
      final LocalizationBundle bundle = getBundle();
      getMessager().addSuccess(bundle.getString("documentTemplate.update.success"),
          updated.getName(bundle.getLocale().getLanguage()));
    } catch (DocumentTemplateRuntimeException | IOException e) {
      throw new WebApplicationException(e.getMessage(), BAD_REQUEST);
    }
  }

  /**
   * Deletes document templates from their identifier.
   * @param documentTemplateIds list of document template identifier.
   */
  public void deleteDocumentTemplates(final List<String> documentTemplateIds) {
    if (!documentTemplateIds.isEmpty()) {
      final List<DocumentTemplate> documentTemplates = documentTemplateIds.stream()
          .map(this::getDocumentTemplate)
          .collect(Collectors.toList());
      documentTemplates.forEach(service::remove);
      clearCachedList();
      final LocalizationBundle bundle = getBundle();
      if (documentTemplateIds.size() == 1) {
        getMessager().addSuccess(bundle.getString("documentTemplate.delete.success"),
            documentTemplates.stream()
                .map(t -> t.getName(bundle.getLocale().getLanguage()))
                .findFirst()
                .orElse(EMPTY));
      } else {
        getMessager().addSuccess(bundle.getString("documentTemplates.delete.success"));
      }
    }
  }

  /**
   * Sorts the document templates against the given sorted list of document template identifier.
   * @param sortedDocumentTemplateIds list of identifier as string.
   */
  public void sortDocumentTemplates(final List<String> sortedDocumentTemplateIds) {
    final AtomicInteger index = new AtomicInteger(0);
    final Map<String, Integer> newPositions = sortedDocumentTemplateIds.stream()
        .collect(Collectors.toMap(i -> i, i -> index.getAndIncrement()));
    clearCachedList();
    final List<DocumentTemplate> updated = getAllDocumentTemplates().stream().filter(t -> {
      final Integer newPosition = newPositions.get(t.getId());
      if (newPosition == null) {
        getMessager().addInfo(getBundle().getString("documentTemplates.list.changed.already"));
        throw new WebApplicationException(CONFLICT);
      }
      if (!newPosition.equals(t.getPosition())) {
        t.setPosition(newPosition);
        return true;
      }
      return false;
    }).collect(Collectors.toList());
    if (!updated.isEmpty()) {
      clearCachedList();
      updated.forEach(t -> service.put(t, null));
      getMessager().addSuccess(getBundle().getString("documentTemplates.sort.success"));
    }
  }

  /**
   * Clears the cached list of document template.
   */
  public void clearCachedList() {
    cachedList.clear();
  }

  /**
   * Gets all the sorted list of document templates.
   * <p>
   *   No filtering rule is performed.
   * </p>
   * @return a list of {@link DocumentTemplate} instance.
   */
  public List<DocumentTemplate> getAllDocumentTemplates() {
    return cachedList.get();
  }

  /**
   * Indicates if it exists at least one document template.
   * @return true if exists, false otherwise.
   */
  public boolean existsDocumentTemplate() {
    return !cachedList.get().isEmpty();
  }

  /**
   * Gets the common calendar bundle according to the given locale.
   * @return a localized bundle.
   */
  private LocalizationBundle getBundle() {
    User owner = User.getCurrentRequester();
    String userLanguage = owner.getUserPreferences().getLanguage();
    return getLocalizationBundle("org.silverpeas.documentTemplate.multilang.documentTemplate",
        userLanguage);
  }

  private WebMessager getMessager() {
    return WebMessager.getInstance();
  }
}
