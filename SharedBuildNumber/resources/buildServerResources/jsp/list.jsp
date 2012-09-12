<%--
  ~ list.jsp from TeamCityPlugins modified Monday, September 10, 2012 23:53:28 CDT (-0500).
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
		%><%@ taglib prefix="graph" tagdir="/WEB-INF/tags/graph" %>
<jsp:useBean id="numResults" scope="request" type="java.lang.Integer" />
<jsp:useBean id="buildNumbers" scope="request" type="java.util.SortedSet<net.nicholaswilliams.java.teamcity.plugin.buildNumber.pojo.SharedBuildNumber>" />
<jsp:useBean id="sortedBy" scope="request" type="java.lang.String" />
<jsp:useBean id="sortClass" scope="request" type="java.lang.String" />
<jsp:useBean id="sortChange" scope="request" type="java.lang.String" />
<jsp:useBean id="sbnParameterPrefix" scope="request" type="java.lang.String" />

<div id="sharedBuildNumbersTable" class="refreshable">
	<div id="sharedBuildNumbersTableInner" class="refreshableInner">
		<div id="sharedBuildNumbersListContainer">
			<div>You have ${numResults} shared build numbers.</div>
			<c:if test="${numResults > 0}"><table id="sharedBuildNumbers" class="dark sortable userList borderBottom" cellpadding="0" cellspacing="0">
				<thead>
					<tr>
						<c:choose>
							<c:when test="${sortedBy == 'name'}">
								<th class="name sortable firstCell sharedBuildNumberId" onclick="BS.openUrl(event, '/admin/admin.html?item=sharedBuildNumbers');">
									<span id="SORT_BY_ID">ID</span>
								</th>
								<th class="name sortable sharedBuildNumberName" onclick="BS.openUrl(event, '/admin/admin.html?item=sharedBuildNumbers&sort=name&direction=${sortChange}');">
									<span id="SORT_BY_NAME" class="${sortClass}">Name</span>
								</th>
							</c:when>
							<c:otherwise>
								<th class="name sortable firstCell sharedBuildNumberId" onclick="BS.openUrl(event, '/admin/admin.html?item=sharedBuildNumbers&direction=${sortChange}');">
									<span id="SORT_BY_ID" class="${sortClass}">ID</span>
								</th>
								<th class="name sortable sharedBuildNumberName" onclick="BS.openUrl(event, '/admin/admin.html?item=sharedBuildNumbers&sort=name');">
									<span id="SORT_BY_NAME">Name</span>
								</th>
							</c:otherwise>
						</c:choose>
						<th class="name sharedBuildNumberFormat">Format</th>
						<th class="name sharedBuildNumberCounter">Counter</th>
						<th class="name lastCell sharedBuildNumberParameter" colspan="3">Property</th>
					</tr>
				</thead>
				<tbody>
				<c:forEach items="${buildNumbers}" var="buildNumber">
					<tr>
						<td class="highlight sharedBuildNumberId" title="Click to edit" onclick="return editSharedBuildNumber(event, ${buildNumber.id});">${buildNumber.id}</td>
						<td class="highlight sharedBuildNumberName" title="Click to edit" onclick="return editSharedBuildNumber(event, ${buildNumber.id});">${buildNumber.name}</td>
						<td class="highlight sharedBuildNumberFormat" title="Click to edit" onclick="return editSharedBuildNumber(event, ${buildNumber.id});">${buildNumber.format}</td>
						<td class="highlight sharedBuildNumberCounter" title="Click to edit" onclick="return editSharedBuildNumber(event, ${buildNumber.id});">${buildNumber.counter}</td>
						<td class="highlight sharedBuildNumberParameter" title="Click to edit" onclick="return editSharedBuildNumber(event, ${buildNumber.id});">%${sbnParameterPrefix}${buildNumber.id}%</td>
						<td class="highlight edit" title="Click to edit">
							<a href="<c:url value="/admin/admin.html?item=sharedBuildNumbers&action=edit&id=${buildNumber.id}" />">edit</a>
						</td>
						<td class="highlight edit">
							<a href="#" onclick="return deleteSharedBuildNumber(${buildNumber.id});" title="Click to delete">delete</a>
						</td>
					</tr>
				</c:forEach>
				</tbody>
			</table>
			<script type="text/javascript" language="javascript">

				(function() {
					var highlightableElements = $j("#sharedBuildNumbers td.highlight");
					highlightableElements.each(
							function(i, element) {
								BS.TableHighlighting.createInitElementFunction.call(this, element, 'Click to edit');
							}
					);
					highlightableElements.mouseover();
				})();

			</script></c:if>
			<p>
				<a class="btn" href="<c:url value="/admin/admin.html?item=sharedBuildNumbers&action=add" />">
					<span class="addNew">Create shared build number</span>
				</a>
			</p>
		</div>
	</div>
</div>

<%@ include file="deleteForm.jsp" %>
