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

package org.silverpeas.core.contribution.template.publication;

import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.backgroundprocess.AbstractBackgroundProcessRequest;
import org.silverpeas.core.backgroundprocess.BackgroundProcessTask;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.record.GenericRecordSetManager;
import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.kernel.logging.SilverLogger;

import java.util.Set;

import static org.silverpeas.kernel.util.StringUtil.isDefined;

/**
 * @author silveryocha
 */
@Service
public class PublicationTemplateIntegrityProcessor implements Initialization {

  @Override
  public void init() {
    BackgroundProcessTask.push(new DeprecatedTemplateCleaner());
  }

  private static class DeprecatedTemplateCleaner extends AbstractBackgroundProcessRequest {

    @Override
    protected void process() {
      try {
        final Set<String> componentIds =
            GenericRecordSetManager.getInstance().getAllComponentIdsOfRecords();
        Transaction.performInOne(() -> {
          componentIds.stream()
              .filter(c -> isDefined(SilverpeasComponentInstance.getComponentName(c)))
              .filter(c -> SilverpeasComponentInstance.getById(c).isEmpty())
              .forEach(c -> PublicationTemplateManager.getInstance().delete(c));
          return null;
        });
      } catch (FormException e) {
        SilverLogger.getLogger(this).error(e);
      }
    }
  }
}
