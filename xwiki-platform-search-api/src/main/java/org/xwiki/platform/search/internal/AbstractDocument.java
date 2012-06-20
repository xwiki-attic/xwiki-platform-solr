package org.xwiki.platform.search.internal;

import java.util.Date;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.context.Execution;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.platform.search.IndexFields;
import org.xwiki.rendering.syntax.Syntax;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

/**
 * @version $Id$
 */

public abstract class AbstractDocument 
{
   /*
    * Stores the type of the document
    */
    protected String type;
    
    private String version;
    
    private EntityReference Docref;
    
    private String wiki;

    private String documentTitle;

    private String author;

    private String creator;

    private String language;

    private Date creationDate;

    private Date modificationDate;
    
    
    @Inject
    protected Execution execution;
    

    public AbstractDocument(String type, XWikiDocument doc,boolean deleted)
    {   
        
        XWikiContext context = (XWikiContext) this.execution.getContext().getProperty(XWikiContext.EXECUTIONCONTEXT_KEY);    
        
        /*
         * sets the document title, language
         */
        setDocumentTitle(doc.getRenderedTitle(Syntax.PLAIN_1_0, context));
        setLanguage(doc.getLanguage());
        setEntityReference(doc.getDocumentReference());
    }
    
    /*
     * It gets the Unique id of the document
     */
    public String getId()
    {
        StringBuilder retval = new StringBuilder();

        retval.append(getFullName());
        retval.append(".");
        retval.append(getLanguage());

        return retval.toString();
    }

  /*
   * Its implementation will vary for XWiki doc, attachments .
   */
   
    public abstract String getFullText(XWikiDocument doc);
    
       

    /**
     * @param author The author to set.
     */
    public void setAuthor(String author)
    {
        this.author = author;
    }

    /**
     * @param version the version of the document
     */
    public void setVersion(String version)
    {
        this.version = version;
    }

    /**
     * @param documentTitle the document title
     */
    public void setDocumentTitle(String documentTitle)
    {
        this.documentTitle = documentTitle;
    }

    /**
     * @param modificationDate The modificationDate to set.
     */
    public void setModificationDate(Date modificationDate)
    {
        this.modificationDate = modificationDate;
        
    }

    public void setEntityReference(DocumentReference docref)
    {
        Docref= docref;
    }
    
    
    public String getDocumentTitle()
    {
        return this.documentTitle;
    }
    

    public EntityReference getEntityReference()
    {
        return this.Docref;
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

    
    public String getDocumentFullName()
    {
        return Utils.<EntityReferenceSerializer<String>> getComponent(EntityReferenceSerializer.TYPE_STRING, "local")
            .serialize(getEntityReference());
    }

    public String getVersion()
    {
        return this.version;
    }

    public Date getCreationDate()
    {
        return this.creationDate;
    }

    public void setCreationDate(Date creationDate)
    {
        this.creationDate = creationDate;
    }

    public String getCreator()
    {
        return this.creator;
    }

    public void setCreator(String creator)
    {
        this.creator = creator;
    }

    public String getFullName()
    {
        return Utils.<EntityReferenceSerializer<String>> getComponent(EntityReferenceSerializer.TYPE_STRING).serialize(
            getEntityReference());
    }

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

    // Object

    @Override
    public String toString()
    {
        return getId();
    }

    
}
