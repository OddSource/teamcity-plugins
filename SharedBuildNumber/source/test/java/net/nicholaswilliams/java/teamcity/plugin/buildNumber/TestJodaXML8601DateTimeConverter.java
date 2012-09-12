/*
 * TestJodaXML8601DateTimeConverter.java from TeamCityPlugins modified Monday, September 10, 2012 11:21:48 CDT (-0500).
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

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * Test class for JodaXML8601DateTimeConverter.
 */
public class TestJodaXML8601DateTimeConverter
{
	private JodaXML8601DateTimeConverter converter;

	@Before
	public void setUp()
	{
		this.converter = new JodaXML8601DateTimeConverter();
	}

	@After
	public void tearDown()
	{

	}

	@Test
	public void testGetDefaultType()
	{
		assertEquals("The default type is not correct.", DateTime.class, this.converter.getDefaultType());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testSetDateTimeFormatter01()
	{
		this.converter.setDateTimeFormatter(null);
	}

	@Test
	public void testSetDateTimeFormatter02()
	{
		assertSame("The initial formatter is not correct.", ISODateTimeFormat.dateTime(),
				   this.converter.getDateTimeFormatter());

		this.converter.setDateTimeFormatter(DateTimeFormat.fullDateTime());

		assertSame("The formatter is not correct.", DateTimeFormat.fullDateTime(),
				   this.converter.getDateTimeFormatter());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testSetBackupDateTimeFormatter01()
	{
		this.converter.setBackupDateTimeFormatter(null);
	}

	@Test
	public void testSetBackupDateTimeFormatter02()
	{
		assertSame("The initial formatter is not correct.", ISODateTimeFormat.dateTimeNoMillis(),
				   this.converter.getBackupDateTimeFormatter());

		this.converter.setBackupDateTimeFormatter(DateTimeFormat.fullDateTime());

		assertSame("The formatter is not correct.", DateTimeFormat.fullDateTime(),
				   this.converter.getBackupDateTimeFormatter());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testIllegalArgument01()
	{
		this.converter.convertToType(null, "2012-09-09T16:44:48.431-06:00");
	}

	@Test(expected=IllegalArgumentException.class)
	public void testIllegalArgument02()
	{
		this.converter.convertToType(DateTime.class, null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testIllegalArgument03()
	{
		this.converter.convertToType(AtomicInteger.class, "2012-09-09T16:44:48.431-06:00");
	}

	@Test
	public void testDefaultConfiguration01()
	{
		DateTime dateTime = this.converter.convertToType(DateTime.class, "2012-09-09T16:44:48.431-06:00");

		assertNotNull("The date time should not be null.", dateTime);
	}

	@Test
	public void testDefaultConfiguration02()
	{
		DateTime dateTime = this.converter.convertToType(DateTime.class, "2012-09-09T16:44:48.431Z");

		assertNotNull("The date time should not be null.", dateTime);
	}

	@Test
	public void testDefaultConfiguration03()
	{
		DateTime dateTime = this.converter.convertToType(DateTime.class, "2012-09-09T16:44:48-06:00");

		assertNotNull("The date time should not be null.", dateTime);
	}

	@Test
	public void testDefaultConfiguration04()
	{
		DateTime dateTime = this.converter.convertToType(DateTime.class, "2012-09-09T16:44:48Z");

		assertNotNull("The date time should not be null.", dateTime);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testDefaultConfiguration05()
	{
		this.converter.convertToType(DateTime.class, "09/09/2012 15:22:24");
	}

	@Test
	public void testOtherConfiguration01()
	{
		this.converter.setDateTimeFormatter(DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss"));
		this.converter.setDateTimeFormatter(DateTimeFormat.forPattern("MM/dd/yy HH:mm:ss"));

		DateTime dateTime = this.converter.convertToType(DateTime.class, "09/09/2012 15:22:24");

		assertNotNull("The date time should not be null.", dateTime);
	}

	@Test
	public void testOtherConfiguration02()
	{
		this.converter.setDateTimeFormatter(DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss"));
		this.converter.setDateTimeFormatter(DateTimeFormat.forPattern("MM/dd/yy HH:mm:ss"));

		DateTime dateTime = this.converter.convertToType(DateTime.class, "09/09/12 15:22:24");

		assertNotNull("The date time should not be null.", dateTime);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testOtherConfiguration03()
	{
		this.converter.setDateTimeFormatter(DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss"));
		this.converter.setDateTimeFormatter(DateTimeFormat.forPattern("MM/dd/yy HH:mm:ss"));

		this.converter.convertToType(DateTime.class, "2012/09/09 15:22:24");
	}
}