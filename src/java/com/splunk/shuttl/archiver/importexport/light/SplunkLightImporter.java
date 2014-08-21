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
package com.splunk.shuttl.archiver.importexport.light;

import com.splunk.shuttl.archiver.importexport.BucketImporter;
import com.splunk.shuttl.archiver.importexport.ShellExecutor;
import com.splunk.shuttl.archiver.model.LocalBucket;

public class SplunkLightImporter implements BucketImporter {

	private final SplunkRebuildTool splunkRebuildTool;
	private final ShellExecutor shellExecutor;

	public SplunkLightImporter(SplunkRebuildTool splunkRebuildTool,
			ShellExecutor shellExecutor) {
		this.splunkRebuildTool = splunkRebuildTool;
		this.shellExecutor = shellExecutor;
	}

	@Override
	public LocalBucket importBucket(LocalBucket b) {
		throw new UnsupportedOperationException();
	}

	public static SplunkLightImporter create() {
		return new SplunkLightImporter(new SplunkRebuildTool(),
				ShellExecutor.getInstance());
	}

	private static class SplunkRebuildTool {

	}

}
