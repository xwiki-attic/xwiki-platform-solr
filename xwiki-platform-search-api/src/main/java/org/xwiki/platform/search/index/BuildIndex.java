/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.platform.search.index;

import java.util.List;

import org.xwiki.bridge.DocumentModelBridge;
import org.xwiki.component.annotation.Role;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.xpn.xwiki.XWikiException;

/**
 * @version $Id$
 */
@Role
public interface BuildIndex
{
    /**
     * Index the document.
     * 
     * @param document reference to the document to be indexed.
     * @return true if document indexing is successful.
     */
    boolean buildDocumentIndex(DocumentReference document);

    /**
     * Index the documents.
     * 
     * @param documents List of documents to be indexed.
     * @return the Number of documents scheduled for indexing. -1 in case of errors.
     */
    int buildDocumentIndex(List<DocumentReference> documents);

    /**
     * Build the index for the current wiki.
     * 
     * @param wikiReference WikiReference reference to the Wiki
     * @return number of documents to index.
     * @throws SearchIndexingException Exception thrown in case of indexing errors.
     * @throws XWikiException thrown in case of XWiki syntax errors.
     */
    int buildWikiIndex(WikiReference wikiReference) throws SearchIndexingException, XWikiException;

    /**
     * Build the index for the given space.
     * 
     * @param reference SpaceReference
     * @return number of documents to index.
     * @throws SearchIndexingException Exception thrown in case of indexing errors.
     * @throws XWikiException thrown in case of XWiki syntax errors.
     */
    int buildWikiSpaceIndex(SpaceReference reference) throws SearchIndexingException, XWikiException;

    /**
     * Build the index for the current wiki farm.
     * 
     * @return number of documents to index.
     * @throws SearchIndexingException thrown in case of indexing errors.
     * @throws XWikiException thrown in case of XWiki syntax errors.
     */
    int buildWikiFarmIndex() throws SearchIndexingException, XWikiException;

    /**
     * @param attachment reference to the attachment.
     * @param doc DocumentModelBridge.
     * @return the boolean true if successfully indexed
     */
    int buildAttachmentIndex(AttachmentReference attachment, DocumentModelBridge doc);
}
