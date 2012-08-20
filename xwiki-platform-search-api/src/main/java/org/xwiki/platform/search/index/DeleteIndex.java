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
public interface DeleteIndex
{
    /**
     * Delete the index for document.
     * 
     * @param documentReference Document reference to which index needs to be deleted.
     * @return true if document index deletion is successful.
     */
    boolean deleteDocumentIndex(DocumentReference documentReference);

    /**
     * @param wikiReference WikiReference reference to the Wiki
     * @return the boolean value true is successfully deleted.
     * @throws SearchIndexingException thrown in case of indexing errors.
     * @throws XWikiException thrown in case of XWiki syntax errors.
     */
    boolean deleteWikiIndex(WikiReference wikiReference) throws SearchIndexingException, XWikiException;

    /**
     * @param spaceReference reference to XWiki space.
     * @return the boolean value true is successfully deleted.
     * @throws SearchIndexingException thrown in case of indexing errors.
     * @throws XWikiException thrown in case of XWiki syntax errors.
     */
    boolean deleteSpaceIndex(SpaceReference spaceReference) throws SearchIndexingException, XWikiException;

    /**
     * @return the boolean value true is successfully deleted.
     * @throws SearchIndexingException thrown in case of indexing errors.
     * @throws XWikiException thrown in case of XWiki syntax errors.
     */
    boolean deleteEntireIndex() throws SearchIndexingException, XWikiException;

    /**
     * @param attachment attachment reference.
     * @param doc DocumentModelBridge.
     * @return boolean if Index of the attachment is deleted.
     */
    int deleteAttachmentIndex(AttachmentReference attachment, DocumentModelBridge doc);
}
