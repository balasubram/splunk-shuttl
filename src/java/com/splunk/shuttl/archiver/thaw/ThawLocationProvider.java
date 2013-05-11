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
package com.splunk.shuttl.archiver.thaw;

import static com.splunk.shuttl.archiver.LogFormatter.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.splunk.shuttl.archiver.LocalFileSystemPaths;
import com.splunk.shuttl.archiver.model.Bucket;

/**
 * Provides location for where to thaw {@link Bucket}s.
 */
public class ThawLocationProvider {

	private final SplunkIndexesLayer splunkIndexesLayer;
	private final LocalFileSystemPaths localFileSystemPaths;
	private final ThawBucketDirectoryNamer thawBucketDirectoryNamer;

	/**
	 * @param splunkIndexesLayer
	 *          for looking up the thaw directory.
	 * @param localFileSystemPaths
	 *          thaw buckets live while they are transfered.
	 * @param thawBucketDirectoryNamer
	 */
	public ThawLocationProvider(SplunkIndexesLayer splunkIndexesLayer,
			LocalFileSystemPaths localFileSystemPaths,
			ThawBucketDirectoryNamer thawBucketDirectoryNamer) {
		this.splunkIndexesLayer = splunkIndexesLayer;
		this.localFileSystemPaths = localFileSystemPaths;
		this.thawBucketDirectoryNamer = thawBucketDirectoryNamer;
	}

	/**
	 * @param bucket
	 *          to get location in thaw for.
	 * @throws IOException
	 *           if that location can not be found.
	 */
	public File getLocationInThawForBucket(Bucket bucket) throws IOException {
		File thawLocation = splunkIndexesLayer.getThawLocation(bucket.getIndex());
		String bucketDirectoryName = thawBucketDirectoryNamer
				.getBucketDirectoryName(bucket);
		return new File(thawLocation, bucketDirectoryName);
	}

	/**
	 * @param bucket
	 *          to get transfer location for.
	 * @return non existing local where the bucket can be transfered.
	 */
	public File getThawTransferLocation(Bucket bucket) {
		File transferDir = localFileSystemPaths.getThawTransfersDirectory(bucket);
		File file = new File(transferDir, bucket.getName());
		if (file.exists())
			deleteFile(file);
		return file;
	}

	private void deleteFile(File file) {
		boolean wasDeleted = FileUtils.deleteQuietly(file);
		if (!wasDeleted)
			Logger.getLogger(getClass()).warn(
					warn("Tried deleting a file", "Could not delete it",
							"Will not do anything", "file", file));
	}

}
