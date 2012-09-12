/*
 * TestAdditionalServerPropertyExtension.java from TeamCityPlugins modified Wednesday, September 12, 2012 17:19:45 CDT (-0500).
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

package net.nicholaswilliams.java.teamcity.plugin.linux;

import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Test class for AdditionalServerPropertyExtension.
 */
public class TestAdditionalServerPropertyExtension
{
	private PluginDescriptor descriptor;

	private LinuxPropertiesLocator locator;

	private AdditionalServerPropertyExtension extension;

	@Before
	public void setUp()
	{
		this.descriptor = createStrictMock(PluginDescriptor.class);

		this.locator = createStrictMock(LinuxPropertiesLocator.class);

		this.extension = new AdditionalServerPropertyExtension(this.descriptor, this.locator);
	}

	@After
	public void tearDown()
	{
		verify(this.descriptor, this.locator);
	}

	@Test
	public void testRegister01()
	{
		File pluginRoot = new File(".");
		Map<String, String> properties = new LinkedHashMap<String, String>();

		expect(this.descriptor.getPluginRoot()).andReturn(pluginRoot);
		expect(this.locator.locateProperties(same(pluginRoot))).andReturn(properties);

		replay(this.descriptor, this.locator);

		int size = System.getProperties().size();
		assertTrue("There should be system properties.", size > 1);

		this.extension.register();

		assertEquals("The number of properties should not have changed.", size, System.getProperties().size());
	}

	@Test
	public void testRegister02()
	{
		File pluginRoot = new File(".");
		Map<String, String> properties = new LinkedHashMap<String, String>();
		properties.put("myProperty1", "someValue");
		properties.put("yourOtherTwo", "anotherAwesome");
		properties.put("finalLastCool", "okayNifty");

		expect(this.descriptor.getPluginRoot()).andReturn(pluginRoot);
		expect(this.locator.locateProperties(same(pluginRoot))).andReturn(properties);

		replay(this.descriptor, this.locator);

		assertNull("myProperty1 should be null.", System.getProperty("myProperty1"));
		assertNull("yourOtherTwo should be null.", System.getProperty("yourOtherTwo"));
		assertNull("finalLastCool should be null.", System.getProperty("finalLastCool"));

		try
		{
			this.extension.register();

			assertEquals("myProperty1 is not correct.", "someValue", System.getProperty("myProperty1"));
			assertEquals("yourOtherTwo is not correct.", "anotherAwesome", System.getProperty("yourOtherTwo"));
			assertEquals("finalLastCool is not correct.", "okayNifty", System.getProperty("finalLastCool"));
		}
		finally
		{
			System.getProperties().remove("myProperty1");
			System.getProperties().remove("yourOtherTwo");
			System.getProperties().remove("finalLastCool");

			assertNull("myProperty1 should be null again.", System.getProperty("myProperty1"));
			assertNull("yourOtherTwo should be null again.", System.getProperty("yourOtherTwo"));
			assertNull("finalLastCool should be null again.", System.getProperty("finalLastCool"));
		}
	}

	@Test
	public void testRegister03()
	{
		File pluginRoot = new File(".");
		Map<String, String> properties = new LinkedHashMap<String, String>();
		properties.put("my.property", "your.value");

		expect(this.descriptor.getPluginRoot()).andReturn(pluginRoot);
		expect(this.locator.locateProperties(same(pluginRoot))).andReturn(properties);

		replay(this.descriptor, this.locator);

		assertNull("my.property should be null.", System.getProperty("my.property"));

		try
		{
			this.extension.register();

			assertEquals("my.property is not correct.", "your.value", System.getProperty("my.property"));
		}
		finally
		{
			System.getProperties().remove("my.property");

			assertNull("my.property should be null again.", System.getProperty("my.property"));
		}
	}
}