/*
 * PluginFileUtils.java from TeamCityPlugins modified Monday, September 10, 2012 18:39:21 CDT (-0500).
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

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * A helper class for various file operations.
 *
 * @author Nick Williams
 * @version 1.0.0
 * @since 1.0.0
 */
public class PluginFileUtils
{
	public static File getCanonicalFile(File file)
	{
		try
		{
			return file.getCanonicalFile();
		}
		catch(IOException e)
		{
			throw new RuntimeException(
					"Failed to get canonical file for absolute path [" + file.getAbsolutePath() + "].", e
			);
		}
	}

	public static void copyResource(Class<?> relativeClass, String resourceName, File destinationFile)
	{
		InputStream inputStream = null;
		OutputStream outputStream = null;
		try
		{
			inputStream = relativeClass.getResourceAsStream(resourceName);
			if(inputStream == null)
			{
				throw new RuntimeException(
						"No resource with name [" + resourceName + "] found relative to class [" +
						relativeClass.getName() + "]."
				);
			}

			outputStream = new FileOutputStream(destinationFile);

			IOUtils.copy(inputStream, outputStream);
		}
		catch(IOException e)
		{
			throw new RuntimeException(
					"Failed to copy resource [" + resourceName + "] relative to class [" + relativeClass.getName() +
					"] to destination file [" + destinationFile.getAbsolutePath() + "]."
			);
		}
		finally
		{
			try
			{
				if(outputStream != null)
					outputStream.close();
			}
			catch(IOException ignore) { }

			try
			{
				if(inputStream != null)
					inputStream.close();
			}
			catch(IOException ignore) { }
		}
	}

	@SuppressWarnings("unchecked")
	public static List<String> readLines(Class<?> relativeClass, String resourceName)
	{
		InputStream inputStream = null;
		try
		{
			inputStream = relativeClass.getResourceAsStream(resourceName);
			if(inputStream == null)
			{
				throw new RuntimeException(
						"No resource with name [" + resourceName + "] found relative to class [" +
						relativeClass.getName() + "]."
				);
			}

			return IOUtils.readLines(inputStream);
		}
		catch(IOException e)
		{
			throw new RuntimeException(
					"Failed to read lines from resource [" + resourceName + "] relative to class [" +
					relativeClass.getName() + "]."
			);
		}
		finally
		{
			try
			{
				if(inputStream != null)
					inputStream.close();
			}
			catch(IOException ignore) { }
		}
	}
}
