/**
 * Copyright 2009-2015 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.logging;

/**
 * 由于不同的日志框架，拥有不同的日志级别，所以MyBatis在内部定义了Log接口，
 * 用于让其他日志框架适配MyBatis内部规定的日志级别
 */
public interface Log {

  boolean isDebugEnabled();

  boolean isTraceEnabled();

  /**
   * -----------规定的日志级别-----------
   */

  void error(String s, Throwable e);

  void error(String s);

  void debug(String s);

  void trace(String s);

  void warn(String s);

}
