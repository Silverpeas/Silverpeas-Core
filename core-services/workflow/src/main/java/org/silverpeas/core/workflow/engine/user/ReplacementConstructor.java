/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

package org.silverpeas.core.workflow.engine.user;

import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.workflow.api.user.Replacement;
import org.silverpeas.core.workflow.api.user.User;

import java.util.Objects;

/**
 * Implementation of the constructor of {@link Replacement} objects.
 * @author mmoquillon
 */
@Service
public class ReplacementConstructor implements Replacement.Constructor {

  private ReplacementImpl replacement;

  public ReplacementConstructor() {
    // for bean injection engine
  }

  @Override
  public Replacement.Constructor between(final User incumbent, final User substitute) {
    replacement = new ReplacementImpl().setIncumbent(incumbent).setSubstitute(substitute);
    return this;
  }

  @Override
  public Replacement.Constructor inWorkflow(final String workflowInstanceId) {
    replacement.setWorkflowId(workflowInstanceId);
    return this;
  }

  @SuppressWarnings("unchecked")
  @Override
  public ReplacementImpl during(final Period period) {
    Objects.requireNonNull(replacement.getIncumbent(),
        "The incumbent in a replacement must be defined");
    Objects.requireNonNull(replacement.getSubstitute(),
        "The substitute in a replacement must be defined");
    StringUtil.requireDefined(replacement.getWorkflowInstanceId(),
        "The workflow instance in a replacement must be defined");
    return replacement.setPeriod(period);
  }
}
