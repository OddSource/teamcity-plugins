/*
 * ConfigurationDigesterModule.java from TeamCityPlugins modified Saturday, September 8, 2012 17:28:13 CDT (-0500).
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
import org.apache.commons.digester3.binder.AbstractRulesModule;

/**
 * Defines the rules for turning the configuration file into the object structure.
 *
 * @author Nick Williams
 * @version 1.0.0
 * @since 1.0.0
 */
public class ConfigurationDigesterModule extends AbstractRulesModule
{
	@Override
	protected void configure()
	{
		forPattern("shared-build-number-config").createObject().ofType(ConfigurationEntity.class)
				.then().setProperties();

		forPattern("shared-build-number-config/last-update").setBeanProperty().withName("lastUpdate");

		forPattern("shared-build-number-config/settings").createObject().ofType(SettingsEntity.class)
				.then().setProperties()
				.then().setNext("setSettings");

		forPattern("shared-build-number-config/settings/buildNumberIdSequence").setBeanProperty();

		forPattern("shared-build-number-config/build-numbers/build-number").createObject()
				.ofType(SharedBuildNumberEntity.class)
				.then().setProperties()
				.then().setNext("addOrUpdateBuildNumber");

		forPattern("shared-build-number-config/build-numbers/build-number/name").setBeanProperty();
		forPattern("shared-build-number-config/build-numbers/build-number/description").setBeanProperty();
		forPattern("shared-build-number-config/build-numbers/build-number/format").setBeanProperty();
		forPattern("shared-build-number-config/build-numbers/build-number/dateFormat").setBeanProperty();
		forPattern("shared-build-number-config/build-numbers/build-number/counter").setBeanProperty();
	}
}
