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
package org.apache.ibatis.executor;

/**
 * @author Clinton Begin
 */
// mybatis的错误信息的构建类，记录一次报错所涉及到的关键信息
// https://blog.csdn.net/lqzkcx3/article/details/84944285

// 记录一次sql执行的过程中，关键地方的错误记录，涉及activity，object，message，sql的。其他的异常还是throw出去的。ErrorContext相当于调用链监控的简化
public class ErrorContext {

  private static final String LINE_SEPARATOR = System.getProperty("line.separator","\n");
  // 隔离不同线程的ErrorContext对象
  private static final ThreadLocal<ErrorContext> LOCAL = new ThreadLocal<>();

  private ErrorContext stored;
  // 错误的来源，即xml的名字
  private String resource;
  // 错误发生操作，比如 execute an query，setting paramters等
  private String activity;
  // 错误涉及到的对象id，即xml中insert，update，delete，select的id属性值
  private String object;
  // 错误的描述信息，记录的是mybatis在启动，运行过程中抛出的异常对应的message
  private String message;
  // 发生错误的sql语句
  private String sql;
  // 异常的堆栈
  private Throwable cause;

  private ErrorContext() {
  }

  // 创建ErrorContext对象，并放入LOCAL
  public static ErrorContext instance() {
    ErrorContext context = LOCAL.get();
    if (context == null) {
      context = new ErrorContext();
      LOCAL.set(context);
    }
    return context;
  }

  // 会将当前线程的 ErrorContext 对象缓存在新建的 ErrorContext 的 stored 字段中，然后将新建的 ErrorContext 对象与当前线程绑定

  // 所以store() recall()两个方法时组队使用的：
  //    BaseStatementHandler类的generateKeys方法：
  //    protected void generateKeys(Object parameter) {
  //      KeyGenerator keyGenerator = mappedStatement.getKeyGenerator();
  //      ErrorContext.instance().store();
  //      keyGenerator.processBefore(executor, mappedStatement, null, parameter);
  //      ErrorContext.instance().recall();
  //    }
  public ErrorContext store() {
    ErrorContext newContext = new ErrorContext();
    newContext.stored = this;
    LOCAL.set(newContext);
    return LOCAL.get();
  }

  // 将当前的线程的ErrorContext对象替换成stored存储的ErrorContext对象，做一个复原。该方法和store（）方法组队使用，用来处理在
  // 一次sql调用时，一个线程需要有多个ErrorContext对象来记录调用链信息。这里的stored字段可以将很多个ErrorContext对象串联起来，类似于单链表。
  // tomcat中的Valve的next字段也是类似于这个逻辑。

  // 所以store() recall()两个方法时组队使用的：
  //    BaseStatementHandler类的generateKeys方法：
  //    protected void generateKeys(Object parameter) {
  //      KeyGenerator keyGenerator = mappedStatement.getKeyGenerator();
  //      ErrorContext.instance().store();
  //      keyGenerator.processBefore(executor, mappedStatement, null, parameter);
  //      ErrorContext.instance().recall();
  //    }
  public ErrorContext recall() {
    if (stored != null) {
      LOCAL.set(stored);
      stored = null;
    }
    return LOCAL.get();
  }

  public ErrorContext resource(String resource) {
    this.resource = resource;
    return this;
  }

  public ErrorContext activity(String activity) {
    this.activity = activity;
    return this;
  }

  public ErrorContext object(String object) {
    this.object = object;
    return this;
  }

  public ErrorContext message(String message) {
    this.message = message;
    return this;
  }

  public ErrorContext sql(String sql) {
    this.sql = sql;
    return this;
  }

  public ErrorContext cause(Throwable cause) {
    this.cause = cause;
    return this;
  }

  // 将当前线程的ErrorContext相关变量置空，清楚Local中的对应ErrorContext对象
  public ErrorContext reset() {
    resource = null;
    activity = null;
    object = null;
    message = null;
    sql = null;
    cause = null;
    LOCAL.remove();
    return this;
  }

  @Override
  public String toString() {
    StringBuilder description = new StringBuilder();

    // message
    if (this.message != null) {
      description.append(LINE_SEPARATOR);
      description.append("### ");
      description.append(this.message);
    }

    // resource
    if (resource != null) {
      description.append(LINE_SEPARATOR);
      description.append("### The error may exist in ");
      description.append(resource);
    }

    // object
    if (object != null) {
      description.append(LINE_SEPARATOR);
      description.append("### The error may involve ");
      description.append(object);
    }

    // activity
    if (activity != null) {
      description.append(LINE_SEPARATOR);
      description.append("### The error occurred while ");
      description.append(activity);
    }

    // activity
    if (sql != null) {
      description.append(LINE_SEPARATOR);
      description.append("### SQL: ");
      description.append(sql.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ').trim());
    }

    // cause
    if (cause != null) {
      description.append(LINE_SEPARATOR);
      description.append("### Cause: ");
      description.append(cause.toString());
    }

    return description.toString();
  }

}
