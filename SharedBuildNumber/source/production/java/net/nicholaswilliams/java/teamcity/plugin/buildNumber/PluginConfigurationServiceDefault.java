/*
 * PluginConfigurationServiceDefault.java from TeamCityPlugins modified Friday, September 7, 2012 22:43:27 CDT (-0500).
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

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.configuration.ChangeListener;
import jetbrains.buildServer.configuration.FileWatcher;
import jetbrains.buildServer.serverSide.ServerPaths;
import net.nicholaswilliams.java.teamcity.plugin.buildNumber.pojo.ConfigurationEntity;
import net.nicholaswilliams.java.teamcity.plugin.buildNumber.pojo.SettingsEntity;
import net.nicholaswilliams.java.teamcity.plugin.buildNumber.pojo.SharedBuildNumber;
import net.nicholaswilliams.java.teamcity.plugin.buildNumber.pojo.SharedBuildNumberEntity;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.binder.DigesterLoader;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.FatalBeanException;
import org.springframework.util.Assert;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Default class for managing the configuration for this plugin.
 *
 * @author Nick Williams
 * @version 1.0.0
 * @since 1.0.0
 */
public class PluginConfigurationServiceDefault implements PluginConfigurationService, ChangeListener
{
	private static final Logger logger = Logger.getInstance("jetbrains.buildServer.PLUGIN.sharedBuildNumber.SERVICE");

	private static final String DIST_CONFIG_XML_FILE_NAME = "shared-build-number-config.xml.dist";

	private final File xsdFile;

	private final File configFile;

	private final DigesterLoader digesterLoader;

	private final ReentrantReadWriteLock configLock;

	private FileWatcher configFileWatcher;

	private List<String> configFileHeader;

	private ConfigurationEntity configuration;

	public PluginConfigurationServiceDefault(@NotNull ServerPaths serverPaths)
	{
		File configDirectory = PluginFileUtils.getCanonicalFile(new File(serverPaths.getConfigDir()));
		this.xsdFile = new File(configDirectory, PluginConfigurationService.CONFIG_XSD_FILE_NAME);
		this.configFile = new File(configDirectory, PluginConfigurationService.CONFIG_XML_FILE_NAME);

		this.digesterLoader = DigesterLoader.newLoader(new ConfigurationDigesterModule());

		this.configLock = new ReentrantReadWriteLock();
	}

	protected void finalize() throws Throwable
	{
		super.finalize();

		this.destroy();
	}

	@Override
	public synchronized int getNextBuildNumberId() throws IOException
	{
		PluginConfigurationServiceDefault.logger.info(
				"Getting next shared build number ID and incrementing the sequence."
		);

		this.configLock.writeLock().lock();

		try
		{
			SettingsEntity settings = this.configuration.getSettings();

			int nextBuildNumberId = settings.getAndIncrementBuildNumberIdSequence();

			this.saveConfiguration();

			return nextBuildNumberId;
		}
		finally
		{
			this.configLock.writeLock().unlock();
		}
	}

	@Override
	@NotNull
	public int[] getAllSharedBuildNumberIds()
	{
		this.configLock.readLock().lock();

		try
		{
			Collection<SharedBuildNumberEntity> buildNumbers = this.configuration.getBuildNumbers();
			int[] ids = new int[buildNumbers.size()];

			int i = 0;
			for(SharedBuildNumberEntity buildNumber : buildNumbers)
				ids[i++] = buildNumber.getId();
			return ids;
		}
		finally
		{
			this.configLock.readLock().unlock();
		}
	}

	@Override
	@NotNull
	public SortedSet<SharedBuildNumber> getAllSharedBuildNumbersSortedById(final boolean descending)
	{
		if(PluginConfigurationServiceDefault.logger.isDebugEnabled())
			PluginConfigurationServiceDefault.logger.debug("Getting all shared build numbers ordered by ID.");

		this.configLock.readLock().lock();

		try
		{
			return this.getTranslatedSet(this.configuration.getBuildNumbers(), new Comparator<SharedBuildNumber>()
			{
				@Override
				public int compare(SharedBuildNumber left, SharedBuildNumber right)
				{
					int id1 = left.getId();
					int id2 = right.getId();

					if(descending)
						return id1 < id2 ? 1 : (id2 < id1 ? -1 : 0);
					else
						return id1 < id2 ? -1 : (id2 < id1 ? 1 : 0);
				}
			});
		}
		finally
		{
			this.configLock.readLock().unlock();
		}
	}

	@Override
	@NotNull
	public SortedSet<SharedBuildNumber> getAllSharedBuildNumbersSortedByName(final boolean descending)
	{
		if(PluginConfigurationServiceDefault.logger.isDebugEnabled())
			PluginConfigurationServiceDefault.logger.debug("Getting all shared build numbers ordered by name.");

		this.configLock.readLock().lock();

		try
		{
			return this.getTranslatedSet(this.configuration.getBuildNumbers(), new Comparator<SharedBuildNumber>()
			{
				@Override
				public int compare(SharedBuildNumber left, SharedBuildNumber right)
				{
					if(left.getName() == null && right.getName() == null)
						return 0;

					if(descending)
						return right.getName() == null ? -1 : right.getName().compareTo(left.getName());
					else
						return left.getName() == null ? -1 : left.getName().compareTo(right.getName());
				}
			});
		}
		finally
		{
			this.configLock.readLock().unlock();
		}
	}

	private SortedSet<SharedBuildNumber> getTranslatedSet(Collection<SharedBuildNumberEntity> buildNumbers,
														  Comparator<SharedBuildNumber> comparator)
	{
		TreeSet<SharedBuildNumber> set = new TreeSet<SharedBuildNumber>(comparator);

		for(SharedBuildNumberEntity entity : buildNumbers)
			set.add(new SharedBuildNumber(entity));

		return set;
	}

	@Override
	@Nullable
	public SharedBuildNumber getSharedBuildNumber(int id)
	{
		if(PluginConfigurationServiceDefault.logger.isDebugEnabled())
			PluginConfigurationServiceDefault.logger.debug("Getting shared build number [" + id + "].");

		this.configLock.readLock().lock();

		try
		{
			SharedBuildNumberEntity entity = this.configuration.getBuildNumber(id);
			return entity == null ? null : new SharedBuildNumber(entity);
		}
		finally
		{
			this.configLock.readLock().unlock();
		}
	}

	@Override
	@NotNull
	public String getSharedBuildNumberName(int id)
	{
		this.configLock.readLock().lock();

		try
		{
			SharedBuildNumberEntity entity = this.configuration.getBuildNumber(id);
			return entity == null ? "" : entity.getName();
		}
		finally
		{
			this.configLock.readLock().unlock();
		}
	}

	@Override
	public void deleteSharedBuildNumber(int id) throws IOException
	{
		this.configLock.writeLock().lock();

		try
		{
			this.configuration.removeBuildNumber(id);
			this.saveConfiguration();
		}
		finally
		{
			this.configLock.writeLock().unlock();
		}
	}

	@Override
	@Nullable
	public String getAndIncrementFormattedSharedBuildNumber(int id) throws IOException
	{
		this.configLock.writeLock().lock();
		this.configLock.readLock().lock();

		try
		{
			SharedBuildNumberEntity buildNumber = this.configuration.getBuildNumber(id);
			if(buildNumber == null)
				return null;

			int counter = buildNumber.getAndIncrementCounter();
			this.saveConfiguration();
			this.configLock.writeLock().unlock();

			String number = buildNumber.getFormat().replace("{0}", Integer.toString(counter));

			if(number.toLowerCase().contains("{d}"))
			{
				String date = new SimpleDateFormat(buildNumber.getDateFormat()).format(new Date());
				number = number.replace("{d}", date).replace("{D}", date);
			}

			return number;
		}
		finally
		{
			if(this.configLock.writeLock().isHeldByCurrentThread())
				this.configLock.writeLock().unlock();
			this.configLock.readLock().unlock();
		}
	}

	@Override
	public void saveSharedBuildNumber(@NotNull SharedBuildNumber sharedBuildNumber) throws IOException
	{
		Assert.notNull(sharedBuildNumber, "The shared build number cannot be null.");

		PluginConfigurationServiceDefault.logger.info(
				"Saving shared build number [" + sharedBuildNumber.getId() + "]."
		);

		this.configLock.writeLock().lock();

		try
		{
			SharedBuildNumberEntity entity = this.configuration.getBuildNumber(sharedBuildNumber.getId());
			if(entity == null)
				entity = new SharedBuildNumberEntity();

			if(entity.getCounter() > sharedBuildNumber.getCounter())
			{
				throw new IllegalArgumentException(
						"You cannot decrease the counter number; if changed, it can only be increased."
				);
			}

			entity.setId(sharedBuildNumber.getId());
			entity.setName(sharedBuildNumber.getName());
			entity.setDescription(sharedBuildNumber.getDescription());
			entity.setFormat(sharedBuildNumber.getFormat());
			entity.setDateFormat(sharedBuildNumber.getDateFormat());
			entity.setIncrementOnceForChain(sharedBuildNumber.isIncrementOnceForChain());
			entity.setCounter(sharedBuildNumber.getCounter());

			this.configuration.addOrUpdateBuildNumber(entity);
			this.saveConfiguration();
		}
		finally
		{
			this.configLock.writeLock().unlock();
		}
	}

	@Override
	public void changeOccured(String requester)
	{
		PluginConfigurationServiceDefault.logger.info(
				"Observed change in configuration file. Reloading configuration."
		);

		this.configLock.writeLock().lock();

		try
		{
			this.configFileWatcher.stop();

			this.loadConfiguration();

			this.configFileWatcher.start();
		}
		finally
		{
			this.configLock.writeLock().unlock();
		}
	}

	public void initialize()
	{
		PluginConfigurationServiceDefault.logger.info("Initializing the advanced shared build number plugin.");

		this.configLock.writeLock().lock();

		PluginConfigurationServiceDefault.logger.debug("");

		try
		{
			if(!this.xsdFile.exists() || !this.xsdFile.canRead())
				this.copyXsdFileToDestination();

			this.loadDefaultConfigFileHeader();

			if(!this.configFile.exists())
				this.copyDefaultConfigFileToDestination();
			else if(!this.configFile.canRead() || !this.configFile.canWrite())
				throw new FatalBeanException("Existing configuration in place, but not readable and writable");

			this.initializeXmlDigesterLoader();

			this.loadConfiguration();

			this.initializeFileWatcher();
		}
		finally
		{
			this.configLock.writeLock().unlock();
		}
	}

	public void destroy()
	{
		this.configLock.writeLock().lock();

		try
		{
			if(this.configFileWatcher != null)
			{
				this.configFileWatcher.stop();
				this.configFileWatcher = null;
			}
		}
		finally
		{
			this.configLock.writeLock().unlock();
		}
	}

	protected void copyXsdFileToDestination()
	{
		PluginConfigurationServiceDefault.logger.info("Copying distributed XSD file to TeamCity config directory.");

		try
		{
			if(this.xsdFile.exists())
				FileUtils.forceDelete(this.xsdFile);

			PluginFileUtils.copyResource(
					this.getClass(), PluginConfigurationService.CONFIG_XSD_FILE_NAME, this.xsdFile
			);
		}
		catch(Exception e)
		{
			throw new FatalBeanException("Could not copy distributed configuration XSD file", e);
		}
	}

	protected void loadDefaultConfigFileHeader()
	{
		if(PluginConfigurationServiceDefault.logger.isDebugEnabled())
			PluginConfigurationServiceDefault.logger.debug("Loading and caching header lines from distributed config.");

		try
		{
			List<String> lines = PluginFileUtils.readLines(
					this.getClass(), PluginConfigurationServiceDefault.DIST_CONFIG_XML_FILE_NAME
			);

			this.configFileHeader = Collections.unmodifiableList(lines.subList(0, 22));
		}
		catch(Exception e)
		{
			throw new FatalBeanException("Could not read distributed configuration XML file", e);
		}
	}

	protected void copyDefaultConfigFileToDestination()
	{
		PluginConfigurationServiceDefault.logger.info("Coping distributed config file to TeamCity config directory.");

		try
		{
			PluginFileUtils.copyResource(
					this.getClass(), PluginConfigurationServiceDefault.DIST_CONFIG_XML_FILE_NAME, this.configFile
			);
		}
		catch(Exception e)
		{
			throw new FatalBeanException("Could not copy distributed configuration XML file", e);
		}
	}

	protected void initializeXmlDigesterLoader()
	{
		if(PluginConfigurationServiceDefault.logger.isDebugEnabled())
			PluginConfigurationServiceDefault.logger.debug("Initializing the XML digester.");

		Schema schema;
		try
		{
			schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(this.xsdFile);
		}
		catch(SAXException e)
		{
			throw new FatalBeanException("Could not parse plugin configuration XSD", e);
		}

		this.digesterLoader.setNamespaceAware(true);
		this.digesterLoader.setSchema(schema);
		this.digesterLoader.setErrorHandler(new ConfigurationErrorHandler());
		this.digesterLoader.setUseContextClassLoader(false);
		this.digesterLoader.setClassLoader(Digester.class.getClassLoader());

		ConvertUtils.register(new JodaXML8601DateTimeConverter(), DateTime.class);
	}

	private Digester newDigester() throws SAXException, ParserConfigurationException
	{
		Digester digester = this.digesterLoader.newDigester();
		digester.setFeature("http://xml.org/sax/features/validation", true);
		digester.setFeature("http://apache.org/xml/features/validation/schema", true);
		digester.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);
		return digester;
	}

	protected void loadConfiguration()
	{
		PluginConfigurationServiceDefault.logger.info("Loading the plugin configuration from the XML file.");

		try
		{
			this.configuration = this.newDigester().parse(this.configFile);
		}
		catch(IOException e)
		{
			throw new FatalBeanException("Could not read plugin configuration XML file", e);
		}
		catch(SAXException e)
		{
			throw new FatalBeanException("Could not parse plugin configuration XML", e);
		}
		catch(ParserConfigurationException e)
		{
			throw new FatalBeanException("Could not configure the configuration XML parser", e);
		}
	}

	protected void initializeFileWatcher()
	{
		if(PluginConfigurationServiceDefault.logger.isDebugEnabled())
			PluginConfigurationServiceDefault.logger.debug("Watching " + this.configFile.getPath() + " for changes.");

		this.configFileWatcher = new FileWatcher(this.configFile);
		this.configFileWatcher.setSleepingPeriod(10000);
		this.configFileWatcher.registerListener(this);
		this.configFileWatcher.start();
	}

	protected void saveConfiguration() throws IOException
	{
		PluginConfigurationServiceDefault.logger.info("Saving the plugin configuration to the XML file.");

		this.configFileWatcher.stop();

		this.configuration.setLastUpdate(new DateTime());

		List<String> lines = new ArrayList<String>(this.configFileHeader);
		lines.add("");

		lines.add(
				"\t<last-update>" + ISODateTimeFormat.dateTime().print(this.configuration.getLastUpdate()) +
				"</last-update>"
		);
		lines.add("");

		this.writeSettings(lines);

		this.writeBuildNumbers(lines);

		lines.add("</shared-build-number-config>");
		lines.add("");

		try
		{
			FileUtils.writeLines(this.configFile, lines);
		}
		finally
		{
			this.initializeFileWatcher();
		}
	}

	protected void writeSettings(List<String> lines)
	{
		SettingsEntity settings = this.configuration.getSettings();
		lines.add("\t<settings>");
		lines.add("\t\t<buildNumberIdSequence>" + settings.getBuildNumberIdSequence() + "</buildNumberIdSequence>");
		lines.add("\t</settings>");
		lines.add("");
	}

	protected void writeBuildNumbers(List<String> lines)
	{
		lines.add("\t<build-numbers>");
		int i = 0;

		TreeSet<SharedBuildNumberEntity> set = new TreeSet<SharedBuildNumberEntity>(
				new Comparator<SharedBuildNumberEntity>()
				{
					@Override
					public int compare(SharedBuildNumberEntity left, SharedBuildNumberEntity right)
					{
						int id1 = left.getId();
						int id2 = right.getId();

						return id1 < id2 ? -1 : (id2 < id1 ? 1 : 0);
					}
				}
		);
		set.addAll(this.configuration.getBuildNumbers());

		for(SharedBuildNumberEntity number : set)
		{
			if(i > 0)
				lines.add("");
			i++;

			lines.add(
					"\t\t<build-number id=\"" + number.getId() + "\" incrementOnceForChain=\"" +
					number.isIncrementOnceForChain() + "\">"
			);

			lines.add("\t\t\t<name><![CDATA[" + number.getName() + "]]></name>");
			lines.add("\t\t\t<description><![CDATA[" + number.getDescription() + "]]></description>");
			lines.add("\t\t\t<format><![CDATA[" + number.getFormat() + "]]></format>");
			if(number.getDateFormat() != null && number.getDateFormat().trim().length() > 0)
				lines.add("\t\t\t<dateFormat>" + number.getDateFormat() + "</dateFormat>");
			lines.add("\t\t\t<counter>" + number.getCounter() + "</counter>");

			lines.add("\t\t</build-number>");
		}
		if(i == 0)
			lines.add("");
		lines.add("\t</build-numbers>");
		lines.add("");
	}
}
