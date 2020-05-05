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
package org.apache.ibatis.reflection.property;

import java.util.Iterator;

/**
 * 例1: 参数: user[1].linkman.name
 *         children=linkman.name
 *         indexedName=user[1]
 *         name=user
 *         index=1
 * 属性分解为标记，迭代器模式
 * @author Clinton Begin
 */
public class PropertyTokenizer implements Iterator<PropertyTokenizer> {
  private String name;//名称
  private final String indexedName;//带索引的名称
  private String index;//索引
  private final String children;//子名称

  public PropertyTokenizer(String fullname) {
    //找出第一个“.”的索引
    int delim = fullname.indexOf('.');
    if (delim > -1) {
      //存在“.”
      name = fullname.substring(0, delim);
      children = fullname.substring(delim + 1);
    } else {
      //不存在“.”
      name = fullname;
      children = null;
    }
    indexedName = name;
    //第一个“[”的索引
    delim = name.indexOf('[');
    if (delim > -1) {
      index = name.substring(delim + 1, name.length() - 1);
      name = name.substring(0, delim);
    }
  }

  public String getName() {
    return name;
  }

  public String getIndex() {
    return index;
  }

  public String getIndexedName() {
    return indexedName;
  }

  public String getChildren() {
    return children;
  }

  @Override
  public boolean hasNext() {
    return children != null;
  }

  @Override
  public PropertyTokenizer next() {
    return new PropertyTokenizer(children);
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Remove is not supported, as it has no meaning in the context of properties.");
  }
}
