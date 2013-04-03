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
package com.splunk.shuttl.archiver.archive;

import static java.util.Arrays.*;
import static org.mockito.Mockito.*;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.splunk.shuttl.archiver.bucketlock.BucketLock;
import com.splunk.shuttl.archiver.bucketlock.SimpleFileLock.NotLockedException;
import com.splunk.shuttl.archiver.model.LocalBucket;

@Test(groups = { "fast-unit" })
public class BucketShuttlerRunnerTest {

	BucketShuttlerRunner bucketArchiverRunner;
	BucketShuttler bucketShuttler;
	LocalBucket bucket;
	BucketLock bucketLock;

	@BeforeMethod
	public void setUp() {
		bucketShuttler = mock(BucketShuttler.class);
		bucket = mock(LocalBucket.class);
		bucketLock = mock(BucketLock.class);
		when(bucketLock.isLocked()).thenReturn(true);
		bucketArchiverRunner = new BucketShuttlerRunner(bucketShuttler, bucket,
				asList(bucketLock));
	}

	@Test(expectedExceptions = { NotLockedException.class })
	public void constructor_bucketLockIsNotLocked_throwNotLockedException() {
		BucketLock bucketLock = mock(BucketLock.class);
		when(bucketLock.isLocked()).thenReturn(false);
		new BucketShuttlerRunner(bucketShuttler, bucket, asList(bucketLock));
	}

	@Test(groups = { "fast-unit" })
	public void run_successfulArchiving_closesAndDeletesBucketLock() {
		bucketArchiverRunner.run();
		verify(bucketLock).closeLock();
		verify(bucketLock).deleteLockFile();
	}

	public void run_failedArchiving_closesAndDeletesBucketLock() {
		doThrow(new RuntimeException()).when(bucketShuttler).shuttlBucket(bucket);
		try {
			bucketArchiverRunner.run();
		} catch (RuntimeException re) {
			// Do nothing.
		}
		verify(bucketLock).closeLock();
		verify(bucketLock).deleteLockFile();
	}

	@Test(expectedExceptions = { IllegalStateException.class })
	public void run_bucketLockUnlocked_throwIllegalStateException() {
		when(bucketLock.isLocked()).thenReturn(false);
		bucketArchiverRunner.run();
	}

}
