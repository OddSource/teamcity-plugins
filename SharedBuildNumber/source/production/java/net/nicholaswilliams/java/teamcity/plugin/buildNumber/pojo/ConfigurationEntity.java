/*
 * Config.java from TeamCityPlugins modified Saturday, September 8, 2012 12:51:02 CDT (-0500).
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

package net.nicholaswilliams.java.teamcity.plugin.buildNumber.pojo;

import org.joda.time.DateTime;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

/**
 * Holds the plugin configuration.
 *
 * @author Nick Williams
 * @version 1.0.0
 * @since 1.0.0
 */
public class ConfigurationEntity
{
	private DateTime lastUpdate;

	private SettingsEntity settings;

	private Map<Integer, SharedBuildNumberEntity> buildNumbers = new Hashtable<Integer, SharedBuildNumberEntity>();

	public DateTime getLastUpdate()
	{
		return this.lastUpdate;
	}

	public void setLastUpdate(DateTime lastUpdate)
	{
		this.lastUpdate = lastUpdate;
	}

	public SettingsEntity getSettings()
	{
		return this.settings;
	}

	public void setSettings(SettingsEntity settings)
	{
		this.settings = settings;
	}

	public Collection<SharedBuildNumberEntity> getBuildNumbers()
	{
		return this.buildNumbers.values();
	}

	public SharedBuildNumberEntity getBuildNumber(int id)
	{
		return this.buildNumbers.get(id);
	}

	public void addOrUpdateBuildNumber(SharedBuildNumberEntity sharedBuildNumber)
	{
		this.buildNumbers.put(sharedBuildNumber.getId(), sharedBuildNumber);
	}

	public void removeBuildNumber(int id)
	{
		this.buildNumbers.remove(id);
	}
}
