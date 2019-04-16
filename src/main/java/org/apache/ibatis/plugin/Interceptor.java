/**
 *    Copyright 2009-2015 the original author or authors.
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

import java.util.Properties;

/**
 * @author Clinton Begin
 *
 * Interceptor在mybatis中只支持（Executor、StatementHandler、ParameterHandler和ResultSetHandler四个接口中的一种）
 */
public interface Interceptor {
  //执行拦截器方法
  Object intercept(Invocation invocation) throws Throwable;

  // 创建interceptor对象，一般用mybatis提供的Plugin.wrap(...)方法
  // 该方法内部会判断，当前对象是否匹配当前interceptor，匹配则创建代理对象，不匹配则放回当前对象。
  Object plugin(Object target);

  // mybatis加载初始化时，会根据xml或注解的配置，将拦截器中的kv键值取出，调用这个方法做初始化操作
  void setProperties(Properties properties);

}
