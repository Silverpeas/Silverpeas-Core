package org.silverpeas.util;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author mmoquillon
 */
public class CDIContainer implements BeanContainer {

  @Override
  public <T> T getBeanByName(final String name) {
    BeanManager beanManager = CDI.current().getBeanManager();
    Bean<T> bean = (Bean<T>) beanManager.getBeans(name).stream()
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Cannot find an instance of name " + name));
    CreationalContext<T> ctx = beanManager.createCreationalContext(bean);
    Type type = bean.getTypes().stream().findFirst().get();
    T ref = (T) beanManager.getReference(bean, type, ctx);

    return ref;
  }

  @Override
  public <T> T getBeanByType(final Class<T> type, Class<? extends Annotation>... qualifierClasses) {
    Annotation[] qualifiers = new Annotation[qualifierClasses.length];
    for (Class<? extends Annotation> qualifierClass : qualifierClasses) {
      try {
        qualifiers[qualifiers.length] = qualifierClass.newInstance();
      } catch (InstantiationException | IllegalAccessException e) {
        throw new IllegalArgumentException(
            "Impossible to create a new instance of qualifier in error: " +
                qualifierClass.getName());
      }
    }
    BeanManager beanManager = CDI.current().getBeanManager();
    Bean<T> bean = (Bean<T>) beanManager.getBeans(type, qualifiers).stream().findFirst()
        .orElseThrow(
            () -> new IllegalStateException("Cannot find an instance of type " + type.getName()));
    CreationalContext<T> ctx = beanManager.createCreationalContext(bean);
    T ref = (T) beanManager.getReference(bean, type, ctx);

    return ref;
  }

  @Override
  public <T> Set<T> getAllBeansByType(final Class<T> type) {
    BeanManager beanManager = CDI.current().getBeanManager();
    Set<T> refs = beanManager.getBeans(type).stream()
        .map(bean -> {
          CreationalContext ctx = beanManager.createCreationalContext(bean);
          return (T) beanManager.getReference(bean, type, ctx);
        })
        .collect(Collectors.toSet());
    return refs;
  }
}
