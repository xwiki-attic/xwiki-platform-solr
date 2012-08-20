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
import java.util.Map;

import org.xwiki.bridge.DocumentModelBridge;
import org.xwiki.component.annotation.Role;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.platform.search.index.internal.AbstractDocumentIndexerStatus;

/**
 * XWiki DoucumentIndexer API.
 * 
 * @version $Id$
 */
@Role
public interface DocumentIndexer
{
    /**
     * Index the document.
     * 
     * @param docs document reference to the document to be indexed.
     * @return true if document indexing is successful.
     */
    boolean indexDocument(DocumentReference docs);

    /**
     * Index the documents.
     * 
     * @param docs documents List of documents to be indexed.
     * 
     */
    void indexDocuments(List<DocumentReference> docs);

    /**
     * Index the documents.
     * 
     * @param reference Entity reference of Wiki or Space
     * @param docs documents List of documents to be indexed.
     * 
     */
    void indexDocuments(EntityReference reference, List<DocumentReference> docs);

    /**
     * Delete the index for document.
     * 
     * @param doc document Document reference to which index needs to be deleted.
     * @return true if document index deletion is successful.
     */
    boolean deleteIndex(DocumentReference doc);

    /**
     * Delete the index for the given list of documents.
     * 
     * @param docs list of Document reference to which index needs to be deleted.
     * 
     */
    void deleteIndex(List<DocumentReference> docs);

    /**
     * Delete the index for the given list of documents.
     * 
     * @param reference Entity reference of Wiki or Space
     * @param docs list of Document reference to which index needs to be deleted.
     * 
     */
    void deleteIndex(EntityReference reference, List<DocumentReference> docs);

    /**
     * Deletes the entire index of the current wiki.
     * 
     * @return true if the index deletion is successful.
     */
    boolean deleteEntireIndex();

    /**
     * Sets the SearchEngineObject.
     * 
     * @param server reference to the backend used
     */
    void setSearchEngineObject(Object server);

    /**
     * @return status of indexing process as a Map of indexing thread and its status.
     */
    Map<String, AbstractDocumentIndexerStatus> getStatus();

    /**
     * @param threadId of Indexing thread.
     * @return status of given thread.
     */
    DocumentIndexerStatus getStatus(String threadId);

    /**
     * @param attachment attachment reference of the files
     * @param doc parent document
     * @return boolean value
     */
    boolean indexAttachment(AttachmentReference attachment, DocumentModelBridge doc);

    /**
     * @param attachment attachment reference of the files
     * @param doc parent document
     * @return boolean value
     */
    boolean deleteIndexAttachment(AttachmentReference attachment, DocumentModelBridge doc);

}
