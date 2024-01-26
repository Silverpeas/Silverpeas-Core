package org.silverpeas.core.admin.service;

import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.space.SpaceInstLight;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.lang.String.valueOf;
import static java.util.Collections.emptySet;
import static org.silverpeas.kernel.util.StringUtil.isDefined;

/**
 * Representation of a Silverpeas's space and component structure more or less filtered.
 * <p>
 * This representation is essentially used by services using caches in order to provide as fast
 * as possible the data.
 * </p>
 * <p>
 * When this object is initialized from the root space (which does not exist physically in data),
 * {@link #getSubSpaces()} must be used to start parsing the data.
 * </p>
 */
public class SpaceWithSubSpacesAndComponents {

  private SpaceInstLight space;
  private List<SpaceWithSubSpacesAndComponents> subSpaces;
  private List<SilverpeasComponentInstance> components;

  public SpaceWithSubSpacesAndComponents(SpaceInstLight space) {
    this.space = space;
  }

  void setSubSpaces(final List<SpaceWithSubSpacesAndComponents> subSpaces) {
    this.subSpaces = subSpaces;
  }

  void setComponents(final List<SilverpeasComponentInstance> components) {
    this.components = components;
  }

  /**
   * Gets the data of current space.
   * <p>
   * The only way to check if the space is the root one (which does not exist physically into
   * data) is to use {@link SpaceInstLight#isRoot()} method.<br/>
   * Please not checking by using {@link SpaceInstLight#getId()} method which could eventually
   * throwing exception.
   * </p>
   * @return the {@link SpaceInstLight} representing the current space.
   */
  public SpaceInstLight getSpace() {
    return space;
  }

  /**
   * Gets the list of space just under the current space.
   * @return list of {@link SpaceWithSubSpacesAndComponents} instances.
   */
  public List<SpaceWithSubSpacesAndComponents> getSubSpaces() {
    return subSpaces;
  }

  /**
   * Gets the list of component of current space.
   * @return list of {@link ComponentInstLight} instances.
   */
  public List<SilverpeasComponentInstance> getComponents() {
    return components;
  }

  /**
   * Gets an instance of {@link ComponentInstanceSelector} initialize from the current view the
   * current instance is representing.
   * @return a {@link ComponentInstanceSelector} instance which MUST be parametrized by using
   * {@link ComponentInstanceSelector#fromAllSpaces()},
   * {@link ComponentInstanceSelector#fromSpaces(Set)} or
   * {@link ComponentInstanceSelector#fromSubSpacesOfSpaces(Set)} before calling
   * {@link ComponentInstanceSelector#select()}.
   */
  public ComponentInstanceSelector componentInstanceSelector() {
    return new ComponentInstanceSelector(this);
  }

  /**
   * This class permits to select component instances from a
   * {@link SpaceWithSubSpacesAndComponents} representing a current view according some rules.
   */
  public static class ComponentInstanceSelector {
    private final SpaceWithSubSpacesAndComponents currentSpaceView;
    private Boolean inAllSpaces;
    private Boolean inCurrentSpace;
    private Set<String> spaces = emptySet();
    private Set<String> componentIdsToExclude = emptySet();

    private ComponentInstanceSelector(final SpaceWithSubSpacesAndComponents currentSpaceView) {
      this.currentSpaceView = currentSpaceView;
    }

    /**
     * Calling this method means that all component instances from the view have to be selected.
     * @return the selector itself.
     */
    public ComponentInstanceSelector fromAllSpaces() {
      inAllSpaces = true;
      inCurrentSpace = false;
      return this;
    }

    /**
     * Calling this method means that all component instances from the given spaces have to be
     * selected.
     * @param spaces set of space identifiers.
     * @return the selector itself.
     */
    public ComponentInstanceSelector fromSpaces(final Set<String> spaces) {
      inAllSpaces = false;
      inCurrentSpace = true;
      this.spaces = spaces;
      return this;
    }

    /**
     * Calling this method means that all component instances from sub spaces of the given spaces
     * have to be selected.
     * @param spaces set of space identifiers.
     * @return the selector itself.
     */
    public ComponentInstanceSelector fromSubSpacesOfSpaces(final Set<String> spaces) {
      inAllSpaces = false;
      inCurrentSpace = false;
      this.spaces = spaces;
      return this;
    }

    /**
     * Calling this method in order to exclude some component instances from the result.
     * @param componentIdsToExclude set of component instance identifiers.
     * @return the selector itself.
     */
    public ComponentInstanceSelector excludingComponentInstances(
        final Set<String> componentIdsToExclude) {
      this.componentIdsToExclude = componentIdsToExclude;
      return this;
    }

    /**
     * Processes the selection.
     * <p>
     * Please parametrize the selector before calling this method.
     * </p>
     * @return a list of {@link SilverpeasComponentInstance} instances.
     */
    public List<SilverpeasComponentInstance> select() {
      if (inAllSpaces == null || inCurrentSpace == null) {
        throw new IllegalStateException(
            "Please parametrize the selector by using at least fromAllSpaces, fromSpaces or " +
                "fromSubSpacesOfSpaces method");
      }
      final List<SilverpeasComponentInstance> result = new ArrayList<>();
      fillComponentIdsFromSpaceView(currentSpaceView, result, inAllSpaces, inCurrentSpace);
      return result;
    }

    private void fillComponentIdsFromSpaceView(
        final SpaceWithSubSpacesAndComponents currentSpaceView,
        final List<SilverpeasComponentInstance> result, final boolean aggregateFromAllSpaces,
        final boolean aggregateFromCurrentSpace) {
      final SpaceInstLight currentSpace = currentSpaceView.getSpace();
      if (currentSpace != null) {
        final boolean isCurrentSpaceCandidate = isDefined(currentSpace.getFatherId())
            && (spaces.contains(currentSpace.getId()) ||spaces.contains(valueOf(currentSpace.getLocalId())));
        if ((aggregateFromAllSpaces || (aggregateFromCurrentSpace && isCurrentSpaceCandidate))
            && currentSpaceView.getComponents() != null) {
          currentSpaceView.getComponents().stream()
              .filter(a -> !componentIdsToExclude.contains(a.getId()))
              .forEach(result::add);
        }
        if (currentSpaceView.getSubSpaces() != null) {
          currentSpaceView.getSubSpaces().forEach(s -> fillComponentIdsFromSpaceView(s, result,
              aggregateFromAllSpaces || isCurrentSpaceCandidate, aggregateFromCurrentSpace));
        }
      }
    }
  }
}
