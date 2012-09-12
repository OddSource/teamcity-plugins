/*
 * BuildNumberPropertiesProvider.java from TeamCityPlugins modified Friday, September 7, 2012 22:38:36 CDT (-0500).
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
import jetbrains.buildServer.ExtensionHolder;
import jetbrains.buildServer.agent.ServerProvidedProperties;
import jetbrains.buildServer.parameters.ProcessingResult;
import jetbrains.buildServer.parameters.ReferencesResolverUtil;
import jetbrains.buildServer.parameters.impl.CompositeParametersProviderImpl;
import jetbrains.buildServer.parameters.impl.MapParametersProviderImpl;
import jetbrains.buildServer.parameters.impl.ParametersResolverUtil;
import jetbrains.buildServer.parameters.impl.ReferenceResolver;
import jetbrains.buildServer.serverSide.BuildStartContext;
import jetbrains.buildServer.serverSide.BuildStartContextProcessor;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SRunnerContext;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.parameters.AbstractBuildParametersProvider;
import jetbrains.buildServer.serverSide.parameters.BuildParametersProvider;
import jetbrains.buildServer.serverSide.parameters.ParameterDescriptionProvider;
import org.apache.commons.lang.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Performs the actual updating of build properties. This class is heavily derived from and a modified version of
 * jetbrains.buildServer.server.autoincrementer.AutoincrementPropertiesProvider from the
 * <a href="http://confluence.jetbrains.net/display/TW/Autoincrementer" target="_blank">autoincrementer plugin</a>,
 * copyright 2000-2009 JetBrains s.r.o. Used with permission under the terms of the Apache License 2.0, which covers
 * all use and modification of the original source code with attribution. See NOTICE.txt for more information.
 *
 * @author Nick Williams
 * @version 1.0.0
 * @since 1.0.0
 */
public class BuildNumberPropertiesProvider
		extends AbstractBuildParametersProvider
		implements BuildStartContextProcessor, ParameterDescriptionProvider
{
	private static final Logger logger = Logger.getInstance("jetbrains.buildServer.PLUGIN.sharedBuildNumber.PROVIDER");

	public static final String PARAMETER_PREFIX = "sharedBuildNumber.id";

	private PluginConfigurationService configurationService;

	public BuildNumberPropertiesProvider(@NotNull ExtensionHolder extensionHolder,
										 @NotNull PluginConfigurationService configurationService)
	{
		this.configurationService = configurationService;

		extensionHolder.registerExtension(
				BuildParametersProvider.class, BuildNumberPropertiesProvider.class.getName(), this
		);
		extensionHolder.registerExtension(
				ParameterDescriptionProvider.class, BuildNumberPropertiesProvider.class.getName(), this
		);
		extensionHolder.registerExtension(
				BuildStartContextProcessor.class, BuildNumberPropertiesProvider.class.getName(), this
		);
	}

	@NotNull
	@Override
	public Collection<String> getParametersAvailableOnAgent(@NotNull final SBuild build)
	{
		TreeSet<String> parameters = new TreeSet<String>();
		for(int id : this.configurationService.getAllSharedBuildNumberIds())
			parameters.add(BuildNumberPropertiesProvider.PARAMETER_PREFIX + id);
		return parameters;
	}

	@Override
	public String describe(@NotNull String parameterName)
	{
		if(!parameterName.startsWith(BuildNumberPropertiesProvider.PARAMETER_PREFIX))
			return null;

		Integer id = this.extractBuildIdFromParameter(parameterName);

		return id == null ? null : this.configurationService.getSharedBuildNumberName(id);
	}

	@Override
	public boolean isVisible(@NotNull String parameterName)
	{
		return parameterName.startsWith(BuildNumberPropertiesProvider.PARAMETER_PREFIX);
	}

	@Override
	public void updateParameters(@NotNull BuildStartContext buildStartContext)
	{
		SRunningBuild runningBuild = buildStartContext.getBuild();

		BuildNumberPropertiesProvider.logger.info(
				"Processing build [" + runningBuild.getFullName() + "] for shared build number parameters."
		);

		if(runningBuild.getBuildType() == null)
		{
			if(BuildNumberPropertiesProvider.logger.isDebugEnabled())
			{
				BuildNumberPropertiesProvider.logger.debug(
						"No build type exists for build; shared build number does not apply."
				);
			}
			return;
		}

		Set<String> parameters = new HashSet<String>();
		parameters.addAll(runningBuild.getParametersProvider().getAll().values());

		for(SRunnerContext runnerContext : buildStartContext.getRunnerContexts())
		{
			parameters.addAll(runnerContext.getParameters().values());
		}

		Set<String> pluginParameters = this.extractSharedBuildNumberParameters(parameters);
		if(pluginParameters.isEmpty())
		{
			if(BuildNumberPropertiesProvider.logger.isDebugEnabled())
			{
				BuildNumberPropertiesProvider.logger.debug(
						"No shared build number parameters found in " + parameters + "."
				);
			}
			return;
		}

		this.updateSharedParameters(buildStartContext, pluginParameters);

		this.updateBuildNumber(buildStartContext, runningBuild);
	}

	@NotNull
	private Set<String> extractSharedBuildNumberParameters(@NotNull final Collection<String> parameters)
	{
		Set<String> pluginParameters = new HashSet<String>();

		for(String parameter : parameters)
		{
			Collection<String> references = ReferencesResolverUtil.getReferences(parameter);
			for(String reference : references)
			{
				if(reference.startsWith(BuildNumberPropertiesProvider.PARAMETER_PREFIX))
					pluginParameters.add(reference);
			}
		}

		return pluginParameters;
	}

	@Nullable
	private Integer extractBuildIdFromParameter(String parameter)
	{
		String idString = parameter.replace(BuildNumberPropertiesProvider.PARAMETER_PREFIX, "");

		return NumberUtils.isDigits(idString) ? Integer.parseInt(idString) : null;
	}

	private void updateSharedParameters(@NotNull BuildStartContext buildStartContext,
										  @NotNull Set<String> pluginParameters)
	{
		for(String parameter : pluginParameters)
		{
			Integer id = this.extractBuildIdFromParameter(parameter);
			if(id != null)
			{
				try
				{
					String buildNumber = this.configurationService.getAndIncrementFormattedSharedBuildNumber(id);

					if(buildNumber == null)
					{
						BuildNumberPropertiesProvider.logger.error("No shared build number found for ID [" + id + "].");
						continue;
					}

					if(BuildNumberPropertiesProvider.logger.isDebugEnabled())
					{
						BuildNumberPropertiesProvider.logger.debug(
								"Next shared build number for ID [" + id + "] is [" + buildNumber + "]."
						);
					}

					buildStartContext.addSharedParameter(parameter, buildNumber);
				}
				catch(IOException e)
				{
					BuildNumberPropertiesProvider.logger.error(
							"Could not increment build number for ID [" + id + "].", e
					);
				}
			}
			else
			{
				BuildNumberPropertiesProvider.logger.error(
						"Shared build number parameter [" + parameter +
						"] was not formatted correctly; ID could not be extracted."
				);
			}
		}
	}

	private void updateBuildNumber(@NotNull BuildStartContext buildStartContext, @NotNull SRunningBuild runningBuild)
	{
		CompositeParametersProviderImpl provider = new CompositeParametersProviderImpl();
		for(SRunnerContext runnerContext: buildStartContext.getRunnerContexts())
		{
			provider.appendParametersProvider(new MapParametersProviderImpl(runnerContext.getParameters()));
		}
		provider.appendParametersProvider(new MapParametersProviderImpl(buildStartContext.getSharedParameters()));

		ReferenceResolver resolver = new ReferenceResolver();
		String originalBuildNumber = runningBuild.getRawBuildNumber();
		ProcessingResult result = ParametersResolverUtil.resolveSingleValue(originalBuildNumber, provider, resolver);

		if(result.isModified())
		{
			String newBuildNumber = result.getResult();
			runningBuild.setBuildNumber(newBuildNumber);

			buildStartContext.addSharedParameter(ServerProvidedProperties.SYSTEM_BUILD_NUMBER, newBuildNumber);
			buildStartContext.addSharedParameter(ServerProvidedProperties.ENV_BUILD_NUMBER, newBuildNumber);

			if(BuildNumberPropertiesProvider.logger.isDebugEnabled())
			{
				BuildNumberPropertiesProvider.logger.debug(
						"Updated build number of build [" + runningBuild.getFullName() + "] to [" + newBuildNumber +
						"]."
				);
			}
		}
		else
		{
			BuildNumberPropertiesProvider.logger.warn(
					"Failed to update build number of build [" + runningBuild.getFullName() + "]; original value [" +
					originalBuildNumber + "] was not modified on parameter resolution."
			);
		}
	}
}
