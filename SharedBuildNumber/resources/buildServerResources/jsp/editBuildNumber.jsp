<%--
  ~ addBuildNumber.jsp from TeamCityPlugins modified Tuesday, September 11, 2012 15:06:05 CDT (-0500).
  ~
  ~ Copyright 2010-2012 the original author or authors.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~      http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>

<%@ page contentType="text/html;charset=UTF-8" language="java" session="true" errorPage="/runtimeError.jsp"
		%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
		%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
		%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
		%><%@ taglib prefix="bs" tagdir="/WEB-INF/tags"
		%><%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout"
		%><%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms"
		%><%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz"
		%><%@ taglib prefix="afn" uri="/WEB-INF/functions/authz"
		%><%@ taglib prefix="graph" tagdir="/WEB-INF/tags/graph"
		%><%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<jsp:useBean id="sbnParameterPrefix" scope="request" type="java.lang.String" />
<jsp:useBean id="sharedBuildNumberForm" scope="request" type="net.nicholaswilliams.java.teamcity.plugin.buildNumber.pojo.SharedBuildNumber" />

<script type="text/javascript" language="javascript">
	extendMainNavigation([
			{ title: '<bs:escapeForJs text="${sharedBuildNumberForm.name}" forHTMLAttribute="true"/>', selected: true }
	]);
</script>

<form:form action="/admin/adminSharedBuildNumbers.html" modelAttribute="sharedBuildNumberForm" id="sharedBuildNumberForm" onsubmit="return submitSharedBuildNumberForm();">
	<input type="hidden" name="action" value="edit" />
	<form:hidden path="id" />
	<div id="sharedBuildNumberFormDiv">
		<p>
			<label>ID:</label>
			<span style="font-weight: bold;">${sharedBuildNumberForm.id}</span>
		</p>
		<p>
			<label>System Property:</label>
			<span style="font-weight: bold;">%${sbnParameterPrefix}${sharedBuildNumberForm.id}%</span>
			<span class="smallNote">To use this shared build number, put this property in the build configuration's build number format.</span>
		</p>
		<p>
			<form:label path="name">Name: <span class="mandatoryAsterix" title="Mandatory field">*</span></form:label>
			<form:input path="name" cssClass="longField" maxlength="60" required="reqiured" pattern=".{5,60}" /><form:errors path="name" cssClass="error" />
			<span class="smallNote">Must be at least 5 characters long.</span>
		</p>
		<p>
			<form:label path="description">Description:</form:label>
			<form:input path="description" cssClass="longField" /><form:errors path="description" cssClass="error" />
		</p>
		<p>
			<form:label path="format">Build number format: <span class="mandatoryAsterix" title="Mandatory field">*</span></form:label>
			<form:input path="format" cssClass="longField" maxlength="250" required="reqiured" pattern=".{3,}" oninput="checkFormats(this.form);" /><form:errors path="format" cssClass="error" />
			<span class="smallNote">
				Format may include '{0}' as a placeholder for build counter value and/or '{d}' as a placeholder for the
				date/time stamp the build starts, for example 1.{0} or 2.{d} or 2.1.{d}.{0}. It may not contain
				references to other variables. If the date format is not granular, it is recommended you use both date
				and the build counter, instead of just the counter, to guarantee build number uniqueness.
			</span>
			<span class="smallNote">
				Note: maximum length of a build number after all substitutions is 256 characters.
			</span>
		</p>
		<p>
			<form:label path="dateFormat">Date format: <span class="mandatoryAsterix" id="dateFormatAsterisk" title="Mandatory field" style="display:none;">*</span></form:label>
			<form:input path="dateFormat" cssClass="longField" maxlength="100" oninput="checkFormats(this.form);" /><form:errors path="dateFormat" cssClass="error" />
			<span class="smallNote">
				The date format is optional unless the build number format includes '{d}' as a placeholder. In that
				case, the date format is required. Date format should use the syntax specified in Java's
				<a href="http://docs.oracle.com/javase/6/docs/api/java/text/SimpleDateFormat.html" target="_blank">SimpleDateFormat</a>
				API documentation.
			</span>
		</p>
		<p>
			<form:label path="counter">Build counter: <span class="mandatoryAsterix" title="Mandatory field">*</span></form:label>
			<form:input path="counter" type="number" size="15" maxlength="15" required="reqiured" step="1" /><form:errors path="counter" cssClass="error" />
			<span class="smallNote">
				It is not recommended that you decrease the build counter, as proper behavior is not guaranteed.
			</span>
		</p>

		<div class="saveButtonsBlock">
			<input type="button" value="Cancel" class="btn cancel" onclick="BS.openUrl(event, '/admin/admin.html?item=sharedBuildNumbers'); return false">
			<input type="submit" value="Save Changes" class="btn btn_primary submitButton">
			<img id="saving" style="display: none;" class="progressRing progressRingDefault" src="<c:url value="/img/ajax-loader.gif" />" width="16" height="16" alt="Please wait..." title="Please wait...">
		</div>
	</div>
</form:form>
