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

package org.apache.flink.runtime.io.network.netty;

import org.apache.flink.runtime.io.network.ConnectionID;
import org.apache.flink.runtime.io.network.ConnectionManager;
import org.apache.flink.runtime.io.network.PartitionRequestClient;
import org.apache.flink.runtime.io.network.TaskEventPublisher;
import org.apache.flink.runtime.io.network.partition.ResultPartitionProvider;

import java.io.IOException;

import static org.apache.flink.util.Preconditions.checkNotNull;

/**
 * 在Flink中, 不同Task之间的网络传输基于Netty实现。
 * ConnectionManager管理所有的网络的连接，而NettyConnectionManager就是ConnectionManager的具体实现。
 */
public class NettyConnectionManager implements ConnectionManager {

	private final NettyServer server;

	private final NettyClient client;

	private final NettyBufferPool bufferPool;

	private final PartitionRequestClientFactory partitionRequestClientFactory;

	private final NettyProtocol nettyProtocol;

	public NettyConnectionManager(
		ResultPartitionProvider partitionProvider,
		TaskEventPublisher taskEventPublisher,
		NettyConfig nettyConfig) {

		this.server = new NettyServer(nettyConfig);
		this.client = new NettyClient(nettyConfig);
		this.bufferPool = new NettyBufferPool(nettyConfig.getNumberOfArenas());

		this.partitionRequestClientFactory = new PartitionRequestClientFactory(client, nettyConfig.getNetworkRetries());

		this.nettyProtocol = new NettyProtocol(checkNotNull(partitionProvider), checkNotNull(taskEventPublisher));
	}

	/** NettyConnectionManager在启动的时候会创建并启动NettyClient和NettyServer，NettyServer会启动一个服务端监听，
	 * 等待其它NettyClient的连接 */
	@Override
	public int start() throws IOException {
		client.init(nettyProtocol, bufferPool);

		return server.init(nettyProtocol, bufferPool);
	}

	/** 当RemoteInputChannel请求一个远端的ResultSubpartition时, NettyClient就会发起和请求的ResultSubpartition
	 * 所在Task的NettyServer的连接，后续所有的数据交换都在这个连接上进行。
	 * 两个Task之间只会建立一个连接，这个连接会在不同的RemoteInputChannel和ResultSubpartition之间进行复用 */
	@Override
	public PartitionRequestClient createPartitionRequestClient(ConnectionID connectionId)
			throws IOException, InterruptedException {
		// 这里实际上会建立和其他Task的server的连接
		// 返回的PartitionRequestClient中封装了netty channel和chanel handler
		return partitionRequestClientFactory.createPartitionRequestClient(connectionId);
	}

	@Override
	public void closeOpenChannelConnections(ConnectionID connectionId) {
		partitionRequestClientFactory.closeOpenChannelConnections(connectionId);
	}

	@Override
	public int getNumberOfActiveConnections() {
		return partitionRequestClientFactory.getNumberOfActiveClients();
	}

	@Override
	public void shutdown() {
		client.shutdown();
		server.shutdown();
	}

	NettyClient getClient() {
		return client;
	}

	NettyServer getServer() {
		return server;
	}

	NettyBufferPool getBufferPool() {
		return bufferPool;
	}
}
