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
package com.splunk.shuttl.archiver.util;

import static com.splunk.shuttl.archiver.LogFormatter.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

/**
 * File utils for production code. Using the name UtilsFile instead of FileUtils
 * to avoid naming collisions with {@link FileUtils}.
 */
public class UtilsFile {

	private final static Logger logger = Logger.getLogger(UtilsFile.class);

	/**
	 * Creates a {@link File} and its parents without throwing exceptions. See
	 * {@link FileUtils#touch(File)}
	 */
	public static void touch(File file) {
		try {
			FileUtils.touch(file);
		} catch (IOException e) {
			logger.debug(did("Tried to create file and its parents",
					"Got IOException", "The file to be created", "file", file,
					"exception", e));
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return {@link FileOutputStream} for the file sent in. Logs and throws a
	 *         RuntimeException if something goes wrong.
	 */

	public static FileOutputStream getFileOutputStreamSilent(File file) {
		try {
			return new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			logger.debug(did(
					"Created a FileOuputStream for file: " + file.getAbsolutePath(),
					"Got FileNotFoundException", "File to exist", "file", file,
					"exception", e));
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param file
	 * @return
	 */
	public static RandomAccessFile getRandomAccessFileSilent(File file) {
		try {
			return new RandomAccessFile(file, "rw");
		} catch (FileNotFoundException e) {
			logger
					.debug(did(
							"Created a RandomAccessFile for file: " + file.getAbsolutePath(),
							e, "To create the RandomAccessFile object", "file", file,
							"exception", e));
			throw new RuntimeException(e);
		}
	}


		/**
	 * Gets the file name with the given extension. <br/>
	 * <br/>
	 * Note: Doesn't use {@link FilenameUtils#removeExtension(String)} because it
	 * doesn't account for multiple extensions, i.e. tar.gz
	 */
	public static String getFileNameSansExt(File file, String extension) {
		String fileName = file.getName();
		int extensionIndex = fileName.lastIndexOf(extension);
		return fileName.substring(0, extensionIndex);
	}
}
