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

/**
 * XWiki DoucumentIndexer API
 * 
 * @version $Id$
 */
@Role
public interface DocumentIndexer
{
    /**
     * Index the document.
     * 
     * @param document reference to the document to be indexed.
     * @return true if document indexing is successful.
     */
    boolean indexDocument(DocumentReference docs);

    /**
     * Index the documents.
     * 
     * @param documents List of documents to be indexed.
     * @return the Number of documents scheduled for indexing. -1 in case of errors.
     */
    void indexDocuments(List<DocumentReference> docs);

    /**
     * Delete the index for document.
     * 
     * @param doc List of document references to which index needs to be deleted.
     * @return true if document index deletion is successful.
     */
    boolean deleteIndex(DocumentReference doc);

    /**
     * Delete the index for the given list of documents.
     * 
     * @param document Document reference to which index needs to be deleted.
     * @return true if document index deletion is successful.
     */
    void deleteIndex(List<DocumentReference> docs);

    /**
     * Deletes the entire index of the current wiki.
     * 
     * @return true if the index deletion is successful.
     */
    boolean deleteEntireIndex();

    /**
     * Sets the SearchEngineObject
     * 
     * @param server reference to the backend used
     */
    void setSearchEngineObject(Object server);

    /**
     * @return status of indexing process as a Map of indexing thread and its status.
     */
    Map<String, DocumentIndexerStatus> getStatus();

    /**
     * @param threadId of Indexing thread.
     * @return status of given thread.
     */
    DocumentIndexerStatus getStatus(String threadId);
    
    /**
     * 
     * @param attachment attachment reference of the files
     * @param doc parent document
     * @return boolean value
     */
     boolean indexAttachment(AttachmentReference attachment, DocumentModelBridge doc);
     
    /**
     * 
     * @param attachment attachment reference of the files
     * @param doc parent document
     * @return boolean value
     */
     boolean deleteIndexAttachment(AttachmentReference attachment, DocumentModelBridge doc);

}
