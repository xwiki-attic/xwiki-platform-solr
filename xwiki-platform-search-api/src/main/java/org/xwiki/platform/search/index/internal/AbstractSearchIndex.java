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

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.xwiki.bridge.DocumentModelBridge;
import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.bridge.event.DocumentDeletedEvent;
import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.bridge.event.WikiDeletedEvent;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;
import org.xwiki.platform.search.index.SearchIndex;

import com.xpn.xwiki.internal.event.AbstractAttachmentEvent;
import com.xpn.xwiki.internal.event.AttachmentAddedEvent;
import com.xpn.xwiki.internal.event.AttachmentDeletedEvent;
import com.xpn.xwiki.internal.event.AttachmentUpdatedEvent;

/**
 * @version $Id$
 */
public abstract class AbstractSearchIndex implements SearchIndex, EventListener
{

    /**
     * Stores the list of Events.
     */
    private static final List<Event> EVENTS = Arrays.<Event> asList(new DocumentUpdatedEvent(),
        new DocumentCreatedEvent(), new DocumentDeletedEvent(), new AttachmentAddedEvent(),
        new AttachmentDeletedEvent(), new AttachmentUpdatedEvent());

    /**
     * Logger.
     */
    @Inject
    protected Logger logger;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.observation.EventListener#getEvents()
     */
    @Override
    public List<Event> getEvents()
    {
        return EVENTS;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.observation.EventListener#onEvent(org.xwiki.observation.event.Event, java.lang.Object,
     *      java.lang.Object)
     */
    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        try {
            if (event instanceof DocumentUpdatedEvent || event instanceof DocumentCreatedEvent) {
                getBuildIndexInstance().buildDocumentIndex(((DocumentModelBridge) source).getDocumentReference());
            } else if (event instanceof DocumentDeletedEvent) {
                getDeleteIndexInstance().deleteDocumentIndex(((DocumentModelBridge) source).getDocumentReference());
            } else if (event instanceof AttachmentUpdatedEvent || event instanceof AttachmentAddedEvent) {
                AttachmentReference attachref =
                    new AttachmentReference(((AbstractAttachmentEvent) event).getName(),
                        ((DocumentModelBridge) source).getDocumentReference());
                getBuildIndexInstance().buildAttachmentIndex(attachref, (DocumentModelBridge) source);

            } else if (event instanceof AttachmentDeletedEvent) {

                AttachmentReference attachref =
                    new AttachmentReference(((AbstractAttachmentEvent) event).getName(),
                        ((DocumentModelBridge) source).getDocumentReference());
                getDeleteIndexInstance().deleteAttachmentIndex(attachref, (DocumentModelBridge) source);

            } else if (event instanceof WikiDeletedEvent) {
                // TO DO
            }
        } catch (Exception e) {
            logger.error("Exception during " + event.toString() + " event, document build/delete index failed.", e);
        }
    }
}
