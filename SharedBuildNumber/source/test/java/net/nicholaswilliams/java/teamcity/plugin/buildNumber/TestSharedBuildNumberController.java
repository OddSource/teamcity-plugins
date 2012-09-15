/*
 * TestSharedBuildNumberController.java from TeamCityPlugins modified Saturday, September 15, 2012 11:38:31 CDT (-0500).
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

import jetbrains.buildServer.controllers.admin.AdminPage;
import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.web.openapi.PageExtension;
import jetbrains.buildServer.web.openapi.PagePlace;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PlaceId;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.PositionConstraint;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import net.nicholaswilliams.java.teamcity.plugin.buildNumber.pojo.SharedBuildNumber;
import org.apache.commons.lang.math.NumberUtils;
import org.easymock.Capture;
import org.easymock.CaptureType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Test class for SharedBuildNumberController.
 */
public class TestSharedBuildNumberController
{
	private static int testNum = 0;

	private WebControllerManager manager;

	private PluginDescriptor descriptor;

	private PagePlaces places;

	private HttpServletRequest request;

	private HttpServletResponse response;

	private SUser user;

	private PluginConfigurationService service;

	private AdminPage extension;

	private SharedBuildNumberController controller;

	@Before
	public void setUp()
	{
		TestSharedBuildNumberController.testNum++;

		this.manager = createStrictMock(WebControllerManager.class);
		this.descriptor = createStrictMock(PluginDescriptor.class);
		this.places = createStrictMock(PagePlaces.class);
		this.request = createStrictMock(HttpServletRequest.class);
		this.response = createMock(HttpServletResponse.class);
		this.user = createStrictMock(SUser.class);
		this.service = createStrictMock(PluginConfigurationService.class);

		PagePlace pagePlace = createStrictMock(PagePlace.class);

		Capture<Controller> controllerCapture = new Capture<Controller>();
		Capture<PageExtension> pageExtensionCapture = new Capture<PageExtension>(CaptureType.ALL);
		Capture<PositionConstraint> positionConstraintCapture = new Capture<PositionConstraint>();

		this.manager.registerController(eq("/admin/adminSharedBuildNumbers.html"), capture(controllerCapture));
		expectLastCall();

		expect(this.descriptor.getPluginResourcesPath("jsp/list.jsp"))
				.andReturn("/plugin/" + testNum + "/jsp/list.jsp");
		expect(this.descriptor.getPluginResourcesPath("jsp/addBuildNumber.jsp"))
				.andReturn("/plugin/" + testNum + "/jsp/addBuildNumber.jsp");
		expect(this.descriptor.getPluginResourcesPath("jsp/editBuildNumber.jsp"))
				.andReturn("/plugin/" + testNum + "/jsp/editBuildNumber.jsp");
		expect(this.descriptor.getPluginResourcesPath("jsp/adminTab.jsp"))
				.andReturn("/plugin/" + testNum + "/jsp/adminTab.jsp");
		expect(this.descriptor.getPluginResourcesPath("css/sharedBuildNumbers.css"))
				.andReturn("/plugin/" + testNum + "/css/sharedBuildNumbers.css");
		expect(this.descriptor.getPluginResourcesPath("js/sharedBuildNumbers.js"))
				.andReturn("/plugin/" + testNum + "/js/sharedBuildNumbers.js");

		expect(this.places.getPlaceById(PlaceId.ADMIN_SERVER_CONFIGURATION_TAB)).andReturn(pagePlace).times(3);

		expect(pagePlace.removeExtension(capture(pageExtensionCapture))).andReturn(true);
		pagePlace.addExtension(capture(pageExtensionCapture), capture(positionConstraintCapture));
		expectLastCall().times(2);

		replay(this.manager, this.descriptor, this.places, pagePlace);

		this.controller = new SharedBuildNumberController(this.manager, this.descriptor, this.places, this.service);

		assertSame("The registered controller is not correct.", this.controller, controllerCapture.getValue());

		List<PageExtension> pageExtensions = pageExtensionCapture.getValues();
		assertEquals("The list is the wrong size.", 3, pageExtensions.size());
		assertSame("The list content is incorrect (1).", pageExtensions.get(0), pageExtensions.get(1));
		assertSame("The list content is incorrect (1).", pageExtensions.get(0), pageExtensions.get(2));
		PageExtension pageExtension = pageExtensions.get(0);
		assertTrue("The page extension is not correct.", pageExtension instanceof AdminPage);
		this.extension = (AdminPage)pageExtension;

		verify(pagePlace);
	}

	@After
	public void tearDown()
	{
		verify(this.manager, this.descriptor, this.places, this.request, this.response, this.user, this.service);
	}

	@Test
	public void testPageExtensionProperties()
	{
		replay(this.service, this.request, this.response, this.user);

		assertEquals("The settings group is not correct.", "Project-related Settings", this.extension.getGroup());
		assertEquals("The tab ID is not correct.", SharedBuildNumberController.TAB_ID, this.extension.getTabId());
		assertEquals("The includeUrl is not correct.", "/plugin/" + testNum + "/jsp/adminTab.jsp",
					 this.extension.getIncludeUrl());
		assertEquals("The title is not correct.", "Shared Build Numbers", this.extension.getTabTitle());
	}

	@Test
	public void testPageExtensionIsAvailable01()
	{
		this.request = createStrictMock(HttpServletRequest.class);
		this.user = createStrictMock(SUser.class);

		expect(this.request.getParameter("userKey")).andReturn("aUserKey");
		expect(this.request.getAttribute("aUserKey")).andReturn(this.user);
		expect(this.user.isSystemAdministratorRoleGranted()).andReturn(false);
		expect(this.user.isPermissionGrantedGlobally(Permission.EDIT_PROJECT)).andReturn(false);

		replay(this.service, this.request, this.response, this.user);

		assertFalse("The return value should be false.", this.extension.isAvailable(request));
	}

	@Test
	public void testPageExtensionIsAvailable02()
	{
		this.request = createStrictMock(HttpServletRequest.class);
		this.user = createStrictMock(SUser.class);

		expect(this.request.getParameter("userKey")).andReturn("aUserKey");
		expect(this.request.getAttribute("aUserKey")).andReturn(this.user);
		expect(this.user.isSystemAdministratorRoleGranted()).andReturn(false);
		expect(this.user.isPermissionGrantedGlobally(Permission.EDIT_PROJECT)).andReturn(true);

		replay(this.service, this.request, this.response, this.user);

		assertTrue("The return value should be true.", this.extension.isAvailable(this.request));
	}

	@Test
	public void testPageExtensionIsAvailable03()
	{
		this.request = createStrictMock(HttpServletRequest.class);
		this.user = createStrictMock(SUser.class);

		expect(this.request.getParameter("userKey")).andReturn("aUserKey");
		expect(this.request.getAttribute("aUserKey")).andReturn(this.user);
		expect(this.user.isSystemAdministratorRoleGranted()).andReturn(true);

		replay(this.service, this.request, this.response, this.user);

		assertTrue("The return value should be true.", this.extension.isAvailable(this.request));
	}

	private void setUpSecurity()
	{
		this.request = createStrictMock(HttpServletRequest.class);
		this.user = createStrictMock(SUser.class);

		expect(this.request.getParameter("userKey")).andReturn("aUserKey");
		expect(this.request.getAttribute("aUserKey")).andReturn(this.user);
		expect(this.user.isSystemAdministratorRoleGranted()).andReturn(true);
		expect(this.user.getUsername()).andReturn("nicholas.williams").anyTimes();

		replay(this.user);
	}

	@Test
	public void testDoHandleBadPage01() throws IOException, ServletException
	{
		this.setUpSecurity();

		expect(this.request.getParameter("action")).andReturn("badPage").atLeastOnce();
		expect(this.request.getMethod()).andReturn("GET").atLeastOnce();
		expect(this.request.getRequestURI()).andReturn("/admin/adminSharedBuildNumbers.html");
		expect(this.request.getParameterNames()).andReturn(new StringTokenizer("action", ",")).atLeastOnce();
		expect(this.request.getParameterValues("action")).andReturn(new String[] {"badPage"});
		expect(this.request.getRemoteAddr()).andReturn("4.2.2.2");
		expect(this.request.getRemotePort()).andReturn(1838);
		expect(this.request.getParameter("userKey")).andReturn("aUserKey");
		expect(this.request.getAttribute("aUserKey")).andReturn(this.user);
		expect(this.request.getAttribute("pageUrl")).andReturn(null);
		this.response.sendError(404, "The page you referenced does not exist.");
		expectLastCall();

		replay(this.service, this.request, this.response);

		ModelAndView modelAndView = this.controller.doHandle(this.request, this.response);

		assertNull("The model and view should not be null.", modelAndView);
	}

	@Test
	public void testDoHandleBadPage02() throws IOException, ServletException
	{
		this.setUpSecurity();

		expect(this.request.getParameter("action")).andReturn("badPage").atLeastOnce();
		expect(this.request.getMethod()).andReturn("POST").atLeastOnce();
		expect(this.request.getRequestURI()).andReturn("/admin/adminSharedBuildNumbers.html");
		expect(this.request.getParameterNames()).andReturn(new StringTokenizer("action", ",")).atLeastOnce();
		expect(this.request.getParameterValues("action")).andReturn(new String[] {"badPage"});
		expect(this.request.getRemoteAddr()).andReturn("4.2.2.2");
		expect(this.request.getRemotePort()).andReturn(1838);
		expect(this.request.getParameter("userKey")).andReturn("aUserKey");
		expect(this.request.getAttribute("aUserKey")).andReturn(this.user);
		expect(this.request.getAttribute("pageUrl")).andReturn(null);
		this.response.sendError(404, "The page you referenced does not exist.");
		expectLastCall();

		replay(this.service, this.request, this.response);

		ModelAndView modelAndView = this.controller.doHandle(this.request, this.response);

		assertNull("The model and view should not be null.", modelAndView);
	}

	@Test
	public void testDoHandleBadPage03() throws IOException, ServletException
	{
		this.setUpSecurity();

		expect(this.request.getParameter("action")).andReturn("delete").atLeastOnce();
		expect(this.request.getMethod()).andReturn("GET").atLeastOnce();
		expect(this.request.getRequestURI()).andReturn("/admin/adminSharedBuildNumbers.html");
		expect(this.request.getParameterNames()).andReturn(new StringTokenizer("action", ",")).atLeastOnce();
		expect(this.request.getParameterValues("action")).andReturn(new String[] {"badPage"});
		expect(this.request.getRemoteAddr()).andReturn("4.2.2.2");
		expect(this.request.getRemotePort()).andReturn(1838);
		expect(this.request.getParameter("userKey")).andReturn("aUserKey");
		expect(this.request.getAttribute("aUserKey")).andReturn(this.user);
		expect(this.request.getAttribute("pageUrl")).andReturn(null);
		this.response.sendError(404, "The page you referenced does not exist.");
		expectLastCall();

		replay(this.service, this.request, this.response);

		ModelAndView modelAndView = this.controller.doHandle(this.request, this.response);

		assertNull("The model and view should not be null.", modelAndView);
	}

	@Test
	public void testDoHandleListPage01() throws IOException, ServletException
	{
		this.setUpSecurity();

		SortedSet<SharedBuildNumber> set = new TreeSet<SharedBuildNumber>();

		expect(this.request.getParameter("action")).andReturn(null);
		expect(this.request.getMethod()).andReturn("GET");
		expect(this.request.getParameter("direction")).andReturn(null);
		expect(this.request.getParameter("sort")).andReturn(null);
		expect(this.service.getAllSharedBuildNumbersSortedById(false)).andReturn(set);

		replay(this.service, this.request, this.response);

		ModelAndView modelAndView = this.controller.doHandle(this.request, this.response);

		assertNotNull("The model and view should not be null.", modelAndView);
		assertEquals("The view is not correct.", "/plugin/" + testNum + "/jsp/list.jsp",
					 modelAndView.getViewName());

		Map<String, Object> model = modelAndView.getModel();

		assertNotNull("The model should not be null.", model);
		assertEquals("sortedBy is not correct.", "id", model.get("sortedBy"));
		assertEquals("sortClass is not correct.", "sortedAsc", model.get("sortClass"));
		assertEquals("sortChange is not correct.", "desc", model.get("sortChange"));
		assertEquals("sbnParameterPrefix is not correct.", BuildNumberPropertiesProvider.PARAMETER_PREFIX,
					 model.get("sbnParameterPrefix"));
		assertEquals("numResults is not correct.", 0, model.get("numResults"));
		assertSame("buildNumbers is not correct.", set, model.get("buildNumbers"));
	}

	@Test
	public void testDoHandleListPage02() throws IOException, ServletException
	{
		this.setUpSecurity();

		SortedSet<SharedBuildNumber> set = new TreeSet<SharedBuildNumber>();

		expect(this.request.getParameter("action")).andReturn("");
		expect(this.request.getMethod()).andReturn("GET");
		expect(this.request.getParameter("direction")).andReturn(null);
		expect(this.request.getParameter("sort")).andReturn(null);
		expect(this.service.getAllSharedBuildNumbersSortedById(false)).andReturn(set);

		replay(this.service, this.request, this.response);

		ModelAndView modelAndView = this.controller.doHandle(this.request, this.response);

		assertNotNull("The model and view should not be null.", modelAndView);
		assertEquals("The view is not correct.", "/plugin/" + testNum + "/jsp/list.jsp",
					 modelAndView.getViewName());

		Map<String, Object> model = modelAndView.getModel();

		assertNotNull("The model should not be null.", model);
		assertEquals("sortedBy is not correct.", "id", model.get("sortedBy"));
		assertEquals("sortClass is not correct.", "sortedAsc", model.get("sortClass"));
		assertEquals("sortChange is not correct.", "desc", model.get("sortChange"));
		assertEquals("sbnParameterPrefix is not correct.", BuildNumberPropertiesProvider.PARAMETER_PREFIX,
					 model.get("sbnParameterPrefix"));
		assertEquals("numResults is not correct.", 0, model.get("numResults"));
		assertSame("buildNumbers is not correct.", set, model.get("buildNumbers"));
	}

	@Test
	public void testDoHandleListPage03() throws IOException, ServletException
	{
		this.setUpSecurity();

		SortedSet<SharedBuildNumber> set = new TreeSet<SharedBuildNumber>();

		expect(this.request.getParameter("action")).andReturn("list");
		expect(this.request.getMethod()).andReturn("POST");
		expect(this.request.getParameter("direction")).andReturn("asc");
		expect(this.request.getParameter("sort")).andReturn("id");
		expect(this.service.getAllSharedBuildNumbersSortedById(false)).andReturn(set);

		replay(this.service, this.request, this.response);

		ModelAndView modelAndView = this.controller.doHandle(this.request, this.response);

		assertNotNull("The model and view should not be null.", modelAndView);
		assertEquals("The view is not correct.", "/plugin/" + testNum + "/jsp/list.jsp",
					 modelAndView.getViewName());

		Map<String, Object> model = modelAndView.getModel();

		assertNotNull("The model should not be null.", model);
		assertEquals("sortedBy is not correct.", "id", model.get("sortedBy"));
		assertEquals("sortClass is not correct.", "sortedAsc", model.get("sortClass"));
		assertEquals("sortChange is not correct.", "desc", model.get("sortChange"));
		assertEquals("sbnParameterPrefix is not correct.", BuildNumberPropertiesProvider.PARAMETER_PREFIX,
					 model.get("sbnParameterPrefix"));
		assertEquals("numResults is not correct.", 0, model.get("numResults"));
		assertSame("buildNumbers is not correct.", set, model.get("buildNumbers"));
	}

	@Test
	public void testDoHandleListPage04() throws IOException, ServletException
	{
		this.setUpSecurity();

		SortedSet<SharedBuildNumber> set = new TreeSet<SharedBuildNumber>();
		set.add(new SharedBuildNumber(12));

		expect(this.request.getParameter("action")).andReturn("list");
		expect(this.request.getMethod()).andReturn("POST");
		expect(this.request.getParameter("direction")).andReturn("desc");
		expect(this.request.getParameter("sort")).andReturn("id");
		expect(this.service.getAllSharedBuildNumbersSortedById(true)).andReturn(set);

		replay(this.service, this.request, this.response);

		ModelAndView modelAndView = this.controller.doHandle(this.request, this.response);

		assertNotNull("The model and view should not be null.", modelAndView);
		assertEquals("The view is not correct.", "/plugin/" + testNum + "/jsp/list.jsp",
					 modelAndView.getViewName());

		Map<String, Object> model = modelAndView.getModel();

		assertNotNull("The model should not be null.", model);
		assertEquals("sortedBy is not correct.", "id", model.get("sortedBy"));
		assertEquals("sortClass is not correct.", "sortedDesc", model.get("sortClass"));
		assertEquals("sortChange is not correct.", "asc", model.get("sortChange"));
		assertEquals("sbnParameterPrefix is not correct.", BuildNumberPropertiesProvider.PARAMETER_PREFIX,
					 model.get("sbnParameterPrefix"));
		assertEquals("numResults is not correct.", 1, model.get("numResults"));
		assertSame("buildNumbers is not correct.", set, model.get("buildNumbers"));
	}

	@Test
	public void testDoHandleListPage05() throws IOException, ServletException
	{
		this.setUpSecurity();

		SharedBuildNumber bn1 = new SharedBuildNumber(12);
		bn1.setName("hello");

		SharedBuildNumber bn2 = new SharedBuildNumber(22);
		bn2.setName("world");

		SortedSet<SharedBuildNumber> set = new TreeSet<SharedBuildNumber>(new Comparator<SharedBuildNumber>()
		{
			@Override
			public int compare(SharedBuildNumber left, SharedBuildNumber right)
			{
				return left.getName().compareTo(right.getName());
			}
		});
		set.add(bn1);
		set.add(bn2);

		expect(this.request.getParameter("action")).andReturn("list");
		expect(this.request.getMethod()).andReturn("POST");
		expect(this.request.getParameter("direction")).andReturn("asc");
		expect(this.request.getParameter("sort")).andReturn("name");
		expect(this.service.getAllSharedBuildNumbersSortedByName(false)).andReturn(set);

		replay(this.service, this.request, this.response);

		ModelAndView modelAndView = this.controller.doHandle(this.request, this.response);

		assertNotNull("The model and view should not be null.", modelAndView);
		assertEquals("The view is not correct.", "/plugin/" + testNum + "/jsp/list.jsp",
					 modelAndView.getViewName());

		Map<String, Object> model = modelAndView.getModel();

		assertNotNull("The model should not be null.", model);
		assertEquals("sortedBy is not correct.", "name", model.get("sortedBy"));
		assertEquals("sortClass is not correct.", "sortedAsc", model.get("sortClass"));
		assertEquals("sortChange is not correct.", "desc", model.get("sortChange"));
		assertEquals("sbnParameterPrefix is not correct.", BuildNumberPropertiesProvider.PARAMETER_PREFIX,
					 model.get("sbnParameterPrefix"));
		assertEquals("numResults is not correct.", 2, model.get("numResults"));
		assertSame("buildNumbers is not correct.", set, model.get("buildNumbers"));
	}

	@Test
	public void testDoHandleListPage06() throws IOException, ServletException
	{
		this.setUpSecurity();

		SortedSet<SharedBuildNumber> set = new TreeSet<SharedBuildNumber>(new Comparator<SharedBuildNumber>()
		{
			@Override
			public int compare(SharedBuildNumber left, SharedBuildNumber right)
			{
				return NumberUtils.compare(left.getId(), right.getCounter());
			}
		});
		set.add(new SharedBuildNumber(12));
		set.add(new SharedBuildNumber(22));
		set.add(new SharedBuildNumber(15));

		expect(this.request.getParameter("action")).andReturn("list");
		expect(this.request.getMethod()).andReturn("POST");
		expect(this.request.getParameter("direction")).andReturn("desc");
		expect(this.request.getParameter("sort")).andReturn("name");
		expect(this.service.getAllSharedBuildNumbersSortedByName(true)).andReturn(set);

		replay(this.service, this.request, this.response);

		ModelAndView modelAndView = this.controller.doHandle(this.request, this.response);

		assertNotNull("The model and view should not be null.", modelAndView);
		assertEquals("The view is not correct.", "/plugin/" + testNum + "/jsp/list.jsp",
					 modelAndView.getViewName());

		Map<String, Object> model = modelAndView.getModel();

		assertNotNull("The model should not be null.", model);
		assertEquals("sortedBy is not correct.", "name", model.get("sortedBy"));
		assertEquals("sortClass is not correct.", "sortedDesc", model.get("sortClass"));
		assertEquals("sortChange is not correct.", "asc", model.get("sortChange"));
		assertEquals("sbnParameterPrefix is not correct.", BuildNumberPropertiesProvider.PARAMETER_PREFIX,
					 model.get("sbnParameterPrefix"));
		assertEquals("numResults is not correct.", 3, model.get("numResults"));
		assertSame("buildNumbers is not correct.", set, model.get("buildNumbers"));
	}

	@Test
	public void testDoHandleAddBuildNumberGet01() throws IOException, ServletException
	{
		this.setUpSecurity();

		expect(this.request.getParameter("action")).andReturn("add");
		expect(this.request.getMethod()).andReturn("GET");

		replay(this.service, this.request, this.response);

		ModelAndView modelAndView = this.controller.doHandle(this.request, this.response);

		assertNotNull("The model and view should not be null.", modelAndView);
		assertEquals("The view is not correct.", "/plugin/" + testNum + "/jsp/addBuildNumber.jsp",
					 modelAndView.getViewName());

		Map<String, Object> model = modelAndView.getModel();
		assertNotNull("The model should not be null.", model);

		Object object = model.get("sharedBuildNumberForm");
		assertNotNull("sharedBuildNumberForm should not be null.", object);
		assertTrue("sharedBuildNumberForm should be a SharedBuildNumber.", object instanceof SharedBuildNumber);

		SharedBuildNumber form = (SharedBuildNumber)object;
		assertEquals("The format is not correct.", "{0}", form.getFormat());
		assertEquals("The date format is not correct.", "yyyyMMddHHmmss", form.getDateFormat());
		assertEquals("The counter is not correct.", 1, form.getCounter());
	}

	@Test
	public void testDoHandleAddBuildNumberPost01() throws IOException, ServletException
	{
		this.setUpSecurity();

		expect(this.request.getParameter("action")).andReturn("add");
		expect(this.request.getMethod()).andReturn("POST");
		expect(this.request.getParameter("name")).andReturn("help");
		expect(this.request.getParameter("description")).andReturn("");
		expect(this.request.getParameter("format")).andReturn("{0");
		expect(this.request.getParameter("dateFormat")).andReturn("Ym");
		expect(this.request.getParameter("counter")).andReturn("15.1");

		replay(this.service, this.request, this.response);

		ModelAndView modelAndView = this.controller.doHandle(this.request, this.response);

		assertNotNull("The model and view should not be null.", modelAndView);
		assertEquals("The view is not correct.", "/plugin/" + testNum + "/jsp/addBuildNumber.jsp",
					 modelAndView.getViewName());

		Map<String, Object> model = modelAndView.getModel();
		assertNotNull("The model should not be null.", model);

		Object object = model.get(BindingResult.MODEL_KEY_PREFIX + "sharedBuildNumberForm");
		assertNotNull("The binding result attribute should not be null.", object);
		assertTrue("The binding result attribute should be a binding result object.", object instanceof BindingResult);

		BindingResult result = (BindingResult)object;
		assertEquals("The binding result object name is not correct.", "sharedBuildNumberForm", result.getObjectName());
		assertTrue("The binding result should have errors.", result.hasErrors());
		assertEquals("The binding result should have 3 errors.", 3, result.getErrorCount());

		List<FieldError> errors = result.getFieldErrors();
		assertNotNull("The list of errors should not be null.", errors);
		assertEquals("The list length is not correct.", 3, errors.size());
		assertEquals("The first error is not correct.", "counter", errors.get(0).getField());
		assertEquals("The first error has the wrong message.", "The counter must be a positive integer.",
					 errors.get(0).getDefaultMessage());
		assertEquals("The second error is not correct.", "name", errors.get(1).getField());
		assertEquals("The second error has the wrong message.", "The name must be between 5 and 60 characters long.",
					 errors.get(1).getDefaultMessage());
		assertEquals("The third error is not correct.", "format", errors.get(2).getField());
		assertEquals("The third error has the wrong message.",
					 "The build number format must be at least 3 characters long.",
					 errors.get(2).getDefaultMessage());

		object = model.get("sharedBuildNumberForm");
		assertNotNull("sharedBuildNumberForm should not be null.", object);
		assertTrue("sharedBuildNumberForm should be a SharedBuildNumber.", object instanceof SharedBuildNumber);

		SharedBuildNumber form = (SharedBuildNumber)object;
		assertEquals("The name is not correct.", "help", form.getName());
		assertEquals("The description is not correct.", "", form.getDescription());
		assertEquals("The format is not correct.", "{0", form.getFormat());
		assertEquals("The date format is not correct.", "Ym", form.getDateFormat());
		assertEquals("The counter is not correct.", 1, form.getCounter());
	}

	@Test
	public void testDoHandleAddBuildNumberPost02() throws IOException, ServletException
	{
		this.setUpSecurity();

		expect(this.request.getParameter("action")).andReturn("add");
		expect(this.request.getMethod()).andReturn("POST");
		expect(this.request.getParameter("name")).andReturn("Hello");
		expect(this.request.getParameter("description")).andReturn("This is a description.");
		expect(this.request.getParameter("format")).andReturn("1.0.0.{D}");
		expect(this.request.getParameter("dateFormat")).andReturn("Ym");
		expect(this.request.getParameter("counter")).andReturn("-16");

		replay(this.service, this.request, this.response);

		ModelAndView modelAndView = this.controller.doHandle(this.request, this.response);

		assertNotNull("The model and view should not be null.", modelAndView);
		assertEquals("The view is not correct.", "/plugin/" + testNum + "/jsp/addBuildNumber.jsp",
					 modelAndView.getViewName());

		Map<String, Object> model = modelAndView.getModel();
		assertNotNull("The model should not be null.", model);

		Object object = model.get(BindingResult.MODEL_KEY_PREFIX + "sharedBuildNumberForm");
		assertNotNull("The binding result attribute should not be null.", object);
		assertTrue("The binding result attribute should be a binding result object.", object instanceof BindingResult);

		BindingResult result = (BindingResult)object;
		assertEquals("The binding result object name is not correct.", "sharedBuildNumberForm", result.getObjectName());
		assertTrue("The binding result should have errors.", result.hasErrors());
		assertEquals("The binding result should have 2 errors.", 2, result.getErrorCount());

		List<FieldError> errors = result.getFieldErrors();
		assertNotNull("The list of errors should not be null.", errors);
		assertEquals("The list length is not correct.", 2, errors.size());
		assertEquals("The first error is not correct.", "counter", errors.get(0).getField());
		assertEquals("The first error has the wrong message.", "The counter must be a positive integer.",
					 errors.get(0).getDefaultMessage());
		assertEquals("The second error is not correct.", "dateFormat", errors.get(1).getField());
		assertEquals("The second error has the wrong message.", "The date format must be at least 3 characters long.",
					 errors.get(1).getDefaultMessage());

		object = model.get("sharedBuildNumberForm");
		assertNotNull("sharedBuildNumberForm should not be null.", object);
		assertTrue("sharedBuildNumberForm should be a SharedBuildNumber.", object instanceof SharedBuildNumber);

		SharedBuildNumber form = (SharedBuildNumber)object;
		assertEquals("The name is not correct.", "Hello", form.getName());
		assertEquals("The description is not correct.", "This is a description.", form.getDescription());
		assertEquals("The format is not correct.", "1.0.0.{D}", form.getFormat());
		assertEquals("The date format is not correct.", "Ym", form.getDateFormat());
		assertEquals("The counter is not correct.", 1, form.getCounter());
	}

	@Test
	public void testDoHandleAddBuildNumberPost03() throws IOException, ServletException
	{
		this.setUpSecurity();

		Capture<SharedBuildNumber> capture = new Capture<SharedBuildNumber>();

		expect(this.request.getParameter("action")).andReturn("add");
		expect(this.request.getMethod()).andReturn("POST");
		expect(this.request.getParameter("name")).andReturn("Hello Again");
		expect(this.request.getParameter("description")).andReturn("This is another description.");
		expect(this.request.getParameter("format")).andReturn("2.3.0.{d}.{0}");
		expect(this.request.getParameter("dateFormat")).andReturn("YMdHms");
		expect(this.request.getParameter("counter")).andReturn("19");
		expect(this.service.getNextBuildNumberId()).andReturn(71);
		this.service.saveSharedBuildNumber(capture(capture));
		expectLastCall();

		replay(this.service, this.request, this.response);

		ModelAndView modelAndView = this.controller.doHandle(this.request, this.response);

		assertNotNull("The model and view should not be null.", modelAndView);

		View view = modelAndView.getView();

		assertNotNull("The view should not be null.", view);
		assertTrue("The view should be a redirect view.", view instanceof RedirectView);
		assertEquals("The redirect URL is not correct.", "/admin/admin.html?item=sharedBuildNumbers",
					 ((RedirectView) view).getUrl());

		SharedBuildNumber number = capture.getValue();
		assertNotNull("The shared build number should not be null.", number);
		assertEquals("The ID is not correct.", 71, number.getId());
		assertEquals("The name is not correct.", "Hello Again", number.getName());
		assertEquals("The description is not correct.", "This is another description.", number.getDescription());
		assertEquals("The format is not correct.", "2.3.0.{d}.{0}", number.getFormat());
		assertEquals("The date format is not correct.", "YMdHms", number.getDateFormat());
		assertEquals("The counter is not correct.", 19, number.getCounter());
	}

	@Test
	public void testDoHandleEditBuildNumberGet01() throws IOException, ServletException
	{
		this.setUpSecurity();

		expect(this.request.getParameter("action")).andReturn("edit");
		expect(this.request.getMethod()).andReturn("GET");
		expect(this.request.getParameter("id")).andReturn("a17");
		expect(this.request.getMethod()).andReturn("GET");
		expect(this.request.getRequestURI()).andReturn("/admin/adminSharedBuildNumbers.html");
		expect(this.request.getParameterNames()).andReturn(new StringTokenizer("action,id", ",")).atLeastOnce();
		expect(this.request.getParameterValues("action")).andReturn(new String[] {"edit"});
		expect(this.request.getParameterValues("id")).andReturn(new String[] {"a17"});
		expect(this.request.getRemoteAddr()).andReturn("4.3.3.3");
		expect(this.request.getRemotePort()).andReturn(7619);
		expect(this.request.getParameter("userKey")).andReturn("anotherUserKey");
		expect(this.request.getAttribute("anotherUserKey")).andReturn(this.user);
		expect(this.request.getAttribute("pageUrl")).andReturn(null);
		this.response.sendError(404, "The shared build number you are trying to edit does not exist.");
		expectLastCall();

		replay(this.service, this.request, this.response);

		ModelAndView modelAndView = this.controller.doHandle(this.request, this.response);

		assertNull("The model and view should be null.", modelAndView);
	}

	@Test
	public void testDoHandleEditBuildNumberGet02() throws IOException, ServletException
	{
		this.setUpSecurity();

		SharedBuildNumber number = new SharedBuildNumber(17);

		expect(this.request.getParameter("action")).andReturn("edit");
		expect(this.request.getMethod()).andReturn("GET");
		expect(this.request.getParameter("id")).andReturn("17");
		expect(this.service.getSharedBuildNumber(17)).andReturn(number);

		replay(this.service, this.request, this.response);

		ModelAndView modelAndView = this.controller.doHandle(this.request, this.response);

		assertNotNull("The model and view should not be null.", modelAndView);
		assertEquals("The view is not correct.", "/plugin/" + testNum + "/jsp/editBuildNumber.jsp",
					 modelAndView.getViewName());

		Map<String, Object> model = modelAndView.getModel();
		assertNotNull("The model should not be null.", model);
		assertEquals("sbnParameterPrefix is not correct.", BuildNumberPropertiesProvider.PARAMETER_PREFIX,
					 model.get("sbnParameterPrefix"));
		assertSame("sharedBuildNumberForm is not correct.", number, model.get("sharedBuildNumberForm"));
	}

	@Test
	public void testDoHandleEditBuildNumberPost01() throws IOException, ServletException
	{
		this.setUpSecurity();

		expect(this.request.getParameter("action")).andReturn("edit");
		expect(this.request.getMethod()).andReturn("POST");
		expect(this.request.getParameter("id")).andReturn("a53");
		expect(this.request.getMethod()).andReturn("POST");
		expect(this.request.getRequestURI()).andReturn("/admin/adminSharedBuildNumbers.html");
		expect(this.request.getParameterNames()).andReturn(new StringTokenizer("action,id", ",")).atLeastOnce();
		expect(this.request.getParameterValues("action")).andReturn(new String[] {"edit"});
		expect(this.request.getParameterValues("id")).andReturn(new String[] {"a53"});
		expect(this.request.getRemoteAddr()).andReturn("4.4.4.4");
		expect(this.request.getRemotePort()).andReturn(4837);
		expect(this.request.getParameter("userKey")).andReturn("anotherUserKey");
		expect(this.request.getAttribute("anotherUserKey")).andReturn(this.user);
		expect(this.request.getAttribute("pageUrl")).andReturn(null);
		this.response.sendError(404, "The shared build number you are trying to edit does not exist.");
		expectLastCall();

		replay(this.service, this.request, this.response);

		ModelAndView modelAndView = this.controller.doHandle(this.request, this.response);

		assertNull("The model and view should be null.", modelAndView);
	}

	@Test
	public void testDoHandleEditBuildNumberPost02() throws IOException, ServletException
	{
		this.setUpSecurity();

		SharedBuildNumber originalNumber = new SharedBuildNumber(37);

		expect(this.request.getParameter("action")).andReturn("edit");
		expect(this.request.getMethod()).andReturn("POST");
		expect(this.request.getParameter("id")).andReturn("37");
		expect(this.service.getSharedBuildNumber(37)).andReturn(originalNumber);
		expect(this.request.getParameter("name")).andReturn("help");
		expect(this.request.getParameter("description")).andReturn("");
		expect(this.request.getParameter("format")).andReturn("{0");
		expect(this.request.getParameter("dateFormat")).andReturn("Ym");
		expect(this.request.getParameter("counter")).andReturn("15.1");

		replay(this.service, this.request, this.response);

		ModelAndView modelAndView = this.controller.doHandle(this.request, this.response);

		assertNotNull("The model and view should not be null.", modelAndView);
		assertEquals("The view is not correct.", "/plugin/" + testNum + "/jsp/editBuildNumber.jsp",
					 modelAndView.getViewName());

		Map<String, Object> model = modelAndView.getModel();
		assertNotNull("The model should not be null.", model);
		assertEquals("sbnParameterPrefix is not correct.", BuildNumberPropertiesProvider.PARAMETER_PREFIX,
					 model.get("sbnParameterPrefix"));

		Object object = model.get(BindingResult.MODEL_KEY_PREFIX + "sharedBuildNumberForm");
		assertNotNull("The binding result attribute should not be null.", object);
		assertTrue("The binding result attribute should be a binding result object.", object instanceof BindingResult);

		BindingResult result = (BindingResult)object;
		assertEquals("The binding result object name is not correct.", "sharedBuildNumberForm", result.getObjectName());
		assertTrue("The binding result should have errors.", result.hasErrors());
		assertEquals("The binding result should have 3 errors.", 3, result.getErrorCount());

		List<FieldError> errors = result.getFieldErrors();
		assertNotNull("The list of errors should not be null.", errors);
		assertEquals("The list length is not correct.", 3, errors.size());
		assertEquals("The first error is not correct.", "counter", errors.get(0).getField());
		assertEquals("The first error has the wrong message.", "The counter must be a positive integer.",
					 errors.get(0).getDefaultMessage());
		assertEquals("The second error is not correct.", "name", errors.get(1).getField());
		assertEquals("The second error has the wrong message.", "The name must be between 5 and 60 characters long.",
					 errors.get(1).getDefaultMessage());
		assertEquals("The third error is not correct.", "format", errors.get(2).getField());
		assertEquals("The third error has the wrong message.",
					 "The build number format must be at least 3 characters long.",
					 errors.get(2).getDefaultMessage());

		object = model.get("sharedBuildNumberForm");
		assertNotNull("sharedBuildNumberForm should not be null.", object);
		assertTrue("sharedBuildNumberForm should be a SharedBuildNumber.", object instanceof SharedBuildNumber);

		SharedBuildNumber form = (SharedBuildNumber)object;
		assertEquals("The name is not correct.", "help", form.getName());
		assertEquals("The description is not correct.", "", form.getDescription());
		assertEquals("The format is not correct.", "{0", form.getFormat());
		assertEquals("The date format is not correct.", "Ym", form.getDateFormat());
		assertEquals("The counter is not correct.", 1, form.getCounter());
	}

	@Test
	public void testDoHandleEditBuildNumberPost03() throws IOException, ServletException
	{
		this.setUpSecurity();

		SharedBuildNumber originalNumber = new SharedBuildNumber(26);

		expect(this.request.getParameter("action")).andReturn("edit");
		expect(this.request.getMethod()).andReturn("POST");
		expect(this.request.getParameter("id")).andReturn("26");
		expect(this.service.getSharedBuildNumber(26)).andReturn(originalNumber);
		expect(this.request.getParameter("name")).andReturn("Hello");
		expect(this.request.getParameter("description")).andReturn("This is a description.");
		expect(this.request.getParameter("format")).andReturn("1.0.0.{D}");
		expect(this.request.getParameter("dateFormat")).andReturn("Ym");
		expect(this.request.getParameter("counter")).andReturn("-16");

		replay(this.service, this.request, this.response);

		ModelAndView modelAndView = this.controller.doHandle(this.request, this.response);

		assertNotNull("The model and view should not be null.", modelAndView);
		assertEquals("The view is not correct.", "/plugin/" + testNum + "/jsp/editBuildNumber.jsp",
					 modelAndView.getViewName());

		Map<String, Object> model = modelAndView.getModel();
		assertNotNull("The model should not be null.", model);
		assertEquals("sbnParameterPrefix is not correct.", BuildNumberPropertiesProvider.PARAMETER_PREFIX,
					 model.get("sbnParameterPrefix"));

		Object object = model.get(BindingResult.MODEL_KEY_PREFIX + "sharedBuildNumberForm");
		assertNotNull("The binding result attribute should not be null.", object);
		assertTrue("The binding result attribute should be a binding result object.", object instanceof BindingResult);

		BindingResult result = (BindingResult)object;
		assertEquals("The binding result object name is not correct.", "sharedBuildNumberForm", result.getObjectName());
		assertTrue("The binding result should have errors.", result.hasErrors());
		assertEquals("The binding result should have 2 errors.", 2, result.getErrorCount());

		List<FieldError> errors = result.getFieldErrors();
		assertNotNull("The list of errors should not be null.", errors);
		assertEquals("The list length is not correct.", 2, errors.size());
		assertEquals("The first error is not correct.", "counter", errors.get(0).getField());
		assertEquals("The first error has the wrong message.", "The counter must be a positive integer.",
					 errors.get(0).getDefaultMessage());
		assertEquals("The second error is not correct.", "dateFormat", errors.get(1).getField());
		assertEquals("The second error has the wrong message.", "The date format must be at least 3 characters long.",
					 errors.get(1).getDefaultMessage());

		object = model.get("sharedBuildNumberForm");
		assertNotNull("sharedBuildNumberForm should not be null.", object);
		assertTrue("sharedBuildNumberForm should be a SharedBuildNumber.", object instanceof SharedBuildNumber);

		SharedBuildNumber form = (SharedBuildNumber)object;
		assertEquals("The name is not correct.", "Hello", form.getName());
		assertEquals("The description is not correct.", "This is a description.", form.getDescription());
		assertEquals("The format is not correct.", "1.0.0.{D}", form.getFormat());
		assertEquals("The date format is not correct.", "Ym", form.getDateFormat());
		assertEquals("The counter is not correct.", 1, form.getCounter());
	}

	@Test
	public void testDoHandleEditBuildNumberPost04() throws IOException, ServletException
	{
		this.setUpSecurity();

		SharedBuildNumber originalNumber = new SharedBuildNumber(45);

		Capture<SharedBuildNumber> capture = new Capture<SharedBuildNumber>();

		expect(this.request.getParameter("action")).andReturn("edit");
		expect(this.request.getMethod()).andReturn("POST");
		expect(this.request.getParameter("id")).andReturn("45");
		expect(this.service.getSharedBuildNumber(45)).andReturn(originalNumber);
		expect(this.request.getParameter("name")).andReturn("Hello Again");
		expect(this.request.getParameter("description")).andReturn("This is another description.");
		expect(this.request.getParameter("format")).andReturn("2.3.0.{d}.{0}");
		expect(this.request.getParameter("dateFormat")).andReturn("YMdHms");
		expect(this.request.getParameter("counter")).andReturn("0");
		this.service.saveSharedBuildNumber(capture(capture));
		expectLastCall();

		replay(this.service, this.request, this.response);

		ModelAndView modelAndView = this.controller.doHandle(this.request, this.response);

		assertNotNull("The model and view should not be null.", modelAndView);

		View view = modelAndView.getView();

		assertNotNull("The view should not be null.", view);
		assertTrue("The view should be a redirect view.", view instanceof RedirectView);
		assertEquals("The redirect URL is not correct.", "/admin/admin.html?item=sharedBuildNumbers",
					 ((RedirectView) view).getUrl());

		SharedBuildNumber number = capture.getValue();
		assertNotNull("The shared build number should not be null.", number);
		assertSame("The shared build number is not correct.", originalNumber, number);
		assertEquals("The ID is not correct.", 45, number.getId());
		assertEquals("The name is not correct.", "Hello Again", number.getName());
		assertEquals("The description is not correct.", "This is another description.", number.getDescription());
		assertEquals("The format is not correct.", "2.3.0.{d}.{0}", number.getFormat());
		assertEquals("The date format is not correct.", "YMdHms", number.getDateFormat());
		assertEquals("The counter is not correct.", 1, number.getCounter());
	}

	@Test
	public void testDoHandleDeleteBuildNumberPost01() throws IOException, ServletException
	{
		this.setUpSecurity();

		expect(this.request.getParameter("action")).andReturn("delete").atLeastOnce();
		expect(this.request.getMethod()).andReturn("POST").atLeastOnce();
		expect(this.request.getParameter("id")).andReturn("a31");
		expect(this.request.getMethod()).andReturn("POST");
		expect(this.request.getRequestURI()).andReturn("/admin/adminSharedBuildNumbers.html");
		expect(this.request.getParameterNames()).andReturn(new StringTokenizer("action,id", ",")).atLeastOnce();
		expect(this.request.getParameterValues("action")).andReturn(new String[] {"delete"});
		expect(this.request.getParameterValues("id")).andReturn(new String[] {"a31"});
		expect(this.request.getRemoteAddr()).andReturn("4.5.5.5");
		expect(this.request.getRemotePort()).andReturn(2398);
		expect(this.request.getParameter("userKey")).andReturn("anotherUserKey");
		expect(this.request.getAttribute("anotherUserKey")).andReturn(this.user);
		expect(this.request.getAttribute("pageUrl")).andReturn(null);
		this.response.sendError(404, "The shared build number you are trying to delete does not exist.");
		expectLastCall();

		replay(this.service, this.request, this.response);

		ModelAndView modelAndView = this.controller.doHandle(this.request, this.response);

		assertNull("The model and view should be null.", modelAndView);
	}

	@Test
	public void testDoHandleDeleteBuildNumberPost02() throws IOException, ServletException
	{
		this.setUpSecurity();

		expect(this.request.getParameter("action")).andReturn("delete").atLeastOnce();
		expect(this.request.getMethod()).andReturn("POST").atLeastOnce();
		expect(this.request.getParameter("id")).andReturn("31");
		this.service.deleteSharedBuildNumber(31);
		expectLastCall();

		replay(this.service, this.request, this.response);

		ModelAndView modelAndView = this.controller.doHandle(this.request, this.response);

		assertNotNull("The model and view should not be null.", modelAndView);

		View view = modelAndView.getView();

		assertNotNull("The view should not be null.", view);
		assertTrue("The view should be a redirect view.", view instanceof RedirectView);
		assertEquals("The redirect URL is not correct.", "/admin/admin.html?item=sharedBuildNumbers",
					 ((RedirectView) view).getUrl());
	}
}