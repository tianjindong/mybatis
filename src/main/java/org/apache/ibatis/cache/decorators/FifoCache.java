/**
 *    Copyright 2009-2019 the original author or authors.
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
package org.apache.ibatis.cache.decorators;

import java.util.Deque;
import java.util.LinkedList;

import org.apache.ibatis.cache.Cache;

/**
 * 缓存淘汰策略装饰器（先进先出）
 *
 * @author Clinton Begin
 */
public class FifoCache implements Cache {

  /**
   * 被装饰对象
   */
  private final Cache delegate;
  /**
   * 双端队列
   */
  private final Deque<Object> keyList;
  /**
   * 缓存的上限个数，触发这个值就会激活缓存淘汰策略
   */
  private int size;

  public FifoCache(Cache delegate) {
    this.delegate = delegate;
    this.keyList = new LinkedList<>();
    this.size = 1024;
  }

  @Override
  public String getId() {
    return delegate.getId();
  }

  @Override
  public int getSize() {
    return delegate.getSize();
  }

  public void setSize(int size) {
    this.size = size;
  }

  @Override
  public void putObject(Object key, Object value) {
    //将缓存键放入队列中，如果队列超长，则删除队首的键，以及其对应的缓存
    cycleKeyList(key);
    delegate.putObject(key, value);
  }

  @Override
  public Object getObject(Object key) {
    return delegate.getObject(key);
  }

  @Override
  public Object removeObject(Object key) {
    return delegate.removeObject(key);
  }

  @Override
  public void clear() {
    delegate.clear();
    keyList.clear();
  }

  /**
   * 将缓存键放入队列中，如果队列超长，则删除队首的键，以及其对应的缓存
   * @param key
   */
  private void cycleKeyList(Object key) {
    //在双端队列队尾放入缓存键
    keyList.addLast(key);
    //如果缓存个数大于了极限值
    if (keyList.size() > size) {
      //移除双端队列对数的保存的Key
      Object oldestKey = keyList.removeFirst();
      //通过这个Key删除队首元素
      delegate.removeObject(oldestKey);
    }
  }

}
