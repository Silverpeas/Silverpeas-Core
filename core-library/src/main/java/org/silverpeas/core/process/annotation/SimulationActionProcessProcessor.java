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

package org.silverpeas.core.process.annotation;

import org.silverpeas.core.ActionType;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.personalization.UserPreferences;
import org.silverpeas.core.process.ProcessProvider;
import org.silverpeas.core.process.annotation.SimulationActionProcessProcessor.SourceSupplier.Sources;
import org.silverpeas.core.process.annotation.SimulationActionProcessProcessor.TargetSupplier.Targets;
import org.silverpeas.core.process.management.ProcessExecutionContext;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.util.MemoizedSupplier;
import org.silverpeas.core.util.Process;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Named;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.text.MessageFormat.format;
import static java.util.Optional.ofNullable;

/**
 * Centralizing the management of a simulation of actions performed by a caller.
 * <p>It can be used directly into a treatment.</p>
 * <p>It is also used by {@link SimulationActionProcessAnnotationInterceptor} mechanism.</p>
 * @author silveryocha
 */
@Bean
@Named
public class SimulationActionProcessProcessor {

  static final String SIMULATION_PROCESS_PERFORMED = "@SIMULATION_PROCESS_PERFORMED@";
  static final String SIMULATION_PROCESS_PROCESSING = "@SIMULATION_PROCESS_PROCESSING@";

  private final List<Context> contexts = new ArrayList<>();
  private TargetSupplier targetSupplier;
  private Supplier<String> language;
  private Method caller;

  protected SimulationActionProcessProcessor() {
    // hidden constructor
  }

  /**
   * @see #andWithContext(Consumer)
   */
  public static SimulationActionProcessProcessor get() {
    return ServiceProvider.getService(SimulationActionProcessProcessor.class);
  }

  /**
   *
   * @return true if a simulation is performing
   */
  public boolean isSimulationProcessPerforming() {
    return CacheServiceProvider.getRequestCacheService()
        .getCache()
        .get(SIMULATION_PROCESS_PROCESSING) != null;
  }

  /**
   * @see #andWithContext(Consumer)
   */
  public SourceSupplier withContext(final Consumer<Sources> consumer) {
    return andWithContext(consumer);
  }

  /**
   * Registering a new context by indicating the source instances which will be given to
   * {@link SimulationElementLister#listElements(ResourceReference, String)} or
   * {@link SimulationElementLister#listElements(Object, String, ResourceReference)} as sources.
   * @param consumer a consumer that provides {@link Sources}.
   * @return a {@link SourceSupplier} instance.
   */
  SourceSupplier andWithContext(final Consumer<Sources> consumer) {
    final Context context = new Context(this);
    contexts.add(context);
    return context.set(consumer);
  }

  /**
   * This method performs first the simulation against the defined context, and then execute the
   * given process.
   * <p>
   * If an error occurred, one of the transversal exceptions is thrown and the process is not be
   * performed.
   * </p>
   * <p>
   *   If the execution is performed into the context of an existing simulation process, then the
   *   given process is executed directly without performing any simulation.
   * </p>
   */
  public <T> T execute(final Process<T> process) {
    // Simulation is processed only if no simulation is already working
    if (CacheServiceProvider.getRequestCacheService()
        .getCache()
        .get(SIMULATION_PROCESS_PERFORMED) == null) {
      try {
        // Indicating that a functional check is processing
        CacheServiceProvider.getRequestCacheService()
            .getCache()
            .put(SIMULATION_PROCESS_PROCESSING, true);
        try {
          for (final Context context : contexts) {
            simulate(context);
          }
        } finally {
          // Removing the flag indicating that a functional check is processing (out of the
          // service)
          CacheServiceProvider.getRequestCacheService()
              .getCache()
              .remove(SIMULATION_PROCESS_PROCESSING);
        }
        // Invoking finally the proxy method initially called
        return process.execute();
      } catch (Exception e) {
        ofNullable(caller)
            .map(c -> "Error in intercepted method: " + c.getName())
            .ifPresent(m -> SilverLogger.getLogger(this).error(m, e));
        throw new SilverpeasRuntimeException(e);
      } finally {
        // Removing the flag indicating that a functional check has been performed (out of the
        // service)
        CacheServiceProvider.getRequestCacheService()
            .getCache()
            .remove(SIMULATION_PROCESS_PERFORMED);
      }
    }

    // Invoking finally the proxy method initially called
    try {
      return process.execute();
    } catch (Exception e) {
      throw new SilverpeasRuntimeException(e);
    }
  }

  private void simulate(final Context context) throws Exception {
    final List<ResourceReference> sourcePKs = context.sourceSupplier.getSources().sourcePKs;
    final List<ResourceReference> targetPKs = targetSupplier.getTargets().targetPKs;
    final List<Object> sourceObjects = context.sourceSupplier.getSources().sourceObjects;

    // Performing verify if necessary
    if ((!sourcePKs.isEmpty() || !sourceObjects.isEmpty()) && !targetPKs.isEmpty()) {
      final String uiLanguage = getLanguage();

      // Element container
      final Map<Class<SimulationElement<?>>, List<SimulationElement<?>>> elements = new HashMap<>();

      // Processing for each target
      for (ResourceReference targetPK : targetPKs) {
        final ActionType actionType = context.action.type.get();

        // Element lister
        SimulationElementLister elementLister = context.lister.supplier.get();
        elementLister.setActionType(actionType);
        elementLister.setElements(elements);

        // Browsing sourcePKs
        sourcePKs.stream()
            .filter(sourcePK -> ActionType.MOVE != actionType || !sourcePK.getInstanceId().equals(targetPK.getInstanceId()))
            .forEach(sourcePK -> elementLister.listElements(sourcePK, uiLanguage));

        // Browsing sourceObjects
        sourceObjects.forEach(sourceObject -> elementLister.listElements(sourceObject, uiLanguage, targetPK));

        ProcessProvider.getProcessManagement()
            .execute(new SimulationElementConversionProcess(elements, targetPK, actionType),
                new ProcessExecutionContext(targetPK.getInstanceId()));

        // Indicating that a functional check has been performed
        CacheServiceProvider.getRequestCacheService()
            .getCache()
            .put(SIMULATION_PROCESS_PERFORMED, true);
      }
    } else {
      ofNullable(caller)
          .map(c -> format(
              "Intercepted method ''{0}'', but SourcePK, SourceObject or TargetPK annotations are" +
                  " missing on parameter specifications...", c.getName()))
          .ifPresent(m -> SilverLogger.getLogger(this).warn(m));
    }
  }

  private String getLanguage() {
    return ofNullable(language.get()).filter(StringUtil::isDefined)
        .orElseGet(() -> ofNullable(User.getCurrentRequester()).map(User::getUserPreferences)
            .map(UserPreferences::getLanguage)
            .orElseGet(DisplayI18NHelper::getDefaultLanguage));
  }

  /**
   * Sets manually a language which will be used to format the messages.
   * <p>
   * If not set, then the one of {@link User#getCurrentRequester()} is taken. If no user, then
   * {@link DisplayI18NHelper#getDefaultLanguage()} is taken.
   * </p>
   * @param language a language.
   * @return itself.
   */
  public SimulationActionProcessProcessor setLanguage(final Supplier<String> language) {
    this.language = language;
    return this;
  }

  /**
   * Permits to indicate the method that calls the simulation.
   * @param caller a {@link Method} instance.
   * @return itself.
   */
  public SimulationActionProcessProcessor fromMethod(final Method caller) {
    this.caller = caller;
    return this;
  }

  public static class Context {
    private final SimulationActionProcessProcessor process;
    private SourceSupplier sourceSupplier;
    private Lister lister;
    private Action action;

    private Context(final SimulationActionProcessProcessor process) {
      this.process = process;
    }

    SourceSupplier set(final Consumer<Sources> sources) {
      sourceSupplier = new SourceSupplier(this, sources);
      return sourceSupplier;
    }
  }

  public static class SourceSupplier {
    private final Context context;
    private final Consumer<Sources> consumer;
    private final MemoizedSupplier<Sources> sourceCache = new MemoizedSupplier<>(() -> {
      final Sources sources = new Sources();
      getConsumer().accept(sources);
      return sources;
    });

    private SourceSupplier(final Context context, final Consumer<Sources> consumer) {
      this.context = context;
      this.consumer = consumer;
    }

    private Consumer<Sources> getConsumer() {
      return consumer;
    }

    private Sources getSources() {
      return sourceCache.get();
    }

    /**
     * Sets the {@link SimulationElementLister} supplier.
     * @param supplier a supplier of {@link SimulationElementLister}.
     * @return a {@link Lister} instance.
     */
    public Lister listElementsWith(final Supplier<SimulationElementLister> supplier) {
      context.lister = new Lister(context);
      return context.lister.set(supplier);
    }

    public static class Sources {
      private final List<Object> sourceObjects = new ArrayList<>();
      private final List<ResourceReference> sourcePKs = new ArrayList<>();

      public List<Object> getSourceObjects() {
        return sourceObjects;
      }

      public List<ResourceReference> getSourcePKs() {
        return sourcePKs;
      }
    }
  }

  public static class Lister {
    private final Context context;
    private Supplier<SimulationElementLister> supplier;

    private Lister(final Context context) {
      this.context = context;
    }

    private Lister set(final Supplier<SimulationElementLister> supplier) {
      this.supplier = supplier;
      return this;
    }

    /**
     * Indicates the action type is performed into the context of simulation.
     * <p>
     * It MUST represents the reality of the action performed out of the simulation.
     * </p>
     * @param actionType a {@link Supplier} of {@link ActionType} constant.
     * @return the {@link SimulationActionProcessProcessor} rightly initialized.
     */
    public Action byAction(final Supplier<ActionType> actionType) {
      context.action = new Action(context);
      return context.action.set(actionType);
    }
  }

  public static class TargetSupplier {
    private final Consumer<Targets> consumer;
    final MemoizedSupplier<Targets> targetCache = new MemoizedSupplier<>(() -> {
      final Targets targets = new Targets();
      getConsumer().accept(targets);
      return targets;
    });

    private TargetSupplier(final Consumer<Targets> consumer) {
      this.consumer = consumer;
    }

    private Consumer<Targets> getConsumer() {
      return consumer;
    }

    private Targets getTargets() {
      return targetCache.get();
    }

    public static class Targets {
      private final List<ResourceReference> targetPKs = new ArrayList<>();

      public List<ResourceReference> getTargetPKs() {
        return targetPKs;
      }
    }
  }

  public static class Action {
    private final Context context;
    private Supplier<ActionType> type;

    private Action(final Context context) {
      this.context = context;
    }

    Action set(final Supplier<ActionType> actionType) {
      this.type = actionType;
      return this;
    }

    /**
     * Registering a new context by indicating the source instances which will be given to
     * {@link SimulationElementLister#listElements(ResourceReference, String)} or
     * {@link SimulationElementLister#listElements(Object, String, ResourceReference)} as sources.
     * @param consumer a consumer that provides {@link Sources}.
     * @return a {@link SourceSupplier} instance.
     */
    public SourceSupplier andWithContext(final Consumer<Sources> consumer) {
      final Context newContext = new Context(context.process);
      context.process.contexts.add(newContext);
      return newContext.set(consumer);
    }

    /**
     * Indicates {@link ResourceReference} instances which will be given to
     * {@link SimulationElementLister#listElements(Object, String, ResourceReference)} or
     * {@link SimulationElementLister#listElements(ResourceReference, String)} as targets.
     * @param consumer a consumer that provides {@link Targets}.
     * @return a {@link TargetSupplier} instance.
     */
    public SimulationActionProcessProcessor toTargets(final Consumer<Targets> consumer) {
      context.process.targetSupplier = new TargetSupplier(consumer);
      return context.process;
    }
  }
}
