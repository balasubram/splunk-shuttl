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
package com.splunk.shuttl.archiver.bucketlock;

import static org.testng.AssertJUnit.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.splunk.shuttl.testutil.ShellClassRunner;
import com.splunk.shuttl.testutil.TUtilsFile;

/**
 * Fixture: One {@link SimpleFileLock} running in this JVM and another
 * {@link SimpleFileLock} in another JVM, that uses
 * {@link SimpleFileLock#tryLockShared()} to acquire lock.
 */
@Test(groups = { "slow-unit" })
public class SimpleFileLockSharedTest {

	private static final File FILE_TO_LOCK = new File(
			TUtilsFile.getShuttlTestDirectory(),
			SimpleFileLockSharedTest.class.getName() + "-fileToLock");

	private static final int GOT_SHARED_LOCK = 5;
	private static final int NO_SHARED_LOCK = 4;
	private static final int GOT_EXCLUSIVE_LOCK = 10;
	private static final int NO_EXCLUSIVE_LOCK = 11;
	private static final int DATA = 18;

	SimpleFileLock simpleLock;
	ShellClassRunner otherJvmLocker;

	@BeforeMethod
	public void setUp() throws IOException {
		assertTrue(FILE_TO_LOCK.createNewFile());

		otherJvmLocker = new ShellClassRunner();
		simpleLock = getSimpleFileLock();
	}

	@AfterMethod
	public void tearDown() throws IOException {
		otherJvmLocker.kill();
		FileUtils.forceDelete(FILE_TO_LOCK);
	}

	/**
	 * Read this test case to understand the rest of the test cases. <br/>
	 * <br/>
	 * Uses the {@link LockShared_Release} class in combination with
	 * {@link ShellClassRunner} for first creating a shared lock in another JVM,
	 * and then trying to lock again on this JVM, which doesn't work. <br/>
	 * See {@link LockShared_Release} for more information of how it works.
	 */
	@Test(groups = { "slow-unit" }, timeOut = 3000)
	public void tryLockShared_noLockOnFile_acquiresLockAndCannotBeLockWithTryLock()
			throws IOException {
		otherJvmLocker.runClassAsync(LockShared_Release.class);
		waitForOtherJvmToTryLock();
		assertFalse(simpleLock.tryLockExclusive());
		signalOtherJvmToFinish();
		assertEquals(GOT_SHARED_LOCK, (int) otherJvmLocker.getExitCode());
	}

	private void waitForOtherJvmToTryLock() throws IOException {
		InputStream in = otherJvmLocker.getInputStreamFromClass();
		in.read();
	}

	private void signalOtherJvmToFinish() throws IOException {
		OutputStream out = otherJvmLocker.getOutputStreamToClass();
		out.write(DATA);
		out.flush();
	}

	@Test(timeOut = 3000)
	public void tryLockShared_lockIsConvertedBetweenExclusiveToShared_acquiresLockAndExclusiveLockAfterDoesntWork()
			throws IOException {
		assertTrue(simpleLock.tryLockExclusive());
		assertTrue(simpleLock.tryConvertExclusiveToSharedLock());
		otherJvmLocker.runClassAsync(LockShared_Release.class);
		waitForOtherJvmToTryLock();
		simpleLock.closeLock();

		// Should not acquire lock even though we just closed it, because of the
		// shared lock by LockShared_Release in the other JVM.
		SimpleFileLock newLock = getSimpleFileLock();
		assertFalse(newLock.tryLockExclusive());
		signalOtherJvmToFinish();
		assertEquals(GOT_SHARED_LOCK, (int) otherJvmLocker.getExitCode());
	}

	@Test(timeOut = 3000)
	public void tryLockShared_fileIsLockedExclusive_cannotGetSharedLock()
			throws IOException {
		assertTrue(simpleLock.tryLockExclusive());
		otherJvmLocker.runClassAsync(LockShared_Release.class);
		waitForOtherJvmToTryLock();
		signalOtherJvmToFinish();
		simpleLock.closeLock();
		assertEquals(NO_SHARED_LOCK, (int) otherJvmLocker.getExitCode());
	}

	@Test(timeOut = 3000)
	public void tryLockExclusive_fileIsLockedExclusive_otherJvmCannotGetExclusiveLock()
			throws IOException {
		assertTrue(simpleLock.tryLockExclusive());
		otherJvmLocker.runClassAsync(LockExclusive_Release.class);
		waitForOtherJvmToTryLock();
		signalOtherJvmToFinish();
		simpleLock.closeLock();
		assertEquals(NO_EXCLUSIVE_LOCK, (int) otherJvmLocker.getExitCode());
	}

	@Test(timeOut = 3000)
	public void tryLockExclusive_otherJvmGetsExclusiveLock_thisJvmCannotGetExclusiveLock()
			throws IOException {
		otherJvmLocker.runClassAsync(LockExclusive_Release.class);
		waitForOtherJvmToTryLock();
		assertFalse(simpleLock.tryLockExclusive());
		signalOtherJvmToFinish();
		simpleLock.closeLock();
		assertEquals(GOT_EXCLUSIVE_LOCK, (int) otherJvmLocker.getExitCode());
	}

	@Test(timeOut = 3000)
	public void tryLockExclusive_otherJvmGetsExclusiveLockThenGetsKilled_thisJvmGetsExclusiveLock()
			throws IOException {
		otherJvmLocker.runClassAsync(LockExclusive_Release.class);
		waitForOtherJvmToTryLock();
		assertFalse(simpleLock.tryLockExclusive());
		otherJvmLocker.kill();
		assertTrue(simpleLock.tryLockExclusive());
		simpleLock.closeLock();
	}

	private static SimpleFileLock getSimpleFileLock() {
		return SimpleFileLock.createFromFile(FILE_TO_LOCK);
	}

	/**
	 * Communicates via stdin and stdout, to synchronize that calls are made in
	 * the right order. This class first locks a {@link SimpleFileLock} with a
	 * shared lock, writes to stdout and waits for a response. It releases the
	 * lock once it has gotten a response.
	 */
	private static class LockShared_Release {
		@SuppressWarnings("unused")
		public static void main(String[] args) throws IOException {
			SimpleFileLock sharedLock = getSimpleFileLock();
			boolean lockedShared = sharedLock.tryLockShared();
			writeData(); // Notifies that it's first action has been made
			readData(); // Waits until it can go again.
			sharedLock.closeLock();
			if (lockedShared)
				System.exit(GOT_SHARED_LOCK);
			else
				System.exit(NO_SHARED_LOCK);
		}
	}

	private static void writeData() {
		System.out.write(DATA);
		System.out.flush();
	}

	private static void readData() throws IOException {
		InputStream in = System.in;
		in.read();
	}

	/**
	 * Communicates via stdin and stdout, to synchronize that calls are made in
	 * the right order. This class first locks a {@link SimpleFileLock} with a
	 * shared lock, writes to stdout and waits for a response. It releases the
	 * lock once it has gotten a response.
	 */
	private static class LockExclusive_Release {
		@SuppressWarnings("unused")
		public static void main(String[] args) throws IOException {
			SimpleFileLock exclusiveLock = getSimpleFileLock();
			boolean lockExclusive = exclusiveLock.tryLockExclusive();
			writeData(); // Notifies that it's first action has been made
			readData(); // Waits until it can go again.
			exclusiveLock.closeLock();
			if (lockExclusive)
				System.exit(GOT_EXCLUSIVE_LOCK);
			else
				System.exit(NO_EXCLUSIVE_LOCK);
		}
	}
}
