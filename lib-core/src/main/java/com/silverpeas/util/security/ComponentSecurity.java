package com.silverpeas.util.security;

public interface ComponentSecurity {

  /**
   * Check if a user is authorized to access to a given object in a defined
   * component.
   * 
   * @param componentId
   *          - id of the component
   * @param userId
   *          - id of the user
   * @param objectId
   *          - id of the object
   * @return true if user is authorized to access to component's object, false
   *         otherwise.
   */
  public boolean isAccessAuthorized(String componentId, String userId,
      String objectId);

  /**
   * Check if a user is authorized to access to a given object in a defined
   * component. Usefull if component uses several objects.
   * 
   * @param componentId
   *          - id of the component
   * @param userId
   *          - id of the user
   * @param objectId
   *          - id of the object
   * @param objectType
   *          - type of the object (ex : PublicationDetail, NodeDetail...)
   * @return true if user is authorized to access to component's object, false
   *         otherwise.
   */
  public boolean isAccessAuthorized(String componentId, String userId,
      String objectId, String objectType);

  /**
   * Check if a user is authorized to access to a given object in a defined
   * component. Usefull if component uses several objects.
   * 
   * @param componentId
   *          - id of the component
   * @param userId
   *          - id of the user
   * @param objectId
   *          - id of the object
   * @param objectType
   *          - type of the object (ex : PublicationDetail, NodeDetail...)
   * @return true if user is authorized to access to component's object, false
   *         otherwise.
   */
  public boolean isObjectAvailable(String componentId, String userId,
      String objectId, String objectType);

  public void enableCache();

  public void disableCache();

}