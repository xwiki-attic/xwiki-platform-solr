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
package org.xwiki.platform.search.index.internal;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.bridge.DocumentModelBridge;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.platform.search.index.DocumentIndexer;
import org.xwiki.platform.search.index.SearchIndexingException;

import com.xpn.xwiki.XWikiException;

/**
 * @version $Id$
 */
@Component
@Named("solrj")
@Singleton
public class SolrjDeleteIndex extends AbstractDeleteIndex
{

    /**
     * Document indexer for solrj.
     */
    @Inject
    @Named(SolrjDocumentIndexer.HINT)
    private DocumentIndexer indexer;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.internal.AbstractDeleteIndex#deleteIndex(org.xwiki.model.reference.EntityReference)
     */
    @Override
    public boolean deleteIndex(EntityReference entityReference)
    {
        try {
            indexer.deleteIndex(entityReference, null);
            return true;
        } catch (Exception e) {
            logger.error("Deleting the wiki index for the Wiki[" + entityReference.getName() + "]");
        }
        return false;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.DeleteIndex#deleteEntireIndex()
     */
    @Override
    public boolean deleteEntireIndex() throws SearchIndexingException, XWikiException
    {
        this.indexer.deleteEntireIndex();
        return true;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.DeleteIndex#deleteAttachmentIndex(org.xwiki.model.reference.AttachmentReference,
     *      org.xwiki.bridge.DocumentModelBridge)
     */
    @Override
    public int deleteAttachmentIndex(AttachmentReference attachment, DocumentModelBridge doc)
    {
        indexer.deleteIndexAttachment(attachment, doc);
        return 0;
    }
}
