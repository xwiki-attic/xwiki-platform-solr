README
-----------------------------------------------

1. Get the XWiki Enterprise, xwiki-enterprise-jetty-hsqldb. Tested on 3.6

2. Extract it to some location.

3. XWiki Enterprise 4.2-SNAPSHOT has lucene 3.5 libraries.Delete lucene-analyzer, lucene-core, lucene-queryparser of ver 3.5.0 from xwiki-enterprise-jetty-hsqldb-4.2-SNAPSHOT/webapps/xwiki/WEB-INF/lib

4. Download Solr from http://www.apache.org/dyn/closer.cgi/lucene/solr/3.6.0
   Add the below jar files to lib.

    apache-solr-core-3.6.0.jar
    apache-solr-solrj-3.6.0.jar
    apache-solr-velocity-3.6.0.jar
    apache-solr-langid-3.6.0.jar

5. Download Lucene from http://www.apache.org/dyn/closer.cgi/lucene/java/3.6.0
   Add the below jar files to lib

    lucene-analyzers-3.6.0.jar
    lucene-core-3.6.0.jar
    lucene-grouping-3.6.0.jar
    lucene-highlighter-3.6.0.jar
    lucene-queryparser-3.6.0.jar
    lucene-spellchecker-3.6.0.jar
    lucene-spatial-3.6.0.jar
    lucene-memory-3.6.0.jar
    lucene-misc-3.6.0.jar

6. Add configuration to xwiki.properties

    ----------------------------------------------------------------------
    Search
    ----------------------------------------------------------------------
    Search backend to be used by wiki search for indexing and search query retrieval.

     The possible options
     
     * solrj
     * lucene
     * solr-remote
     * search.backend = solrj

   Solrj - Embedded solr server.
   
   search.solr.home='/path/to/solr/home/

7. Copy solrconfig.xml and schema.xml from the solr folder provided in the repository. Modify the following in the solrconfig.xml
   <lib dir="/Users/administrator/apache-solr-3.6.0/dist/" regex="apache-solr-cell-\d.*\.jar" />
   <lib dir="/Users/administrator/apache-solr-3.6.0/contrib/extraction/lib" regex=".*\.jar" /> 
   Replace the above with the path where the dist and contrib folders are present.

8. Build the code and copy xwiki-platform-search-api-3.6-SNAPSHOT.jar and xwiki-platform-search-api-4.2-SNAPSHOT.jar to the XWiki Eneterprise lib directory.


9. Start the Server using start_xwiki.sh or start_xwiki.bat

10. Create a page in Sandbox - SearchPage
   Copy the content of https://gist.github.com/2295648

11. Save and View the SearchPage, Start searching. 