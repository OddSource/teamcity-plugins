/*
 * TestPluginConfigurationServiceDefault.java from TeamCityPlugins modified Saturday, September 15, 2012 11:41:35 CDT (-0500).
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

import jetbrains.buildServer.serverSide.ServerPaths;
import net.nicholaswilliams.java.teamcity.plugin.buildNumber.pojo.ConfigurationEntity;
import net.nicholaswilliams.java.teamcity.plugin.buildNumber.pojo.SettingsEntity;
import net.nicholaswilliams.java.teamcity.plugin.buildNumber.pojo.SharedBuildNumber;
import net.nicholaswilliams.java.teamcity.plugin.buildNumber.pojo.SharedBuildNumberEntity;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.easymock.IAnswer;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.FatalBeanException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.SortedSet;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Test class for PluginConfigurationServiceDefault.
 */
public class TestPluginConfigurationServiceDefault
{
	private static final File workingDirectory = PluginFileUtils.getCanonicalFile(new File("."));

	private PluginConfigurationServiceDefault service;

	@Before
	public void setUp()
	{
		ServerPaths serverPaths = new ServerPaths("bad/path1", workingDirectory.getPath(), "bad/path2");

		this.service = createMockBuilder(PluginConfigurationServiceDefault.class)
				.withConstructor(ServerPaths.class)
				.withArgs(serverPaths)
				.addMockedMethod("saveConfiguration")
				.createStrictMock();
	}

	@After
	public void tearDown()
	{

	}

	private ConfigurationEntity getConfiguration()
	{
		ConfigurationEntity configuration = new ConfigurationEntity();
		configuration.setLastUpdate(new DateTime());
		configuration.setSettings(new SettingsEntity());

		try
		{
			Field field = PluginConfigurationServiceDefault.class.getDeclaredField("configuration");
			field.setAccessible(true);
			field.set(this.service, configuration);
		}
		catch(IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
		catch(NoSuchFieldException e)
		{
			throw new RuntimeException(e);
		}

		return configuration;
	}

	@Test
	public void testGetNextBuildNumberId01() throws IOException
	{
		final ConfigurationEntity configuration = this.getConfiguration();

		assertEquals("The initial sequence value is not correct.", 1,
					 configuration.getSettings().getBuildNumberIdSequence());

		this.service.saveConfiguration();
		expectLastCall().andAnswer(new IAnswer<Void>()
		{
			@Override
			public Void answer() throws Throwable
			{
				assertEquals("The new sequence value is not correct.", 2,
							 configuration.getSettings().getBuildNumberIdSequence());

				return null;
			}
		});

		replay(this.service);

		int id = this.service.getNextBuildNumberId();

		assertEquals("The build number ID is not correct.", 1, id);

		verify(this.service);
	}

	@Test
	public void testGetNextBuildNumberId02() throws IOException
	{
		final ConfigurationEntity configuration = this.getConfiguration();
		configuration.getSettings().setBuildNumberIdSequence(15);

		assertEquals("The initial sequence value is not correct.", 15,
					 configuration.getSettings().getBuildNumberIdSequence());

		this.service.saveConfiguration();
		expectLastCall().andAnswer(new IAnswer<Void>()
		{
			@Override
			public Void answer() throws Throwable
			{
				assertEquals("The new sequence value is not correct.", 16,
							 configuration.getSettings().getBuildNumberIdSequence());

				return null;
			}
		});

		replay(this.service);

		int id = this.service.getNextBuildNumberId();

		assertEquals("The build number ID is not correct.", 15, id);

		verify(this.service);
	}

	@Test
	public void testGetAllSharedBuildNumberIds01()
	{
		final ConfigurationEntity configuration = this.getConfiguration();

		SharedBuildNumberEntity sharedBuildNumber1 = new SharedBuildNumberEntity();
		sharedBuildNumber1.setId(5);
		configuration.addOrUpdateBuildNumber(sharedBuildNumber1);

		SharedBuildNumberEntity sharedBuildNumber2 = new SharedBuildNumberEntity();
		sharedBuildNumber2.setId(22);
		configuration.addOrUpdateBuildNumber(sharedBuildNumber2);

		SharedBuildNumberEntity sharedBuildNumber3 = new SharedBuildNumberEntity();
		sharedBuildNumber3.setId(1);
		configuration.addOrUpdateBuildNumber(sharedBuildNumber3);

		replay(this.service);

		int[] ids = this.service.getAllSharedBuildNumberIds();

		assertNotNull("The list of IDs should not be null.", ids);
		assertTrue("The list of IDs should contain 1.", ArrayUtils.contains(ids, 1));
		assertTrue("The list of IDs should contain 5.", ArrayUtils.contains(ids, 5));
		assertTrue("The list of IDs should contain 22.", ArrayUtils.contains(ids, 22));

		verify(this.service);
	}

	@Test
	public void testGetAllSharedBuildNumbersSortedById01()
	{
		final ConfigurationEntity configuration = this.getConfiguration();

		SharedBuildNumberEntity sharedBuildNumber1 = new SharedBuildNumberEntity();
		sharedBuildNumber1.setId(5);
		configuration.addOrUpdateBuildNumber(sharedBuildNumber1);

		SharedBuildNumberEntity sharedBuildNumber2 = new SharedBuildNumberEntity();
		sharedBuildNumber2.setId(22);
		configuration.addOrUpdateBuildNumber(sharedBuildNumber2);

		SharedBuildNumberEntity sharedBuildNumber3 = new SharedBuildNumberEntity();
		sharedBuildNumber3.setId(1);
		configuration.addOrUpdateBuildNumber(sharedBuildNumber3);

		replay(this.service);

		SortedSet<SharedBuildNumber> set = this.service.getAllSharedBuildNumbersSortedById(false);

		assertNotNull("The set should not be null.", set);
		assertEquals("The set is the wrong size.", 3, set.size());

		SharedBuildNumberEntity[] values = new SharedBuildNumberEntity[] {
				sharedBuildNumber3, sharedBuildNumber1, sharedBuildNumber2
		};

		int i = 0;
		for(SharedBuildNumber sharedBuildNumber : set)
		{
			assertEquals("The build number is not correct.", values[i].getId(), sharedBuildNumber.getId());

			i++;
		}

		verify(this.service);
	}

	@Test
	public void testGetAllSharedBuildNumbersSortedById02()
	{
		final ConfigurationEntity configuration = this.getConfiguration();

		SharedBuildNumberEntity sharedBuildNumber1 = new SharedBuildNumberEntity();
		sharedBuildNumber1.setId(5);
		configuration.addOrUpdateBuildNumber(sharedBuildNumber1);

		SharedBuildNumberEntity sharedBuildNumber2 = new SharedBuildNumberEntity();
		sharedBuildNumber2.setId(22);
		configuration.addOrUpdateBuildNumber(sharedBuildNumber2);

		SharedBuildNumberEntity sharedBuildNumber3 = new SharedBuildNumberEntity();
		sharedBuildNumber3.setId(1);
		configuration.addOrUpdateBuildNumber(sharedBuildNumber3);

		replay(this.service);

		SortedSet<SharedBuildNumber> set = this.service.getAllSharedBuildNumbersSortedById(true);

		assertNotNull("The set should not be null.", set);
		assertEquals("The set is the wrong size.", 3, set.size());

		SharedBuildNumberEntity[] values = new SharedBuildNumberEntity[] {
				sharedBuildNumber2, sharedBuildNumber1, sharedBuildNumber3
		};

		int i = 0;
		for(SharedBuildNumber sharedBuildNumber : set)
		{
			assertEquals("The build number is not correct.", values[i].getId(), sharedBuildNumber.getId());

			i++;
		}

		verify(this.service);
	}

	@Test
	public void testGetAllSharedBuildNumbersSortedByName01()
	{
		final ConfigurationEntity configuration = this.getConfiguration();

		SharedBuildNumberEntity sharedBuildNumber1 = new SharedBuildNumberEntity();
		sharedBuildNumber1.setId(1);
		sharedBuildNumber1.setName("This is a killer name!");
		configuration.addOrUpdateBuildNumber(sharedBuildNumber1);

		SharedBuildNumberEntity sharedBuildNumber2 = new SharedBuildNumberEntity();
		sharedBuildNumber2.setId(2);
		sharedBuildNumber2.setName("Hello, World.");
		configuration.addOrUpdateBuildNumber(sharedBuildNumber2);

		SharedBuildNumberEntity sharedBuildNumber3 = new SharedBuildNumberEntity();
		sharedBuildNumber3.setId(3);
		sharedBuildNumber3.setName("This is a cool name.");
		configuration.addOrUpdateBuildNumber(sharedBuildNumber3);

		replay(this.service);

		SortedSet<SharedBuildNumber> set = this.service.getAllSharedBuildNumbersSortedByName(false);

		assertNotNull("The set should not be null.", set);
		assertEquals("The set is the wrong size.", 3, set.size());

		SharedBuildNumberEntity[] values = new SharedBuildNumberEntity[] {
				sharedBuildNumber2, sharedBuildNumber3, sharedBuildNumber1
		};

		int i = 0;
		for(SharedBuildNumber sharedBuildNumber : set)
		{
			assertEquals("The build number is not correct.", values[i].getId(), sharedBuildNumber.getId());

			i++;
		}

		verify(this.service);
	}

	@Test
	public void testGetAllSharedBuildNumbersSortedByName02()
	{
		final ConfigurationEntity configuration = this.getConfiguration();

		SharedBuildNumberEntity sharedBuildNumber1 = new SharedBuildNumberEntity();
		sharedBuildNumber1.setId(1);
		sharedBuildNumber1.setName("This is a killer name!");
		configuration.addOrUpdateBuildNumber(sharedBuildNumber1);

		SharedBuildNumberEntity sharedBuildNumber2 = new SharedBuildNumberEntity();
		sharedBuildNumber2.setId(2);
		sharedBuildNumber2.setName("Hello, World.");
		configuration.addOrUpdateBuildNumber(sharedBuildNumber2);

		SharedBuildNumberEntity sharedBuildNumber3 = new SharedBuildNumberEntity();
		sharedBuildNumber3.setId(3);
		sharedBuildNumber3.setName("This is a cool name.");
		configuration.addOrUpdateBuildNumber(sharedBuildNumber3);

		replay(this.service);

		SortedSet<SharedBuildNumber> set = this.service.getAllSharedBuildNumbersSortedByName(true);

		assertNotNull("The set should not be null.", set);
		assertEquals("The set is the wrong size.", 3, set.size());

		SharedBuildNumberEntity[] values = new SharedBuildNumberEntity[] {
				sharedBuildNumber1, sharedBuildNumber3, sharedBuildNumber2
		};

		int i = 0;
		for(SharedBuildNumber sharedBuildNumber : set)
		{
			assertEquals("The build number is not correct.", values[i].getId(), sharedBuildNumber.getId());

			i++;
		}

		verify(this.service);
	}

	@Test
	public void testGetSharedBuildNumber01()
	{
		final ConfigurationEntity configuration = this.getConfiguration();

		SharedBuildNumberEntity sharedBuildNumber1 = new SharedBuildNumberEntity();
		sharedBuildNumber1.setId(1);
		sharedBuildNumber1.setName("This is a killer name!");
		sharedBuildNumber1.setDescription("This is some description.");
		sharedBuildNumber1.setFormat("myFormat01");
		sharedBuildNumber1.setDateFormat("myFormat02");
		sharedBuildNumber1.setIncrementOnceForChain(false);
		sharedBuildNumber1.setCounter(12);
		configuration.addOrUpdateBuildNumber(sharedBuildNumber1);

		SharedBuildNumberEntity sharedBuildNumber2 = new SharedBuildNumberEntity();
		sharedBuildNumber2.setId(2);
		sharedBuildNumber2.setName("Hello, World.");
		sharedBuildNumber1.setDescription("This is another description.");
		sharedBuildNumber1.setFormat("anotherFormat01");
		sharedBuildNumber1.setDateFormat("anotherFormat02");
		sharedBuildNumber1.setIncrementOnceForChain(true);
		sharedBuildNumber1.setCounter(76);
		configuration.addOrUpdateBuildNumber(sharedBuildNumber2);

		replay(this.service);

		SharedBuildNumber returned = this.service.getSharedBuildNumber(12);

		assertNull("The returned build number should be null.", returned);

		verify(this.service);
	}

	@Test
	public void testGetSharedBuildNumber02()
	{
		final ConfigurationEntity configuration = this.getConfiguration();

		SharedBuildNumberEntity sharedBuildNumber1 = new SharedBuildNumberEntity();
		sharedBuildNumber1.setId(1);
		sharedBuildNumber1.setName("This is a killer name!");
		sharedBuildNumber1.setDescription("This is some description.");
		sharedBuildNumber1.setFormat("myFormat01");
		sharedBuildNumber1.setDateFormat("myFormat02");
		sharedBuildNumber1.setIncrementOnceForChain(false);
		sharedBuildNumber1.setCounter(12);
		configuration.addOrUpdateBuildNumber(sharedBuildNumber1);

		SharedBuildNumberEntity sharedBuildNumber2 = new SharedBuildNumberEntity();
		sharedBuildNumber2.setId(2);
		sharedBuildNumber2.setName("Hello, World.");
		sharedBuildNumber2.setDescription("This is another description.");
		sharedBuildNumber2.setFormat("anotherFormat01");
		sharedBuildNumber2.setDateFormat("anotherFormat02");
		sharedBuildNumber2.setIncrementOnceForChain(true);
		sharedBuildNumber2.setCounter(76);
		configuration.addOrUpdateBuildNumber(sharedBuildNumber2);

		replay(this.service);

		SharedBuildNumber returned = this.service.getSharedBuildNumber(1);

		assertNotNull("The returned build number should not be null.", returned);

		assertEquals("The ID is not correct.", 1, returned.getId());
		assertEquals("The name is not correct.", "This is a killer name!", returned.getName());
		assertEquals("The description is not correct.", "This is some description.", returned.getDescription());
		assertEquals("The format is not correct.", "myFormat01", returned.getFormat());
		assertEquals("The date format is not correct.", "myFormat02", returned.getDateFormat());
		assertFalse("The increment once flag should be false.", returned.isIncrementOnceForChain());
		assertEquals("The counter is not correct.", 12, returned.getCounter());

		verify(this.service);
	}

	@Test
	public void testGetSharedBuildNumber03()
	{
		final ConfigurationEntity configuration = this.getConfiguration();

		SharedBuildNumberEntity sharedBuildNumber1 = new SharedBuildNumberEntity();
		sharedBuildNumber1.setId(1);
		sharedBuildNumber1.setName("This is a killer name!");
		sharedBuildNumber1.setDescription("This is some description.");
		sharedBuildNumber1.setFormat("myFormat01");
		sharedBuildNumber1.setDateFormat("myFormat02");
		sharedBuildNumber1.setIncrementOnceForChain(false);
		sharedBuildNumber1.setCounter(12);
		configuration.addOrUpdateBuildNumber(sharedBuildNumber1);

		SharedBuildNumberEntity sharedBuildNumber2 = new SharedBuildNumberEntity();
		sharedBuildNumber2.setId(2);
		sharedBuildNumber2.setName("Hello, World.");
		sharedBuildNumber2.setDescription("This is another description.");
		sharedBuildNumber2.setFormat("anotherFormat01");
		sharedBuildNumber2.setDateFormat("anotherFormat02");
		sharedBuildNumber2.setIncrementOnceForChain(true);
		sharedBuildNumber2.setCounter(76);
		configuration.addOrUpdateBuildNumber(sharedBuildNumber2);

		replay(this.service);

		SharedBuildNumber returned = this.service.getSharedBuildNumber(2);

		assertNotNull("The returned build number should not be null.", returned);

		assertEquals("The ID is not correct.", 2, returned.getId());
		assertEquals("The name is not correct.", "Hello, World.", returned.getName());
		assertEquals("The description is not correct.", "This is another description.", returned.getDescription());
		assertEquals("The format is not correct.", "anotherFormat01", returned.getFormat());
		assertEquals("The date format is not correct.", "anotherFormat02", returned.getDateFormat());
		assertTrue("The increment once flag should be true.", returned.isIncrementOnceForChain());
		assertEquals("The counter is not correct.", 76, returned.getCounter());

		verify(this.service);
	}

	@Test
	public void testGetSharedBuildNumberName01()
	{
		final ConfigurationEntity configuration = this.getConfiguration();

		SharedBuildNumberEntity sharedBuildNumber1 = new SharedBuildNumberEntity();
		sharedBuildNumber1.setId(1);
		sharedBuildNumber1.setName("This is a killer name!");
		configuration.addOrUpdateBuildNumber(sharedBuildNumber1);

		SharedBuildNumberEntity sharedBuildNumber2 = new SharedBuildNumberEntity();
		sharedBuildNumber2.setId(2);
		sharedBuildNumber2.setName("Hello, World.");
		configuration.addOrUpdateBuildNumber(sharedBuildNumber2);

		replay(this.service);

		String returned = this.service.getSharedBuildNumberName(12);

		assertNotNull("The returned build number name should be null.", returned);
		assertEquals("The returned build number name is not correct.", "", returned);

		verify(this.service);
	}

	@Test
	public void testGetSharedBuildNumberName02()
	{
		final ConfigurationEntity configuration = this.getConfiguration();

		SharedBuildNumberEntity sharedBuildNumber1 = new SharedBuildNumberEntity();
		sharedBuildNumber1.setId(1);
		sharedBuildNumber1.setName("This is a killer name!");
		configuration.addOrUpdateBuildNumber(sharedBuildNumber1);

		SharedBuildNumberEntity sharedBuildNumber2 = new SharedBuildNumberEntity();
		sharedBuildNumber2.setId(2);
		sharedBuildNumber2.setName("Hello, World.");
		configuration.addOrUpdateBuildNumber(sharedBuildNumber2);

		replay(this.service);

		String returned = this.service.getSharedBuildNumberName(1);

		assertNotNull("The returned build number name should be null.", returned);
		assertEquals("The returned build number name is not correct.", "This is a killer name!", returned);

		verify(this.service);
	}

	@Test
	public void testGetSharedBuildNumberName03()
	{
		final ConfigurationEntity configuration = this.getConfiguration();

		SharedBuildNumberEntity sharedBuildNumber1 = new SharedBuildNumberEntity();
		sharedBuildNumber1.setId(1);
		sharedBuildNumber1.setName("This is a killer name!");
		configuration.addOrUpdateBuildNumber(sharedBuildNumber1);

		SharedBuildNumberEntity sharedBuildNumber2 = new SharedBuildNumberEntity();
		sharedBuildNumber2.setId(2);
		sharedBuildNumber2.setName("Hello, World.");
		configuration.addOrUpdateBuildNumber(sharedBuildNumber2);

		replay(this.service);

		String returned = this.service.getSharedBuildNumberName(2);

		assertNotNull("The returned build number name should be null.", returned);
		assertEquals("The returned build number name is not correct.", "Hello, World.", returned);

		verify(this.service);
	}

	@Test
	public void testDeleteSharedBuildNumber01() throws IOException
	{
		final ConfigurationEntity configuration = this.getConfiguration();

		final SharedBuildNumberEntity sharedBuildNumber1 = new SharedBuildNumberEntity();
		sharedBuildNumber1.setId(1);
		configuration.addOrUpdateBuildNumber(sharedBuildNumber1);

		final SharedBuildNumberEntity sharedBuildNumber2 = new SharedBuildNumberEntity();
		sharedBuildNumber2.setId(2);
		configuration.addOrUpdateBuildNumber(sharedBuildNumber2);

		this.service.saveConfiguration();
		expectLastCall().andAnswer(new IAnswer<Void>()
		{
			@Override
			public Void answer() throws Throwable
			{
				assertEquals("The number of build numbers is not correct.", 1, configuration.getBuildNumbers().size());
				assertTrue("The remaining shared build number is not correct.",
						   configuration.getBuildNumbers().contains(sharedBuildNumber2));

				return null;
			}
		});

		replay(this.service);

		this.service.deleteSharedBuildNumber(1);

		verify(this.service);
	}

	@Test
	public void testDeleteSharedBuildNumber02() throws IOException
	{
		final ConfigurationEntity configuration = this.getConfiguration();

		final SharedBuildNumberEntity sharedBuildNumber1 = new SharedBuildNumberEntity();
		sharedBuildNumber1.setId(1);
		configuration.addOrUpdateBuildNumber(sharedBuildNumber1);

		final SharedBuildNumberEntity sharedBuildNumber2 = new SharedBuildNumberEntity();
		sharedBuildNumber2.setId(2);
		configuration.addOrUpdateBuildNumber(sharedBuildNumber2);

		this.service.saveConfiguration();
		expectLastCall().andAnswer(new IAnswer<Void>()
		{
			@Override
			public Void answer() throws Throwable
			{
				assertEquals("The number of build numbers is not correct.", 1, configuration.getBuildNumbers().size());
				assertTrue("The remaining shared build number is not correct.",
						   configuration.getBuildNumbers().contains(sharedBuildNumber1));

				return null;
			}
		});

		replay(this.service);

		this.service.deleteSharedBuildNumber(2);

		verify(this.service);
	}

	@Test
	public void testGetAnIncrementFormattedSharedBuildNumber01() throws IOException
	{
		final ConfigurationEntity configuration = this.getConfiguration();

		final SharedBuildNumberEntity sharedBuildNumber1 = new SharedBuildNumberEntity();
		sharedBuildNumber1.setId(1);
		sharedBuildNumber1.setFormat("1.0.0.{0}");
		sharedBuildNumber1.setCounter(76);
		configuration.addOrUpdateBuildNumber(sharedBuildNumber1);

		final SharedBuildNumberEntity sharedBuildNumber2 = new SharedBuildNumberEntity();
		sharedBuildNumber2.setId(2);
		sharedBuildNumber2.setFormat("1.0.0.{d}-beta");
		sharedBuildNumber2.setDateFormat("yyyyMMdd");
		sharedBuildNumber2.setCounter(15);
		configuration.addOrUpdateBuildNumber(sharedBuildNumber2);

		this.service.saveConfiguration();
		expectLastCall().andAnswer(new IAnswer<Void>()
		{
			@Override
			public Void answer() throws Throwable
			{
				assertEquals("The new counter value is not correct.", 77, sharedBuildNumber1.getCounter());

				return null;
			}
		});

		replay(this.service);

		String buildNumber = this.service.getAndIncrementFormattedSharedBuildNumber(1);

		assertNotNull("The formatted build number should not be null.", buildNumber);
		assertEquals("The formatted build number is not correct.", "1.0.0.76", buildNumber);

		verify(this.service);
	}

	@Test
	public void testGetAnIncrementFormattedSharedBuildNumber02() throws IOException
	{
		final ConfigurationEntity configuration = this.getConfiguration();

		final SharedBuildNumberEntity sharedBuildNumber1 = new SharedBuildNumberEntity();
		sharedBuildNumber1.setId(1);
		sharedBuildNumber1.setFormat("1.0.0.{0}");
		sharedBuildNumber1.setCounter(76);
		configuration.addOrUpdateBuildNumber(sharedBuildNumber1);

		final SharedBuildNumberEntity sharedBuildNumber2 = new SharedBuildNumberEntity();
		sharedBuildNumber2.setId(2);
		sharedBuildNumber2.setFormat("1.0.0.{d}-beta");
		sharedBuildNumber2.setDateFormat("yyyyMMdd");
		sharedBuildNumber2.setCounter(15);
		configuration.addOrUpdateBuildNumber(sharedBuildNumber2);

		this.service.saveConfiguration();
		expectLastCall().andAnswer(new IAnswer<Void>()
		{
			@Override
			public Void answer() throws Throwable
			{
				assertEquals("The new counter value is not correct.", 16, sharedBuildNumber2.getCounter());

				return null;
			}
		});

		replay(this.service);

		String buildNumber = this.service.getAndIncrementFormattedSharedBuildNumber(2);

		assertNotNull("The formatted build number should not be null.", buildNumber);
		assertEquals("The formatted build number is not correct.",
					 "1.0.0." + (new SimpleDateFormat("yyyyMMdd").format(new Date())) + "-beta",
					 buildNumber);

		verify(this.service);
	}

	@Test
	public void testGetAnIncrementFormattedSharedBuildNumber03() throws IOException
	{
		final ConfigurationEntity configuration = this.getConfiguration();

		final SharedBuildNumberEntity sharedBuildNumber1 = new SharedBuildNumberEntity();
		sharedBuildNumber1.setId(1);
		sharedBuildNumber1.setFormat("2.4.3.{0}-alpha");
		sharedBuildNumber1.setCounter(1966);
		configuration.addOrUpdateBuildNumber(sharedBuildNumber1);

		final SharedBuildNumberEntity sharedBuildNumber2 = new SharedBuildNumberEntity();
		sharedBuildNumber2.setId(2);
		sharedBuildNumber2.setFormat("7.1.{D}.{0}");
		sharedBuildNumber2.setDateFormat("yyMMdd");
		sharedBuildNumber2.setCounter(23);
		configuration.addOrUpdateBuildNumber(sharedBuildNumber2);

		this.service.saveConfiguration();
		expectLastCall().andAnswer(new IAnswer<Void>()
		{
			@Override
			public Void answer() throws Throwable
			{
				assertEquals("The new counter value is not correct.", 1967, sharedBuildNumber1.getCounter());

				return null;
			}
		});

		replay(this.service);

		String buildNumber = this.service.getAndIncrementFormattedSharedBuildNumber(1);

		assertNotNull("The formatted build number should not be null.", buildNumber);
		assertEquals("The formatted build number is not correct.", "2.4.3.1966-alpha", buildNumber);

		verify(this.service);
	}

	@Test
	public void testGetAnIncrementFormattedSharedBuildNumber04() throws IOException
	{
		final ConfigurationEntity configuration = this.getConfiguration();

		final SharedBuildNumberEntity sharedBuildNumber1 = new SharedBuildNumberEntity();
		sharedBuildNumber1.setId(1);
		sharedBuildNumber1.setFormat("2.4.3.{0}-alpha");
		sharedBuildNumber1.setCounter(1966);
		configuration.addOrUpdateBuildNumber(sharedBuildNumber1);

		final SharedBuildNumberEntity sharedBuildNumber2 = new SharedBuildNumberEntity();
		sharedBuildNumber2.setId(2);
		sharedBuildNumber2.setFormat("7.1.{D}.{0}");
		sharedBuildNumber2.setDateFormat("yyMMdd");
		sharedBuildNumber2.setCounter(23);
		configuration.addOrUpdateBuildNumber(sharedBuildNumber2);

		this.service.saveConfiguration();
		expectLastCall().andAnswer(new IAnswer<Void>()
		{
			@Override
			public Void answer() throws Throwable
			{
				assertEquals("The new counter value is not correct.", 24, sharedBuildNumber2.getCounter());

				return null;
			}
		});

		replay(this.service);

		String buildNumber = this.service.getAndIncrementFormattedSharedBuildNumber(2);

		assertNotNull("The formatted build number should not be null.", buildNumber);
		assertEquals("The formatted build number is not correct.",
					 "7.1." + (new SimpleDateFormat("yyMMdd").format(new Date())) + ".23",
					 buildNumber);

		verify(this.service);
	}

	@Test
	public void testSaveSharedBuildNumber01() throws IOException
	{
		final ConfigurationEntity configuration = this.getConfiguration();

		final SharedBuildNumberEntity sharedBuildNumber1 = new SharedBuildNumberEntity();
		sharedBuildNumber1.setId(1);
		sharedBuildNumber1.setName("This is a killer name!");
		sharedBuildNumber1.setDescription("This is some description.");
		sharedBuildNumber1.setFormat("myFormat01");
		sharedBuildNumber1.setDateFormat("myFormat02");
		sharedBuildNumber1.setIncrementOnceForChain(false);
		sharedBuildNumber1.setCounter(12);
		configuration.addOrUpdateBuildNumber(sharedBuildNumber1);

		this.service.saveConfiguration();
		expectLastCall().andAnswer(new IAnswer<Void>()
		{
			@Override
			public Void answer() throws Throwable
			{
				assertEquals("The number of build numbers is not correct.", 2, configuration.getBuildNumbers().size());

				SharedBuildNumberEntity entity = configuration.getBuildNumber(12);
				assertEquals("The name is not correct.", "Hello, World.", entity.getName());
				assertEquals("The description is not correct.", "This is another description.",
							 entity.getDescription());
				assertEquals("The format is not correct.", "anotherFormat01", entity.getFormat());
				assertEquals("The date format is not correct.", "anotherFormat02", entity.getDateFormat());
				assertTrue("The increment once flag should be true.", entity.isIncrementOnceForChain());
				assertEquals("The counter is not correct.", 76, entity.getCounter());

				return null;
			}
		});

		replay(this.service);

		SharedBuildNumber number = new SharedBuildNumber(12);
		number.setName("Hello, World.");
		number.setDescription("This is another description.");
		number.setFormat("anotherFormat01");
		number.setDateFormat("anotherFormat02");
		number.setIncrementOnceForChain(true);
		number.setCounter(76);

		this.service.saveSharedBuildNumber(number);

		verify(this.service);
	}

	@Test
	public void testSaveSharedBuildNumber02() throws IOException
	{
		final ConfigurationEntity configuration = this.getConfiguration();

		final SharedBuildNumberEntity sharedBuildNumber1 = new SharedBuildNumberEntity();
		sharedBuildNumber1.setId(1);
		sharedBuildNumber1.setName("This is a killer name!");
		sharedBuildNumber1.setDescription("This is some description.");
		sharedBuildNumber1.setFormat("myFormat01");
		sharedBuildNumber1.setDateFormat("myFormat02");
		sharedBuildNumber1.setIncrementOnceForChain(false);
		sharedBuildNumber1.setCounter(12);
		configuration.addOrUpdateBuildNumber(sharedBuildNumber1);

		this.service.saveConfiguration();
		expectLastCall().andAnswer(new IAnswer<Void>()
		{
			@Override
			public Void answer() throws Throwable
			{
				assertEquals("The number of build numbers is not correct.", 1, configuration.getBuildNumbers().size());

				assertEquals("The name is not correct.", "Changed Name", sharedBuildNumber1.getName());
				assertEquals("The description is not correct.", "This is the last description.",
							 sharedBuildNumber1.getDescription());
				assertEquals("The format is not correct.", "coolFormat01", sharedBuildNumber1.getFormat());
				assertEquals("The date format is not correct.", "coolFormat02", sharedBuildNumber1.getDateFormat());
				assertTrue("The increment once flag should be true.", sharedBuildNumber1.isIncrementOnceForChain());
				assertEquals("The counter is not correct.", 17, sharedBuildNumber1.getCounter());

				return null;
			}
		});

		replay(this.service);

		SharedBuildNumber number = new SharedBuildNumber(1);
		number.setName("Changed Name");
		number.setDescription("This is the last description.");
		number.setFormat("coolFormat01");
		number.setDateFormat("coolFormat02");
		number.setIncrementOnceForChain(true);
		number.setCounter(17);

		this.service.saveSharedBuildNumber(number);

		verify(this.service);
	}

	@Test
	public void testSaveSharedBuildNumber03() throws IOException
	{
		final ConfigurationEntity configuration = this.getConfiguration();

		final SharedBuildNumberEntity sharedBuildNumber1 = new SharedBuildNumberEntity();
		sharedBuildNumber1.setId(1);
		sharedBuildNumber1.setName("This is a killer name!");
		sharedBuildNumber1.setDescription("This is some description.");
		sharedBuildNumber1.setFormat("myFormat01");
		sharedBuildNumber1.setDateFormat("myFormat02");
		sharedBuildNumber1.setIncrementOnceForChain(false);
		sharedBuildNumber1.setCounter(12);
		configuration.addOrUpdateBuildNumber(sharedBuildNumber1);

		replay(this.service);

		SharedBuildNumber number = new SharedBuildNumber(1);
		number.setName("Changed Name");
		number.setDescription("This is the last description.");
		number.setFormat("coolFormat01");
		number.setDateFormat("coolFormat02");
		number.setIncrementOnceForChain(true);
		number.setCounter(11);

		try
		{
			this.service.saveSharedBuildNumber(number);
			fail("Expected exception java.lang.IllegalArgumentException, got no exception.");
		}
		catch(IllegalArgumentException e)
		{
			assertEquals("The message is not correct.",
						 "You cannot decrease the counter number; if changed, it can only be increased.",
						 e.getMessage());
		}

		verify(this.service);
	}

	private ConfigurationEntity getExistingConfiguration()
	{
		try
		{
			Field field = PluginConfigurationServiceDefault.class.getDeclaredField("configuration");
			field.setAccessible(true);
			return (ConfigurationEntity)field.get(this.service);
		}
		catch(IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
		catch(NoSuchFieldException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Test
	public void testInitialize01() throws IOException
	{
		File xsd = new File(TestPluginConfigurationServiceDefault.workingDirectory,
							PluginConfigurationService.CONFIG_XSD_FILE_NAME);
		File xml = new File(TestPluginConfigurationServiceDefault.workingDirectory,
							PluginConfigurationService.CONFIG_XML_FILE_NAME);

		assertFalse("The XSD file should not exist yet.", xsd.exists());
		assertFalse("The XML file should not exist yet.", xml.exists());

		replay(this.service);

		try
		{
			this.service.initialize();

			assertTrue("The XSD file should exist now.", xsd.exists());
			assertTrue("The XML file should exist now.", xml.exists());

			assertEquals("The XSD contents are not correct.",
						 IOUtils.toString(
								 this.getClass().getResourceAsStream("./" + PluginConfigurationService.CONFIG_XSD_FILE_NAME)
						 ),
						 FileUtils.readFileToString(xsd));

			assertEquals("The XML contents are not correct.",
						 IOUtils.toString(
								 this.getClass().getResourceAsStream("./" + PluginConfigurationService.CONFIG_XML_FILE_NAME + ".dist")
						 ),
						 FileUtils.readFileToString(xml));

			ConfigurationEntity configuration = this.getExistingConfiguration();

			assertNotNull("The configuration should not be null.", configuration);

			assertNotNull("The last update date should not be null.", configuration.getLastUpdate());
			assertTrue("The last update date should be before now.", configuration.getLastUpdate().isBeforeNow());
			assertTrue("The last update date should be after 8/8/2012.",
					   configuration.getLastUpdate().isAfter(new DateTime(2012, 8, 8, 0, 0, 0, 0)));

			SettingsEntity settings = configuration.getSettings();
			assertNotNull("The settings object should not be null.", settings);
			assertEquals("The sequence setting is not correct.", 3, settings.getBuildNumberIdSequence());

			Collection<SharedBuildNumberEntity> buildNumbers = configuration.getBuildNumbers();
			assertNotNull("The collection should not be null.", buildNumbers);
			assertEquals("The collection is the wrong size.", 2, buildNumbers.size());

			SharedBuildNumberEntity n1 = null;
			SharedBuildNumberEntity n2 = null;
			for(SharedBuildNumberEntity buildNumber : buildNumbers)
			{
				if(buildNumber.getId() == 1)
					n1 = buildNumber;
				else if(buildNumber.getId() == 2)
					n2 = buildNumber;
			}

			assertNotNull("Build number 1 should not be null.", n1);
			assertEquals("The name is not correct (1).", "Sample Counter-Based Build Number", n1.getName());
			assertEquals("The description is not correct (1).",
						 "This sample build number exists when the plugin in installed. It can be safely removed or used " +
						 "for testing purposes.",
						 n1.getDescription());
			assertEquals("The format is not correct (1).", "1.0.0.{0}", n1.getFormat());
			assertNull("The date format should be null (1).", n1.getDateFormat());
			assertEquals("The counter is not correct (1).", 123, n1.getCounter());

			assertNotNull("Build number 2 should not be null.", n2);
			assertEquals("The name is not correct (2).", "Sample Date-Based Build Number", n2.getName());
			assertEquals("The description is not correct (2).",
						 "This sample build number exists when the plugin in installed. It can be safely removed or used " +
						 "for testing purposes.",
						 n2.getDescription());
			assertEquals("The format is not correct (2).", "2.0.0.{d}", n2.getFormat());
			assertEquals("The date format is not correct (2).", "yyyyMMddHHmmss", n2.getDateFormat());
			assertEquals("The counter is not correct (2).", 1, n2.getCounter());

			this.service.destroy();

			verify(this.service);
		}
		finally
		{
			try
			{
				FileUtils.forceDelete(xsd);
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}

			try
			{
				FileUtils.forceDelete(xml);
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	@Test
	public void testInitialize02() throws IOException
	{
		File xsd = new File(TestPluginConfigurationServiceDefault.workingDirectory,
							PluginConfigurationService.CONFIG_XSD_FILE_NAME);
		File xml = new File(TestPluginConfigurationServiceDefault.workingDirectory,
							PluginConfigurationService.CONFIG_XML_FILE_NAME);

		assertFalse("The XSD file should not exist yet.", xsd.exists());
		assertFalse("The XML file should not exist yet.", xml.exists());

		replay(this.service);

		try
		{
			this.service.initialize();

			assertTrue("The XSD file should exist now.", xsd.exists());
			assertTrue("The XML file should exist now.", xml.exists());

			ConfigurationEntity configuration = this.getExistingConfiguration();

			assertNotNull("The configuration should not be null.", configuration);

			FileUtils.forceDelete(xml);

			try
			{
				this.service.changeOccured("");
				fail("Expected exception org.springframework.beans.FatalBeanException, got no exception.");
			}
			catch(FatalBeanException e)
			{
				assertTrue("The error message [" + e.getMessage() + "] is not correct.",
						   e.getMessage().startsWith("Could not read plugin configuration XML file;"));
			}

			this.service.destroy();

			verify(this.service);
		}
		finally
		{
			try
			{
				FileUtils.forceDelete(xsd);
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}

			try
			{
				if(xml.exists())
					FileUtils.forceDelete(xml);
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	@Test
	public void testInitialize03() throws IOException
	{
		File xsd = new File(TestPluginConfigurationServiceDefault.workingDirectory,
							PluginConfigurationService.CONFIG_XSD_FILE_NAME);
		File xml = new File(TestPluginConfigurationServiceDefault.workingDirectory,
							PluginConfigurationService.CONFIG_XML_FILE_NAME);

		assertFalse("The XSD file should not exist yet.", xsd.exists());
		assertFalse("The XML file should not exist yet.", xml.exists());

		replay(this.service);

		try
		{
			this.service.initialize();

			assertTrue("The XSD file should exist now.", xsd.exists());
			assertTrue("The XML file should exist now.", xml.exists());

			ConfigurationEntity configuration = this.getExistingConfiguration();

			assertNotNull("The configuration should not be null.", configuration);

			PluginFileUtils.copyResource(this.getClass(), "testInvalidLastUpdateDate.xml", xml);

			try
			{
				this.service.changeOccured("");
				fail("Expected exception org.springframework.beans.FatalBeanException, got no exception.");
			}
			catch(FatalBeanException e)
			{
				assertTrue("The error message [" + e.getMessage() + "] is not correct.",
						   e.getMessage().startsWith("Could not parse plugin configuration XML;"));
			}

			this.service.destroy();

			verify(this.service);
		}
		finally
		{
			try
			{
				FileUtils.forceDelete(xsd);
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}

			try
			{
				FileUtils.forceDelete(xml);
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	@Test
	public void testInitializeAndSave01() throws IOException
	{
		ServerPaths serverPaths = new ServerPaths("bad/path1", workingDirectory.getPath(), "bad/path2");

		this.service = new PluginConfigurationServiceDefault(serverPaths);

		File xsd = new File(TestPluginConfigurationServiceDefault.workingDirectory,
							PluginConfigurationService.CONFIG_XSD_FILE_NAME);
		File xml = new File(TestPluginConfigurationServiceDefault.workingDirectory,
							PluginConfigurationService.CONFIG_XML_FILE_NAME);

		assertFalse("The XSD file should not exist yet.", xsd.exists());
		assertFalse("The XML file should not exist yet.", xml.exists());

		try
		{
			this.service.initialize();

			assertTrue("The XSD file should exist now.", xsd.exists());
			assertTrue("The XML file should exist now.", xml.exists());

			ConfigurationEntity configuration = this.getExistingConfiguration();

			assertNotNull("The configuration should not be null.", configuration);

			assertEquals("There should be two build numbers.", 2, configuration.getBuildNumbers().size());

			String originalContents = FileUtils.readFileToString(xml);

			SharedBuildNumber number = new SharedBuildNumber(12);
			number.setName("The Best Name");
			number.setDescription("Some other lame description.");
			number.setFormat("7.1.0.{0}");
			number.setCounter(3);

			this.service.saveSharedBuildNumber(number);

			assertFalse("The file contents should have changed.",
						originalContents.equals(FileUtils.readFileToString(xml)));

			assertEquals("There should be three build numbers now.", 3, configuration.getBuildNumbers().size());

			this.service.loadConfiguration();

			configuration = this.getExistingConfiguration();

			assertNotNull("The configuration should not be null.", configuration);

			assertEquals("There should still be three build numbers.", 3, configuration.getBuildNumbers().size());

			SharedBuildNumber returned = this.service.getSharedBuildNumber(12);

			assertNotNull("The build number should not be null.", returned);
			assertEquals("The ID is not correct.", 12, returned.getId());
			assertEquals("The name is not correct.", "The Best Name", returned.getName());
			assertEquals("The description is not correct.", "Some other lame description.", returned.getDescription());
			assertEquals("The format is not correct.", "7.1.0.{0}", returned.getFormat());
			assertNull("The date format should be null.", returned.getDateFormat());
			assertFalse("The increment once flag is not correct.", returned.isIncrementOnceForChain());
			assertEquals("The counter is not correct.", 3, returned.getCounter());

			this.service.destroy();
		}
		catch(FatalBeanException e)
		{
			System.out.println(FileUtils.readFileToString(xml));
			throw e;
		}
		finally
		{
			try
			{
				FileUtils.forceDelete(xsd);
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}

			try
			{
				FileUtils.forceDelete(xml);
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}
}