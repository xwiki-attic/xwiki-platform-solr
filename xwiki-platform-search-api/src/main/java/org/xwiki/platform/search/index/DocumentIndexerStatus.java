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
public abstract class DocumentIndexerStatus
{
    protected long estimatedCompletionTime;

    protected String estimatedCompletionTimeAsString;

    protected float indexingSpeed;

    protected int indexedDocs;

    protected int totalDocCount;

    protected long elapsedTime;

    protected String elapsedTimeAsString;
    
    protected String title;
    
    protected String entityName;
    
    protected String entityType;
    

    
    

    /**
     * @return the indexedDocs
     */
    public int getIndexedDocs()
    {
        return indexedDocs;
    }

    /**
     * @param indexedDocs the indexedDocs to set
     */
    public void setIndexedDocs(int indexedDocs)
    {
        this.indexedDocs = indexedDocs;
    }

    /**
     * @return the title
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title)
    {
        this.title = title;
    }

    /**
     * @return the lastIndexedDocumentIndex
     */
    public synchronized int getLastIndexedDocumentIndex()
    {
        return indexedDocs;
    }

    /**
     * @param lastIndexedDocumentIndex the lastIndexedDocumentIndex to set
     */
    public synchronized void setLastIndexedDocumentIndex(int lastIndexedDocumentIndex)
    {
        this.indexedDocs = lastIndexedDocumentIndex;
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
    public synchronized void setIndexingSpeed(float indexingSpeed)
    {
        this.indexingSpeed = indexingSpeed;
    }

    /**
     * @return the indexingSpeed
     */
    public synchronized float getIndexingSpeed()
    {
        return indexingSpeed;
    }

    /**
     * @return the completion time stamp in milli seconds.
     */
    public synchronized long getEstimatedCompletionTime()
    {
        return this.estimatedCompletionTime;
    }

    /**
     * @return the completion time stamp.
     */
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
     * @return the elapsedTime
     */
    public long getElapsedTime()
    {
        return elapsedTime;
    }

    /**
     * @param elapsedTime the elapsedTime to set
     */
    public void setElapsedTime(long elapsedTime)
    {
        this.elapsedTime = elapsedTime;
    }

    /**
     * @param estimatedCompletionTime the estimatedCompletionTime to set
     */
    public void setEstimatedCompletionTime(long estimatedCompletionTime)
    {
        this.estimatedCompletionTime = estimatedCompletionTime;
    }

    /**
     * @param estimatedCompletionTimeAsString the estimatedCompletionTimeAsString to set
     */
    public void setEstimatedCompletionTimeAsString(String estimatedCompletionTimeAsString)
    {
        this.estimatedCompletionTimeAsString = estimatedCompletionTimeAsString;
    }

    /**
     * @return the elapsedTimeAsString
     */
    public String getElapsedTimeAsString()
    {
        return elapsedTimeAsString;
    }

    /**
     * @param elapsedTimeAsString the elapsedTimeAsString to set
     */
    public void setElapsedTimeAsString(String elapsedTimeAsString)
    {
        this.elapsedTimeAsString = elapsedTimeAsString;
    }

    /**
     * @return queue size of the indexing process.
     */
    public synchronized int getQueueSize()
    {
        return (this.totalDocCount - this.indexedDocs);
    }

    public abstract void addStepDetails(long elapsedTime1, int docsIndexed,List<?> recentlyIndexedDocs);


    protected String formatIntoHHMMSS(int secondsInput)
    {
        int hours = (int) (secondsInput / 3600), remainder = (int) (secondsInput % 3600), minutes =
            (int) remainder / 60, seconds = remainder % 60;

        return ((hours < 10 ? "0" : "") + hours + ":" + (minutes < 10 ? "0" : "") + minutes + ":"
            + (seconds < 10 ? "0" : "") + seconds);
    }

    /**
     * @return the entityName
     */
    public String getEntityName()
    {
        return entityName;
    }

    /**
     * @param entityName the entityName to set
     */
    public void setEntityName(String entityName)
    {
        this.entityName = entityName;
    }

    /**
     * @return the entityType
     */
    public String getEntityType()
    {
        return entityType;
    }

    /**
     * @param entityType the entityType to set
     */
    public void setEntityType(String entityType)
    {
        this.entityType = entityType;
    }

    /**
     * @return the recentlyIndexedDocs
     */
    public abstract List<?> getRecentlyIndexedDocs();



}
