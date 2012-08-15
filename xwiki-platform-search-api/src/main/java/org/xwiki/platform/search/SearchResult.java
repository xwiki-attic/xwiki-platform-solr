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
    /**
     * id of the document.
     */
    private String id;
    
    /**
     * reference of the Document.
     */
    private DocumentReference reference;
    
     /**
     * title of the document.
     */
    private String title;
    
    /**
     *  score of the document.
     */
    private float score;
    
    /**
     * content of the document.
     */
    private String content;
    
    /**
     * Name of the wiki.
     */
    private String wikiName;
    
    /**
     * Name of the space.
     */
    private String spaceName;
    
    /**
     * Name of the page.
     */
    private String pageName;
    
    /**
     * Highlighted content in the returned document.
     */
    private String highlightText;
    
    /**
     * language of the document.
     */
    private String language;
    
    /**
     * url of the attachment.
     */
    private String url;
    
    /**
     * type of the attachment.
     */
    private String type;
    
    /**
     * filename of the attachment.
     */
    private String fileName;
    
    /**
     * mimtype of teh attachment.
     */
    private String mimeType;
    
    /**
     * name of the Object.
     */
    private String objectName;
    
    /**
     * Name of the property.
     */
    private String propertyName;
    
    /**
     * Value of the property.
     */
    private String propertyValue;
    
    /**
     * 
     * @param id of the document
     * @param wikiName  name of the Wiki
     * @param spaceName name of the space
     * @param pageName name of the page.
     * @param language language of the page.
     */
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
     
    /**
     * 
     * @return String the url.
     */
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
     * @param pageName Name of the page
     */
    public void setPageName(String pageName)
    {
        this.pageName = pageName;
    }

    /**
     * @param url of the attachment
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
    
    /**
     * 
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
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

    /**
     * @return the propertyValue
     */
    public String getPropertyValue()
    {
        return propertyValue;
    }

    /**
     * @param propertyValue the propertyValue to set
     */
    public void setPropertyValue(String propertyValue)
    {
        this.propertyValue = propertyValue;
    }

    
}
