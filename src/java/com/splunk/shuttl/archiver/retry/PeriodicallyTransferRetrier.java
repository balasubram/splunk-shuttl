// Copyright (C) 2011 Splunk Inc.
//
// Splunk Inc. licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.splunk.shuttl.archiver.retry;

import com.splunk.shuttl.archiver.archive.recovery.FailedBucketsArchiver;
import com.splunk.shuttl.archiver.bucketlock.BucketLocker.SharedLockBucketHandler;

/**
 * Periodically retries to transfer buckets moved by the bucket mover.
 */
public class PeriodicallyTransferRetrier implements Runnable {

	private final FailedBucketsArchiver failedBucketsArchiver;
	private final SharedLockBucketHandler sharedLockBucketHandler;
	private final long retryTime;

	private boolean stop = false;

	public PeriodicallyTransferRetrier(
			FailedBucketsArchiver failedBucketsArchiver,
			SharedLockBucketHandler sharedLockBucketHandler, long retryTime) {
		this.failedBucketsArchiver = failedBucketsArchiver;
		this.sharedLockBucketHandler = sharedLockBucketHandler;
		this.retryTime = retryTime;
	}

	@Override
	public void run() {
		while (!stop && !Thread.currentThread().isInterrupted()) {
			try {
				Thread.sleep(retryTime);
			} catch (InterruptedException e) {
				return;
			}
			failedBucketsArchiver.archiveFailedBuckets(sharedLockBucketHandler);
		}
	}

	/**
	 * Stops the retrier
	 */
	public void stop() {
		stop = true;
	}

}
