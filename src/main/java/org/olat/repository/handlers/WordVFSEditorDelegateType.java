/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.repository.handlers;

import java.io.InputStream;

import org.olat.core.commons.services.doceditor.ContentProviderFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.fileresource.types.DocFileResource;

/**
 * 
 * Initial date: 25 Mar 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class WordVFSEditorDelegateType implements DocumentEditorDelegateType {

	@Override
	public String getCreateLabelI18nKey() {
		return "new.word";
	}

	@Override
	public OLATResourceable getOLATResourceable() {
		return new DocFileResource();
	}

	@Override
	public String getSuffix() {
		return "docx";
	}

	@Override
	public InputStream getContent() {
		return ContentProviderFactory.emptyDocx().getContent();
	}

}
