package com.silverpeas.jpa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitPostProcessor;

/**
 * Processor of persistence units dedicated to merge all JPA settings defining the same persistence
 * unit.
 */
public class MergingPersistenceUnitPostProcessor implements PersistenceUnitPostProcessor {
  private Map<String, List<String>> puiClasses = new HashMap<String, List<String>>();

  @Override
  public void postProcessPersistenceUnitInfo(MutablePersistenceUnitInfo pui) {
    List<String> classes = puiClasses.get(pui.getPersistenceUnitName());
    if (classes == null) {
      classes = new ArrayList<String>();
      puiClasses.put(pui.getPersistenceUnitName(), classes);
    }
    pui.getManagedClassNames().addAll(classes);
    classes.addAll(pui.getManagedClassNames());
  }
}
