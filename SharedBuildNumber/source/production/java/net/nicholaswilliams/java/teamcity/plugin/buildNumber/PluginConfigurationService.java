/*
 * PluginConfigurationService.java from TeamCityPlugins modified Friday, September 7, 2012 22:41:25 CDT (-0500).
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

import net.nicholaswilliams.java.teamcity.plugin.buildNumber.pojo.SharedBuildNumber;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.SortedSet;

/**
 * Specifies an interface for managing the configuration for this plugin.
 *
 * @author Nick Williams
 * @version 1.0.0
 * @since 1.0.0
 */
public interface PluginConfigurationService
{
	public static final String CONFIG_XSD_FILE_NAME = "shared-build-number-config-1.0.xsd";

	public static final String CONFIG_XML_FILE_NAME = "shared-build-number-config.xml";

	public int getNextBuildNumberId() throws IOException;

	@NotNull
	public int[] getAllSharedBuildNumberIds();

	@NotNull
	public SortedSet<SharedBuildNumber> getAllSharedBuildNumbersSortedById(boolean descending);

	@NotNull
	public SortedSet<SharedBuildNumber> getAllSharedBuildNumbersSortedByName(boolean descending);

	@Nullable
	public SharedBuildNumber getSharedBuildNumber(int id);

	@NotNull
	public String getSharedBuildNumberName(int id);

	public void deleteSharedBuildNumber(int id) throws IOException;

	@Nullable
	public String getAndIncrementFormattedSharedBuildNumber(int id) throws IOException;

	public void saveSharedBuildNumber(@NotNull SharedBuildNumber sharedBuildNumber) throws IOException;
}
