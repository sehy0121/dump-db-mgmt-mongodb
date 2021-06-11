package io.dsub.discogs.batch;

import java.lang.reflect.Method;
import java.util.Arrays;
import javax.sql.DataSource;
import net.ttddyy.dsproxy.listener.logging.SLF4JLogLevel;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("log_query")
public class DatasourceProxyBeanPostProcessor implements BeanPostProcessor {

  @Override
  public Object postProcessBeforeInitialization(final Object bean, final String beanName)
      throws BeansException {
    return bean;
  }

  @Override
  public Object postProcessAfterInitialization(final Object bean, final String beanName)
      throws BeansException {
    if (bean instanceof DataSource) {
      ProxyFactory factory = new ProxyFactory(bean);
      factory.setProxyTargetClass(true);
      factory.addAdvice(new ProxyDataSourceInterceptor((DataSource) bean));
      return factory.getProxy();
    }
    return bean;
  }

  private static class ProxyDataSourceInterceptor implements MethodInterceptor {

    private final DataSource dataSource;

    public ProxyDataSourceInterceptor(final DataSource dataSource) {
      this.dataSource =
          ProxyDataSourceBuilder.create(dataSource)
              .countQuery()
              .logQueryBySlf4j(SLF4JLogLevel.INFO)
              .build();
    }

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {

      String methodName = invocation.getMethod().getName();

      Method proxyMethod =
          Arrays.stream(dataSource.getClass().getDeclaredMethods())
              .filter(method -> method.getParameterCount() == 0)
              .filter(method -> method.getName().equals(methodName))
              .findFirst()
              .orElse(null);

      if (proxyMethod != null) {
        return proxyMethod.invoke(dataSource, invocation.getArguments());
      }
      return invocation.proceed();
    }
  }
}
