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
package com.splunk.shuttl.server.mbeans;

import java.io.FileNotFoundException;

import org.apache.log4j.Logger;

import com.splunk.shuttl.server.mbeans.util.JAXBUtils;

/**
 * Base class containing logic for saving and refreshing a physical .xml file.
 */
public abstract class MBeanBase<T> implements MBeanPersistance {
	Logger logger = Logger.getLogger(MBeanBase.class);

	@Override
	public void save() throws ShuttlMBeanException {
		try {
			JAXBUtils.save(getConfClass(), getConfObject(), getPathToXmlFile());
		} catch (Exception e) {
			logger.error(e);
			throw new ShuttlMBeanException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void refresh() throws ShuttlMBeanException {
		try {
			T conf = (T) JAXBUtils.refresh(getConfClass(), getPathToXmlFile());
			setConfObject(conf);
		} catch (FileNotFoundException fnfe) {
			throw new RuntimeException(fnfe);
		} catch (Exception e) {
			logger.error(e);
			throw new ShuttlMBeanException(e);
		}
	}

	protected abstract String getPathToXmlFile();

	protected abstract T getConfObject();

	protected abstract Class<T> getConfClass();

	protected abstract void setConfObject(T conf);

}
