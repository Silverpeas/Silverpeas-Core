package org.silverpeas.core.util.annotation;

import org.silverpeas.kernel.logging.SilverLogger;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.lang.reflect.Method;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Interceptor
@TimeMeasuring
@Priority(Interceptor.Priority.APPLICATION)
public class TimeMeasuringProcessor {

    private static final String LOG_NAMESPACE = "silverpeas.core.benchmark";
    private static final String PROXY_PREFIX = "$Proxy";

    @AroundInvoke
    public Object processAuthentication(InvocationContext context) throws Exception {
        long start = System.nanoTime();
        Object result = context.proceed();
        long end = System.nanoTime();

        String target = getObjectClassName(context.getTarget());
        String method = getMethodName(context.getMethod());
        String parameters = Stream.of(context.getParameters())
                .map(Object::toString)
                .collect(Collectors.joining(","));

        SilverLogger.getLogger(LOG_NAMESPACE).info("MEASURED TIME OF {0}#{1}({2}): {3}ms", target, method, parameters,
                (end - start) / 1000000);
        return result;
    }

    private String getObjectClassName(Object object) {
        String className = object.getClass().getSimpleName();
        if (className.contains(PROXY_PREFIX)) {
            return className.substring(0, className.indexOf(PROXY_PREFIX));
        }
        return className;
    }

    private String getMethodName(Method method) {
        String methodName = method.getName();
        if (methodName.contains(PROXY_PREFIX)) {
            return methodName.substring(0, methodName.indexOf(PROXY_PREFIX));
        }
        return methodName;
    }
}
