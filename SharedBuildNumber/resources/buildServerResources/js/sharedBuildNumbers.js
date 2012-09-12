/*
 * main.js from TeamCityPlugins modified Tuesday, September 11, 2012 00:23:06 CDT (-0500).
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

if(!String.prototype.trim)
{
	String.prototype.trim = function()
	{
		return this.replace(/^\s+|\s+$/g, '');
	};
}

function editSharedBuildNumber(event, id)
{
	BS.openUrl(event, '/admin/admin.html?item=sharedBuildNumbers&action=edit&id=' + id);
	return false;
}

function deleteSharedBuildNumber(id)
{
	if(confirm('Are you sure you want to delete this shared build number? You will not be able to recover these settings.'))
	{
		var form = document.getElementById('deleteSharedBuildNumberForm');
		form.elements['id'].value = id;
		form.submit();
	}
	return false;
}

function submitSharedBuildNumberForm()
{
	var form = document.getElementById('sharedBuildNumberForm');

	if(form.elements['format'].value.trim().length < 3)
	{
		alert('The build number format must be at least 3 characters long.');
		return false;
	}

	if(form.elements['format'].value.indexOf('{d}') > -1 && form.elements['dateFormat'].value.trim().length < 3)
	{
		alert('You are using the \'{d}\' date substitution in the build number format. Because of this, you must specify a date format at least 3 characters long.');
		return false;
	}

	if(form.elements['counter'].value.trim().length == 0 || Number(form.elements['counter'].value.trim().length) == Number.NaN)
	{
		alert('The build number');
		return false;
	}

	document.getElementById('saving').style.display = 'inline';
	return true;
}

function checkFormats(form)
{
	if(form.elements['format'].value.indexOf('{d}') > -1)
	{
		form.elements['dateFormat'].required = true;
		form.elements['dateFormat'].pattern = '.{3,}';
		document.getElementById('dateFormatAsterisk').style.display = 'inline';
	}
	else
	{
		form.elements['dateFormat'].required = false;
		form.elements['dateFormat'].pattern = '';
		document.getElementById('dateFormatAsterisk').style.display = 'none';
	}

	if(form.elements['dateFormat'].setCustomValidity)
	{
		if(form.elements['format'].value.indexOf('{d}') > -1 && form.elements['dateFormat'].value.trim().length < 3)
		{
			form.elements['dateFormat'].setCustomValidity('You are using the \'{d}\' date substitution in the build number format. Because of this, you must specify a date format at least 3 characters long.');
		}
		else if(form.elements['dateFormat'].value.trim().length > 0 && form.elements['dateFormat'].value.trim().length < 3)
		{
			form.elements['dateFormat'].setCustomValidity('If specified, the date format must be at least 3 characters long.');
		}
		else
		{
			form.elements['dateFormat'].setCustomValidity('');
		}
	}
}

function extendMainNavigation(links)
{
	var ul = document.getElementById('main_navigation');

	var lis = ul.getElementsByTagName('li');

	var sbnLi = lis[lis.length - 1];
	sbnLi.removeAttribute('class');
	sbnLi.innerHTML = '<a href="#" onclick="return BS.openUrl(event, \'/admin/admin.html?item=sharedBuildNumbers\');">Shared Build Numbers</a>';

	for(var i = 0; i < links.length; i++)
	{
		var li = document.createElement('li');
		var link = links[i];

		var css = i == links.length - 1 ? 'last ' : '';
		css += link.selected === true ? 'selected' : '';
		if(css.length > 0)
			li.setAttribute('class', css);

		if(link.url != null && link.url.length > 0)
			li.innerHTML = '<a href="' + link.url + '">' + link.title + '</a>';
		else
			li.innerHTML = link.title;

		ul.appendChild(li);
	}
}