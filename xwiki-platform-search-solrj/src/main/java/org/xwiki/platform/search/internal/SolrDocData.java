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

package org.xwiki.platform.search.internal;

import java.util.Date;
import java.util.List;

import org.xwiki.model.reference.EntityReference;
import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.xwiki.context.Execution;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.platform.search.IndexFields;
import org.xwiki.rendering.syntax.Syntax;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.BaseProperty;
import com.xpn.xwiki.objects.classes.PasswordClass;
import com.xpn.xwiki.web.Utils;
import org.xwiki.context.Execution;
import javax.inject.Inject;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;

public class SolrDocData
{
    private Object wikiobject;
    
    private String docid;
       
    private EntityReference Docref;
    
    private String fulltext;

    private String language;
    
    
    
    @Inject
   protected Execution execution;
    
    
    /*
     * the object of the SolrDocData is created and added to the queue or any data structure(not decided yet) while indexing. 
     * The XWikiDocument or XWikiAttachment and corresponding document reference is passed in the constructor.
     * Later the SolrDocData is retrieved and the getSolrdoc method is called to get the solrinputdocument.
     */
    public SolrDocData(Object obj, EntityReference entityReference)
    {
        wikiobject = obj;
        setEntityReference(entityReference);
       
        
    }
             
    
    /**
     * It gets the Unique id of the document: Is it needed??
     */
    public String getId()
    {
        StringBuilder retval = new StringBuilder();

        //should add here..

        return retval.toString();
    }
    
    
    /*
     * get and set methods fore entity reference member variable as XWikiDoc or 
     * XWikiAttachment could be converted to SolrDoc
     */
    
    public EntityReference getEntityReference()
    {
        return this.Docref;
    }
    
   public void setEntityReference(EntityReference entityref)
   {
       this.Docref=entityref;
   }

   
    public String getDocumentName()
    {
        EntityReference extract = getEntityReference().extractReference(EntityType.DOCUMENT);

        return extract != null ? extract.getName() : null;
    }

   
    public String getDocumentSpace()
    {
        EntityReference extract = getEntityReference().extractReference(EntityType.SPACE);

        return extract != null ? extract.getName() : null;
       
    }

  
    public String getWiki()
    {   
        EntityReference extract = getEntityReference().extractReference(EntityType.WIKI);

        return extract != null ? extract.getName() : null;
        
    }
    
    /*
     * get and set methods for language of the docs or attachments
     */
    public String getLanguage()
    {
        return this.language;
    }

    public void setLanguage(String lang)
    {
        if (!StringUtils.isEmpty(lang)) {
            this.language = lang;
        } else {
            this.language = "default";
        }
        
    }   

    /*
     * Extracting the full text of the document including the document's object
     */
    
    public String getFullText(XWikiDocument doc)       
    {
        StringBuilder sb = new StringBuilder();
        sb.append(" ");
        sb.append(StringUtils.lowerCase(doc.getContent()));
        sb.append(" ");

        getObjectFullText(sb, doc);
        return sb.toString();
    }
    
    /*
     * Add to the string builder the value of title,category,content and extract of XWiki.ArticleClass
     */
    
    private void getObjectFullText(StringBuilder sb,XWikiDocument doc)
    {
        for (List<BaseObject> objects : doc.getXObjects().values()) {
            for (BaseObject obj : objects) {
                extractObjectContent(sb, obj);
            }
        } 
    }
    
    private void extractObjectContent(StringBuilder contentText, BaseObject baseObject)
    {
        if (baseObject != null) {
            String[] propertyNames = baseObject.getPropertyNames();
            for (String propertyName : propertyNames) {
                getObjectContentAsText(contentText, baseObject, propertyName);
                contentText.append(" ");
            }
        }
    }
    
    
    private void getObjectContentAsText(StringBuilder contentText, BaseObject baseObject, String property)
    {
        BaseProperty baseProperty = (BaseProperty) baseObject.getField(property);
        if (baseProperty != null && baseProperty.getValue() != null) {
            if (!(baseObject.getXClass(getXWikiContext()).getField(property) instanceof PasswordClass)) {
                contentText.append(StringUtils.lowerCase(baseProperty.getValue().toString()));
            }
        }
    }
    
    
    
    
    /*
     * Creating the SolrInputDocuments
     */
    public SolrInputDocument getSolrDocument()
    
    {
        SolrInputDocument sdoc = new SolrInputDocument();
        
        if(wikiobject instanceof XWikiDocument)
        {
             sdoc= getSolrDoc((XWikiDocument)wikiobject);
           
        }
        
        if(wikiobject instanceof XWikiAttachment)
        {
            sdoc= getSolrAttachment((XWikiAttachment)wikiobject);
            return sdoc;
        }
        
        return sdoc;
       
    }
    
    
    /*
     * creating solrdoc for XWikiDocuments
     */
    private SolrInputDocument getSolrDoc(XWikiDocument doc)
    
    {
        SolrInputDocument sdoc = new SolrInputDocument();
        
        try{ 
            XWikiDocument tdoc = doc.getTranslatedDocument(language, getXWikiContext());
            DocumentReference docRef = doc.getDocumentReference();
            String lang = "_" + language;
            sdoc.addField(IndexFields.DOCUMENT_NAME + lang, getDocumentName());
            sdoc.addField(IndexFields.DOCUMENT_TITLE + lang, tdoc.getTitle());
            sdoc.addField(IndexFields.DOCUMENT_SPACE + lang, getDocumentSpace());
            sdoc.addField(IndexFields.FULLTEXT + lang, getFullText(tdoc));
            sdoc.addField(IndexFields.DOCUMENT_LANGUAGE, getLanguage());
            sdoc.addField(IndexFields.DOCUMENT_ID, tdoc.getId());
            sdoc.addField(IndexFields.DOCUMENT_AUTHOR, tdoc.getAuthor());
            sdoc.addField(IndexFields.DOCUMENT_CREATIONDATE, tdoc.getCreationDate());
            sdoc.addField(IndexFields.DOCUMENT_DATE, tdoc.getDate());
            sdoc.addField(IndexFields.DOCUMENT_VERSION, tdoc.getVersion());
            sdoc.addField(IndexFields.DOCUMENT_FULLNAME, tdoc.getFullName());
            sdoc.addField(IndexFields.DOCUMENT_WIKI, tdoc.getWikiName());          
        }
        catch(Exception e)
        {
            e.printStackTrace(); 
        }

        
        return sdoc;
    }
    
    
    
    /*
     * creating solrdoc for XWikiAttachments-----> yet to implement
     */
    private SolrInputDocument getSolrAttachment(XWikiAttachment attachment)
    {
       SolrInputDocument sdoc = new SolrInputDocument();
       try{ 
           XWikiDocument doc = attachment.getDoc();
           XWikiDocument tdoc = doc.getTranslatedDocument(language, getXWikiContext());
           String lang = "_" + language;
           sdoc.addField(IndexFields.DOCUMENT_NAME + lang, getDocumentName());
           sdoc.addField(IndexFields.DOCUMENT_TITLE + lang, tdoc.getTitle());
           sdoc.addField(IndexFields.DOCUMENT_SPACE + lang, getDocumentSpace());
           sdoc.addField(IndexFields.DOCUMENT_LANGUAGE + lang, getLanguage());
           sdoc.addField(IndexFields.DOCUMENT_ID + lang , tdoc.getId());
           sdoc.addField(IndexFields.DOCUMENT_AUTHOR + lang , tdoc.getAuthor());
           sdoc.addField(IndexFields.DOCUMENT_DATE + lang , tdoc.getDate());
           sdoc.addField(IndexFields.DOCUMENT_VERSION + lang , tdoc.getVersion());
           sdoc.addField(IndexFields.DOCUMENT_FULLNAME + lang , tdoc.getFullName());
           sdoc.addField(IndexFields.DOCUMENT_WIKI + lang , tdoc.getWikiName());  
           sdoc.addField(IndexFields.FILENAME + lang , attachment.getFilename()); 
           sdoc.addField(IndexFields.MIME_TYPE + lang , attachment.getMimeType(getXWikiContext()));
           sdoc.addField(IndexFields.DOC_REFERENCE + lang , attachment.getDoc());        
           
       }
       catch(Exception e)
       {
           e.printStackTrace();   
       }
        
        return sdoc;
    }
    
    protected XWikiContext getXWikiContext()
    {
        return (XWikiContext) this.execution.getContext().getProperty(XWikiContext.EXECUTIONCONTEXT_KEY);
    }

    
}
