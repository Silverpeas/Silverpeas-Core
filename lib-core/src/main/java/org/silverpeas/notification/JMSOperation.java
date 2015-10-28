package org.silverpeas.notification;

import org.silverpeas.persistence.Transaction;
import org.silverpeas.util.ServiceProvider;

import javax.jms.JMSContext;

/**
 * A JMS operation processor. But why?<br/>
 * In fact, {@link javax.jms.JMSContext} can be used as <code>application-managed</code> way or it
 * can be used as <code>container-managed</code>.
 * <p>
 * In the first way and according to the documentation, the {@link javax.jms.JMSContext} must be
 * get by using {@link javax.jms.ConnectionFactory} and it has to be explicitly closed when it is
 * no more needed. For example:
 * <pre>
 *   {@literal@}Resource
 *   private javax.jms.ConnectionFactory jmsConnectionFactory;
 *
 *   ...
 *
 *   public void performSend(Destination destination , Serializable event)
 *     try (JMSContext context = jmsConnectionFactory.createContext()) {
 *       final JMSProducer producer = context.createProducer();
 *       producer.send(destination, event);
 *     }
 *   }
 * </pre>
 * So that is kind of manual management.
 * </p>
 * <p>
 * In the second way, the {@link javax.jms.JMSContext} must be injected and used directly. For
 * example:
 * <pre>
 *   {@literal@}Inject
 *   private javax.jms.JMSContext context;
 *
 *   ...
 *
 *   public void performSend(Destination destination , Serializable event)
 *     final JMSProducer producer = context.createProducer();
 *     producer.send(destination, event);
 *   }
 * </pre>
 * There is no manual management here concerning the {@link javax.jms.JMSContext} because that is
 * handled by the container.<br/>
 * But to be managed efficiently, a JTA transaction must exist, and also, the following rule must
 * be
 * verified (from documentation of {@link javax.jms.JMSContext}): Applications running in the Java
 * EE web and EJB containers are not permitted to create more than one active session on a
 * connection so combining them in a single object takes advantage of this restriction to offer a
 * simpler API.
 * </p>
 * <p>
 * So, to get the code the simplest possible in callers, this class provides static method that
 * simplifies the JMS send by observing the different mandatory rules.
 * </p>
 * @author Yohann Chastagnier
 */
public class JMSOperation {

  /**
   * Realizes a JMS operation with a managed {@link JMSContext}.
   * @param operation the operation to realize.
   * @see org.silverpeas.notification.JMSOperation
   */
  public static void realize(final Operation operation) {
    Transaction.performInOne(() -> {
      final JMSContext context = ServiceProvider.getService(JMSContext.class);
      operation.realize(context);
      return null;
    });
  }


  /**
   * Defines a JMS operation to perform.
   */
  @FunctionalInterface
  public interface Operation {
    void realize(JMSContext jmsContext) throws Exception;
  }
}
