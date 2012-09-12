/*
 * JodaDateTimeConverter.java from TeamCityPlugins modified Sunday, September 9, 2012 16:36:46 CDT (-0500).
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

import org.apache.commons.beanutils.converters.AbstractConverter;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.util.Assert;

/**
 * Converts ISO-8601-formatted XML dates to Joda {@link DateTime}s.
 *
 * @author Nick Williams
 * @version 1.0.0
 * @since 1.0.0
 */
public class JodaXML8601DateTimeConverter extends AbstractConverter
{
	private DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTime();

	private DateTimeFormatter backupDateTimeFormatter = ISODateTimeFormat.dateTimeNoMillis();

	@NotNull
	public DateTimeFormatter getDateTimeFormatter()
	{
		return this.dateTimeFormatter;
	}

	public void setDateTimeFormatter(@NotNull DateTimeFormatter dateTimeFormatter)
	{
		Assert.notNull(dateTimeFormatter, "The date/time format cannot be null.");

		this.dateTimeFormatter = dateTimeFormatter;
	}

	@NotNull
	public DateTimeFormatter getBackupDateTimeFormatter()
	{
		return this.backupDateTimeFormatter;
	}

	public void setBackupDateTimeFormatter(@NotNull DateTimeFormatter backupDateTimeFormatter)
	{
		Assert.notNull(dateTimeFormatter, "The date/time format cannot be null.");

		this.backupDateTimeFormatter = backupDateTimeFormatter;
	}

	@Override
	@NotNull
	protected Class<DateTime> getDefaultType()
	{
		return DateTime.class;
	}

	@Override
	@NotNull
	protected DateTime convertToType(@NotNull Class typeClass, @NotNull Object value)
	{
		Assert.notNull(typeClass, "The type class cannot be null.");
		Assert.notNull(value, "The value cannot be null.");

		if(typeClass != DateTime.class)
			throw new IllegalArgumentException("This converter can only convert to Joda Time's DateTime.");

		String string = value.toString();

		try
		{
			return this.dateTimeFormatter.parseDateTime(string);
		}
		catch(IllegalArgumentException e)
		{
			return this.backupDateTimeFormatter.parseDateTime(string);
		}
	}
}
