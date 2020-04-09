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
package org.apache.ibatis.reflection.factory;

import java.util.List;
import java.util.Properties;

/**
 * MyBatis使用ObjectFactory来创建所有需要的新对象。
 *
 * @author Clinton Begin
 */
public interface ObjectFactory {

  /**
   * 设置配置属性。
   * @param properties configuration properties
   */
  default void setProperties(Properties properties) {
    // NOP
  }

  /**
   * 使用默认构造函数创建新对象。
   *
   * @param <T>
   *          the generic type
   * @param type
   *          Object type
   * @return the t
   */
  <T> T create(Class<T> type);

  /**
   * 使用指定的构造函数和参数创建新对象。
   *
   * @param <T>
   *          the generic type
   * @param type
   *          Object type
   * @param constructorArgTypes
   *          Constructor argument types
   * @param constructorArgs
   *          Constructor argument values
   * @return the t
   */
  <T> T create(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs);

  /**
   * 判断是否为一个集合类型
   * 它的主要目的是支持非java.util。集合对象，比如Scala集合
   *
   * @param <T>
   *          the generic type
   * @param type
   *          Object type
   * @return whether it is a collection or not
   * @since 3.1.0
   */
  <T> boolean isCollection(Class<T> type);

}
