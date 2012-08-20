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

/**
 * @version $Id$
 */
public interface DocumentIndexerStatus
{

    /**
     * @return the indexedDocs
     */
    int getIndexedDocs();

    /**
     * @param indexedDocs the indexedDocs to set
     */
    void setIndexedDocs(int indexedDocs);

    /**
     * @return the title
     */
    String getTitle();

    /**
     * @param title the title to set
     */
    void setTitle(String title);

    /**
     * @return the lastIndexedDocumentIndex
     */
    int getLastIndexedDocumentIndex();

    /**
     * @param lastIndexedDocumentIndex the lastIndexedDocumentIndex to set
     */
    void setLastIndexedDocumentIndex(int lastIndexedDocumentIndex);

    /**
     * @return the totalDocCount
     */
    int getTotalDocCount();

    /**
     * @param totalDocCount the totalDocCount to set
     */
    void setTotalDocCount(int totalDocCount);

    /**
     * @param indexingSpeed the indexingSpeed to set
     */
    void setIndexingSpeed(float indexingSpeed);

    /**
     * @return the indexingSpeed
     */
    float getIndexingSpeed();

    /**
     * @return the completion time stamp in milli seconds.
     */
    long getEstimatedCompletionTime();

    /**
     * @return the completion time stamp.
     */
    String getEstimatedCompletionTimeAsString();

    /**
     * @return the elapsedTime
     */
    long getElapsedTime();

    /**
     * @param elapsedTime the elapsedTime to set
     */
    void setElapsedTime(long elapsedTime);

    /**
     * @param estimatedCompletionTime the estimatedCompletionTime to set
     */
    void setEstimatedCompletionTime(long estimatedCompletionTime);

    /**
     * @param estimatedCompletionTimeAsString the estimatedCompletionTimeAsString to set
     */
    void setEstimatedCompletionTimeAsString(String estimatedCompletionTimeAsString);

    /**
     * @return the elapsedTimeAsString
     */
    String getElapsedTimeAsString();

    /**
     * @param elapsedTimeAsString the elapsedTimeAsString to set
     */
    void setElapsedTimeAsString(String elapsedTimeAsString);

    /**
     * @return queue size of the indexing process.
     */
    int getQueueSize();

    /**
     * @param elapsedTime1 .
     * @param docsIndexed no of document indexed
     * @param recentlyIndexedDocs list of recently indexed docs
     */
    void addStepDetails(long elapsedTime1, int docsIndexed, List< ? > recentlyIndexedDocs);

    /**
     * @return the entityName
     */
    String getEntityName();

    /**
     * @param entityName the entityName to set
     */
    void setEntityName(String entityName);

    /**
     * @return the entityType
     */
    String getEntityType();

    /**
     * @param entityType the entityType to set
     */
    void setEntityType(String entityType);

}
