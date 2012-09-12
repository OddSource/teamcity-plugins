/*
 * AdditionalPropertyExtension.java from TeamCityPlugins modified Friday, September 7, 2012 09:59:17 CDT (-0500).
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
import jetbrains.buildServer.serverSide.ServerExtension;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
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
public class AdditionalServerPropertyExtension implements ServerExtension
{
	private static final Logger logger = Logger.getInstance("jetbrains.buildServer.PLUGIN.linuxSystemProperties.SERVER");

	private PluginDescriptor descriptor;

	private LinuxPropertiesLocator locator;

	public AdditionalServerPropertyExtension(@NotNull PluginDescriptor descriptor,
											 @NotNull LinuxPropertiesLocator locator)
	{
		AdditionalServerPropertyExtension.logger.info("Initializing linux-system-properties server extension.");

		this.descriptor = descriptor;
		this.locator = locator;
	}

	public void register()
	{
		AdditionalServerPropertyExtension.logger.info("Loading additional system properties.");

		Map<String, String> properties = this.locator.locateProperties(this.descriptor.getPluginRoot());

		for(String key : properties.keySet())
			System.setProperty(key, properties.get(key));
	}
}
