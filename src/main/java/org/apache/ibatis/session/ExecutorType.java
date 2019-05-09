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
package org.apache.ibatis.session;

/**
 * @author Clinton Begin
 */
public enum ExecutorType {
  // 这个执行器类型不做特殊的事情，为每个语句的执行创建一个新的预处理语句
  SIMPLE,
  //这个执行器类型会复用预处理语句
  REUSE,
  //这个执行器类型会批量执行所有的更新语句，为每个语句的执行创建一个新的预处理语句
  BATCH
}
