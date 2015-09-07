package io.ino.solrs.quasar;

import io.ino.solrs.AsyncSolrClient;

import java.util.ArrayList;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;

import scala.PartialFunction;
import scala.concurrent.ExecutionContext;
import scala.concurrent.ExecutionContextExecutor;
import scala.concurrent.Future;
import scala.concurrent.impl.ExecutionContextImpl;
import scala.runtime.AbstractPartialFunction;
import co.paralleluniverse.fibers.FiberAsync;
import co.paralleluniverse.fibers.SuspendExecution;

/**
 * Quasar-aware hbase GET operation.
 * 
 * NB: it's not thread-safe.
 * 
 * @author fabio
 *
 */
public class FiberSearchRequest extends FiberAsync<QueryResponse, SolrServerException> {
	// reference to outer defined solrclient (do not close it)
	private AsyncSolrClient solrClient;
	
	private final SolrQuery solrQuery;
	
	protected FiberSearchRequest(AsyncSolrClient solrClient, String collection, String query) {
		this.solrClient = solrClient;
		this.solrQuery = new SolrQuery();
		this.solrQuery.setParam("collection", collection);
		this.solrQuery.setQuery(query);
	}
	
	
	/**
	 * Bridge classes to use Scala future callbacks in Java.
	 * 
	 * @author fabio
	 *
	 * @param <T>
	 */
	static abstract class OnSuccess<T> extends AbstractPartialFunction<T, Void> {
		
		/* (non-Javadoc)
		 * @see scala.runtime.AbstractPartialFunction#apply(java.lang.Object)
		 */
		@Override
		public Void apply(T x) {
			onSuccess(x);
			return null;
		}

		protected abstract void onSuccess(T x);

		public boolean isDefinedAt(T arg) {
			return true;
		}
		
	}
	static abstract class OnFailure extends AbstractPartialFunction<Throwable, Void> {
		/* (non-Javadoc)
		 * @see scala.runtime.AbstractPartialFunction#apply(java.lang.Object)
		 */
		@Override
		public Void apply(Throwable x) {
			onFailure(x);
			return null;
		}
		
		protected abstract void onFailure(Throwable x);
		
		public boolean isDefinedAt(Throwable arg) {
			return true;
		}
	}
	
	@Override
	protected void requestAsync() {
		
		Future<QueryResponse> fut = solrClient.query(solrQuery);
		ExecutionContextExecutor scalaExecutionContext = new ExecutionContext.Implicits$().global();
		fut.onSuccess(new OnSuccess<QueryResponse>(){
				@Override
				protected void onSuccess(QueryResponse resp) {
					asyncCompleted(resp);
				}
			}, scalaExecutionContext );
		fut.onFailure(new OnFailure(){
				@Override
				protected void onFailure(Throwable ex) {
					asyncFailed( ex );
				}
			}, scalaExecutionContext );
		
	}
	
	
	/**
	 * Set start document index.
	 * 
	 * @param start
	 * @return
	 */
	public FiberSearchRequest setStart(int start) {
		this.solrQuery.setStart(start);
		return this;
	}

	/**
	 * Set max number of documents to fetch.
	 * 
	 * @param max
	 * @return
	 */
	public FiberSearchRequest setRows(int max) {
		this.solrQuery.setRows(max);
		return this;
	}
	
	/**
	 * Set sort string in query.
	 * 
	 * @param str sort string (ex. 'name asc, age desc')
	 * @return
	 */
	public FiberSearchRequest setSort(String str) {
		this.solrQuery.setParam("sort", str);
		return this;
	}
	
	/**
	 * Fiber-blocking GET operation.
	 *  
	 * @return list of asynchbase key-value
	 * @throws SuspendExecution  never thrown, used only to instruments method with quasar fiber.
	 */
	public QueryResponse search() throws SuspendExecution, SolrServerException {
		try {
			return this.run();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return new QueryResponse();
	}
	

}
