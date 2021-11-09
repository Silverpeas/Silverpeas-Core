/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.ddwe;

import org.silverpeas.core.ApplicationService;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.content.ddwe.DragAndDropWbeFile;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.model.WysiwygContent;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.wbe.WbeEdition;
import org.silverpeas.core.wbe.WbeFile;
import org.silverpeas.core.wbe.WbeHostManager;
import org.silverpeas.core.wbe.WbeUser;
import org.silverpeas.core.web.ddwe.DragAndDropEditorConfig;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentInstanceRoutingMapProviderByInstance;
import org.silverpeas.core.web.mvc.webcomponent.annotation.Homepage;
import org.silverpeas.core.web.mvc.webcomponent.annotation.RedirectToInternalJsp;
import org.silverpeas.core.web.mvc.webcomponent.annotation.WebComponentController;

import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.text.MessageFormat.format;
import static java.util.function.Predicate.not;
import static org.silverpeas.core.util.StringUtil.EMPTY;
import static org.silverpeas.core.wbe.WbeLogger.logger;

/**
 * Handles the Drag&Drop Web Editor.
 * @author silveryocha
 */
@WebComponentController(DragAndDropWebEditorController.WBE_COMPONENT_NAME)
public class DragAndDropWebEditorController extends
    org.silverpeas.core.web.mvc.webcomponent.WebComponentController<DragAndDropWebEditorRequestContext> {

  public static final String WBE_COMPONENT_NAME = "ddwe";

  public DragAndDropWebEditorController(final MainSessionController controller, final ComponentContext context) {
    super(controller, context, "org.silverpeas.ddwe.multilang.ddwe", null, "org.silverpeas.ddwe.ddweSettings");
  }

  @Override
  protected void onInstantiation(final DragAndDropWebEditorRequestContext context) {
    // nothing to do
  }

  @Override
  public String getComponentName() {
    return WBE_COMPONENT_NAME;
  }

  @GET
  @Path("Main")
  @Homepage
  @RedirectToInternalJsp("editor.jsp")
  public void home(DragAndDropWebEditorRequestContext context) {
    process(context, e -> {
      context.getRequest().setAttribute("DdweUser", e.getUser());
      context.getRequest().setAttribute("DdweFile", e.getFile());
      prepareConnectorButtons(context);
      prepareBrowseBarPath(context);
      getTemporaryContent(e).getSimpleContent()
          .ifPresent(c -> context.getRequest().setAttribute("currentFileContent", c));
      final Set<String> currentEditorIds = Stream.concat(
              WbeHostManager.get().getEditorsOfFile(e.getFile()).stream(), Stream.of(e.getUser()))
          .map(WbeUser::getId)
          .collect(Collectors.toSet());
      WbeHostManager.get().notifyEditionWith(e.getFile(), currentEditorIds);
      return null;
    });
  }

  @POST
  @Path("rstTmpContent")
  @Produces(MediaType.APPLICATION_JSON)
  public String resetTemporaryContent(final DragAndDropWebEditorRequestContext context) {
    return process(context, e -> {
      e.getFile().resetTemporaryContent();
      return EMPTY;
    });
  }

  @POST
  @Path("store")
  @Produces(MediaType.APPLICATION_JSON)
  public String store(final DragAndDropWebEditorRequestContext context) {
    return process(context, e -> {
      try {
        e.getFile().updateFrom(context.getRequest().getInputStream());
      } catch (IOException ex) {
        throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
      }
      return EMPTY;
    });
  }

  @GET
  @Path("load")
  @Produces(MediaType.APPLICATION_JSON)
  public String load(final DragAndDropWebEditorRequestContext context) {
    return process(context, e -> {
      if (context.isLoadFromEditorInitialization()) {
        final DragAndDropEditorContent finalContent = getFinalContent(e);
        final DragAndDropEditorContent tmpContent = getTemporaryContent(e);
        if (!finalContent.equals(tmpContent)) {
          finalContent.setTemporaryInlinedHtml(tmpContent.getInlinedHtml());
        }
        return finalContent.getEncodedJson();
      } else {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
          e.getFile().loadInto(baos);
          return baos.toString();
        } catch (IOException ioException) {
          throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }
      }
    });
  }

  @GET
  @Path("result")
  @RedirectToInternalJsp("result.jsp")
  public void result(final DragAndDropWebEditorRequestContext context) {
    process(context, e -> {
      final String currentFileContent = getTemporaryContent(e).getSimpleContent()
          .orElseGet(() -> getTemporaryContent(e).getInlinedHtml());
      final WysiwygContent wysiwygContent = new WysiwygContent(null, currentFileContent);
      context.getRequest().setAttribute("result", wysiwygContent.getRenderer().renderView());
      return null;
    });
  }

  private void prepareConnectorButtons(final DragAndDropWebEditorRequestContext context) {
    context.getWbeEdition()
        .map(WbeEdition::getConfiguration)
        .flatMap(DragAndDropEditorConfig::getFrom)
        .ifPresent(c -> {
          context.getRequest().setAttribute("validateUrl", c.getValidateUrl());
          context.getRequest().setAttribute("cancelUrl", c.getCancelUrl());
        });
  }

  private void prepareBrowseBarPath(final DragAndDropWebEditorRequestContext context) {
    context.getWbeEdition()
        .map(WbeEdition::getConfiguration)
        .flatMap(DragAndDropEditorConfig::getFrom)
        .map(DragAndDropEditorConfig::getManualBrowseBarElements)
        .filter(not(List::isEmpty))
        .ifPresentOrElse(l -> context.getRequest()
            .setAttribute("browseBarPath", l.stream()
                .map(el -> Pair.of(el.getLabel(), el.getLink()))
                .collect(Collectors.toList())), () -> context.getWbeEdition()
            .map(WbeEdition::getFile)
            .map(DragAndDropWbeFile.class::cast)
            .flatMap(DragAndDropWbeFile::linkedToContribution)
            .flatMap(i -> ApplicationService.getInstance(i.getComponentInstanceId()).getContributionById(i))
            .flatMap(c -> ((Contribution) c).getResourcePath())
            .ifPresent(p -> context.getRequest().setAttribute("browseBarPath", p.stream().map(i -> {
              final ContributionIdentifier identifier = i.getIdentifier();
              final URI permalink = ComponentInstanceRoutingMapProviderByInstance.get()
                  .getByInstanceId(identifier.getComponentInstanceId())
                  .relativeToSilverpeas()
                  .getPermalink(identifier);
              return Pair.of(i.getTitle(), permalink.toString());
            }).collect(Collectors.toList()))));
  }

  private DragAndDropEditorContent getTemporaryContent(final DragAndDropWebEditorFileEditionContext editionContext) {
    return new DragAndDropEditorContent(editionContext.getFile().getTemporaryContent().orElse(EMPTY));
  }


  private DragAndDropEditorContent getFinalContent(final DragAndDropWebEditorFileEditionContext editionContext) {
    return new DragAndDropEditorContent(editionContext.getFile().getFinalContent().orElse(EMPTY));
  }

  protected <T> T process(final DragAndDropWebEditorRequestContext context,
      Function<DragAndDropWebEditorFileEditionContext, T> supplier) {
    try {
      final String accessToken = context.getAccessToken();
      final String fileId = context.getFileId();
      final DragAndDropWebEditorFileEditionContext editionContext = WbeHostManager.get()
          .getEditionContextFrom(fileId, accessToken, (u, f) ->
              new DragAndDropWebEditorFileEditionContext(u.orElse(null), f.orElse(null)));
      checkEditionContext(context, editionContext);
      return supplier.apply(editionContext);
    } catch (WebApplicationException e) {
      logger().error(e);
      context.getResponse().setStatus(e.getResponse().getStatus());
    } catch (Exception e) {
      logger().error(e);
      context.getResponse().setStatus(Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }
    return null;
  }

  private void checkEditionContext(final DragAndDropWebEditorRequestContext context,
      DragAndDropWebEditorFileEditionContext editionContext) {
    if (editionContext.getUser() == null) {
      throw new WebApplicationException(Status.UNAUTHORIZED);
    }
    final User user = editionContext.getUser().asSilverpeas();
    final WbeFile wbeFile = editionContext.getFile();
    if (wbeFile == null) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }
    if (HttpMethod.GET.equals(context.getRequest().getMethod()) && !wbeFile.canBeAccessedBy(user)) {
      final String error = format("User {0} can not access the file {1}", user.getId(), wbeFile);
      logger().error(error);
      throw new WebApplicationException(Status.UNAUTHORIZED);
    } else if (!HttpMethod.GET.equals(context.getRequest().getMethod()) && !wbeFile.canBeModifiedBy(user)){
      final String error = format("User {0} can not modify the file {1}", user.getId(), wbeFile);
      logger().error(error);
      throw new WebApplicationException(Status.UNAUTHORIZED);
    }
  }
}
