/*
 * SharedBuildNumberController.java from TeamCityPlugins modified Monday, September 10, 2012 22:12:29 CDT (-0500).
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
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.controllers.admin.AdminPage;
import jetbrains.buildServer.serverSide.auth.AccessDeniedException;
import jetbrains.buildServer.serverSide.auth.AuthUtil;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.PositionConstraint;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildServer.web.util.SessionUser;
import jetbrains.buildServer.web.util.WebAuthUtil;
import jetbrains.buildServer.web.util.WebUtil;
import net.nicholaswilliams.java.teamcity.plugin.buildNumber.pojo.SharedBuildNumber;
import org.apache.commons.lang.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;
import java.util.SortedSet;

/**
 * Handles the UI for managing shared build numbers.
 *
 * @author Nick Williams
 * @version 1.0.0
 * @since 1.0.0
 */
public class SharedBuildNumberController extends BaseController
{
	private static final Logger logger =
			Logger.getInstance("jetbrains.buildServer.PLUGIN.sharedBuildNumber.CONTROLLER");

	private static final String FORM = "sharedBuildNumberForm";

	private static final String PREFIX = "sbnParameterPrefix";

	public static String TAB_ID = "sharedBuildNumbers";

	private PluginConfigurationService configurationService;

	private String listJspPagePath;

	private String addJspPagePath;

	private String editJspPagePath;

	public SharedBuildNumberController(@NotNull WebControllerManager controllerManager,
									   @NotNull PluginDescriptor pluginDescriptor,
									   @NotNull PagePlaces pagePlaces,
									   @NotNull PluginConfigurationService configurationService)
	{
		SharedBuildNumberController.logger.info(
				"Initializing shared build number controller; plugging in Administrative tab extension."
		);

		this.listJspPagePath = pluginDescriptor.getPluginResourcesPath("jsp/list.jsp");
		this.addJspPagePath = pluginDescriptor.getPluginResourcesPath("jsp/addBuildNumber.jsp");
		this.editJspPagePath = pluginDescriptor.getPluginResourcesPath("jsp/editBuildNumber.jsp");

		this.configurationService = configurationService;

		AdminTab tab = new AdminTab(pagePlaces, pluginDescriptor);
		tab.addCssFile(pluginDescriptor.getPluginResourcesPath("css/sharedBuildNumbers.css"));
		tab.addCssFile("/css/settingsBlock.css");
		tab.addJsFile(pluginDescriptor.getPluginResourcesPath("js/sharedBuildNumbers.js"));
		tab.setPluginName("sharedBuildNumbers");
		tab.setPosition(PositionConstraint.between(Arrays.asList("projects"), Arrays.asList("cleanup")));
		tab.register();

		controllerManager.registerController("/admin/adminSharedBuildNumbers.html", this);
	}

	@Override
	protected ModelAndView doHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response)
			throws ServletException, IOException
	{
		SUser user = SessionUser.getUser(request);

		if(this.hasAccess(user))
		{
			String action = request.getParameter("action");
			if(action == null || action.trim().length() == 0)
				action = "list";
			else
				action = action.trim();

			boolean isPost = this.isPost(request);

			if(action.equals("list"))
			{
				return this.listBuildNumbers(request);
			}
			else if(action.equals("add"))
			{
				if(isPost)
					return this.submitAddBuildNumber(request);
				return this.addBuildNumber();
			}
			else if(action.equals("edit"))
			{
				if(isPost)
					return this.submitEditBuildNumber(request, response);
				return this.editBuildNumber(request, response);
			}
			else if(action.equals("delete") && isPost)
			{
				return this.submitDeleteBuildNumber(request, response);
			}
			else
			{
				SharedBuildNumberController.logger.info(
						"Unsupported action [" + action + "] in shared build number controller."
				);
				WebUtil.notFound(request, response, "The page you referenced does not exist.",
								 SharedBuildNumberController.logger);
				return null;
			}
		}
		else
		{
			SharedBuildNumberController.logger.warn(
					"Access denied to user [" + user.getUsername() + "] for shared build number UI."
			);
			WebAuthUtil.addAccessDeniedMessage(
					request, new AccessDeniedException(
							user,
							"You must be an System Administrator or Global Project Administrator to manage shared " +
								"build numbers."
					)
			);
			return null;
		}
	}

	protected ModelAndView listBuildNumbers(HttpServletRequest request)
	{
		ModelAndView modelAndView = new ModelAndView(this.listJspPagePath);

		boolean descending = "desc".equalsIgnoreCase(request.getParameter("direction"));

		SortedSet<SharedBuildNumber> set;
		if("name".equalsIgnoreCase(request.getParameter("sort")))
		{
			set = this.configurationService.getAllSharedBuildNumbersSortedByName(descending);
			modelAndView.getModel().put("sortedBy", "name");
		}
		else
		{
			set = this.configurationService.getAllSharedBuildNumbersSortedById(descending);
			modelAndView.getModel().put("sortedBy", "id");
		}

		modelAndView.getModel().put(SharedBuildNumberController.PREFIX, BuildNumberPropertiesProvider.PARAMETER_PREFIX);
		modelAndView.getModel().put("numResults", set.size());
		modelAndView.getModel().put("buildNumbers", set);
		modelAndView.getModel().put("sortClass", descending ? "sortedDesc" : "sortedAsc");
		modelAndView.getModel().put("sortChange", descending ? "asc" : "desc");

		return modelAndView;
	}


	protected ModelAndView addBuildNumber()
	{
		ModelAndView modelAndView = new ModelAndView(this.addJspPagePath);

		SharedBuildNumber form = new SharedBuildNumber(0);
		form.setFormat("{0}");
		form.setDateFormat("yyyyMMddHHmmss");

		modelAndView.getModel().put(SharedBuildNumberController.FORM, form);

		return modelAndView;
	}

	protected ModelAndView submitAddBuildNumber(HttpServletRequest request)
			throws IOException
	{
		Map<String, String> errors = new Hashtable<String, String>(6);
		SharedBuildNumber form = this.getAndValidateFormFromRequest(request, 0, errors);

		if(!errors.isEmpty())
		{
			ModelAndView modelAndView = new ModelAndView(this.addJspPagePath);

			modelAndView.getModel().put("errors", errors);
			modelAndView.getModel().put(SharedBuildNumberController.FORM, form);

			return modelAndView;
		}

		SharedBuildNumber buildNumber = new SharedBuildNumber(this.configurationService.getNextBuildNumberId());
		this.copyFormToBuildNumber(form, buildNumber);
		this.configurationService.saveSharedBuildNumber(buildNumber);

		return this.returnRedirectView("/admin/admin.html?item=sharedBuildNumbers");
	}

	protected ModelAndView editBuildNumber(HttpServletRequest request, HttpServletResponse response)
			throws IOException
	{
		Integer id = this.getId(request);
		if(id != null)
		{
			SharedBuildNumber form = this.configurationService.getSharedBuildNumber(id);
			if(form != null)
			{
				ModelAndView modelAndView = new ModelAndView(this.editJspPagePath);

				modelAndView.getModel().put(SharedBuildNumberController.PREFIX, BuildNumberPropertiesProvider.PARAMETER_PREFIX);
				modelAndView.getModel().put(SharedBuildNumberController.FORM, form);

				return modelAndView;
			}
		}

		SharedBuildNumberController.logger.info("Edit requested for non-existent shared build number.");
		WebUtil.notFound(request, response, "The shared build number you are trying to edit does not exist.",
						 SharedBuildNumberController.logger);
		return null;
	}

	protected ModelAndView submitEditBuildNumber(HttpServletRequest request, HttpServletResponse response)
			throws IOException
	{
		Integer id = this.getId(request);
		if(id != null)
		{
			SharedBuildNumber buildNumber = this.configurationService.getSharedBuildNumber(id);
			if(buildNumber != null)
			{
				Map<String, String> errors = new Hashtable<String, String>(6);
				SharedBuildNumber form = this.getAndValidateFormFromRequest(request, id, errors);

				if(!errors.isEmpty())
				{
					ModelAndView modelAndView = new ModelAndView(this.addJspPagePath);

					modelAndView.getModel().put("errors", errors);
					modelAndView.getModel().put(SharedBuildNumberController.PREFIX, BuildNumberPropertiesProvider.PARAMETER_PREFIX);
					modelAndView.getModel().put(SharedBuildNumberController.FORM, form);

					return modelAndView;
				}

				this.copyFormToBuildNumber(form, buildNumber);
				this.configurationService.saveSharedBuildNumber(buildNumber);

				return this.returnRedirectView("/admin/admin.html?item=sharedBuildNumbers");
			}
		}

		SharedBuildNumberController.logger.info("Edit requested for non-existent shared build number.");
		WebUtil.notFound(request, response, "The shared build number you are trying to edit does not exist.",
						 SharedBuildNumberController.logger);
		return null;
	}

	protected ModelAndView submitDeleteBuildNumber(HttpServletRequest request, HttpServletResponse response)
			throws IOException
	{
		Integer id = this.getId(request);
		if(id != null)
		{
			this.configurationService.deleteSharedBuildNumber(id);

			return this.returnRedirectView("/admin/admin.html?item=sharedBuildNumbers");
		}

		SharedBuildNumberController.logger.info("Delete requested for non-existent shared build number.");
		WebUtil.notFound(request, response, "The shared build number you are trying to delete does not exist.",
						 SharedBuildNumberController.logger);
		return null;
	}

	private SharedBuildNumber getAndValidateFormFromRequest(HttpServletRequest request, int id, Map<String, String> errors)
	{
		SharedBuildNumber form = new SharedBuildNumber(id);

		form.setName(request.getParameter("name"));
		form.setDescription(request.getParameter("description"));
		form.setFormat(request.getParameter("format"));
		form.setDateFormat(request.getParameter("dateFormat"));

		String counterString = request.getParameter("counter");
		if(NumberUtils.isDigits(counterString) && !counterString.contains(".") && !counterString.contains("-"))
		{
			form.setCounter(Integer.parseInt(counterString));
			if(form.getCounter() < 1)
				form.setCounter(1);
		}
		else
		{
			errors.put("counter", "The counter must be a positive integer.");
		}

		if(form.getName() == null || form.getName().trim().length() < 5 || form.getName().trim().length() > 60)
			errors.put("name", "The name must be between 5 and 60 characters long.");

		if(form.getFormat() == null || form.getFormat().trim().length() < 3)
			errors.put("format", "The build number format must be at least 3 characters long.");

		if(form.getDateFormat() == null || form.getDateFormat().trim().length() < 3)
			errors.put("dateFormat", "The date format must be at least 3 characters long.");

		return form;
	}

	private void copyFormToBuildNumber(SharedBuildNumber form, SharedBuildNumber buildNumber)
	{
		buildNumber.setName(form.getName());
		buildNumber.setDescription(form.getDescription());
		buildNumber.setFormat(form.getFormat());
		buildNumber.setDateFormat(form.getDateFormat());
		buildNumber.setCounter(form.getCounter());
	}

	private Integer getId(HttpServletRequest request)
	{
		String idString = request.getParameter("id");
		if(NumberUtils.isDigits(idString) && !idString.contains(".") && !idString.contains("-"))
		{
			return Integer.parseInt(idString);
		}
		return null;
	}

	private ModelAndView returnRedirectView(String url)
	{
		return new ModelAndView(new RedirectView(url, true));
	}

	private boolean hasAccess(HttpServletRequest request)
	{
		SUser user = SessionUser.getUser(request);
		return this.hasAccess(user);
	}

	private boolean hasAccess(SUser user)
	{
		return AuthUtil.isSystemAdmin(user) || AuthUtil.hasPermissionToManageAllProjects(user);
	}

	private class AdminTab extends AdminPage
	{
		public AdminTab(@NotNull PagePlaces pagePlaces, @NotNull PluginDescriptor pluginDescriptor)
		{
			super(pagePlaces,
				  SharedBuildNumberController.TAB_ID,
				  pluginDescriptor.getPluginResourcesPath("jsp/adminTab.jsp"),
				  "Shared Build Numbers");
		}

		@Override
		public boolean isAvailable(@NotNull HttpServletRequest request)
		{
			return super.isAvailable(request) && SharedBuildNumberController.this.hasAccess(request);
		}

		@NotNull
		@Override
		public String getGroup()
		{
			return "Project-related Settings";
		}
	}
}
