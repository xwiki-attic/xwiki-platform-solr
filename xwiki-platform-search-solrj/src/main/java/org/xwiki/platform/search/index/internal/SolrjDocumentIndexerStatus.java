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

import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.xwiki.platform.search.index.DocumentIndexerStatus;

/**
 * @version $Id$
 */
public class SolrjDocumentIndexerStatus extends DocumentIndexerStatus
{
    protected List<SolrInputDocument> recentlyIndexedDocs;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.DocumentIndexerStatus#addStepDetails(long, int, java.util.List)
     */
    @Override
    public void addStepDetails(long elapsedTime1, int docsIndexed, List< ? > recentlyIndexedDocs)
    {
        synchronized (this) {
            this.elapsedTime += elapsedTime1;
            this.indexedDocs += docsIndexed;
            int docsRemaining = this.totalDocCount - this.indexedDocs;
            float timeForEachDoc = 0;

            if (this.indexedDocs > 0) {
                timeForEachDoc = (float) (this.elapsedTime / this.indexedDocs);
            }

            this.setEstimatedCompletionTime((long) (docsRemaining * timeForEachDoc));
            if (this.elapsedTime > 0) {

                int a = (this.indexedDocs * 1000);
                float speed = (a / this.elapsedTime);
                System.out.println(speed);
                this.setIndexingSpeed(speed);
            }
            this.setEstimatedCompletionTimeAsString(this.formatIntoHHMMSS((int) (this.estimatedCompletionTime / 1000)));

            this.setElapsedTimeAsString(this.formatIntoHHMMSS((int) (this.elapsedTime / 1000)));

            this.setRecentlyIndexedDocs((List<SolrInputDocument>) recentlyIndexedDocs);

        }

    }

    /**
     * @param recentlyIndexedDocs
     */
    private void setRecentlyIndexedDocs(List<SolrInputDocument> recentlyIndexedDocs)
    {
        this.recentlyIndexedDocs = recentlyIndexedDocs;

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.DocumentIndexerStatus#getRecentlyIndexedDocs()
     */
    @Override
    public List<SolrInputDocument> getRecentlyIndexedDocs()
    {
        // TODO Auto-generated method stub
        return null;
    }

}
