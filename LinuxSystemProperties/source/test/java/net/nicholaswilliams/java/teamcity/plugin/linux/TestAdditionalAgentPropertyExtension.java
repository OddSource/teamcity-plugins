/*
 * TestAdditionalAgentPropertyExtension.java from TeamCityPlugins modified Wednesday, September 12, 2012 17:10:47 CDT (-0500).
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

import jetbrains.buildServer.agent.AgentLifeCycleAdapter;
import jetbrains.buildServer.agent.BuildAgent;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.agent.plugins.beans.PluginDescriptor;
import jetbrains.buildServer.util.EventDispatcher;
import org.easymock.Capture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Test class for AdditionalAgentPropertyExtension.
 */
public class TestAdditionalAgentPropertyExtension
{
	private EventDispatcher<AgentLifeCycleAdapter> dispatcher;

	private PluginDescriptor descriptor;

	private LinuxPropertiesLocator locator;

	private AdditionalAgentPropertyExtension extension;

	@Before
	@SuppressWarnings("unchecked")
	public void setUp()
	{
		this.dispatcher = createMockBuilder(EventDispatcher.class)
				.addMockedMethod("addListener")
				.createStrictMock();

		this.descriptor = createStrictMock(PluginDescriptor.class);

		this.locator = createStrictMock(LinuxPropertiesLocator.class);

		Capture<AgentLifeCycleAdapter> capture = new Capture<AgentLifeCycleAdapter>();

		this.dispatcher.addListener(capture(capture));
		expectLastCall();

		replay(this.dispatcher);

		this.extension = new AdditionalAgentPropertyExtension(this.dispatcher, this.descriptor, this.locator);

		assertSame("The captured adapter is not correct.", this.extension, capture.getValue());
	}

	@After
	public void tearDown()
	{
		verify(this.dispatcher, this.descriptor, this.locator);
	}

	@Test
	public void testBeforeAgentConfigurationLoaded01()
	{
		BuildAgent agent = createStrictMock(BuildAgent.class);
		BuildAgentConfiguration configuration = createStrictMock(BuildAgentConfiguration.class);

		File pluginRoot = new File(".");
		Map<String, String> properties = new LinkedHashMap<String, String>();

		expect(agent.getConfiguration()).andReturn(configuration);
		expect(this.descriptor.getPluginRoot()).andReturn(pluginRoot);
		expect(this.locator.locateProperties(same(pluginRoot))).andReturn(properties);

		replay(this.descriptor, this.locator, agent, configuration);

		this.extension.beforeAgentConfigurationLoaded(agent);

		verify(agent, configuration);
	}

	@Test
	public void testBeforeAgentConfigurationLoaded02()
	{
		BuildAgent agent = createStrictMock(BuildAgent.class);
		BuildAgentConfiguration configuration = createStrictMock(BuildAgentConfiguration.class);

		File pluginRoot = new File(".");
		Map<String, String> properties = new LinkedHashMap<String, String>();
		properties.put("myProperty1", "someValue");
		properties.put("yourOtherTwo", "anotherAwesome");
		properties.put("finalLastCool", "okayNifty");

		expect(agent.getConfiguration()).andReturn(configuration);
		expect(this.descriptor.getPluginRoot()).andReturn(pluginRoot);
		expect(this.locator.locateProperties(same(pluginRoot))).andReturn(properties);
		configuration.addSystemProperty("myProperty1", "someValue");
		expectLastCall();
		configuration.addSystemProperty("yourOtherTwo", "anotherAwesome");
		expectLastCall();
		configuration.addSystemProperty("finalLastCool", "okayNifty");
		expectLastCall();

		replay(this.descriptor, this.locator, agent, configuration);

		this.extension.beforeAgentConfigurationLoaded(agent);

		verify(agent, configuration);
	}

	@Test
	public void testBeforeAgentConfigurationLoaded03()
	{
		BuildAgent agent = createStrictMock(BuildAgent.class);
		BuildAgentConfiguration configuration = createStrictMock(BuildAgentConfiguration.class);

		File pluginRoot = new File(".");
		Map<String, String> properties = new LinkedHashMap<String, String>();
		properties.put("my.property", "your.value");

		expect(agent.getConfiguration()).andReturn(configuration);
		expect(this.descriptor.getPluginRoot()).andReturn(pluginRoot);
		expect(this.locator.locateProperties(same(pluginRoot))).andReturn(properties);
		configuration.addSystemProperty("my.property", "your.value");
		expectLastCall();

		replay(this.descriptor, this.locator, agent, configuration);

		this.extension.beforeAgentConfigurationLoaded(agent);

		verify(agent, configuration);
	}
}