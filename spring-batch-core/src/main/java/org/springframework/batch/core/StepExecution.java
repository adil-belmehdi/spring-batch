/*
 * Copyright 2006-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.batch.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.util.Assert;

/**
 * Batch domain object representation the execution of a step. Unlike
 * {@link JobExecution}, there are additional properties related the processing
 * of items such as commit count, etc.
 *
 * @author Lucas Ward
 * @author Dave Syer
 * @author Mahmoud Ben Hassine
 *
 */
@SuppressWarnings("serial")
public class StepExecution extends Entity {

	private final JobExecution jobExecution;

	private final String stepName;

	private volatile BatchStatus status = BatchStatus.STARTING;

	private volatile long readCount = 0;

	private volatile long writeCount = 0;

	private volatile long commitCount = 0;

	private volatile long rollbackCount = 0;

	private volatile long readSkipCount = 0;

	private volatile long processSkipCount = 0;

	private volatile long writeSkipCount = 0;

	private volatile Date startTime = new Date(System.currentTimeMillis());

	private volatile Date endTime = null;

	private volatile Date lastUpdated = null;

	private volatile ExecutionContext executionContext = new ExecutionContext();

	private volatile ExitStatus exitStatus = ExitStatus.EXECUTING;

	private volatile boolean terminateOnly;

	private volatile long filterCount;

	private transient volatile List<Throwable> failureExceptions = new CopyOnWriteArrayList<>();

	/**
	 * Constructor with mandatory properties.
	 *
	 * @param stepName the step to which this execution belongs
	 * @param jobExecution the current job execution
	 * @param id the id of this execution
	 */
	public StepExecution(String stepName, JobExecution jobExecution, Long id) {
		this(stepName, jobExecution);
		Assert.notNull(jobExecution, "JobExecution must be provided to re-hydrate an existing StepExecution");
		Assert.notNull(id, "The entity Id must be provided to re-hydrate an existing StepExecution");
		setId(id);
		jobExecution.addStepExecution(this);
	}

	/**
	 * Constructor that substitutes in null for the execution id
	 *
	 * @param stepName the step to which this execution belongs
	 * @param jobExecution the current job execution
	 */
	public StepExecution(String stepName, JobExecution jobExecution) {
		super();
		Assert.hasLength(stepName, "A stepName is required");
		this.stepName = stepName;
		this.jobExecution = jobExecution;
	}

	/**
	 * Constructor that requires only a stepName.  Intended only to be
	 * used via serialization libraries to address the circular
	 * reference between {@link JobExecution} and StepExecution.
	 *
	 * @param stepName the name of the executed step
	 */
	@SuppressWarnings("unused")
	private StepExecution(String stepName) {
		super();
		Assert.hasLength(stepName, "A stepName is required");
		this.stepName = stepName;
		this.jobExecution = null;
	}

	/**
	 * Returns the {@link ExecutionContext} for this execution
	 *
	 * @return the attributes
	 */
	public ExecutionContext getExecutionContext() {
		return executionContext;
	}

	/**
	 * Sets the {@link ExecutionContext} for this execution
	 *
	 * @param executionContext the attributes
	 */
	public void setExecutionContext(ExecutionContext executionContext) {
		this.executionContext = executionContext;
	}

	/**
	 * Returns the current number of commits for this execution
	 *
	 * @return the current number of commits
	 */
	public long getCommitCount() {
		return commitCount;
	}

	/**
	 * Sets the current number of commits for this execution
	 *
	 * @param commitCount the current number of commits
	 */
	public void setCommitCount(long commitCount) {
		this.commitCount = commitCount;
	}

	/**
	 * Returns the time that this execution ended
	 *
	 * @return the time that this execution ended
	 */
	public Date getEndTime() {
		return endTime;
	}

	/**
	 * Sets the time that this execution ended
	 *
	 * @param endTime the time that this execution ended
	 */
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	/**
	 * Returns the current number of items read for this execution
	 *
	 * @return the current number of items read for this execution
	 */
	public long getReadCount() {
		return readCount;
	}

	/**
	 * Sets the current number of read items for this execution
	 *
	 * @param readCount the current number of read items for this execution
	 */
	public void setReadCount(long readCount) {
		this.readCount = readCount;
	}

	/**
	 * Returns the current number of items written for this execution
	 *
	 * @return the current number of items written for this execution
	 */
	public long getWriteCount() {
		return writeCount;
	}

	/**
	 * Sets the current number of written items for this execution
	 *
	 * @param writeCount the current number of written items for this execution
	 */
	public void setWriteCount(long writeCount) {
		this.writeCount = writeCount;
	}

	/**
	 * Returns the current number of rollbacks for this execution
	 *
	 * @return the current number of rollbacks for this execution
	 */
	public long getRollbackCount() {
		return rollbackCount;
	}

	/**
	 * Returns the current number of items filtered out of this execution
	 *
	 * @return the current number of items filtered out of this execution
	 */
	public long getFilterCount() {
		return filterCount;
	}

	/**
	 * Public setter for the number of items filtered out of this execution.
	 * @param filterCount the number of items filtered out of this execution to
	 * set
	 */
	public void setFilterCount(long filterCount) {
		this.filterCount = filterCount;
	}

	/**
	 * Setter for number of rollbacks for this execution
	 * @param rollbackCount long the number of rollbacks.
	 */
	public void setRollbackCount(long rollbackCount) {
		this.rollbackCount = rollbackCount;
	}

	/**
	 * Gets the time this execution started
	 *
	 * @return the time this execution started
	 */
	public Date getStartTime() {
		return startTime;
	}

	/**
	 * Sets the time this execution started
	 *
	 * @param startTime the time this execution started
	 */
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	/**
	 * Returns the current status of this step
	 *
	 * @return the current status of this step
	 */
	public BatchStatus getStatus() {
		return status;
	}

	/**
	 * Sets the current status of this step
	 *
	 * @param status the current status of this step
	 */
	public void setStatus(BatchStatus status) {
		this.status = status;
	}

	/**
	 * Upgrade the status field if the provided value is greater than the
	 * existing one. Clients using this method to set the status can be sure
	 * that they don't overwrite a failed status with an successful one.
	 *
	 * @param status the new status value
	 */
	public void upgradeStatus(BatchStatus status) {
		this.status = this.status.upgradeTo(status);
	}

	/**
	 * @return the name of the step
	 */
	public String getStepName() {
		return stepName;
	}

	/**
	 * Accessor for the job execution id.
	 *
	 * @return the jobExecutionId
	 */
	public Long getJobExecutionId() {
		if (jobExecution != null) {
			return jobExecution.getId();
		}
		return null;
	}

	/**
	 * @param exitStatus {@link ExitStatus} instance used to establish the exit status.
	 */
	public void setExitStatus(ExitStatus exitStatus) {
		this.exitStatus = exitStatus;
	}

	/**
	 * @return the exitCode
	 */
	public ExitStatus getExitStatus() {
		return exitStatus;
	}

	/**
	 * Accessor for the execution context information of the enclosing job.
	 *
	 * @return the {@link JobExecution} that was used to start this step
	 * execution.
	 */
	public JobExecution getJobExecution() {
		return jobExecution;
	}

	/**
	 * Factory method for {@link StepContribution}.
	 *
	 * @return a new {@link StepContribution}
	 */
	public StepContribution createStepContribution() {
		return new StepContribution(this);
	}

	/**
	 * On successful execution just before a chunk commit, this method should be
	 * called. Synchronizes access to the {@link StepExecution} so that changes
	 * are atomic.
	 *
	 * @param contribution {@link StepContribution} instance used to update the StepExecution state.
	 */
	public synchronized void apply(StepContribution contribution) {
		readSkipCount += contribution.getReadSkipCount();
		writeSkipCount += contribution.getWriteSkipCount();
		processSkipCount += contribution.getProcessSkipCount();
		filterCount += contribution.getFilterCount();
		readCount += contribution.getReadCount();
		writeCount += contribution.getWriteCount();
		exitStatus = exitStatus.and(contribution.getExitStatus());
	}

	/**
	 * On unsuccessful execution after a chunk has rolled back.
	 */
	public synchronized void incrementRollbackCount() {
		rollbackCount++;
	}

	/**
	 * @return flag to indicate that an execution should halt
	 */
	public boolean isTerminateOnly() {
		return this.terminateOnly;
	}

	/**
	 * Set a flag that will signal to an execution environment that this
	 * execution (and its surrounding job) wishes to exit.
	 */
	public void setTerminateOnly() {
		this.terminateOnly = true;
	}

	/**
	 * @return the total number of items skipped.
	 */
	public long getSkipCount() {
		return readSkipCount + processSkipCount + writeSkipCount;
	}

	/**
	 * Increment the number of commits
	 */
	public void incrementCommitCount() {
		commitCount++;
	}

	/**
	 * Convenience method to get the current job parameters.
	 *
	 * @return the {@link JobParameters} from the enclosing job, or empty if
	 * that is null
	 */
	public JobParameters getJobParameters() {
		if (jobExecution == null) {
			return new JobParameters();
		}
		return jobExecution.getJobParameters();
	}

	/**
	 * @return the number of records skipped on read
	 */
	public long getReadSkipCount() {
		return readSkipCount;
	}

	/**
	 * @return the number of records skipped on write
	 */
	public long getWriteSkipCount() {
		return writeSkipCount;
	}

	/**
	 * Set the number of records skipped on read
	 *
	 * @param readSkipCount long containing read skip count to be used for the step execution.
	 */
	public void setReadSkipCount(long readSkipCount) {
		this.readSkipCount = readSkipCount;
	}

	/**
	 * Set the number of records skipped on write
	 *
	 * @param writeSkipCount long containing write skip count to be used for the step execution.
	 */
	public void setWriteSkipCount(long writeSkipCount) {
		this.writeSkipCount = writeSkipCount;
	}

	/**
	 * @return the number of records skipped during processing
	 */
	public long getProcessSkipCount() {
		return processSkipCount;
	}

	/**
	 * Set the number of records skipped during processing.
	 *
	 * @param processSkipCount long containing process skip count to be used for the step execution.
	 */
	public void setProcessSkipCount(long processSkipCount) {
		this.processSkipCount = processSkipCount;
	}

	/**
	 * @return the Date representing the last time this execution was persisted.
	 */
	public Date getLastUpdated() {
		return lastUpdated;
	}

	/**
	 * Set the time when the StepExecution was last updated before persisting
	 *
	 * @param lastUpdated {@link Date} instance used to establish the last
	 * updated date for the Step Execution.
	 */
	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public List<Throwable> getFailureExceptions() {
		return failureExceptions;
	}

	public void addFailureException(Throwable throwable) {
		this.failureExceptions.add(throwable);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.springframework.batch.container.common.domain.Entity#equals(java.
	 * lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {

		Object jobExecutionId = getJobExecutionId();
		if (jobExecutionId == null || !(obj instanceof StepExecution) || getId() == null) {
			return super.equals(obj);
		}
		StepExecution other = (StepExecution) obj;

		return stepName.equals(other.getStepName()) && (jobExecutionId.equals(other.getJobExecutionId()))
				&& getId().equals(other.getId());
	}

	/**
	 * Deserialize and ensure transient fields are re-instantiated when read
	 * back.
	 *
	 * @param stream instance of {@link ObjectInputStream}.
	 *
	 * @throws IOException thrown if error occurs during read.
	 * @throws ClassNotFoundException thrown if class is not found.
	 */
	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		failureExceptions = new ArrayList<>();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.batch.container.common.domain.Entity#hashCode()
	 */
	@Override
	public int hashCode() {
		Object jobExecutionId = getJobExecutionId();
		Long id = getId();
		return super.hashCode() + 31 * (stepName != null ? stepName.hashCode() : 0) + 91
				* (jobExecutionId != null ? jobExecutionId.hashCode() : 0) + 59 * (id != null ? id.hashCode() : 0);
	}

	@Override
	public String toString() {
		return String.format(getSummary() + ", exitDescription=%s", exitStatus.getExitDescription());
	}

	public String getSummary() {
		return super.toString()
				+ String.format(
						", name=%s, status=%s, exitStatus=%s, readCount=%d, filterCount=%d, writeCount=%d readSkipCount=%d, writeSkipCount=%d"
								+ ", processSkipCount=%d, commitCount=%d, rollbackCount=%d", stepName, status,
								exitStatus.getExitCode(), readCount, filterCount, writeCount, readSkipCount, writeSkipCount,
								processSkipCount, commitCount, rollbackCount);
	}

}
