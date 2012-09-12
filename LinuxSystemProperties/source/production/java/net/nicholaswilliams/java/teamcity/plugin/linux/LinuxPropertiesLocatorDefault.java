/*
 * AbstractLinuxPropertiesLocatorDefalt.java from TeamCityPlugins modified Friday, September 7, 2012 17:25:51 CDT (-0500).
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
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Default implementation of the class that locates the Linux properties os.linux.flavor, os.linux.distribution.name
 * and os.linux.distribution.version.
 *
 * @author Nick Williams
 * @version 1.0.0
 * @since 1.0.0
 */
public class LinuxPropertiesLocatorDefault extends AbstractLinuxPropertiesLocator
{
	private static final Logger logger = Logger.getInstance("jetbrains.buildServer.PLUGIN.linuxSystemProperties.COMMON");

	private static final String OS_NAME_KEY = "os.name";

	@Override
	protected void locateLinuxProperties(Map<String, String> properties)
	{
		if(this.isLinux())
		{
			if(LinuxPropertiesLocatorDefault.logger.isDebugEnabled())
				LinuxPropertiesLocatorDefault.logger.debug("Operating system is Linux; detecting flavor.");

			for(Flavor flavor : Flavor.values())
			{
				try
				{
					File flavorFile = flavor.getFlavorFile();
					if(flavorFile.exists())
					{
						@SuppressWarnings("unchecked")
						List<String> flavorFileContents = FileUtils.readLines(flavorFile);

						properties.put(LinuxPropertiesLocator.OS_FLAVOR_KEY, flavor.name());

						flavor.locateLinuxProperties(flavorFileContents, properties);

						if(LinuxPropertiesLocatorDefault.logger.isDebugEnabled())
						{
							LinuxPropertiesLocatorDefault.logger.debug("Detected Linux flavor " + flavor.name() + ".");
						}

						break;
					}
					else if(LinuxPropertiesLocatorDefault.logger.isDebugEnabled())
					{
						LinuxPropertiesLocatorDefault.logger.debug("Linux flavor not " + flavor.name() + ".");
					}
				}
				catch(IOException e)
				{
					LinuxPropertiesLocatorDefault.logger.info(
							"Linux flavor not " + flavor.name() + "; " + e.toString()
					);
				}
			}
		}
	}

	private boolean isLinux()
	{
		String osName = System.getProperty(LinuxPropertiesLocatorDefault.OS_NAME_KEY);

		if(LinuxPropertiesLocatorDefault.logger.isDebugEnabled())
		{
			LinuxPropertiesLocatorDefault.logger.debug(LinuxPropertiesLocatorDefault.OS_NAME_KEY + " = " + osName);
		}

		return "linux".equalsIgnoreCase(osName);
	}

	private static enum Flavor
	{
		SuSE("/etc/SuSE-brand") {
			@Override
			public void locateLinuxProperties(List<String> flavorFileContents, Map<String, String> properties)
			{
				if(flavorFileContents.size() > 0)
				{
					String dist = flavorFileContents.get(0).trim();
					boolean isSles = "SLES".equalsIgnoreCase(dist);
					properties.put(
							LinuxPropertiesLocator.OS_DIST_NAME_KEY, isSles ? "SuSE Linux Enterprise Server" : dist
					);

					if(flavorFileContents.size() > 1)
					{
						String line = flavorFileContents.get(1);
						if(line.toLowerCase().startsWith("version"))
						{
							String[] parts = line.split("=", 2);
							if(parts.length > 1)
							{
								String version = parts[1].trim();
								if(isSles && !version.contains("."))
								{
									if(LinuxPropertiesLocatorDefault.logger.isDebugEnabled())
									{
										LinuxPropertiesLocatorDefault.logger.debug(
												"SLES found; determining SuSE patch number."
										);
									}

									try
									{
										@SuppressWarnings("unchecked")
										List<String> releaseFileContents =
												FileUtils.readLines(new File("/etc/SuSE-release"));

										if(releaseFileContents.size() > 2)
										{
											line = releaseFileContents.get(2);
											if(line.toLowerCase().startsWith("patchlevel"))
											{
												parts = line.split("=", 2);
												if(parts.length > 1)
													version += "." + parts[1].trim();
											}
										}
									}
									catch(IOException e)
									{
										LinuxPropertiesLocatorDefault.logger.warn(
												"Could not determine SuSE patch number; " + e.toString()
										);
									}
								}
								properties.put(LinuxPropertiesLocator.OS_DIST_VERSION_KEY, parts[1].trim());
							}
						}
					}
				}
				else
				{
					LinuxPropertiesLocatorDefault.logger.warn("Detected SuSE flavor, but /etc/SuSE-brand empty.");
				}
			}
		},

		RedHat("/etc/redhat-release") {
			@Override
			public void locateLinuxProperties(List<String> flavorFileContents, Map<String, String> properties)
			{
				if(flavorFileContents.size() > 0)
				{
					Pattern pattern =
							Pattern.compile("(.+)(\\srelease\\s(\\d+(\\.\\d+)?))(.+)?", Pattern.CASE_INSENSITIVE);
					Matcher matcher = pattern.matcher(flavorFileContents.get(0));

					if(matcher.find() && matcher.groupCount() > 0)
					{
						String dist = matcher.group(1);
						properties.put(LinuxPropertiesLocator.OS_DIST_NAME_KEY, dist.trim());
						if(matcher.groupCount() > 2)
						{
							String version = matcher.group(3);
							properties.put(LinuxPropertiesLocator.OS_DIST_VERSION_KEY, version.trim());
						}
						else
						{
							LinuxPropertiesLocatorDefault.logger.info(
									"Detected RedHat flavor, but /etc/redhat-release contents contained no version " +
									"information. Contents were: [" + flavorFileContents.get(0) + "]"
							);
						}
					}
					else
					{
						LinuxPropertiesLocatorDefault.logger.warn(
								"Detected RedHat flavor, but /etc/redhat-release contents do not match regular " +
								"expression. Contents were: [" + flavorFileContents.get(0) + "]"
						);
					}
				}
				else
				{
					LinuxPropertiesLocatorDefault.logger.warn("Detected RedHat flavor, but /etc/redhat-release empty.");
				}
			}
		},

		Debian("/etc/debian_version") {
			@Override
			public void locateLinuxProperties(List<String> flavorFileContents, Map<String, String> properties)
			{
				File file = new File("/etc/lsb-release");
				if(file.exists())
				{
					try
					{
						Properties lsbProperties = new Properties();
						lsbProperties.load(new FileReader(file));
						properties.put(
								LinuxPropertiesLocator.OS_DIST_NAME_KEY, lsbProperties.getProperty("DISTRIB_ID").trim()
						);
						properties.put(
								LinuxPropertiesLocator.OS_DIST_VERSION_KEY,
								lsbProperties.getProperty("DISTRIB_RELEASE").trim()
						);
					}
					catch(IOException e)
					{
						LinuxPropertiesLocatorDefault.logger.warn(
								"Detected Debian flavor, but failed to read /etc/lsb-release.", e
						);
					}
				}
				else
				{
					if(LinuxPropertiesLocatorDefault.logger.isDebugEnabled())
					{
						LinuxPropertiesLocatorDefault.logger.debug(
								"Detected Debian flavor, but /etc/lsb-release not found."
						);
					}

					properties.put(LinuxPropertiesLocator.OS_DIST_NAME_KEY, "Debian");
					if(flavorFileContents.size() > 0)
					{
						properties.put(LinuxPropertiesLocator.OS_DIST_VERSION_KEY, flavorFileContents.get(0).trim());
					}
					else
					{
						LinuxPropertiesLocatorDefault.logger.warn(
								"Detected Debian flavor, but /etc/debian_version empty."
						);
					}
				}
			}
		};

		private final File flavorFile;

		private Flavor(String flavorFileName)
		{
			this.flavorFile = new File(flavorFileName);
		}

		public File getFlavorFile() throws IOException
		{
			return this.flavorFile.getCanonicalFile();
		}

		public abstract void locateLinuxProperties(List<String> flavorFileContents, Map<String, String> properties);
	}
}
