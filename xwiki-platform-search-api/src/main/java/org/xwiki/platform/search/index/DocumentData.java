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

import org.xwiki.component.annotation.Role;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;

/**
 * @version $Id$
 */
@Role
public interface DocumentData
{

    /**
     * Returns the document id used by indexer.
     * 
     * @param documentReference reference to the document
     * @return document id.
     */
    String getDocumentId(DocumentReference documentReference);

    /**
     * Returns the attachment id used by indexer.
     * 
     * @param attachmentReference reference to the attachment
     * @return attachment id.
     */
    String getAttachmentId(AttachmentReference attachmentReference);

    /**
     * Returns the object id used by indexer.
     * 
     * @param documentReference reference to the document
     * @param object object 
     * @return object id
     */
    String getObjectId(DocumentReference documentReference, Object object);

    /**
     * Returns the property id used by indexer.
     * 
     * @param documentReference reference to the document
     * @param property of the Documents.
     * @return the property ID
     */
    String getPropertyId(DocumentReference documentReference, Object property);

    /**
     * List of objects to be indexed in search engine specific input format.
     * 
     * @param documentReference reference to the document
     * @return list of documents
     */
    Object getInputDocument(DocumentReference documentReference);

    /**
     * List of attachments to be indexed in search engine specific input format.
     * 
     * @param documentReference reference to the document
     * @return list of attachments.
     */
    List< ? > getInputAttachments(DocumentReference documentReference);

    /**
     * List of objects to be indexed in search engine specific input format.
     * 
     * @param documentReference reference to the document
     * @return list of objects
     */
    List< ? > getInputObjects(DocumentReference documentReference);

    /**
     * List of properties to be indexed in search engine specific input format.
     * 
     * @param documentReference reference to the document
     * @return list of properties
     */
    List< ? > getInputProperties(DocumentReference documentReference);
    
    /**
     * 
     * @param documentReference reference to the document.
     * @return the List of Object Id
     */
    List<String> getObjectIdList(DocumentReference documentReference);
    
    /**
     * 
     * @param documentReference reference to the document.
     * @return the list of Property ID.
     */
    List<String> getPropertyIdList(DocumentReference documentReference);
}
