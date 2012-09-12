/*
 * TestBuildNumberPropertiesProvider.java from TeamCityPlugins modified Monday, September 10, 2012 15:04:54 CDT (-0500).
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
import jetbrains.buildServer.serverSide.BuildStartContextProcessor;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.parameters.BuildParametersProvider;
import jetbrains.buildServer.serverSide.parameters.ParameterDescriptionProvider;
import org.easymock.Capture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
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
}