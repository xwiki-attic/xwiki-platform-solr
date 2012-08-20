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

import org.xwiki.model.reference.DocumentReference;
import org.xwiki.platform.search.index.DocumentIndexerStatus;

/**
 * Status of the document indexer.
 * 
 * @version $Id$
 */
public abstract class AbstractDocumentIndexerStatus implements  DocumentIndexerStatus
{
    /**
     * stores the estimated completion time.
     */
    protected long estimatedCompletionTime;

    /**
     * stores the estimated completion time as string.
     */
    protected String estimatedCompletionTimeAsString;

    /**
     * stores the indexing speed.
     */
    protected float indexingSpeed;

    /**
     * stores the no of indexed docs.
     */
    protected int indexedDocs;

    /**
     * stores the total document count.
     */
    protected int totalDocCount;

    /**
     * stores the elapsed time in indexing.
     */
    protected long elapsedTime;

    /**
     * elapsed time in indexing as String.
     */
    protected String elapsedTimeAsString;

    /**
     * 
     */
    protected String title;

    /**
     * 
     */
    protected String entityName;

    /**
     * 
     */
    protected String entityType;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.DocumentIndexerStatus#getIndexedDocs()
     */
    @Override
    public int getIndexedDocs()
    {
        return indexedDocs;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.DocumentIndexerStatus#setIndexedDocs(int)
     */
    @Override
    public void setIndexedDocs(int indexedDocs)
    {
        this.indexedDocs = indexedDocs;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.DocumentIndexerStatus#getTitle()
     */
    @Override
    public String getTitle()
    {
        return title;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.DocumentIndexerStatus#setTitle(java.lang.String)
     */
    @Override
    public void setTitle(String title)
    {
        this.title = title;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.DocumentIndexerStatus#getLastIndexedDocumentIndex()
     */
    @Override
    public synchronized int getLastIndexedDocumentIndex()
    {
        return indexedDocs;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.DocumentIndexerStatus#setLastIndexedDocumentIndex(int)
     */
    @Override
    public synchronized void setLastIndexedDocumentIndex(int lastIndexedDocumentIndex)
    {
        this.indexedDocs = lastIndexedDocumentIndex;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.DocumentIndexerStatus#getTotalDocCount()
     */
    @Override
    public synchronized int getTotalDocCount()
    {
        return totalDocCount;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.DocumentIndexerStatus#setTotalDocCount(int)
     */
    @Override
    public synchronized void setTotalDocCount(int totalDocCount)
    {
        this.totalDocCount = totalDocCount;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.DocumentIndexerStatus#setIndexingSpeed(float)
     */
    @Override
    public synchronized void setIndexingSpeed(float indexingSpeed)
    {
        this.indexingSpeed = indexingSpeed;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.DocumentIndexerStatus#getIndexingSpeed()
     */
    @Override
    public synchronized float getIndexingSpeed()
    {
        return indexingSpeed;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.DocumentIndexerStatus#getEstimatedCompletionTime()
     */
    @Override
    public synchronized long getEstimatedCompletionTime()
    {
        return this.estimatedCompletionTime;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.DocumentIndexerStatus#getEstimatedCompletionTimeAsString()
     */
    @Override
    public synchronized String getEstimatedCompletionTimeAsString()
    {

        return this.estimatedCompletionTimeAsString;
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
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.DocumentIndexerStatus#getElapsedTime()
     */
    @Override
    public long getElapsedTime()
    {
        return elapsedTime;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.DocumentIndexerStatus#setElapsedTime(long)
     */
    @Override
    public void setElapsedTime(long elapsedTime)
    {
        this.elapsedTime = elapsedTime;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.DocumentIndexerStatus#setEstimatedCompletionTime(long)
     */
    @Override
    public void setEstimatedCompletionTime(long estimatedCompletionTime)
    {
        this.estimatedCompletionTime = estimatedCompletionTime;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.DocumentIndexerStatus#setEstimatedCompletionTimeAsString(java.lang.String)
     */
    @Override
    public void setEstimatedCompletionTimeAsString(String estimatedCompletionTimeAsString)
    {
        this.estimatedCompletionTimeAsString = estimatedCompletionTimeAsString;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.DocumentIndexerStatus#getElapsedTimeAsString()
     */
    @Override
    public String getElapsedTimeAsString()
    {
        return elapsedTimeAsString;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.DocumentIndexerStatus#setElapsedTimeAsString(java.lang.String)
     */
    @Override
    public void setElapsedTimeAsString(String elapsedTimeAsString)
    {
        this.elapsedTimeAsString = elapsedTimeAsString;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.DocumentIndexerStatus#getQueueSize()
     */
    @Override
    public synchronized int getQueueSize()
    {
        return (this.totalDocCount - this.indexedDocs);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.DocumentIndexerStatus#addStepDetails(long, int, java.util.List)
     */
    @Override
    public abstract void addStepDetails(long elapsedTime1, int docsIndexed, List< ? > recentlyIndexedDocs);

    /**
     * @param secondsInput time in seconds.
     * @return String
     */
    protected String formatIntoHHMMSS(int secondsInput)
    {
        int hours = (int) (secondsInput / 3600);
        int remainder = (int) (secondsInput % 3600);
        int minutes = (int) remainder / 60;
        int seconds = remainder % 60;

        String zero = "0";
        String colon = ":";
        String empty = "";
        return ((hours < 10 ? zero : empty) + hours + colon + (minutes < 10 ? zero : empty) + minutes + colon
            + (seconds < 10 ? zero : empty) + seconds);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.DocumentIndexerStatus#getEntityName()
     */
    @Override
    public String getEntityName()
    {
        return entityName;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.DocumentIndexerStatus#setEntityName(java.lang.String)
     */
    @Override
    public void setEntityName(String entityName)
    {
        this.entityName = entityName;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.DocumentIndexerStatus#getEntityType()
     */
    @Override
    public String getEntityType()
    {
        return entityType;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.DocumentIndexerStatus#setEntityType(java.lang.String)
     */
    @Override
    public void setEntityType(String entityType)
    {
        this.entityType = entityType;
    }


}
