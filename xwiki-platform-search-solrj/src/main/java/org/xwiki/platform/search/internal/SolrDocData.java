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

import static org.xwiki.platform.search.DocumentField.FULLNAME;
import static org.xwiki.platform.search.DocumentField.FULLTEXT;
import static org.xwiki.platform.search.DocumentField.ID;
import static org.xwiki.platform.search.DocumentField.LANGUAGE;
import static org.xwiki.platform.search.DocumentField.NAME;
import static org.xwiki.platform.search.DocumentField.SPACE;
import static org.xwiki.platform.search.DocumentField.TITLE;
import static org.xwiki.platform.search.DocumentField.VERSION;
import static org.xwiki.platform.search.DocumentField.WIKI;
import static org.xwiki.platform.search.DocumentField.SPACE_FACET;
import static org.xwiki.platform.search.DocumentField.TYPE;
import static org.xwiki.platform.search.DocumentField.DOC_REFERENCE;
import static org.xwiki.platform.search.DocumentField.FILENAME;
import static org.xwiki.platform.search.DocumentField.MIME_TYPE;

import org.apache.solr.common.SolrInputDocument;
import org.xwiki.bridge.DocumentModelBridge;
import org.xwiki.component.annotation.Component;

import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;




/**
 * Helper class to retrieve the SolrDocuments.
 *
 * @version $Id$
 */

@Component

public class SolrDocData
{
 
    StringBuilder retval = new StringBuilder();
    

    
    
    /**
     * It gets the Unique id of the document
     *
     * @return Gives the unique ID of the document
     */
    public String getId(DocumentModelBridge doc)
    {
        
        retval.append(doc.getDocumentReference().getName()).append(".");
        retval.append(doc.getDocumentReference().getLastSpaceReference().getName()).append(".");
        retval.append(doc.getDocumentReference().getWikiReference().getName()).append(".");
        retval.append(doc.getRealLanguage());
        return retval.toString();

    }
    
     
    /**
     * It retrieves the solrInput Document which could be added to the SolrServer. Have done partial Indexing of the
     * document.
     *
     * @param org.xwiki.model.reference.DocumentReference,org.xwiki.bridge.DocumentModelBridge,java.lang.String
     * @return the SolrInputDocument
     */
    public SolrInputDocument getSolrInputDocument(DocumentReference docref, DocumentModelBridge doc, String language,String textContent)

    {
        SolrInputDocument sdoc = new SolrInputDocument();
        if (language == null || language.equals("")) {
            language = "en";
        }
        String lang = "_" + language;
        sdoc.addField(ID, getId(doc));
        sdoc.addField(NAME + lang, docref.getName());
        sdoc.addField(TITLE + lang, doc.getTitle());
        sdoc.addField(FULLTEXT + lang, textContent);
        sdoc.addField(LANGUAGE, doc.getRealLanguage());
        sdoc.addField(VERSION + lang, doc.getVersion());
        sdoc.addField(WIKI + lang, docref.getWikiReference().getName());
        sdoc.addField(SPACE + lang, docref.getLastSpaceReference().getName());
        sdoc.addField(SPACE_FACET + lang, docref.getLastSpaceReference().getName());
        sdoc.addField(FULLNAME + lang, doc.getDocumentReference());
        sdoc.addField(TYPE, docref.getType().name());

        return sdoc;
    }
    
    /**
     * 
     * @param attachment
     * @param doc
     * @param language
     * @return
     */
    public SolrInputDocument getSolrInputAttachment(AttachmentReference attachment, DocumentModelBridge doc, String language, String textContent)
    {
        SolrInputDocument sdoc = new SolrInputDocument();
        if (language == null || language.equals("")) {
            language = "en";
        }
        
        DocumentReference docref = doc.getDocumentReference();
       
        String lang = "_" + language;
        sdoc.addField(ID, getAttachmentID(doc,attachment));
        sdoc.addField(DOC_REFERENCE + lang, attachment.getParent().getName());
        sdoc.addField(NAME + lang, attachment.getParent().getName());
        sdoc.addField(FULLTEXT + lang, textContent);
        sdoc.addField(LANGUAGE, doc.getRealLanguage());
        sdoc.addField(MIME_TYPE + lang, attachment.getType().name());
        sdoc.addField(WIKI + lang, docref.getWikiReference().getName());
        sdoc.addField(SPACE + lang, docref.getLastSpaceReference().getName());
        sdoc.addField(FILENAME + lang, attachment.getName());
        sdoc.addField(FULLNAME + lang, doc.getDocumentReference());
        sdoc.addField(TYPE, attachment.getType().name());
        return sdoc;
    }
    
/**
 * 
 * @param doc
 * @param attachment
 * @return
 */
    private String getAttachmentID(DocumentModelBridge doc,AttachmentReference attachment)
    {   

        return retval.append(getId(doc)).append(".file.").append(attachment.getName()).toString();
    }
    
    
}
