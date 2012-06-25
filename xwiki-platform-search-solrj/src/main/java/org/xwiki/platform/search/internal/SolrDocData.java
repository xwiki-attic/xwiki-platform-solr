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


import javax.inject.Inject;
import org.apache.solr.common.SolrInputDocument;
import org.xwiki.bridge.DocumentModelBridge;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.platform.search.IndexFields;
import org.slf4j.Logger;
import com.xpn.xwiki.XWikiContext;
import org.xwiki.context.Execution;
import javax.inject.Inject;
import org.xwiki.model.reference.EntityReference;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;

/**
 * Helper class to retrieve the SolrDocuments.
 *
 * @version $Id$
 */
public class SolrDocData
{
    @Inject
   protected Execution execution;
    
    /**
     * It gets the Unique id of the document
     * 
     * @return Gives the unique ID of the document
     */
    public String getId(DocumentModelBridge doc)
    {
            StringBuilder retval = new StringBuilder();
            retval.append(doc.getDocumentReference());
            retval.append(".");
            retval.append(doc.getRealLanguage());
            return retval.toString();
        
    }
       
        
    /*
     * Creating the SolrInputDocuments
     */
   /* public SolrInputDocument getSolrInputDocument(EntityReference entityref, Object object, String language)

    {
        SolrInputDocument doc=null; 
              
        if(object instanceof DocumentModelBridge)
        {    
          
           doc= getSolrDocument((DocumentReference)entityref ,(DocumentModelBridge)object, language);

        }

        return doc;

    }*/

    /**
        * It retrieves the solrInput Document which could be added to the SolrServer. Have done 
        * partial Indexing of the document.
        * 
        * @param org.xwiki.model.reference.DocumentReference,org.xwiki.bridge.DocumentModelBridge,java.lang.String
        * @return the SolrInputDocument
        */
    public SolrInputDocument getSolrInputDocument(DocumentReference docref, DocumentModelBridge doc, String language)

    {
        SolrInputDocument sdoc = new SolrInputDocument();
        if (language == null || language.equals("")) {
            language = "en";
        }

        try{
            String lang = "_" + language;
            sdoc.addField("id",getId(doc));
            sdoc.addField(IndexFields.DOCUMENT_NAME + lang, docref.getName());
            sdoc.addField(IndexFields.DOCUMENT_TITLE + lang, doc.getTitle());
            sdoc.addField(IndexFields.FULLTEXT + lang, doc.getContent());
            sdoc.addField(IndexFields.DOCUMENT_LANGUAGE + lang,doc.getRealLanguage());
            sdoc.addField(IndexFields.DOCUMENT_VERSION +lang, doc.getVersion());
            sdoc.addField(IndexFields.DOCUMENT_WIKI + lang, docref.getWikiReference());
            sdoc.addField(IndexFields.DOCUMENT_FULLNAME + lang, doc.getDocumentReference());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        
        return sdoc;
    }
/**
 * 
 * @return the XWikiContext
 */
    protected XWikiContext getXWikiContext()
    {
        return (XWikiContext) this.execution.getContext().getProperty(XWikiContext.EXECUTIONCONTEXT_KEY);
    }


}
