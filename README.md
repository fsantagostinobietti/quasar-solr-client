# *quasar-solr-client:0.1.0* 

Solr client for Quasar library (http://docs.paralleluniverse.co/quasar/).
It is a wrapper for [solrs](https://github.com/inoio/solrs) library


Install artifact in maven local repository :
```
mvn install
```
Now 'hbase-client' library can be use in your Quasar project :
```
	<dependency>
		<groupId>co.paralleluniverse.quasar</groupId>
		<artifactId>quasar-solr-client</artifactId>
		<version>0.1.0</version>
	</dependency>
```


## Features 
Basic query is supported.

## TODO list 
Add INSERT and UPDATE operations.

## Code examples
```java
	private final String QUORUM = "localhost:2182";
	
	QueryResponse resp = solrClient.newSearchRequest("collection1", "*:*").run();
	
	for (SolrDocument doc : resp.getResults()) {
		for ( String fName : doc.getFieldNames() ) {
			System.out.println("### name ["+fName+"] value ["+doc.get(fName)+"]");
		}
	}
```