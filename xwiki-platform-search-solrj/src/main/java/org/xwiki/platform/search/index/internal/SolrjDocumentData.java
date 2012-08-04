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

import static org.xwiki.platform.search.DocumentField.AUTHOR;
import static org.xwiki.platform.search.DocumentField.COMMENT;
import static org.xwiki.platform.search.DocumentField.CREATIONDATE;
import static org.xwiki.platform.search.DocumentField.CREATOR;
import static org.xwiki.platform.search.DocumentField.DATE;
import static org.xwiki.platform.search.DocumentField.DOC_REFERENCE;
import static org.xwiki.platform.search.DocumentField.FILENAME;
import static org.xwiki.platform.search.DocumentField.FULLNAME;
import static org.xwiki.platform.search.DocumentField.FULLTEXT;
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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrInputDocument;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.xwiki.bridge.DocumentModelBridge;
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
            DocumentModelBridge documentModelBridge = documentAccessBridge.getDocument(documentReference);

            // Convert the XWiki syntax of document to plain text.
            WikiPrinter printer = new DefaultWikiPrinter();
            renderer.render(documentModelBridge.getXDOM(), printer);
            String lang = "_" + getLanguage(documentReference);
            sdoc.addField(ID, getDocumentId(documentReference));
            addDocumentReferenceFields(documentReference, sdoc, getLanguage(documentReference));
            sdoc.addField(TITLE + lang, documentModelBridge.getTitle());
            sdoc.addField(FULLTEXT + lang, printer.toString());
            sdoc.addField(VERSION, documentModelBridge.getVersion());
            sdoc.addField(FULLNAME + lang, serializer.serialize(documentReference));
            sdoc.addField(TYPE, documentReference.getType().name());

            // TODO Add methods to DocumentModelBridge
            XWikiDocument xdoc = getXWikiContext().getWiki().getDocument(documentReference, getXWikiContext());
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
                sdoc.addField(COMMENT + lang, buffer.toString());
            }

        } catch (Exception e) {
            logger.error("Exception while fetching input document for " + documentReference.getName());
        }
        return sdoc;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.DocumentData#getInputAttachments(org.xwiki.model.reference.AttachmentReference)
     */
    @Override
    public List< ? > getInputAttachments(DocumentReference documentReference)
    {
        try {
            List<AttachmentReference> attachmentReferences =
                documentAccessBridge.getAttachmentReferences(documentReference);

        } catch (Exception e) {
            logger.error("Exception while fetching input document for " + documentReference.getName());
        }

        return null;
    }

    public SolrInputDocument getSolrInputAttachment(AttachmentReference attachmentReference, String textContent)
    {
        SolrInputDocument sdoc = new SolrInputDocument();

        DocumentReference documentReference = attachmentReference.getDocumentReference();

        String lang = "_" + getLanguage(documentReference);
        sdoc.addField(ID, getAttachmentId(attachmentReference));
        sdoc.addField(DOC_REFERENCE + lang, documentReference.getName());
        sdoc.addField(FULLTEXT + lang, getContentAsText(attachmentReference));
        sdoc.addField(LANGUAGE, getLanguage(documentReference));
        sdoc.addField(MIME_TYPE, attachmentReference.getType().name());
        sdoc.addField(WIKI, documentReference.getWikiReference().getName());
        sdoc.addField(SPACE, documentReference.getLastSpaceReference().getName());
        sdoc.addField(FILENAME + lang, attachmentReference.getName());
        sdoc.addField(FULLNAME + lang, serializer.serialize(attachmentReference));
        sdoc.addField(TYPE, attachmentReference.getType().name());

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
        List<SolrInputDocument> inputObjects = null;
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
        List<SolrInputDocument> inputProperties = null;
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
                                sdoc.addField(docRef.getLastSpaceReference().getName() + "." + docRef.getName() + "."
                                    + property.getName() + "_" + getLanguage(documentReference), property.getValue());
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
     * @param attachment
     * @return
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

    private void addDocumentReferenceFields(DocumentReference documentReference, SolrInputDocument sdoc, String lang)
    {

        logger.info("Language of the document [" + documentReference.getName() + "] is [" + lang + "]");
        sdoc.addField(NAME + "_" + lang, documentReference.getName());
        sdoc.addField(WIKI, documentReference.getWikiReference().getName());
        sdoc.addField(SPACE, documentReference.getLastSpaceReference().getName());
        sdoc.addField(LANGUAGE, lang);
    }
}
