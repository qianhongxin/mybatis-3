/**
 *    Copyright 2009-2018 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.plugin;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.reflection.ExceptionUtil;

/**
 * @author Clinton Begin
 *
 * 拦截器是用jdk动态代理实现
 */
public class Plugin implements InvocationHandler {

  // target可能是Executor、StatementHandler、ParameterHandler和ResultSetHandler四个接口的代理类对象，也可能就是实际的Executor、StatementHandler、ParameterHandler和ResultSetHandler四个接口的对象
  private final Object target;

  private final Interceptor interceptor;

  private final Map<Class<?>, Set<Method>> signatureMap;

  private Plugin(Object target, Interceptor interceptor, Map<Class<?>, Set<Method>> signatureMap) {
    this.target = target;
    this.interceptor = interceptor;
    this.signatureMap = signatureMap;
  }

  // 判断interceptor是否支持target拦截，支持则创建target的代理对象，否则原样返回target
  // 如果一个对象适配多个interceptor，则会被多次代理
  public static Object wrap(Object target, Interceptor interceptor) {
    // 解析 interceptor 的 @interceptors 注解中定义的内容
    Map<Class<?>, Set<Method>> signatureMap = getSignatureMap(interceptor);

    // 获取 target 的字节码对象
    Class<?> type = target.getClass();

    // 根据 type 和 signatureMap，获取 type 查找所有匹配的拦截器支持拦截的接口字节码对象数组
    Class<?>[] interfaces = getAllInterfaces(type, signatureMap);

    // 如果 interfaces 的大小大于 0，则给 target 对象创建代理对象。否则不创建代理对象
    if (interfaces.length > 0) {

      // 创建 target 的代理类对象, 如果是Executor，则返回的就是Excutor的代理类对象。利用的是 JDK 动态代理。代理类$Proxy的字节码生成后会缓存在内存中，不用
      return Proxy.newProxyInstance(type.getClassLoader(), interfaces, new Plugin(target, interceptor, signatureMap));
    }
    return target;
  }

  // 实现 JDK 的 InvocationHandler 接口的 invoke 方法，当代理对象被调用时，代理方法内部就是
  // 执行的这个 invoke 方法，invoke中会有目标方法的调用判断和通知（interceptor）的执行逻辑。这里会有inceptor方法是否执行的判断和目标对象是否调用的判断
  // 即method.invoke(target, args);方法的调用就是执行target的invoke方法，一直执行到原始被代理的
  // 的对象，即原始的和数据库交互的对象
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    try {
      // 获取 method 所属类或接口的字节码对象
      Set<Method> methods = signatureMap.get(method.getDeclaringClass());
      // 判断 methods 中是否包括 method，包含则执行 拦截器的 intercept 方法。 代理创建时，不会根据method判断是否创建，只要type等格式对就创建
      if (methods != null && methods.contains(method)) {
        return interceptor.intercept(new Invocation(target, method, args));
      }
      // 调用 method 的 invoke 方法，进入下一层调用
      return method.invoke(target, args);
    } catch (Exception e) {
      throw ExceptionUtil.unwrapThrowable(e);
    }
  }

  private static Map<Class<?>, Set<Method>> getSignatureMap(Interceptor interceptor) {
    // 获取 @Intercepts 注解对应的字节码对象 interceptsAnnotation
    Intercepts interceptsAnnotation = interceptor.getClass().getAnnotation(Intercepts.class);

    // 如果 interceptsAnnotation 为空，抛出插件定义错误的异常
    if (interceptsAnnotation == null) {
      throw new PluginException("No @Intercepts annotation was found in interceptor " + interceptor.getClass().getName());
    }

    // 获取所有 @Intercepts 中的所有 Signature
    Signature[] sigs = interceptsAnnotation.value();

    // 定义 type 和 method 的映射 map
    Map<Class<?>, Set<Method>> signatureMap = new HashMap<>();

    // 遍历 sigs ，初始化 signatureMap
    for (Signature sig : sigs) {
      Set<Method> methods = signatureMap.computeIfAbsent(sig.type(), k -> new HashSet<>());
      try {
        Method method = sig.type().getMethod(sig.method(), sig.args());
        methods.add(method);
      } catch (NoSuchMethodException e) {
        throw new PluginException("Could not find method on " + sig.type() + " named " + sig.method() + ". Cause: " + e, e);
      }
    }

    // 存储 接口字节码对象 和 接口方法字节码对象 的映射
    return signatureMap;
  }

  // 判断目标对象（Executor、StatementHandler、ParameterHandler和ResultSetHandler四个接口中的一种）是否有拦截器拦截
  // 方法参数：  type：目标对象的字节码对象； signatureMap：是我们在 getSignatureMap 方法中得到的
  private static Class<?>[] getAllInterfaces(Class<?> type, Map<Class<?>, Set<Method>> signatureMap) {
    Set<Class<?>> interfaces = new HashSet<>();
    while (type != null) {
      // 获取 type 的父接口，并遍历
      for (Class<?> c : type.getInterfaces()) {
        // 判断 signatureMap 中是否包含 type 的父接口
        if (signatureMap.containsKey(c)) {
          interfaces.add(c);
        }
      }
      // 获取 type 的父类，继续 while 循环
      type = type.getSuperclass();
    }

    // 返回匹配到的所有接口字节码对象
    return interfaces.toArray(new Class<?>[interfaces.size()]);
  }
}