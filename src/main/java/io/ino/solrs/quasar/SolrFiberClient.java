package io.ino.solrs.quasar;

import io.ino.solrs.AsyncSolrClient;
import io.ino.solrs.CloudSolrServers;
import io.ino.solrs.RoundRobinLB;
import io.ino.solrs.CloudSolrServers.WarmupQueries;
import scala.Option;
import scala.concurrent.duration.Duration;





/**
 * 
 * Thread-safe wrapper for client asynchbase library.
 * You need just one instance for each cluster/quorum.
 * 
 * @author fabio
 *
 */
public class SolrFiberClient {
	//private final static Logger logger = LoggerFactory.getLogger(HbaseClient.class);
	
	private AsyncSolrClient solrClient = null;
	
	public SolrFiberClient(String zkQuorum) {
		Duration zkClientTimeout = CloudSolrServers.$lessinit$greater$default$2();	// default value 2nd parameter
		Duration zkConnectTimeout = CloudSolrServers.$lessinit$greater$default$3();	// default value 3rd parameter
		Duration clusterStateUpdateInterval = CloudSolrServers.$lessinit$greater$default$4();	// default value 4th parameter
		Option<String> defaultCollection = Option.apply(null); // None
		Option<WarmupQueries> warmupQueries = Option.apply(null); // None;
		
		CloudSolrServers servers = new CloudSolrServers( 
					zkQuorum, 
					zkClientTimeout, 
					zkConnectTimeout, 
					clusterStateUpdateInterval, 
					defaultCollection, 
					warmupQueries  );
		
		solrClient = new AsyncSolrClient.Builder(new RoundRobinLB(servers)).build();
	}


	public AsyncSolrClient getSolrClient() {
		return solrClient;
	}
	
	public void close() {
		solrClient.shutdown();
	}
	


	public FiberSearchRequest newSearchRequest(String collection, String query) {
		return new FiberSearchRequest(solrClient, collection, query);
	}

	
	
}
