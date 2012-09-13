/*
 * TestBuildNumberPropertiesProvider.java from TeamCityPlugins modified Wednesday, September 12, 2012 21:22:31 CDT (-0500).
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

import jetbrains.buildServer.ExtensionHolder;
import jetbrains.buildServer.agent.ServerProvidedProperties;
import jetbrains.buildServer.parameters.ParametersProvider;
import jetbrains.buildServer.serverSide.BuildStartContext;
import jetbrains.buildServer.serverSide.BuildStartContextProcessor;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SRunnerContext;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.parameters.BuildParametersProvider;
import jetbrains.buildServer.serverSide.parameters.ParameterDescriptionProvider;
import org.easymock.Capture;
import org.easymock.IAnswer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeSet;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Test class for BuildNumberPropertiesProvider.
 */
public class TestBuildNumberPropertiesProvider
{
	private ExtensionHolder holder;

	private PluginConfigurationService service;

	private BuildNumberPropertiesProvider provider;

	@Before
	public void setUp()
	{
		this.holder = createStrictMock(ExtensionHolder.class);
		this.service = createStrictMock(PluginConfigurationService.class);

		Capture<BuildParametersProvider> c1 = new Capture<BuildParametersProvider>();
		Capture<ParameterDescriptionProvider> c2 = new Capture<ParameterDescriptionProvider>();
		Capture<BuildStartContextProcessor> c3 = new Capture<BuildStartContextProcessor>();

		String name = BuildNumberPropertiesProvider.class.getName();

		this.holder.registerExtension(same(BuildParametersProvider.class), eq(name), capture(c1));
		this.holder.registerExtension(same(ParameterDescriptionProvider.class), eq(name), capture(c2));
		this.holder.registerExtension(same(BuildStartContextProcessor.class), eq(name), capture(c3));

		replay(this.holder);

		this.provider = new BuildNumberPropertiesProvider(this.holder, this.service);

		assertSame("The BuildParametersProvider capture is not correct.", this.provider, c1.getValue());
		assertSame("The ParameterDescriptionProvider capture is not correct.", this.provider, c2.getValue());
		assertSame("The BuildStartContextProcessor capture is not correct.", this.provider, c3.getValue());
	}

	@After
	public void tearDown()
	{
		verify(this.holder, this.service);
	}

	@Test
	public void testGetParametersAvailableOnAgent01()
	{
		SBuild build = createStrictMock(SBuild.class);

		expect(this.service.getAllSharedBuildNumberIds()).andReturn(new int[] { });

		replay(this.service, build);

		Collection<String> parameters = this.provider.getParametersAvailableOnAgent(build);

		assertNotNull("The list of parameters should not be null.", parameters);
		assertEquals("The parameter list size is not correct.", 0, parameters.size());
		assertSame("The list type is not correct.", TreeSet.class, parameters.getClass());

		verify(build);
	}

	@Test
	public void testGetParametersAvailableOnAgent02()
	{
		SBuild build = createStrictMock(SBuild.class);

		expect(this.service.getAllSharedBuildNumberIds()).andReturn(new int[] { 1, 5, 6 });

		replay(this.service, build);

		Collection<String> parameters = this.provider.getParametersAvailableOnAgent(build);

		assertNotNull("The list of parameters should not be null.", parameters);
		assertEquals("The parameter list size is not correct.", 3, parameters.size());
		assertSame("The list type is not correct.", TreeSet.class, parameters.getClass());

		String[] values = new String[] { "sharedBuildNumber.id1", "sharedBuildNumber.id5", "sharedBuildNumber.id6" };

		int i = 0;
		for(String parameter : parameters)
		{
			assertEquals("The parameter is not correct.", values[i], parameter);

			i++;
		}

		verify(build);
	}

	@Test
	public void testDescribe01()
	{
		replay(this.service);

		String description = this.provider.describe("bad.parameter");

		assertNull("The description should be null.", description);
	}

	@Test
	public void testDescribe02()
	{
		replay(this.service);

		String description = this.provider.describe("sharedBuildNumber.id");

		assertNull("The description should be null.", description);
	}

	@Test
	public void testDescribe03()
	{
		replay(this.service);

		String description = this.provider.describe("sharedBuildNumber.idAbc");

		assertNull("The description should be null.", description);
	}

	@Test
	public void testDescribe04()
	{
		expect(this.service.getSharedBuildNumberName(12)).andReturn("My Cool Name");

		replay(this.service);

		String description = this.provider.describe("sharedBuildNumber.id12");

		assertNotNull("The description should be null.", description);
		assertEquals("The build name is not correct.", "My Cool Name", description);
	}

	@Test
	public void testDescribe05()
	{
		expect(this.service.getSharedBuildNumberName(83221)).andReturn("Something Else");

		replay(this.service);

		String description = this.provider.describe("sharedBuildNumber.id83221");

		assertNotNull("The description should be null.", description);
		assertEquals("The build name is not correct.", "Something Else", description);
	}

	@Test
	public void testIsVisible01()
	{
		replay(this.service);

		assertFalse("The return value should be false.", this.provider.isVisible("bad.parameter"));
	}

	@Test
	public void testIsVisible02()
	{
		replay(this.service);

		assertTrue("The return value should be true.", this.provider.isVisible("sharedBuildNumber.id"));
	}

	@Test
	public void testIsVisible03()
	{
		replay(this.service);

		assertTrue("The return value should be true.", this.provider.isVisible("sharedBuildNumber.id123"));
	}

	@Test
	public void testUpdateParameters01()
	{
		BuildStartContext context = createStrictMock(BuildStartContext.class);
		SRunningBuild runningBuild = createStrictMock(SRunningBuild.class);

		expect(context.getBuild()).andReturn(runningBuild);
		expect(runningBuild.getFullName()).andReturn("name");
		expect(runningBuild.getBuildType()).andReturn(null);

		replay(this.service, context, runningBuild);

		this.provider.updateParameters(context);

		verify(context, runningBuild);
	}

	@Test
	public void testUpdateParameters02()
	{
		BuildStartContext context = createStrictMock(BuildStartContext.class);
		SRunningBuild runningBuild = createStrictMock(SRunningBuild.class);
		SBuildType type = createStrictMock(SBuildType.class);
		ParametersProvider parametersProvider = createStrictMock(ParametersProvider.class);

		expect(context.getBuild()).andReturn(runningBuild);
		expect(runningBuild.getFullName()).andReturn("name");
		expect(runningBuild.getBuildType()).andReturn(type);
		expect(runningBuild.getParametersProvider()).andReturn(parametersProvider);
		expect(parametersProvider.getAll()).andReturn(new Hashtable<String, String>());
		context.getRunnerContexts();
		expectLastCall().andAnswer(new IAnswer<Object>()
		{
			@Override
			public Object answer() throws Throwable
			{
				return new ArrayList<SRunnerContext>();
			}
		});

		replay(this.service, context, runningBuild, type, parametersProvider);

		this.provider.updateParameters(context);

		verify(context, runningBuild, type, parametersProvider);
	}

	@Test
	public void testUpdateParameters03()
	{
		BuildStartContext context = createStrictMock(BuildStartContext.class);
		SRunningBuild runningBuild = createStrictMock(SRunningBuild.class);
		SBuildType type = createStrictMock(SBuildType.class);
		ParametersProvider parametersProvider = createStrictMock(ParametersProvider.class);

		Map<String, String> parameters = new Hashtable<String, String>();
		parameters.put("someParameter", "someValue %mySubstitution%");

		expect(context.getBuild()).andReturn(runningBuild);
		expect(runningBuild.getFullName()).andReturn("name");
		expect(runningBuild.getBuildType()).andReturn(type);
		expect(runningBuild.getParametersProvider()).andReturn(parametersProvider);
		expect(parametersProvider.getAll()).andReturn(parameters);
		context.getRunnerContexts();
		expectLastCall().andAnswer(new IAnswer<Object>()
		{
			@Override
			public Object answer() throws Throwable
			{
				return new ArrayList<SRunnerContext>();
			}
		});

		replay(this.service, context, runningBuild, type, parametersProvider);

		this.provider.updateParameters(context);

		verify(context, runningBuild, type, parametersProvider);
	}

	@Test
	public void testUpdateParameters04()
	{
		BuildStartContext context = createStrictMock(BuildStartContext.class);
		SRunningBuild runningBuild = createStrictMock(SRunningBuild.class);
		SBuildType type = createStrictMock(SBuildType.class);
		ParametersProvider parametersProvider = createStrictMock(ParametersProvider.class);

		Map<String, String> parameters = new Hashtable<String, String>();
		parameters.put("someParameter", "someValue %sharedBuildNumber.id%");

		Map<String, String> sharedParameters = new Hashtable<String, String>();

		expect(context.getBuild()).andReturn(runningBuild);
		expect(runningBuild.getFullName()).andReturn("name");
		expect(runningBuild.getBuildType()).andReturn(type);
		expect(runningBuild.getParametersProvider()).andReturn(parametersProvider);
		expect(parametersProvider.getAll()).andReturn(parameters);
		context.getRunnerContexts();
		expectLastCall().andAnswer(new IAnswer<Object>()
		{
			@Override
			public Object answer() throws Throwable
			{
				return new ArrayList<SRunnerContext>();
			}
		});
		expect(context.getSharedParameters()).andReturn(sharedParameters);
		expect(runningBuild.getRawBuildNumber()).andReturn("1.0.1.{0}");
		expect(runningBuild.getFullName()).andReturn("name");

		replay(this.service, context, runningBuild, type, parametersProvider);

		this.provider.updateParameters(context);

		verify(context, runningBuild, type, parametersProvider);
	}

	@Test
	public void testUpdateParameters05() throws IOException
	{
		BuildStartContext context = createStrictMock(BuildStartContext.class);
		SRunningBuild runningBuild = createStrictMock(SRunningBuild.class);
		SBuildType type = createStrictMock(SBuildType.class);
		ParametersProvider parametersProvider = createStrictMock(ParametersProvider.class);

		Map<String, String> parameters = new Hashtable<String, String>();
		parameters.put("someParameter", "someValue %sharedBuildNumber.id2%");

		Map<String, String> sharedParameters = new Hashtable<String, String>();

		expect(context.getBuild()).andReturn(runningBuild);
		expect(runningBuild.getFullName()).andReturn("name");
		expect(runningBuild.getBuildType()).andReturn(type);
		expect(runningBuild.getParametersProvider()).andReturn(parametersProvider);
		expect(parametersProvider.getAll()).andReturn(parameters);
		context.getRunnerContexts();
		expectLastCall().andAnswer(new IAnswer<Object>()
		{
			@Override
			public Object answer() throws Throwable
			{
				return new ArrayList<SRunnerContext>();
			}
		});
		expect(this.service.getAndIncrementFormattedSharedBuildNumber(2)).andReturn(null);
		expect(context.getSharedParameters()).andReturn(sharedParameters);
		expect(runningBuild.getRawBuildNumber()).andReturn("3.1.2.{0}");
		expect(runningBuild.getFullName()).andReturn("name");

		replay(this.service, context, runningBuild, type, parametersProvider);

		this.provider.updateParameters(context);

		verify(context, runningBuild, type, parametersProvider);
	}

	@Test
	public void testUpdateParameters06() throws IOException
	{
		BuildStartContext context = createStrictMock(BuildStartContext.class);
		SRunningBuild runningBuild = createStrictMock(SRunningBuild.class);
		SBuildType type = createStrictMock(SBuildType.class);
		ParametersProvider parametersProvider = createStrictMock(ParametersProvider.class);

		Map<String, String> parameters = new Hashtable<String, String>();
		parameters.put("anotherParameter", "okayValue %sharedBuildNumber.id2%");

		final Map<String, String> sharedParameters = new Hashtable<String, String>();

		expect(context.getBuild()).andReturn(runningBuild);
		expect(runningBuild.getFullName()).andReturn("name");
		expect(runningBuild.getBuildType()).andReturn(type);
		expect(runningBuild.getParametersProvider()).andReturn(parametersProvider);
		expect(parametersProvider.getAll()).andReturn(parameters);
		context.getRunnerContexts();
		expectLastCall().andAnswer(new IAnswer<Object>()
		{
			@Override
			public Object answer() throws Throwable
			{
				return new ArrayList<SRunnerContext>();
			}
		});
		expect(this.service.getAndIncrementFormattedSharedBuildNumber(2)).andReturn("1.1.5.20120912.176");
		context.addSharedParameter("sharedBuildNumber.id2", "1.1.5.20120912.176");
		expectLastCall().andAnswer(new IAnswer<Void>()
		{
			@Override
			public Void answer() throws Throwable
			{
				sharedParameters.put("sharedBuildNumber.id2", "1.1.5.20120912.176");
				return null;
			}
		});
		expect(context.getSharedParameters()).andReturn(sharedParameters);
		expect(runningBuild.getRawBuildNumber()).andReturn("3.1.2.{0}");
		expect(runningBuild.getFullName()).andReturn("name");

		replay(this.service, context, runningBuild, type, parametersProvider);

		this.provider.updateParameters(context);

		verify(context, runningBuild, type, parametersProvider);
	}

	@Test
	public void testUpdateParameters07() throws IOException
	{
		BuildStartContext context = createStrictMock(BuildStartContext.class);
		SRunningBuild runningBuild = createStrictMock(SRunningBuild.class);
		SBuildType type = createStrictMock(SBuildType.class);
		ParametersProvider parametersProvider = createStrictMock(ParametersProvider.class);

		Map<String, String> parameters = new Hashtable<String, String>();
		parameters.put("coolNumberParameter", "preValue %sharedBuildNumber.id7682% postValue");

		final Map<String, String> sharedParameters = new Hashtable<String, String>();

		expect(context.getBuild()).andReturn(runningBuild);
		expect(runningBuild.getFullName()).andReturn("name");
		expect(runningBuild.getBuildType()).andReturn(type);
		expect(runningBuild.getParametersProvider()).andReturn(parametersProvider);
		expect(parametersProvider.getAll()).andReturn(parameters);
		context.getRunnerContexts();
		expectLastCall().andAnswer(new IAnswer<Object>()
		{
			@Override
			public Object answer() throws Throwable
			{
				return new ArrayList<SRunnerContext>();
			}
		});
		expect(this.service.getAndIncrementFormattedSharedBuildNumber(7682)).andReturn("7.2.8.1539");
		context.addSharedParameter("sharedBuildNumber.id7682", "7.2.8.1539");
		expectLastCall().andAnswer(new IAnswer<Void>()
		{
			@Override
			public Void answer() throws Throwable
			{
				sharedParameters.put("sharedBuildNumber.id7682", "7.2.8.1539");
				return null;
			}
		});
		expect(context.getSharedParameters()).andReturn(sharedParameters);
		expect(runningBuild.getRawBuildNumber()).andReturn("%sharedBuildNumber.id7682%");
		runningBuild.setBuildNumber("7.2.8.1539");
		expectLastCall();
		context.addSharedParameter(ServerProvidedProperties.SYSTEM_BUILD_NUMBER, "7.2.8.1539");
		expectLastCall();
		context.addSharedParameter(ServerProvidedProperties.ENV_BUILD_NUMBER, "7.2.8.1539");
		expectLastCall();

		replay(this.service, context, runningBuild, type, parametersProvider);

		this.provider.updateParameters(context);

		verify(context, runningBuild, type, parametersProvider);
	}

	@Test
	public void testUpdateParameters08() throws IOException
	{
		BuildStartContext context = createStrictMock(BuildStartContext.class);
		SRunningBuild runningBuild = createStrictMock(SRunningBuild.class);
		SBuildType type = createStrictMock(SBuildType.class);
		ParametersProvider parametersProvider = createStrictMock(ParametersProvider.class);
		final SRunnerContext runnerContext1 = createStrictMock(SRunnerContext.class);
		final SRunnerContext runnerContext2 = createStrictMock(SRunnerContext.class);

		Map<String, String> parameters = new Hashtable<String, String>();
		parameters.put("buildNumber", "%sharedBuildNumber.id15%");

		final Map<String, String> sharedParameters = new Hashtable<String, String>();

		Map<String, String> rc1parameters = new Hashtable<String, String>();
		rc1parameters.put("rc1 param 1", "%sharedBuildNumber.id15%");
		rc1parameters.put("another", "%sharedBuildNumber.id17%");

		Map<String, String> rc2parameters = new Hashtable<String, String>();
		rc2parameters.put("rc2 param 1", "%sharedBuildNumber.id15%");

		expect(context.getBuild()).andReturn(runningBuild);
		expect(runningBuild.getFullName()).andReturn("name");
		expect(runningBuild.getBuildType()).andReturn(type);
		expect(runningBuild.getParametersProvider()).andReturn(parametersProvider);
		expect(parametersProvider.getAll()).andReturn(parameters);
		context.getRunnerContexts();
		expectLastCall().andAnswer(new IAnswer<Object>()
		{
			@Override
			public Object answer() throws Throwable
			{
				return Arrays.asList(runnerContext1, runnerContext2);
			}
		});
		expect(runnerContext1.getParameters()).andReturn(rc1parameters);
		expect(runnerContext2.getParameters()).andReturn(rc2parameters);
		expect(this.service.getAndIncrementFormattedSharedBuildNumber(15)).andReturn("1.0.1.20120915036671");
		expect(this.service.getAndIncrementFormattedSharedBuildNumber(17)).andReturn("23907");
		context.addSharedParameter("sharedBuildNumber.id15", "1.0.1.20120915036671");
		expectLastCall().andAnswer(new IAnswer<Void>()
		{
			@Override
			public Void answer() throws Throwable
			{
				sharedParameters.put("sharedBuildNumber.id15", "1.0.1.20120915036671");
				return null;
			}
		});
		context.addSharedParameter("sharedBuildNumber.id17", "23907");
		expectLastCall().andAnswer(new IAnswer<Void>()
		{
			@Override
			public Void answer() throws Throwable
			{
				sharedParameters.put("sharedBuildNumber.id17", "23907");
				return null;
			}
		});
		expect(runnerContext1.getParameters()).andReturn(rc1parameters);
		expect(runnerContext2.getParameters()).andReturn(rc2parameters);
		expect(context.getSharedParameters()).andReturn(sharedParameters);
		expect(runningBuild.getRawBuildNumber()).andReturn("%sharedBuildNumber.id17%");
		runningBuild.setBuildNumber("23907");
		expectLastCall();
		context.addSharedParameter(ServerProvidedProperties.SYSTEM_BUILD_NUMBER, "23907");
		expectLastCall();
		context.addSharedParameter(ServerProvidedProperties.ENV_BUILD_NUMBER, "23907");
		expectLastCall();

		replay(this.service, context, runningBuild, type, parametersProvider, runnerContext1, runnerContext2);

		this.provider.updateParameters(context);

		verify(context, runningBuild, type, parametersProvider, runnerContext1, runnerContext2);
	}
}