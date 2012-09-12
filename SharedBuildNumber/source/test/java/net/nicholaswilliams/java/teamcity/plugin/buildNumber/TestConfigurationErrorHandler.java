/*
 * TestConfigurationErrorHandler.java from TeamCityPlugins modified Monday, September 10, 2012 12:17:57 CDT (-0500).
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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Test class for ConfigurationErrorHandler.
 */
public class TestConfigurationErrorHandler
{
	private ConfigurationErrorHandler handler;

	@Before
	public void setUp()
	{
		this.handler = new ConfigurationErrorHandler();
	}

	@After
	public void tearDown()
	{

	}

	@Test(expected=SAXParseException.class)
	public void testWarning() throws SAXException
	{
		this.handler.warning(new SAXParseException("one", "two", "three", 4, 5));
	}

	@Test(expected=SAXParseException.class)
	public void testError() throws SAXException
	{
		this.handler.error(new SAXParseException("one", "two", "three", 4, 5));
	}

	@Test(expected=SAXParseException.class)
	public void testFatal() throws SAXException
	{
		this.handler.fatalError(new SAXParseException("one", "two", "three", 4, 5));
	}
}