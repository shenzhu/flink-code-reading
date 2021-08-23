/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.api.java.functions;

import org.apache.flink.annotation.Internal;

/**
 * Used as a dummy {@link KeySelector} to allow using keyed operators
 * for non-keyed use cases. Essentially, it gives all incoming records
 * the same key, which is a {@code (byte) 0} value.
 *
 * <p>Non-Keyed Windows实际上就是基于Keyed Windows的一种特殊实现，只是使用了一种特殊的NullByteKeySelector，
 * 这样所有的消息得到的Key都是一样的。
 * Non-Keyed Windows的一个问题在于，由于所有消息的key都是一样的，那么所有的消息最终都会被同一个Task处理，
 * 这个Task也会成为整个作业的瓶颈。
 *
 * @param <T> The type of the input element.
 */
@Internal
public class NullByteKeySelector<T> implements KeySelector<T, Byte> {

	private static final long serialVersionUID = 614256539098549020L;

	@Override
	public Byte getKey(T value) throws Exception {
		return 0;
	}
}
