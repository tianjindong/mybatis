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
package org.apache.ibatis.cache.decorators;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.ibatis.cache.Cache;

/**
 * 缓存淘汰策略装饰器（最近最少使用）
 *
 * @author Clinton Begin
 */
public class LruCache implements Cache {

  private final Cache delegate;
  //使用LinkedHashMap维护key的使用顺序，个人认为这种实现方式虽然巧妙，但并不算特别优雅
  private Map<Object, Object> keyMap;
  private Object eldestKey;//最近最久没有使用的Key

  public LruCache(Cache delegate) {
    this.delegate = delegate;
    setSize(1024);
  }

  @Override
  public String getId() {
    return delegate.getId();
  }

  @Override
  public int getSize() {
    return delegate.getSize();
  }

  public void setSize(final int size) {
    //匿名内部类
    keyMap = new LinkedHashMap<Object, Object>(size, .75F, true) {
      private static final long serialVersionUID = 4267176411845948333L;

      /**
       * 重写LinkedHashMap中的removeEldestEntry方法是实现LRU的核心，
       * 这里LRU的实现机制不清楚的话可以研究一下LinkedHashMap源码。
       *
       * 大致思路：LinkedHashMap继承于HashMap并在HashMap的基础上为每一个Map.Entry添加了一个before和after指针，从而
       * 实现链表的功能，并全局维护了一个全局的head 和tail指针，分别指向链表的头和尾。当LinkedHashMap put元素时，会将
       * 这个Entry加入链表的末尾。这样链表头始终指向的是最近最久没有使用的元素，在put方法中会调用removeEldestEntry方法
       * 判断是否删除最久没有使用的元素，如果返回true则会删除最久没有会用过的元素
       * 的元素，
       * @param eldest
       * @return
       */
      @Override
      protected boolean removeEldestEntry(Map.Entry<Object, Object> eldest) {
        boolean tooBig = size() > size;
        if (tooBig) {
          //保存最近最久没有使用的key
          eldestKey = eldest.getKey();
        }
        return tooBig;
      }
    };
  }

  @Override
  public void putObject(Object key, Object value) {
    delegate.putObject(key, value);
    //增加元素后，进行key的后处理。判断是否触发缓存数极限，如果触发了则清除最老的key对应的缓存
    cycleKeyList(key);
  }

  @Override
  public Object getObject(Object key) {
    //调用LinkedHashMap.get()方法是为了让这次使用的key移动到链表末尾
    keyMap.get(key); // touch
    return delegate.getObject(key);
  }

  @Override
  public Object removeObject(Object key) {
    return delegate.removeObject(key);
  }

  @Override
  public void clear() {
    delegate.clear();
    keyMap.clear();
  }

  private void cycleKeyList(Object key) {
    //将key放入keyMap，如果缓存数大于了极限值，则会删除最老Key，并将这个key赋给eldestKey变量
    keyMap.put(key, key);
    if (eldestKey != null) {
      //eldestKey不为空，说明在调用keyMap.put()中触发了删除最老元素的机制，此时需要将真实缓存中对应的key-value删除
      delegate.removeObject(eldestKey);
      eldestKey = null;
    }
  }

}
