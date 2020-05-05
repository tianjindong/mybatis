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
package org.apache.ibatis.reflection;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.reflection.wrapper.BeanWrapper;
import org.apache.ibatis.reflection.wrapper.CollectionWrapper;
import org.apache.ibatis.reflection.wrapper.MapWrapper;
import org.apache.ibatis.reflection.wrapper.ObjectWrapper;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;

/**
 * 封装了对象元信息，包装了MyBatis中五个核心的反射类。也是提供给外部使用的反射工具类，可以利用它可以读取或者修改对象的属性信息。
 *
 * 内部封装了很多set和get方法，方便对原始对象进行赋值操作，其中set和get方法提供多级操作，例如：child.child.name
 * 可以参考MetaObjectTest来跟踪调试，基本上用到了reflection包下所有的类
 * @author Clinton Begin
 */
public class MetaObject {

  //原始对象
  private final Object originalObject;
  //对象的包装类对象
  private final ObjectWrapper objectWrapper;
  //对象的工厂
  private final ObjectFactory objectFactory;
  //对象包装器的工厂
  private final ObjectWrapperFactory objectWrapperFactory;
  //反射器工厂
  private final ReflectorFactory reflectorFactory;

  /**
   * 构造器
   * @param object 原始对象
   * @param objectFactory 对象的构造工厂
   * @param objectWrapperFactory 对象的包装类工程
   * @param reflectorFactory 反射器工厂
   */
  private MetaObject(Object object, ObjectFactory objectFactory, ObjectWrapperFactory objectWrapperFactory, ReflectorFactory reflectorFactory) {
    this.originalObject = object;
    this.objectFactory = objectFactory;
    this.objectWrapperFactory = objectWrapperFactory;
    this.reflectorFactory = reflectorFactory;

    if (object instanceof ObjectWrapper) {
      //如果对象本身已经是ObjectWrapper型，则直接赋给objectWrapper
      this.objectWrapper = (ObjectWrapper) object;
    } else if (objectWrapperFactory.hasWrapperFor(object)) {
      //如果有包装器,调用ObjectWrapperFactory.getWrapperFor
      this.objectWrapper = objectWrapperFactory.getWrapperFor(this, object);
    } else if (object instanceof Map) {
      //如果是Map型，返回MapWrapper
      this.objectWrapper = new MapWrapper(this, (Map) object);
    } else if (object instanceof Collection) {
      //如果是Collection型，返回CollectionWrapper
      this.objectWrapper = new CollectionWrapper(this, (Collection) object);
    } else {
      //除此以外，返回BeanWrapper
      this.objectWrapper = new BeanWrapper(this, object);
    }
  }

  /**
   * 获取一个实体的MetaObject对象，如果实体为空则返回空的MetaObject
   * @param object
   * @param objectFactory
   * @param objectWrapperFactory
   * @param reflectorFactory
   * @return
   */
  public static MetaObject forObject(Object object, ObjectFactory objectFactory, ObjectWrapperFactory objectWrapperFactory, ReflectorFactory reflectorFactory) {
    if (object == null) {
      return SystemMetaObject.NULL_META_OBJECT;
    } else {
      return new MetaObject(object, objectFactory, objectWrapperFactory, reflectorFactory);
    }
  }

  public ObjectFactory getObjectFactory() {
    return objectFactory;
  }

  public ObjectWrapperFactory getObjectWrapperFactory() {
    return objectWrapperFactory;
  }

  public ReflectorFactory getReflectorFactory() {
    return reflectorFactory;
  }

  public Object getOriginalObject() {
    return originalObject;
  }

  /**
   * --------以下方法都是委派给ObjectWrapper------
   */

  /**
   * 查找属性
   * @param propName
   * @param useCamelCaseMapping
   * @return
   */
  public String findProperty(String propName, boolean useCamelCaseMapping) {
    return objectWrapper.findProperty(propName, useCamelCaseMapping);
  }

  /**
   * 获取Getter方法名字列表
   * @return
   */
  public String[] getGetterNames() {
    return objectWrapper.getGetterNames();
  }

  /**
   * 获取getter的名字列表
   * @return
   */
  public String[] getSetterNames() {
    return objectWrapper.getSetterNames();
  }

  /**
   * 获取setter的名字列表
   * @param name
   * @return
   */
  public Class<?> getSetterType(String name) {
    return objectWrapper.getSetterType(name);
  }

  /**
   * 获取getter的名字列表
   * @param name
   * @return
   */
  public Class<?> getGetterType(String name) {
    return objectWrapper.getGetterType(name);
  }

  /**
   * 是否有指定的setter
   * @param name
   * @return
   */
  public boolean hasSetter(String name) {
    return objectWrapper.hasSetter(name);
  }

  /**
   * 是否有指定的getter
   * @param name
   * @return
   */
  public boolean hasGetter(String name) {
    return objectWrapper.hasGetter(name);
  }

  /**
   * 取得具体的值，支持多级操作，例如get("child.child.name")
   * @param name
   * @return
   */
  public Object getValue(String name) {
    ////将name解析为PropertyTokenizer，方便进行值的获取
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      //如果还有子节点
      MetaObject metaValue = metaObjectForProperty(prop.getIndexedName());
      if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
        //如果当前对象的子节点为空，则直接返回null，没必要继续递归寻找子元素了
        return null;
      } else {
        //不为空，则递归（伪递归）获取子节点数据
        return metaValue.getValue(prop.getChildren());
      }
    } else {
      //如果没有子节点了，则获取该值
      return objectWrapper.get(prop);
    }
  }

  /**
   * 设置具体值，支持多级操作
   * @param name 属性名称，类似于OGNL表达式，如果是多级结构，直接person[0].birthdate.year即可
   * @param value
   */
  public void setValue(String name, Object value) {
    //将name解析为PropertyTokenizer，方便进行对象的赋值
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      MetaObject metaValue = metaObjectForProperty(prop.getIndexedName());
      if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
        //如果获取出来的设置空的MetaObject
        if (value == null) {
          // 如果值为空，则不会实例化子路径
          return;
        } else {
          //如果value不为空，则委派给ObjectWrapper.instantiatePropertyValue创建子级对象并获取子级对象的MetaObject
          metaValue = objectWrapper.instantiatePropertyValue(name, prop, objectFactory);
        }
      }
      //继续给子节点赋值
      metaValue.setValue(prop.getChildren(), value);
    } else {
      //到了最后一层了，所以委派给ObjectWrapper.set
      objectWrapper.set(prop, value);
    }
  }

  /**
   * 根据属性名称获取MetaObject
   * @param name
   * @return
   */
  public MetaObject metaObjectForProperty(String name) {
    //获取当前的属性值
    Object value = getValue(name);
    return MetaObject.forObject(value, objectFactory, objectWrapperFactory, reflectorFactory);
  }

  public ObjectWrapper getObjectWrapper() {
    return objectWrapper;
  }

  public boolean isCollection() {
    return objectWrapper.isCollection();
  }

  public void add(Object element) {
    objectWrapper.add(element);
  }

  public <E> void addAll(List<E> list) {
    objectWrapper.addAll(list);
  }

}
