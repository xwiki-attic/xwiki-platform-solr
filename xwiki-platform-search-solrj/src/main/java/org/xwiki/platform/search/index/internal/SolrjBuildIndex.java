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


import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.bridge.DocumentModelBridge;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.platform.search.index.DocumentIndexer;
import org.xwiki.platform.search.index.SearchIndexingException;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

/**
 * @version $Id$
 */
@Component
@Named("solrj")
@Singleton
public class SolrjBuildIndex extends AbstractBuildIndex
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
     * @see org.xwiki.platform.search.index.BuildIndex#buildDocumentIndex(org.xwiki.model.reference.DocumentReference)
     */
    @Override
    public boolean buildDocumentIndex(DocumentReference document)
    {
        return this.indexer.indexDocument(document);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.BuildIndex#buildDocumentIndex(java.util.List)
     */
    @Override
    public int buildDocumentIndex(List<DocumentReference> documents)
    {
        this.indexer.indexDocuments(documents);
        return documents.size();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.BuildIndex#buildWikiIndex(org.xwiki.model.reference.WikiReference)
     */
    @Override
    public int buildWikiIndex(WikiReference wikiReference) throws SearchIndexingException, XWikiException
    {
        String wikiName = wikiReference.getName();

        final XWikiContext xcontext = getXWikiContext();

        String currentWikiName = xcontext.getWiki().getName();

        if (!currentWikiName.equalsIgnoreCase(wikiName)) {
            xcontext.setDatabase(wikiName);
        }

        String hql = "select doc.space, doc.name, doc.version, doc.language from XWikiDocument as doc";
        List<Object[]> documents = xcontext.getWiki().search(hql, xcontext);

        List<DocumentReference> docsList = new ArrayList<DocumentReference>();

        for (Object[] document : documents) {

            String spaceName = (String) document[0];
            DocumentReference documentReference = new DocumentReference(wikiName, spaceName, (String) document[1]);
            docsList.add(documentReference);
        }

        indexer.indexDocuments(wikiReference, docsList);

        return docsList.size();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.BuildIndex#buildWikiSpaceIndex(org.xwiki.model.reference.SpaceReference)
     */
    @Override
    public int buildWikiSpaceIndex(SpaceReference spaceReference) throws SearchIndexingException, XWikiException
    {
        logger.info("Indexing space [" + spaceReference.getName() + "]");

        final XWikiContext xcontext = getXWikiContext();

        WikiReference wikiReference = (WikiReference) spaceReference.getParent();

        String currentWikiName = xcontext.getWiki().getName();

        if (!currentWikiName.equalsIgnoreCase(wikiReference.getName())) {
            xcontext.setDatabase(wikiReference.getName());
        }

        String hql =
            "select doc.space, doc.name, doc.version, doc.language from XWikiDocument as doc where doc.space='"
                + spaceReference.getName() + "'";
        List<Object[]> documents = xcontext.getWiki().search(hql, xcontext);

        List<DocumentReference> docsList = new ArrayList<DocumentReference>();

        for (Object[] document : documents) {

            String spaceName = (String) document[0];
            String language = (String) document[3];

            DocumentReference documentReference =
                new DocumentReference(wikiReference.getName(), spaceName, (String) document[1], language);
            if (documentAccessBridge.exists(documentReference)) {
                docsList.add(documentReference);
            }
        }

        indexer.indexDocuments(spaceReference, docsList);

        return docsList.size();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.BuildIndex#buildAttachmentIndex(org.xwiki.model.reference.AttachmentReference,
     *      org.xwiki.bridge.DocumentModelBridge)
     */
    @Override
    public int buildAttachmentIndex(AttachmentReference attachment, DocumentModelBridge doc)
    {
        indexer.indexAttachment(attachment, doc);
        return 0;
    }
}
