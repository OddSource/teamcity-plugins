/*
 * AbstractLinuxPropertiesLocator.java from TeamCityPlugins modified Friday, September 21, 2012 23:48:37 CDT (-0500).
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
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

/**
 * An abstract class for locating the Linux properties os.linux.flavor, os.linux.distribution.name and
 * os.linux.distribution.version.
 *
 * @author Nick Williams
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class AbstractLinuxPropertiesLocator implements LinuxPropertiesLocator
{
	private static final Logger logger = Logger.getInstance("jetbrains.buildServer.PLUGIN.linuxSystemProperties.COMMON");

	@Override
	public Map<String, String> locateProperties(File pluginRoot)
	{
		Hashtable<String, String> properties = new Hashtable<String, String>();

		this.determinePluginVersion(pluginRoot, properties);
		this.locateLinuxProperties(properties);

		return properties;
	}

	private void determinePluginVersion(File pluginRoot, Map<String, String> properties)
	{
		File versionFile = new File(pluginRoot, "version.properties");
		if(versionFile.exists())
		{
			if(versionFile.canRead())
			{
				Properties versionProperties = new Properties();
				FileInputStream stream = null;
				try
				{
					stream = FileUtils.openInputStream(versionFile);
					versionProperties.load(stream);
					if(versionProperties.getProperty(LinuxPropertiesLocator.PLUGIN_VERSION_KEY) != null)
					{
						properties.put(
								LinuxPropertiesLocator.PLUGIN_VERSION_KEY,
								versionProperties.getProperty(LinuxPropertiesLocator.PLUGIN_VERSION_KEY)
						);
					}
				}
				catch(IOException e)
				{
					AbstractLinuxPropertiesLocator.logger.error(
							"linux-system-properties plugin version file could not be read.", e
					);
				}
				finally
				{
					try
					{
						if(stream != null)
							stream.close();
					}
					catch(IOException e)
					{
						AbstractLinuxPropertiesLocator.logger.warn("Unable to close version file after reading.", e);
					}
				}
			}
			else
			{
				AbstractLinuxPropertiesLocator.logger.warn("linux-system-properties plugin version file not readable.");
			}
		}
		else
		{
			AbstractLinuxPropertiesLocator.logger.warn("linux-system-properties plugin version file not found.");
		}
	}

	protected abstract void locateLinuxProperties(Map<String, String> properties);
}
