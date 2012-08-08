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
package org.xwiki.platform.search;

import org.xwiki.model.reference.DocumentReference;

/**
 * @version $Id$
 */
public class SearchResult
{

    private String id;

    private DocumentReference reference;

    private String title;

    private float score;

    private String content;

    private String wikiName;

    private String spaceName;

    private String pageName;

    private String highlightText;

    private String language;

    private String url;

    private String type;

    private String fileName;

    private String mimeType;

    private String objectName;

    private String propertyName;

    public SearchResult(String id, String wikiName, String spaceName, String pageName, String language)
    {
        this.reference = new DocumentReference(wikiName, spaceName, pageName, language);
        this.wikiName = wikiName;
        this.spaceName = spaceName;
        this.pageName = pageName;
        this.id = id;
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
     * @return the score
     */
    public float getScore()
    {
        return score;
    }

    /**
     * @param score the score to set
     */
    public void setScore(float score)
    {
        this.score = score;
    }

    /**
     * @return the content
     */
    public String getContent()
    {
        return content;
    }

    /**
     * @param content the content to set
     */
    public void setContent(String content)
    {
        this.content = content;
    }

    /**
     * @return the reference
     */
    public DocumentReference getReference()
    {

        return reference;
    }

    public String getDocumentLink()
    {
        String link = "";
        if (this.wikiName != null) {
            link += this.wikiName + ":";
        }

        if (this.spaceName != null) {
            link += this.spaceName + ".";
        }
        return "[[" + link + this.pageName + "]]";
    }

    /**
     * @return the wikiName
     */
    public String getWikiName()
    {
        return wikiName;
    }

    /**
     * @return the spaceName
     */
    public String getSpaceName()
    {
        return spaceName;
    }

    /**
     * @return the pageName
     */
    public String getPageName()
    {
        return pageName;
    }

    /**
     * @return the id
     */
    public String getId()
    {
        return id;
    }

    /**
     * @return the highlightText
     */
    public String getHighlightText()
    {
        return highlightText;
    }

    /**
     * @param highlightText the highlightText to set
     */
    public void setHighlightText(String highlightText)
    {
        this.highlightText = highlightText;
    }

    /**
     * @return the language
     */
    public String getLanguage()
    {
        return language;
    }

    /**
     * @param pageName
     */
    public void setPageName(String pageName)
    {
        this.pageName = pageName;
    }

    /**
     * @param url
     */
    public void setURL(String url)
    {
        this.url = url;
    }

    /**
     * @return the Url
     */
    public String getURL()
    {
        return this.url;
    }

    public String toString()
    {
        return pageName;
    }

    /**
     * @return the type
     */
    public String getType()
    {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type)
    {
        this.type = type;
    }

    /**
     * @return the fileName
     */
    public String getFileName()
    {
        return fileName;
    }

    /**
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    /**
     * @return the mimeType
     */
    public String getMimeType()
    {
        return mimeType;
    }

    /**
     * @param mimeType the mimeType to set
     */
    public void setMimeType(String mimeType)
    {
        this.mimeType = mimeType;
    }

    /**
     * @return the objectName
     */
    public String getObjectName()
    {
        return objectName;
    }

    /**
     * @param objectName the objectName to set
     */
    public void setObjectName(String objectName)
    {
        this.objectName = objectName;
    }

    /**
     * @return the propertyName
     */
    public String getPropertyName()
    {
        return propertyName;
    }

    /**
     * @param propertyName the propertyName to set
     */
    public void setPropertyName(String propertyName)
    {
        this.propertyName = propertyName;
    }

}
