package org.silverpeas.core.security.encryption;

import org.silverpeas.core.contribution.model.SilverpeasContent;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.date.DateTime;

import java.util.HashMap;
import java.util.Map;

/**
 * A content of text to use in the tests on the content encryption.
 */
public class TextContent implements SilverpeasContent {

  public enum Properties {
    Title,
    Description,
    Text;
  }

  private String id;
  private String componentInstanceId;
  private UserDetail author;
  private DateTime creationDate;
  private String title;
  private String description;
  private String text;

  public TextContent(String id, String componentInstanceId, UserDetail author) {
    this.id = id;
    this.componentInstanceId = componentInstanceId;
    this.author = author;
    this.creationDate = DateTime.now();
  }

  /**
   * Gets the identifier of this content in the Silverpeas component providing it. This identifier
   * is only unique among all of the contents managed by the same component (whatever its different
   * instances). As each type of contents in Silverpeas is provided by a single Silverpeas
   * component, the identifier of a content is then specific to the component it belongs to. It is
   * a
   * way for an instance of a such component to identify uniquely the different contents it
   * manages.
   * So, each component can have their own policy to identify their content, whatever the way they
   * are identified in Silverpeas.
   * @return the identifier of this content.
   */
  @Override
  public String getId() {
    return this.id;
  }

  /**
   * Gets the unique identifier of the Silverpeas component instance that manages this content.
   * @return the unique identifier of the component instance in the Silverpeas collaborative
   *         portal.
   */
  @Override
  public String getComponentInstanceId() {
    return this.componentInstanceId;
  }

  /**
   * Gets the unique identifier of this content among all the contents managed in the Silverpeas
   * collaborative portal. It is the alone unique identifier of a content in the whole Silverpeas
   * portal and it is refered as the Silverpeas content identifier or the silver content
   * identifier.
   * For each content put into the Silverpeas collaborative portal, an entry is uniquely created in
   * the whole system so that is can be refered by transversal services and by component instances
   * others the one that manages it. For compatibility reason, the Silverpeas content identifier of
   * contents that are no yet taken into account in the whole system isn't defined, so an empty
   * string is then returned.
   * @return the unique identifier of this content in the whole Silverpeas collaborative portal.
   *         Can
   *         be empty if no such identifier is defined for the type of this content.
   */
  @Override
  public String getSilverpeasContentId() {
    return this.id;
  }

  /**
   * Gets the author that has created this content.
   * @return the detail about the user that created this content.
   */
  @Override
  public UserDetail getCreator() {
    return this.author;
  }

  /**
   * Gets the date at which this content was created.
   * @return the date at which this content was created.
   */
  @Override
  public DateTime getCreationDate() {
    return this.creationDate;
  }

  /**
   * Gets the title of this content if any.
   * @return the resource title. Can be empty if no title was set or no title is defined for a such
   *         content.
   */
  @Override
  public String getTitle() {
    return this.title;
  }

  /**
   * Gets the description of this content if any.
   * @return the resource description. Can be empty if no description was set or no description is
   *         defined for a such
   *         content.
   */
  @Override
  public String getDescription() {
    return this.description;
  }

  /**
   * Gets the type of this content.
   * @return the resource type. This can be Post, Message, Publication, Survey...
   */
  @Override
  public String getContributionType() {
    return "Text";
  }

  @Override
  public boolean canBeAccessedBy(final UserDetail user) {
    return true;
  }

  public void setTitle(final String title) {
    this.title = title;
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  public void setText(final String text) {
    this.text = text;
  }

  public String getText() {
    return text;
  }

  public Map<String, String> getProperties() {
    Map<String, String> properties = new HashMap<String, String>();
    properties.put(Properties.Title.name(), getTitle());
    properties.put(Properties.Description.name(), getDescription());
    properties.put(Properties.Text.name(), getText());
    return properties;
  }
}
