/*
 * TestConfigurationDigesterModule.java from TeamCityPlugins modified Monday, September 10, 2012 09:26:05 CDT (-0500).
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

import net.nicholaswilliams.java.teamcity.plugin.buildNumber.pojo.ConfigurationEntity;
import net.nicholaswilliams.java.teamcity.plugin.buildNumber.pojo.SettingsEntity;
import net.nicholaswilliams.java.teamcity.plugin.buildNumber.pojo.SharedBuildNumberEntity;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.binder.DigesterLoader;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Test class for ConfigurationDigesterModule.
 */
public class TestConfigurationDigesterModule
{
	private Digester digester;

	@Before
	public void setUp() throws SAXException, ParserConfigurationException
	{
		ConfigurationDigesterModule module = new ConfigurationDigesterModule();
		DigesterLoader loader = DigesterLoader.newLoader(module);

		Schema schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
									 .newSchema(this.getResource("shared-build-number-config-1.0.xsd"));

		loader.setNamespaceAware(true);
		loader.setSchema(schema);
		loader.setErrorHandler(new ConfigurationErrorHandler());
		loader.setUseContextClassLoader(false);
		loader.setClassLoader(Digester.class.getClassLoader());

		ConvertUtils.register(new JodaXML8601DateTimeConverter(), DateTime.class);

		this.digester = loader.newDigester();
		this.digester.setFeature("http://xml.org/sax/features/validation", true);
		this.digester.setFeature("http://apache.org/xml/features/validation/schema", true);
		this.digester.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);
	}

	@After
	public void tearDown()
	{
		ConvertUtils.deregister();
	}

	public URL getResource(String file)
	{
		return TestConfigurationDigesterModule.class.getResource("./" + file);
	}

	@Test
	public void testDistributedFile() throws IOException, SAXException
	{
		ConfigurationEntity configuration = this.digester.parse(this.getResource("shared-build-number-config.xml.dist"));

		assertNotNull("The configuration should not be null.", configuration);

		assertNotNull("The last update date should not be null.", configuration.getLastUpdate());
		assertTrue("The last update date should be before now.", configuration.getLastUpdate().isBeforeNow());
		assertTrue("The last update date should be after 8/8/2012.",
				   configuration.getLastUpdate().isAfter(new DateTime(2012, 8, 8, 0, 0, 0, 0)));

		SettingsEntity settings = configuration.getSettings();
		assertNotNull("The settings object should not be null.", settings);
		assertEquals("The sequence setting is not correct.", 3, settings.getBuildNumberIdSequence());

		Collection<SharedBuildNumberEntity> buildNumbers = configuration.getBuildNumbers();
		assertNotNull("The collection should not be null.", buildNumbers);
		assertEquals("The collection is the wrong size.", 2, buildNumbers.size());

		SharedBuildNumberEntity n1 = null;
		SharedBuildNumberEntity n2 = null;
		for(SharedBuildNumberEntity buildNumber : buildNumbers)
		{
			if(buildNumber.getId() == 1)
				n1 = buildNumber;
			else if(buildNumber.getId() == 2)
				n2 = buildNumber;
		}

		assertNotNull("Build number 1 should not be null.", n1);
		assertEquals("The name is not correct (1).", "Sample Counter-Based Build Number", n1.getName());
		assertEquals("The description is not correct (1).",
					 "This sample build number exists when the plugin in installed. It can be safely removed or used " +
					 		"for testing purposes.",
					 n1.getDescription());
		assertEquals("The format is not correct (1).", "1.0.0.{0}", n1.getFormat());
		assertNull("The date format should be null (1).", n1.getDateFormat());
		assertEquals("The counter is not correct (1).", 123, n1.getCounter());

		assertNotNull("Build number 2 should not be null.", n2);
		assertEquals("The name is not correct (2).", "Sample Date-Based Build Number", n2.getName());
		assertEquals("The description is not correct (2).",
					 "This sample build number exists when the plugin in installed. It can be safely removed or used " +
					 "for testing purposes.",
					 n2.getDescription());
		assertEquals("The format is not correct (2).", "2.0.0.{d}", n2.getFormat());
		assertEquals("The date format is not correct (2).", "yyyyMMddHHmmss", n2.getDateFormat());
		assertEquals("The counter is not correct (2).", 1, n2.getCounter());
	}

	@Test
	public void testInvalidFile() throws IOException, SAXException
	{
		try
		{
			this.digester.parse(this.getResource("testInvalidFile.xml"));
			fail("Expected exception org.xml.sax.SAXException, got no exception.");
		}
		catch(SAXException e)
		{
			assertTrue("The error message [" + e.getMessage() + "] is not correct.",
					   e.getMessage().contains("The content of element 'shared-build-number-config' is not complete."));
		}
	}

	@Test
	public void testInvalidLastUpdateDate() throws IOException, SAXException
	{
		try
		{
			this.digester.parse(this.getResource("testInvalidLastUpdateDate.xml"));
			fail("Expected exception org.xml.sax.SAXException, got no exception.");
		}
		catch(SAXException e)
		{
			assertTrue("The error message [" + e.getMessage() + "] is not correct.",
					   e.getMessage().contains("is not a valid value for 'dateTime'."));
		}
	}
}