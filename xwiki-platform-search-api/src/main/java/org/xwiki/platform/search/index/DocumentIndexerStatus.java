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

import org.xwiki.model.reference.DocumentReference;

/**
 * Status of the document indexer.
 *
 * @version $Id$
 */
public class DocumentIndexerStatus
{
    private long estimatedCompletionTime;

    private double indexingSpeed;

    private int lastIndexedDocumentIndex;

    private int totalDocCount;

    private long elapsedTime;

    /**
     * @return the lastIndexedDocumentIndex
     */
    public synchronized int getLastIndexedDocumentIndex()
    {
        return lastIndexedDocumentIndex;
    }

    /**
     * @param lastIndexedDocumentIndex the lastIndexedDocumentIndex to set
     */
    public synchronized void setLastIndexedDocumentIndex(int lastIndexedDocumentIndex)
    {
        this.lastIndexedDocumentIndex = lastIndexedDocumentIndex;
    }

    /**
     * @return the totalDocCount
     */
    public synchronized int getTotalDocCount()
    {
        return totalDocCount;
    }

    /**
     * @param totalDocCount the totalDocCount to set
     */
    public synchronized void setTotalDocCount(int totalDocCount)
    {
        this.totalDocCount = totalDocCount;
    }

    /**
     * @param indexingSpeed the indexingSpeed to set
     */
    public synchronized void setIndexingSpeed(double indexingSpeed)
    {
        this.indexingSpeed = indexingSpeed;
    }

    /**
     * @return the indexingSpeed
     */
    public synchronized double getIndexingSpeed()
    {
        return indexingSpeed;
    }

    /**
     * @return the completion time stamp in milli seconds.
     */
    public synchronized long getEstimatedCompletionTime()
    {
        return this.elapsedTime;
    }

    /**
     * @param count number of documents
     * @return the list of indexed documents.
     */
    List<DocumentReference> getLastIndexedDocuments(int count)
    {
        return null;
    }

    /**
     * @return queue size of the indexing process.
     */
    public synchronized int getQueueSize()
    {
        return (this.totalDocCount - this.lastIndexedDocumentIndex);
    }

    public synchronized void addStepDetails(long elapsedTime, int docsIndexed)
    {
        this.elapsedTime += elapsedTime;
        this.lastIndexedDocumentIndex += docsIndexed;
        System.out.println("Added details..");
    }

    public String toString()
    {
        String toStr =
            "ElapsedTime:" + this.elapsedTime + ", LastIndexedDoc:" + lastIndexedDocumentIndex + ", TotalDoc:"
                + totalDocCount;

        return toStr;
    }
}
