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

import com.splunk.shuttl.archiver.model.Bucket;

/**
 * Exception thrown when trying to thaw a bucket that already has
 * been thawed.
 */
public class BucketAlreadyThawedException extends Exception {

	private static final long serialVersionUID = 1L;

	private final Bucket bucket;

	/**
	 * @param bucket
	 */
	public BucketAlreadyThawedException(Bucket bucket) {
		this.bucket = bucket;
	}

	/**
	 * @return the bucket
	 */
	public Bucket getBucket() {
		return bucket;
	}

}
