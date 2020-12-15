/**
 *    Copyright 2009-2020 the original author or authors.
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
 */
public interface Interceptor {

  /**
   * 拦截器的核心方法，内部实现插件的核心逻辑
   * @param invocation 对当前正在代理的方法的一个封装，内部封装正在调用目标对象方法的Method实例
   * @return
   * @throws Throwable
   */
  Object intercept(Invocation invocation) throws Throwable;

  /**
   * 用于返回目标对象的代理类，内部封装的是JDK动态代理实例的创建操作
   * 在3.5.5版本中使用了JDK8新提供的default关键字，提供了该方法的默认实现。在大多数情况下用户自定义拦截器实现该接口时
   * 不需要实现实现该方法
   * @param target
   * @return
   */
  default Object plugin(Object target) {
    return Plugin.wrap(target, this);
  }

  /**
   * 为插件设置自定义参数值，在MyBatis核心配置文件中配置插件时是可以使用<properties>标签指定插件的属性，当用户配置
   * <properties>属性后，插件在初始化时会调用该方法设置插件属性。由于是默认方法，如果该插件没有参数设置，则该方法不用
   * 在实现类中实现。
   * @param properties
   */
  default void setProperties(Properties properties) {
    // NOP
  }

}
