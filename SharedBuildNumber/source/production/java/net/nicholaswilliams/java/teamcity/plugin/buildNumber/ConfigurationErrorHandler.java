/*
 * ConfigurationErrorHandler.java from TeamCityPlugins modified Monday, September 10, 2012 10:20:54 CDT (-0500).
 *
 * Copyright 2010-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.nicholaswilliams.java.teamcity.plugin.buildNumber;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Simple XML error handler that makes exceptions be thrown when validation problems are encountered.
 *
 * @author Nick Williams
 * @version 1.0.0
 * @since 1.0.0
 */
public class ConfigurationErrorHandler implements ErrorHandler
{
	@Override
	public void warning(SAXParseException e) throws SAXException
	{
		throw e;
	}

	@Override
	public void error(SAXParseException e) throws SAXException
	{
		throw e;
	}

	@Override
	public void fatalError(SAXParseException e) throws SAXException
	{
		throw e;
	}
}
