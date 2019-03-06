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
package org.apache.ibatis.mapping;

import javax.sql.DataSource;

import org.apache.ibatis.transaction.TransactionFactory;

/**
 * @author Clinton Begin
 */
//mybatis-config.xml中的Enviroment配置对应的对象
//<environments default="development">
//<environment id="development">
//<transactionManager type="JDBC">
//<property name="" value=""/>
//</transactionManager>
//<dataSource type="UNPOOLED">
//<property name="driver" value="org.hsqldb.jdbcDriver"/>
//<property name="url" value="jdbc:hsqldb:mem:automapping"/>
//<property name="username" value="sa"/>
//</dataSource>
//</environment>
//</environments>

// 每次启动就只能激活一个environment
// spring-mybatis，事务和数据源都是被spring注入的，不用mybatis在自己配置文件中指定
public final class Environment {
  private final String id;
  private final TransactionFactory transactionFactory;
  private final DataSource dataSource;

  public Environment(String id, TransactionFactory transactionFactory, DataSource dataSource) {
    if (id == null) {
      throw new IllegalArgumentException("Parameter 'id' must not be null");
    }
    if (transactionFactory == null) {
        throw new IllegalArgumentException("Parameter 'transactionFactory' must not be null");
    }
    this.id = id;
    if (dataSource == null) {
      throw new IllegalArgumentException("Parameter 'dataSource' must not be null");
    }
    this.transactionFactory = transactionFactory;
    this.dataSource = dataSource;
  }

  public static class Builder {
      private String id;
      private TransactionFactory transactionFactory;
      private DataSource dataSource;

    public Builder(String id) {
      this.id = id;
    }

    public Builder transactionFactory(TransactionFactory transactionFactory) {
      this.transactionFactory = transactionFactory;
      return this;
    }

    public Builder dataSource(DataSource dataSource) {
      this.dataSource = dataSource;
      return this;
    }

    public String id() {
      return this.id;
    }

    public Environment build() {
      return new Environment(this.id, this.transactionFactory, this.dataSource);
    }

  }

  public String getId() {
    return this.id;
  }

  public TransactionFactory getTransactionFactory() {
    return this.transactionFactory;
  }

  public DataSource getDataSource() {
    return this.dataSource;
  }

}
