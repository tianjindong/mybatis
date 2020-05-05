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
package org.apache.ibatis.reflection.wrapper;

import java.util.List;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.property.PropertyTokenizer;

/**
 * 对象的包装器,提供最基本的get和set方法,不支持多级操作
 * @author Clinton Begin
 */
public interface ObjectWrapper {

  /**
   * get方法
   * @param prop
   * @return
   */
  Object get(PropertyTokenizer prop);

  /**
   * set方法
   * @param prop
   * @param value
   */
  void set(PropertyTokenizer prop, Object value);

  /**
   * 查找属性
   * @param name
   * @param useCamelCaseMapping
   * @return
   */
  String findProperty(String name, boolean useCamelCaseMapping);

  /**
   * 获取所有的getter的名字
   * @return
   */
  String[] getGetterNames();

  /**
   * 获取所有的Setter的名字
   * @return
   */
  String[] getSetterNames();

  /**
   * 获取setter的类型
   * @param name
   * @return
   */
  Class<?> getSetterType(String name);

  /**
   * 获取getter的类型
   * @param name
   * @return
   */
  Class<?> getGetterType(String name);

  /**
   * 判断是否有setter方法
   * @param name
   * @return
   */
  boolean hasSetter(String name);

  /**
   * 判断是否有getter方法
   * @param name
   * @return
   */
  boolean hasGetter(String name);

  /**
   * 实例化属性值
   * @param name
   * @param prop
   * @param objectFactory
   * @return
   */
  MetaObject instantiatePropertyValue(String name, PropertyTokenizer prop, ObjectFactory objectFactory);

  /**
   * 判断是否为集合
   * @return
   */
  boolean isCollection();

  /**
   * 添加属性
   * @param element
   */
  void add(Object element);

  /**
   * 将element所有元素添加进去
   * @param element
   * @param <E>
   */
  <E> void addAll(List<E> element);

}
