/*
 * BuildNumber.java from TeamCityPlugins modified Saturday, September 8, 2012 12:40:46 CDT (-0500).
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
 * A POJO holding the settings for a build number.
 *
 * @author Nick Williams
 * @version 1.0.0
 * @since 1.0.0
 */
public class SharedBuildNumberEntity
{
	private int id;

	private String name;

	private String description;

	private String format;

	private String dateFormat;

	private boolean incrementOnceForChain;

	private AtomicInteger counter = new AtomicInteger(1);

	public int getId()
	{
		return this.id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getDescription()
	{
		return this.description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getFormat()
	{
		return this.format;
	}

	public void setFormat(String format)
	{
		this.format = format;
	}

	public String getDateFormat()
	{
		return this.dateFormat;
	}

	public void setDateFormat(String dateFormat)
	{
		this.dateFormat = dateFormat;
	}

	public boolean isIncrementOnceForChain()
	{
		return this.incrementOnceForChain;
	}

	public void setIncrementOnceForChain(boolean incrementOnceForChain)
	{
		this.incrementOnceForChain = incrementOnceForChain;
	}

	public int getCounter()
	{
		return this.counter.get();
	}

	public void setCounter(int counter)
	{
		this.counter = new AtomicInteger(counter);
	}

	public int getAndIncrementCounter()
	{
		return this.counter.getAndIncrement();
	}
}
