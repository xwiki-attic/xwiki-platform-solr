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

import static org.xwiki.platform.search.DocumentField.ATTACHMENT_CONTENT;
import static org.xwiki.platform.search.DocumentField.AUTHOR;
import static org.xwiki.platform.search.DocumentField.COMMENT;
import static org.xwiki.platform.search.DocumentField.CREATIONDATE;
import static org.xwiki.platform.search.DocumentField.CREATOR;
import static org.xwiki.platform.search.DocumentField.DATE;
import static org.xwiki.platform.search.DocumentField.DOCUMENT_CONTENT;
import static org.xwiki.platform.search.DocumentField.FILENAME;
import static org.xwiki.platform.search.DocumentField.FULLNAME;
import static org.xwiki.platform.search.DocumentField.ID;
import static org.xwiki.platform.search.DocumentField.LANGUAGE;
import static org.xwiki.platform.search.DocumentField.MIME_TYPE;
import static org.xwiki.platform.search.DocumentField.NAME;
import static org.xwiki.platform.search.DocumentField.OBJECT;
import static org.xwiki.platform.search.DocumentField.OBJECT_CONTENT;
import static org.xwiki.platform.search.DocumentField.SPACE;
import static org.xwiki.platform.search.DocumentField.TITLE;
import static org.xwiki.platform.search.DocumentField.TYPE;
import static org.xwiki.platform.search.DocumentField.VERSION;
import static org.xwiki.platform.search.DocumentField.WIKI;
import static org.xwiki.platform.search.DocumentField.PROPERTY_NAME;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrInputDocument;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;

import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.BaseProperty;

/**
 * @version $Id$
 */
@Component
@Named(SolrjDocumentData.HINT)
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class SolrjDocumentData extends AbstractDocumentData
{
    /**
     * solrj-document-data HINT.
     */
    public static final String HINT = "solrj-document-data";

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.DocumentData#getInputDocuments(org.xwiki.model.reference.DocumentReference)
     */
    @Override
    public SolrInputDocument getInputDocument(DocumentReference documentReference)
    {
        SolrInputDocument sdoc = new SolrInputDocument();
        try {

            XWikiDocument xdoc = getXWikiContext().getWiki().getDocument(documentReference, getXWikiContext());

            String doclang = "";
            if (documentReference.getLocale() != null 
                && !StringUtils.isEmpty(documentReference.getLocale().toString())) {
                doclang = documentReference.getLocale().toString();
            }

            XWikiDocument tdoc = xdoc.getTranslatedDocument(doclang, getXWikiContext());

            String language = getLanguage(documentReference);
            sdoc.addField(ID, getDocumentId(documentReference));
            addDocumentReferenceFields(documentReference, sdoc, language);
            sdoc.addField(TYPE, documentReference.getType().name());
            sdoc.addField(FULLNAME + "_" + language, serializer.serialize(documentReference));

            // Replace xwiki document with document model bridge once the bug is fixed.

            // Convert the XWiki syntax of document to plain text.
            WikiPrinter printer = new DefaultWikiPrinter();
            renderer.render(tdoc.getXDOM(), printer);

            sdoc.addField(TITLE + "_" + language, tdoc.getTitle());
            sdoc.addField(DOCUMENT_CONTENT + "_" + language, printer.toString());
            sdoc.addField(VERSION, tdoc.getVersion());

            sdoc.addField(AUTHOR, xdoc.getAuthorReference().getName());
            sdoc.addField(CREATOR, xdoc.getCreatorReference().getName());
            sdoc.addField(CREATIONDATE, xdoc.getCreationDate());
            sdoc.addField(DATE, xdoc.getContentUpdateDate());

            // Index Comments

            List<BaseObject> comments = xdoc.getComments();
            if (comments != null) {
                StringBuffer buffer = new StringBuffer();
                for (BaseObject comment : xdoc.getComments()) {
                    logger.info(comment.toXMLString());
                    String commentString = comment.getStringValue("comment");
                    String author = comment.getStringValue("author");
                    buffer.append(commentString + " by " + author + "  ");
                }
                sdoc.addField(COMMENT + "_" + language, buffer.toString());
            }

        } catch (Exception e) {
            logger.error("Exception during fetching input document for " + documentReference.getName());
        }
        return sdoc;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.DocumentData#getInputAttachments(org.xwiki.model.reference.AttachmentReference)
     */
    @Override
    public List<SolrInputDocument> getInputAttachments(DocumentReference documentReference)
    {
        List<SolrInputDocument> docs = null;
        try {
            List<AttachmentReference> attachmentReferences =
                documentAccessBridge.getAttachmentReferences(documentReference);
            if (attachmentReferences != null) {
                docs = new ArrayList<SolrInputDocument>();
                for (AttachmentReference attachmentReference : attachmentReferences) {
                    docs.add(getSolrInputAttachment(attachmentReference));
                }

            }

        } catch (Exception e) {
            logger.error("Exception while fetching input document for " + documentReference.getName());
        }

        return docs;
    }
    
    /**
     * 
     * @param attachmentReference reference to Attachment.
     * @return SolrInput Document
     */
    public SolrInputDocument getSolrInputAttachment(AttachmentReference attachmentReference)
    {
        SolrInputDocument sdoc = new SolrInputDocument();

        DocumentReference documentReference = attachmentReference.getDocumentReference();

        String lang = "_" + getLanguage(documentReference);
        sdoc.addField(ID, getAttachmentId(attachmentReference));
        sdoc.addField(ATTACHMENT_CONTENT + lang, getContentAsText(attachmentReference));
        sdoc.addField(MIME_TYPE, getMimeType(attachmentReference));
        sdoc.addField(FILENAME + lang, attachmentReference.getName());
        sdoc.addField(FULLNAME + lang, serializer.serialize(attachmentReference));
        sdoc.addField(TYPE, attachmentReference.getType().name());
        addDocumentReferenceFields(documentReference, sdoc, getLanguage(documentReference));

        // XWiki Deprecated code.

        return sdoc;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.DocumentData#getInputObjects(org.xwiki.model.reference.DocumentReference)
     */
    @Override
    public List<SolrInputDocument> getInputObjects(DocumentReference documentReference)
    {
        List<SolrInputDocument> inputObjects = Collections.EMPTY_LIST;
        // Index objects
        try {
            XWikiDocument xdoc = getXWikiContext().getWiki().getDocument(documentReference, getXWikiContext());
            Map<DocumentReference, List<BaseObject>> map = xdoc.getXObjects();
            inputObjects = new ArrayList<SolrInputDocument>();
            List<String> blackListedPropeties = new ArrayList<String>(Arrays.asList("password", "validkey"));
            if (map != null) {
                for (Entry<DocumentReference, List<BaseObject>> entry : map.entrySet()) {
                    DocumentReference docRef = entry.getKey();
                    List<BaseObject> list = entry.getValue();
                    for (BaseObject object : list) {
                        SolrInputDocument sdoc = new SolrInputDocument();
                        StringBuffer buffer = new StringBuffer();
                        for (Object field : object.getFieldList()) {
                            BaseProperty<EntityReference> fieldStr = (BaseProperty<EntityReference>) field;
                            if (!blackListedPropeties.contains(fieldStr.getName())) {
                                buffer.append(fieldStr.getName() + ":" + fieldStr.getValue() + "  ");
                            }
                        }
                        sdoc.addField(ID, getObjectId(documentReference, object));
                        addDocumentReferenceFields(documentReference, sdoc, getLanguage(documentReference));
                        sdoc.addField(OBJECT, docRef.getLastSpaceReference().getName() + "." + docRef.getName());
                        sdoc.addField(OBJECT_CONTENT + "_" + getLanguage(documentReference), buffer.toString());
                        sdoc.addField(TYPE, "OBJECT");
                        inputObjects.add(sdoc);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Exception in retrieving the objects from document. " + documentReference.getName(), e);
        }
        return inputObjects;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.DocumentData#getInputProperties(org.xwiki.model.reference.DocumentReference)
     */
    @Override
    public List<SolrInputDocument> getInputProperties(DocumentReference documentReference)
    {
        List<SolrInputDocument> inputProperties = Collections.EMPTY_LIST;
        // Index objects
        try {
            XWikiDocument xdoc = getXWikiContext().getWiki().getDocument(documentReference, getXWikiContext());
            Map<DocumentReference, List<BaseObject>> map = xdoc.getXObjects();
            inputProperties = new ArrayList<SolrInputDocument>();
            List<String> blackListedPropeties = new ArrayList<String>(Arrays.asList("password", "validkey"));
            if (map != null) {
                for (Entry<DocumentReference, List<BaseObject>> entry : map.entrySet()) {
                    DocumentReference docRef = entry.getKey();
                    List<BaseObject> list = entry.getValue();
                    for (BaseObject object : list) {

                        for (Object field : object.getFieldList()) {
                            BaseProperty<EntityReference> property = (BaseProperty<EntityReference>) field;
                            if (!blackListedPropeties.contains(property.getName())) {
                                SolrInputDocument sdoc = new SolrInputDocument();
                                sdoc.addField(ID, getPropertyId(documentReference, property));
                                String propertyName =
                                    docRef.getLastSpaceReference().getName() + "." + docRef.getName() + "."
                                        + property.getName() + "_" + getLanguage(documentReference);
                                sdoc.addField(PROPERTY_NAME, propertyName);
                                sdoc.addField(propertyName, property.getValue());
                                sdoc.addField(TYPE, "PROPERTY");
                                addDocumentReferenceFields(documentReference, sdoc, getLanguage(documentReference));
                                inputProperties.add(sdoc);
                            }
                        }

                    }
                }
            }
        } catch (Exception e) {
            logger
                .error(
                    "Exception in retrieving the properties from object of the document. "
                        + documentReference.getName(), e);
        }
        return inputProperties;
    }

    /**
     * @param attachment reference to the attachment.
     * @return the ContentText
     */
    private String getContentAsText(AttachmentReference attachment)
    {
        String contentText = null;

        try {
            Tika tika = new Tika();

            Metadata metadata = new Metadata();
            metadata.set(Metadata.RESOURCE_NAME_KEY, attachment.getName());

            InputStream in = documentAccessBridge.getAttachmentContent(attachment);

            contentText = StringUtils.lowerCase(tika.parseToString(in, metadata));
        } catch (Throwable ex) {
            logger.error("Exception while retrieving attachment content for document " + attachment.getName());
        }

        return contentText;
    }
    
    /**
     * 
     * @param documentReference reference to document.
     * @param sdoc SOlr Input Document.
     * @param lang language of the document.
     */
    private void addDocumentReferenceFields(DocumentReference documentReference, SolrInputDocument sdoc, String lang)
    {

        logger.info("Language of the document [" + documentReference.getName() + "] is [" + lang + "]");
        sdoc.addField(NAME + "_" + lang, documentReference.getName());
        sdoc.addField(WIKI, documentReference.getWikiReference().getName());
        sdoc.addField(SPACE, documentReference.getLastSpaceReference().getName());
        sdoc.addField(LANGUAGE, lang);
    }
    
    /**
     * 
     * @param reference to the Attachment
     * @return mimetype.
     */
    private String getMimeType(AttachmentReference reference)
    {
        String mimetype = getXWikiContext().getEngineContext().getMimeType(reference.getName().toLowerCase());
        if (mimetype != null) {
            return mimetype;
        } else {
            return "application/octet-stream";
        }
    }
}
