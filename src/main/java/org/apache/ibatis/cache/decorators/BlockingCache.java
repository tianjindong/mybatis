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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.CacheException;

/**
 * 装饰器，让缓存拥有阻塞的功能，目的是为了防止缓存击穿。（当在缓存中找不到元素时，它设置对缓存键的锁定，这样，其他线程将一直等待，直到该缓存键放入了缓存值）
 *
 * 需要注意的是，这个装饰器并不能保证缓存操作的线程安全
 *
 * Simple and inefficient version of EhCache's BlockingCache decorator.
 * It sets a lock over a cache key when the element is not found in cache.
 * This way, other threads will wait until this element is filled instead of hitting the database.
 *
 * @author Eduardo Macarron
 *
 */
public class BlockingCache implements Cache {

  //超时时间
  private long timeout;
  //被装饰的对象
  private final Cache delegate;
  /**
   * 这里采用分段锁，每一个Key对应一个锁，当在缓存中找不到元素时，它设置对缓存键的锁定。
   * 这样，其他线程将一直等待，直到该缓存键放入了缓存值，这也是防止缓存击穿的典型方案。
   *
   * 需要注意的是，这里每一个Key都对应一个锁，就并不能保证对底层Map的更新操作（主要是put操作），
   * 只由一个线程执行，那这样多线程状态下对底层HashMap的更新操作也是线程不安全的！
   *
   * 也就是说，BlockingCache只是为了解决缓存击穿的问题，而不是解决缓存操作的线程安全问题，
   * 线程安全问题交由SynchronizedCache装饰器来完成
   *
   */
  private final ConcurrentHashMap<Object, ReentrantLock> locks;

  public BlockingCache(Cache delegate) {
    this.delegate = delegate;
    this.locks = new ConcurrentHashMap<>();
  }

  @Override
  public String getId() {
    return delegate.getId();
  }

  @Override
  public int getSize() {
    return delegate.getSize();
  }

  @Override
  public void putObject(Object key, Object value) {
    try {
      delegate.putObject(key, value);
    } finally {
      //释放锁
      releaseLock(key);
    }
  }

  @Override
  public Object getObject(Object key) {
    //尝试获取锁，获取到锁之前一直阻塞，如果超时则抛出异常
    acquireLock(key);
    //调用被装饰对象的getObject方法获取缓存
    Object value = delegate.getObject(key);
    if (value != null) {
      /**
       * 如果value不为空，则释放锁。
       * 这里很多人肯定会有疑问，如果没有获取到值难道就不释放锁了吗？其实不然，当
       * MyBatis获取缓存时没有获取到数据，则会真正执行SQL语句去查询数据库，查询到结果后
       * 会紧接着调用缓存的putObject方法，在这个方法中会进行释放锁的操作
       */
      releaseLock(key);
    }
    return value;
  }

  @Override
  public Object removeObject(Object key) {
    // despite of its name, this method is called only to release locks
    releaseLock(key);
    return null;
  }

  @Override
  public void clear() {
    delegate.clear();
  }

  private ReentrantLock getLockForKey(Object key) {
    //如果存在key对应的锁则返回已经存在的锁，如果不存在则创建一个并返回
    return locks.computeIfAbsent(key, k -> new ReentrantLock());
  }

  /**
   * 尝试获取锁
   * @param key 缓存的key，
   */
  private void acquireLock(Object key) {
    //获取锁对象
    Lock lock = getLockForKey(key);
    if (timeout > 0) {
      //如果锁的获取拥有超时时间
      try {
        //则尝试在指定时间内获取锁，如果获取到了则返回true，超时仍未获取到则返回false
        boolean acquired = lock.tryLock(timeout, TimeUnit.MILLISECONDS);
        if (!acquired) {
          throw new CacheException("Couldn't get a lock in " + timeout + " for the key " +  key + " at the cache " + delegate.getId());
        }
      } catch (InterruptedException e) {
        throw new CacheException("Got interrupted while trying to acquire lock for key " + key, e);
      }
    } else {
      //没有设置超时时间，则直接无限期等待锁
      lock.lock();
    }
  }

  private void releaseLock(Object key) {
    ReentrantLock lock = locks.get(key);
    if (lock.isHeldByCurrentThread()) {
      //如果当前线程拥有此锁，则释放锁
      lock.unlock();
    }
  }

  public long getTimeout() {
    return timeout;
  }

  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }
}
