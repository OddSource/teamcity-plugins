/*
 * TestAbstractLinuxPropertiesLocator.java from TeamCityPlugins modified Wednesday, September 12, 2012 17:34:18 CDT (-0500).
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

import org.apache.commons.io.FileUtils;
import org.easymock.Capture;
import org.easymock.IAnswer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Test class for AbstractLinuxPropertiesLocator.
 */
public class TestAbstractLinuxPropertiesLocator
{
	private AbstractLinuxPropertiesLocator locator;

	@Before
	public void setUp()
	{
		this.locator = createMockBuilder(AbstractLinuxPropertiesLocator.class).withConstructor().createStrictMock();
	}

	@After
	public void tearDown()
	{
		verify(this.locator);
	}

	@Test
	public void testLocateProperties01()
	{
		File pluginRoot = new File(".");

		final Capture<Map<String, String>> capture = new Capture<Map<String, String>>();

		this.locator.locateLinuxProperties(capture(capture));
		expectLastCall().andAnswer(new IAnswer<Void>()
		{
			@Override
			public Void answer() throws Throwable
			{
				assertTrue("The properties map should be empty.", capture.getValue().isEmpty());

				capture.getValue().put("myLinuxProperty01", "1.0.4");

				return null;
			}
		});

		replay(this.locator);

		Map<String, String> properties = this.locator.locateProperties(pluginRoot);

		assertNotNull("The properties should not be null.", properties);
		assertEquals("The linux property is not correct.", "1.0.4", properties.get("myLinuxProperty01"));
		assertFalse("The version property should not be present.",
					properties.containsKey(LinuxPropertiesLocator.PLUGIN_VERSION_KEY));
	}

	@Test
	public void testLocateProperties02() throws IOException
	{
		File pluginRoot = new File(".");
		File versionFile = new File(pluginRoot, "version.properties");
		FileUtils.writeStringToFile(versionFile, "");

		final Capture<Map<String, String>> capture = new Capture<Map<String, String>>();

		this.locator.locateLinuxProperties(capture(capture));
		expectLastCall().andAnswer(new IAnswer<Void>()
		{
			@Override
			public Void answer() throws Throwable
			{
				assertTrue("The properties map should be empty.", capture.getValue().isEmpty());

				capture.getValue().put("yourLinuxProperty02", "3.9.1");

				return null;
			}
		});

		replay(this.locator);

		try
		{
			Map<String, String> properties = this.locator.locateProperties(pluginRoot);

			assertNotNull("The properties should not be null.", properties);
			assertEquals("The linux property is not correct.", "3.9.1", properties.get("yourLinuxProperty02"));
			assertFalse("The version property should not be present.",
						properties.containsKey(LinuxPropertiesLocator.PLUGIN_VERSION_KEY));
		}
		finally
		{
			FileUtils.forceDelete(versionFile);
		}
	}

	@Test
	public void testLocateProperties03() throws IOException
	{
		File pluginRoot = new File(".");
		File versionFile = new File(pluginRoot, "version.properties");
		FileUtils.writeStringToFile(versionFile, LinuxPropertiesLocator.PLUGIN_VERSION_KEY + "=0.9.16-beta");

		final Capture<Map<String, String>> capture = new Capture<Map<String, String>>();

		this.locator.locateLinuxProperties(capture(capture));
		expectLastCall().andAnswer(new IAnswer<Void>()
		{
			@Override
			public Void answer() throws Throwable
			{
				assertFalse("The properties map should not be empty.", capture.getValue().isEmpty());
				assertEquals("The version property is not correct.", "0.9.16-beta",
							 capture.getValue().get(LinuxPropertiesLocator.PLUGIN_VERSION_KEY));

				capture.getValue().put("finalLinuxProperty03", "SuSE");

				return null;
			}
		});

		replay(this.locator);

		try
		{
			Map<String, String> properties = this.locator.locateProperties(pluginRoot);

			assertNotNull("The properties should not be null.", properties);
			assertEquals("The linux property is not correct.", "SuSE", properties.get("finalLinuxProperty03"));
			assertEquals("The version property is not correct.", "0.9.16-beta",
						 properties.get(LinuxPropertiesLocator.PLUGIN_VERSION_KEY));
		}
		finally
		{
			FileUtils.forceDelete(versionFile);
		}
	}

	@Test
	public void testLocateProperties04() throws IOException
	{
		File pluginRoot = new File(".");
		File versionFile = new File(pluginRoot, "version.properties");
		FileUtils.writeStringToFile(versionFile, LinuxPropertiesLocator.PLUGIN_VERSION_KEY + "=1.0.1");

		final Capture<Map<String, String>> capture = new Capture<Map<String, String>>();

		this.locator.locateLinuxProperties(capture(capture));
		expectLastCall().andAnswer(new IAnswer<Void>()
		{
			@Override
			public Void answer() throws Throwable
			{
				assertFalse("The properties map should not be empty.", capture.getValue().isEmpty());
				assertEquals("The version property is not correct.", "1.0.1",
							 capture.getValue().get(LinuxPropertiesLocator.PLUGIN_VERSION_KEY));

				capture.getValue().put("finalLinuxProperty03", "RedHat");

				return null;
			}
		});

		replay(this.locator);

		try
		{
			Map<String, String> properties = this.locator.locateProperties(pluginRoot);

			assertNotNull("The properties should not be null.", properties);
			assertEquals("The linux property is not correct.", "RedHat", properties.get("finalLinuxProperty03"));
			assertEquals("The version property is not correct.", "1.0.1",
						 properties.get(LinuxPropertiesLocator.PLUGIN_VERSION_KEY));
		}
		finally
		{
			FileUtils.forceDelete(versionFile);
		}
	}
}