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

package org.apache.flink.runtime.jobgraph;

/**
 * The ScheduleMode decides how tasks of an execution graph are started.
 */
public enum ScheduleMode {
	/** Schedule tasks lazily from the sources. Downstream tasks are started once their input data are ready */
	/*TODO 适用于批处理。从 SourceTask开始分阶段调度，申请资源的
	   时候，一次性申请本阶段所需要的所有资源。上游 Task执行完毕后开始调度执行下游的 Task
	   读取上游的数据，执行本阶段的计算任务，执行完毕之后，调度后一个阶段的 Task，依次进
	   行调度，直到作业完成
	 */
	LAZY_FROM_SOURCES(true),

	/**
	 * Same as LAZY_FROM_SOURCES just with the difference that it uses batch slot requests which support the
	 * execution of jobs with fewer slots than requested. However, the user needs to make sure that the job
	 * does not contain any pipelined shuffles (every pipelined region can be executed with a single slot).
	 */
	/*TODO 适用于批处理。与分阶段调度基本一样，区别在于该模式下使用批处理资源申请模式，
	   可以在资源不足的情况下执行作业，
	   但是需要确保在本阶段的作业执行中没有Shuffle*/
	LAZY_FROM_SOURCES_WITH_BATCH_SLOT_REQUEST(true),

	/** Schedules all tasks immediately. */
	/*TODO 适用于流计算。一次性申请需要的所有资源，如果资源不足，则作业启动失败*/
	EAGER(false);

	private final boolean allowLazyDeployment;

	ScheduleMode(boolean allowLazyDeployment) {
		this.allowLazyDeployment = allowLazyDeployment;
	}

	/**
	 * Returns whether we are allowed to deploy consumers lazily.
	 */
	public boolean allowLazyDeployment() {
		return allowLazyDeployment;
	}
}
