package org.silverpeas.core.test.unit;

import jakarta.enterprise.inject.spi.Bean;
import org.silverpeas.kernel.ManagedBeanProvider;
import org.silverpeas.kernel.annotation.NonNull;
import org.silverpeas.kernel.exception.MultipleCandidateException;
import org.silverpeas.kernel.test.DependencyResolver;
import org.silverpeas.kernel.test.util.SilverpeasReflectionException;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.util.TypeLiteral;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import static org.mockito.Mockito.mock;

/**
 * A simple resolver for specific CDI types implied in dependencies injection.
 * @author mmoquillon
 */
public class SimpleJEEDependencyResolver extends DependencyResolver {

  @Override
  protected Optional<Object> resolveCustomDependency(@NonNull Field dependency,
      @NonNull Annotation... qualifiers) throws MultipleCandidateException,
      SilverpeasReflectionException {
    Objects.requireNonNull(dependency);
    Objects.requireNonNull(qualifiers);
    Optional<Object> bean;
    if (dependency.getType().equals(Instance.class)) {
      ParameterizedType type = (ParameterizedType) dependency.getGenericType();
      Type beanType = type.getActualTypeArguments()[0];
      bean = Optional.of(new InstanceImpl<>((Class<?>) beanType));
    } else {
      bean = Optional.empty();
    }
    return bean;
  }

  private static class InstanceImpl<T> implements Instance<T> {

    private final Class<T> beansType;

    public InstanceImpl(Class<T> beansType) {
      this.beansType = beansType;
    }

    @Override
    public Instance<T> select(final Annotation... qualifiers) {
      return null;
    }

    @Override
    public <U extends T> Instance<U> select(final Class<U> subtype,
        final Annotation... qualifiers) {
      return null;
    }

    @Override
    public <U extends T> Instance<U> select(final TypeLiteral<U> subtype,
        final Annotation... qualifiers) {
      return null;
    }

    @Override
    public boolean isUnsatisfied() {
      Set<T> beans = resolve();
      return beans.isEmpty();
    }

    @Override
    public boolean isAmbiguous() {
      return false;
    }

    @Override
    public void destroy(final T instance) {
      // nothing to do
    }

    @Override
    public Handle<T> getHandle() {
      return new HandleImpl<>(get());
    }

    @Override
    public Iterable<? extends Handle<T>> handles() {
      return Set.of(getHandle());
    }

    @Nonnull
    @Override
    public Iterator<T> iterator() {
      Set<T> beans = resolve();
      if (beans.isEmpty()) {
        return Collections.singleton(mock(beansType)).iterator();
      } else {
        return beans.iterator();
      }
    }

    @Override
    public T get() {
      Set<T> beans = resolve();
      if (!beans.isEmpty()) {
        return beans.iterator().next();
      } else {
        return mock(beansType);
      }
    }

    private Set<T> resolve() {
      return ManagedBeanProvider.getInstance().getAllManagedBeans(beansType);
    }
  }

  private static class HandleImpl<T> implements Instance.Handle<T> {

    private final T bean;

    public HandleImpl(T bean) {
      this.bean = bean;
    }

    @Override
    public T get() {
      return bean;
    }

    @Override
    public Bean<T> getBean() {
      return null;
    }

    @Override
    public void destroy() {
      // nothing to do for tests
    }

    @Override
    public void close() {
      // nothing to do for tests
    }
  }
}
  