/*
 * AdditionalAgentPropertyExtension.java from TeamCityPlugins modified Friday, September 7, 2012 10:54:42 CDT (-0500).
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

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.agent.AgentExtension;
import jetbrains.buildServer.agent.AgentLifeCycleAdapter;
import jetbrains.buildServer.agent.BuildAgent;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.agent.plugins.beans.PluginDescriptor;
import jetbrains.buildServer.util.EventDispatcher;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Registers additional system properties os.linux.flavor, os.linux.distribution.name and os.linux.distribution.version
 * upon TeamCity startup.
 *
 * @author Nick Williams
 * @version 1.0.0
 * @since 1.0.0
 */
public class AdditionalAgentPropertyExtension extends AgentLifeCycleAdapter implements AgentExtension
{
	private static final Logger logger = Logger.getInstance("jetbrains.buildServer.PLUGIN.linuxSystemProperties.AGENT");

	private PluginDescriptor descriptor;

	private LinuxPropertiesLocator locator;

	public AdditionalAgentPropertyExtension(@NotNull EventDispatcher<AgentLifeCycleAdapter> dispatcher,
											@NotNull PluginDescriptor descriptor,
											@NotNull LinuxPropertiesLocator locator)
	{
		AdditionalAgentPropertyExtension.logger.info("Initializing linux-system-properties agent extension.");

		dispatcher.addListener(this);

		this.descriptor = descriptor;
		this.locator = locator;
	}

	@Override
	public void beforeAgentConfigurationLoaded(@NotNull BuildAgent agent)
	{
		AdditionalAgentPropertyExtension.logger.info("Loading additional system properties.");

		BuildAgentConfiguration configuration = agent.getConfiguration();

		Map<String, String> properties = this.locator.locateProperties(this.descriptor.getPluginRoot());

		for(String key : properties.keySet())
			configuration.addSystemProperty(key, properties.get(key));
	}
}
