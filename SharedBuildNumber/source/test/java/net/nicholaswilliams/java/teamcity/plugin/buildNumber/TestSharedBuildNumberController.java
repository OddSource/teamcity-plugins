/*
 * TestSharedBuildNumberController.java from TeamCityPlugins modified Wednesday, September 12, 2012 22:24:42 CDT (-0500).
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
import org.easymock.Capture;
import org.easymock.CaptureType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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
		verify(this.manager, this.descriptor, this.places, this.request, this.user, this.service);
	}

	@Test
	public void testPageExtensionProperties()
	{
		replay(this.service, this.request, this.user);

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

		replay(this.service, this.request, this.user);

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

		replay(this.service, this.request, this.user);

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

		replay(this.service, this.request, this.user);

		assertTrue("The return value should be true.", this.extension.isAvailable(this.request));
	}

	private void setUpSecurity_hasAccess()
	{
		this.request = createStrictMock(HttpServletRequest.class);
		this.user = createStrictMock(SUser.class);

		expect(this.request.getParameter("userKey")).andReturn("aUserKey");
		expect(this.request.getAttribute("aUserKey")).andReturn(this.user);
		expect(this.user.isSystemAdministratorRoleGranted()).andReturn(true);

		replay(this.user);
	}

	private void setUpSecurity_noAccess()
	{
		this.request = createStrictMock(HttpServletRequest.class);
		this.user = createStrictMock(SUser.class);

		expect(this.request.getParameter("userKey")).andReturn("aUserKey");
		expect(this.request.getAttribute("aUserKey")).andReturn(this.user);
		expect(this.user.isSystemAdministratorRoleGranted()).andReturn(false);
		expect(this.user.isPermissionGrantedGlobally(Permission.EDIT_PROJECT)).andReturn(false);

		replay(this.user);
	}
}