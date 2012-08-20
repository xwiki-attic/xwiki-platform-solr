README
-----------------------------------------------
1. Get the latest XWiki Enterprise 4.2-SNAPSHOT.

2. XWiki Enterprise 4.2-SNAPSHOT has lucene 3.5 libraries.Delete lucene-analyzer, lucene-core, lucene-queryparser of version 3.5.0 , xwiki-platform-search-lucene-4.2-milestone-2.jar from xwiki-enterprise-jetty-hsqldb-4.2-SNAPSHOT/webapps/xwiki/WEB-INF/lib.

3. Download Solr from http://www.apache.org/dyn/closer.cgi/lucene/solr/4.0.0-ALPHA . Add the below jar files to XE/webapps/xwiki/WEB-INF/lib.
   Untill the Solr search is added to xwiki-platform, for convienience the files are hosted in dropbox site here. Solr Lib
   apache-solr-core-4.0.0- ALPHA.jar
   apache-solr-solrj-4.0.0-ALPHA.jar
   apache-solr-velocity-4.0.0-ALPHA.jar
   apache-solr-langid-4.0.0- ALPHA.jar
   apache-solr-analysis-extras-4.0.0- ALPHA.jar
   apache-solr-dataimporthandler-4.0.0-ALPHA.jar
   apache-solr-cell-4.0.0-ALPHA.jar             
   apache-solr-dataimporthandler-extras-4.0.0-ALPHA.jar 
   apache-solr-uima-4.0.0-ALPHA.jar
   apache-solr-clustering-4.0.0-ALPHA.jar

3. Download Lucene from http://www.apache.org/dyn/closer.cgi/lucene/java/3.6.1  .Add the below jar files to XE/webapps/xwiki/WEB-INF/lib.
   Untill the Solr search is added to xwiki-platform, for convienience the files are hosted in dropbox site here. Lucene Lib
   lucene-analyzers-4.0.0-ALPHA.jar
   lucene-icu-4.0.0-ALPHA.jar
   lucene-phonetic-4.0.0-ALPHA.jar
   lucene-spellchecker-4.0.0-ALPHA.jar
   lucene-core-4.0.0-ALPHA.jar
   lucene-join-4.0.0-ALPHA.jar
   lucene-queries-4.0.0-ALPHA.jar
   lucene-stempel-4.0.0-ALPHA.jar
   lucene-facet-4.0.0-ALPHA.jar
   lucene-kuromoji-4.0.0-ALPHA.jar
   lucene-queryparser-4.0.0-ALPHA.jar
   lucene-grouping-4.0.0-ALPHA.jar
   lucene-memory-4.0.0-ALPHA.jar
   lucene-remote-4.0.0-ALPHA.jar
   lucene-highlighter-4.0.0-ALPHA.jar
   lucene-misc-4.0.0-ALPHA.jar
   lucene-spatial-4.0.0-ALPHA.jar

4. Add  google gson library to the XE/webapps/xwiki/WEB-INF/lib. Indexing uses json communication to update the progress bar. Download from http://code.google.com/p/google-gson/downloads/list

5. Add spatial4j library file to XE/webapps/xwiki/WEB-INF/lib. Download from http://repo1.maven.org/maven2/com/spatial4j/spatial4j/0.2/spatial4j-0.2.jar
 
6. Add configuration to xwiki.properties. 
   Search backend to be used by wiki search for indexing and search query retrieval.
   search.backend=solrj
   search.solr.home='/path/to/solr/home/'

7. Build the code and copy xwiki-platform-search-api-4.2-SNAPSHOT.jar and xwiki-platform-search-solrj-4.2-SNAPSHOT.jar to the XWiki Eneterprise lib directory.
   The code is not stable. If checkstyle error occurs, build with -Dxwiki.checkstyle.skip=true

8. Start the Server using start_xwiki.sh or start_xwiki.bat
   Save and View the SearchPage  at http://localhost:8080/xwiki/bin/view/Main/AdvancedSearch, Start searching.        


    