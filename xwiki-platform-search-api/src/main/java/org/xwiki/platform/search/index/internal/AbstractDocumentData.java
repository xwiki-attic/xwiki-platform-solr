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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.context.ExecutionContextManager;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.platform.search.index.DocumentData;
import org.xwiki.rendering.renderer.BlockRenderer;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.BaseProperty;
import com.xpn.xwiki.util.XWikiStubContextProvider;

/**
 * @version $Id$
 */
public abstract class AbstractDocumentData implements DocumentData
{   
    /**
     * DocumentAccessBridge component.
     */
    @Inject 
    protected DocumentAccessBridge documentAccessBridge;
    
    /**
     * EntityReferenceSerializer component.
     */
    @Inject
    @Named("compactwiki")
    protected EntityReferenceSerializer<String> serializer;
    
    /**
     * DocumentReferenceResolver component.
     */
    @Inject
    @Named("explicit")
    protected DocumentReferenceResolver<EntityReference> resolver;
    
    /**
     * Logger component.
     */
    @Inject
    protected Logger logger;
    
    /**
     * BlockRenderer component.
     */
    @Inject
    @Named("plain/1.0")
    protected BlockRenderer renderer;
    
    /**
     * ExecutionContextManager component.
     */
    @Inject
    protected ExecutionContextManager executionContextManager;
    
    /**
     * Execution component.
     */
    @Inject
    protected Execution execution;
    
    /**
     * XWikiStubContextProvider component.
     */
    @Inject 
    protected XWikiStubContextProvider contextProvider;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.DocumentData#getDocumentId(org.xwiki.model.reference.DocumentReference)
     */
    @Override
    public String getDocumentId(DocumentReference documentReference)
    {
        StringBuffer docId = new StringBuffer();
        docId.append(documentReference.getWikiReference().getName().toLowerCase()).append(".");
        docId.append(documentReference.getLastSpaceReference().getName().toLowerCase()).append(".");
        docId.append(documentReference.getName().toLowerCase()).append(".");
        docId.append(getLanguage(documentReference));

        return docId.toString();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.DocumentData#getAttachmentId(org.xwiki.model.reference.AttachmentReference)
     */
    @Override
    public String getAttachmentId(AttachmentReference attachmentReference)
    {
        StringBuffer attachmentId = new StringBuffer();
        attachmentId.append(getDocumentId(attachmentReference.getDocumentReference()));
        attachmentId.append(".file.");
        attachmentId.append(attachmentReference.getName().toLowerCase());
        return attachmentId.toString();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.DocumentData#getObjectId(java.lang.Object)
     */
    @Override
    public String getObjectId(DocumentReference documentReference, Object object)
    {
        BaseObject baseObject = null;
        if (object instanceof BaseObject) {
            baseObject = (BaseObject) object;
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append(getDocumentId(documentReference));
        buffer.append(".");
        buffer.append(baseObject.getReference().getName().toLowerCase());
        buffer.append(".");
        buffer.append(baseObject.getId());
        return buffer.toString();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.DocumentData#getPropertyId(java.lang.Object)
     */
    @Override
    public String getPropertyId(DocumentReference documentReference, Object property)
    {
        BaseProperty baseProperty = null;
        if (property instanceof BaseProperty) {
            baseProperty = (BaseProperty) property;
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append(getDocumentId(documentReference));
        buffer.append(".");
        buffer.append(baseProperty.getName().toLowerCase());
        buffer.append(".");
        buffer.append(baseProperty.getId());
        return buffer.toString();
    }
    
    /**
     * 
     * @param documentReference reference to the document.
     * @return String language
     */
    protected String getLanguage(DocumentReference documentReference)
    {
        String language = null;
        try {
            if (documentReference.getLocale() != null
                && !StringUtils.isEmpty(documentReference.getLocale().getDisplayLanguage())) {
                language = documentReference.getLocale().toString();
            } else if (!StringUtils.isEmpty(documentAccessBridge.getDocument(documentReference).getRealLanguage())) {
                language = documentAccessBridge.getDocument(documentReference).getRealLanguage();
            } else {
                //Multilingual and Default placeholder
                language = "en";
            }
        } catch (Exception e) {
            logger.error("Exception while fetching the language of the document - " + documentReference);
        }
        return language;
    }
    
    /**
     * 
     * @return Execution Context.
     */
    protected ExecutionContext getExecutionContext()
    {
        return this.execution.getContext();
    }

    /**
     * gets the XWikiContext.
     * 
     * @return the XWikiContext
     */
    protected XWikiContext getXWikiContext()
    {
        XWikiContext context = (XWikiContext) execution.getContext().getProperty(XWikiContext.EXECUTIONCONTEXT_KEY);
        if (context == null) {
            context = this.contextProvider.createStubContext();
            logger.info(context.toString());
            getExecutionContext().setProperty(XWikiContext.EXECUTIONCONTEXT_KEY, context);
        }
        return context;
    }
    
    /**
     * 
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.DocumentData#getObjectIdList(org.xwiki.model.reference.DocumentReference)
     */
    public List<String> getObjectIdList(DocumentReference documentReference) {
        List<String> idList = new ArrayList<String>();
        XWikiDocument xdoc;
        try {
            xdoc = getXWikiContext().getWiki().getDocument(documentReference, getXWikiContext());
            Map<DocumentReference, List<BaseObject>> map = xdoc.getXObjects();
            if (map != null) {
                for (Entry<DocumentReference, List<BaseObject>> entry : map.entrySet()) {
                    for (BaseObject object : entry.getValue()) {
                        idList.add(getObjectId(documentReference, object));
                    }
                }
            }
        } catch (XWikiException e) {
            logger.info("Error in fetching the document " + documentReference.getName());
        }

        return idList;
    }
    
    /**
     * 
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.DocumentData#getPropertyIdList(org.xwiki.model.reference.DocumentReference)
     */
    public List<String> getPropertyIdList(DocumentReference documentReference) {
        List<String> idList = new ArrayList<String>();
        XWikiDocument xdoc = null;
        try {
            xdoc = getXWikiContext().getWiki().getDocument(documentReference, getXWikiContext());
            Map<DocumentReference, List<BaseObject>> map = xdoc.getXObjects();
            if (map != null) {
                for (Entry<DocumentReference, List<BaseObject>> entry : map.entrySet()) {
                    for (BaseObject object : entry.getValue()) {
                        for (Object field : object.getFieldList()) {
                            BaseProperty<EntityReference> property = (BaseProperty<EntityReference>) field;
                            idList.add(getPropertyId(documentReference, property));
                        }
                    }
                }
            }
        } catch (XWikiException e) {
            logger.info("Error fetching the document " + documentReference.getName());
        }

        return idList;
    }
}
