/*
 * LinuxPropertiesLocator.java from TeamCityPlugins modified Friday, September 7, 2012 16:48:17 CDT (-0500).
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

import java.io.File;
import java.util.Map;

/**
 * An interface for a class locating the Linux properties os.linux.flavor, os.linux.distribution.name and
 * os.linux.distribution.version and for locating the current plugin version property
 * nwts.plugin.linux.system.properties.version.
 *
 * @author Nick Williams
 * @version 1.0.0
 * @since 1.0.0
 */
public interface LinuxPropertiesLocator
{
	public static final String PLUGIN_VERSION_KEY = "nwts.plugin.linux.system.properties.version";

	public static final String OS_FLAVOR_KEY = "os.linux.flavor";

	public static final String OS_DIST_NAME_KEY = "os.linux.distribution.name";

	public static final String OS_DIST_VERSION_KEY = "os.linux.distribution.version";

	public Map<String, String> locateProperties(File pluginRoot);
}
