/*
 * Settings.java from TeamCityPlugins modified Saturday, September 8, 2012 12:49:52 CDT (-0500).
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

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Holds settings for the plugin.
 *
 * @author Nick Williams
 * @version 1.0.0
 * @since 1.0.0
 */
public class SettingsEntity
{
	private AtomicInteger buildNumberIdSequence = new AtomicInteger(1);

	public int getBuildNumberIdSequence()
	{
		return this.buildNumberIdSequence.get();
	}

	public void setBuildNumberIdSequence(int buildNumberIdSequence)
	{
		this.buildNumberIdSequence = new AtomicInteger(buildNumberIdSequence);
	}

	public int getAndIncrementBuildNumberIdSequence()
	{
		return this.buildNumberIdSequence.getAndIncrement();
	}
}
