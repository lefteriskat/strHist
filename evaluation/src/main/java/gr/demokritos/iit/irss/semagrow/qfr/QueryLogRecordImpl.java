package gr.demokritos.iit.irss.semagrow.qfr;

import gr.demokritos.iit.irss.semagrow.file.MaterializationHandle;
import org.openrdf.model.URI;
import org.openrdf.query.algebra.TupleExpr;

import java.util.*;

/**
* Created by angel on 10/20/14.
*/
public class QueryLogRecordImpl implements QueryLogRecord {

    private UUID session;

    private TupleExpr query;

    private URI endpoint;

    private List<String> bindingNames;

    private long cardinality;

    private Date startTime;

    private Date endTime;

    private long duration;

    private URI results;

    public QueryLogRecordImpl(UUID session, URI endpoint, TupleExpr query) {
        this.session = session;
        this.endpoint = endpoint;
        this.query = query;
        this.bindingNames = new LinkedList<String>();
    }

    public QueryLogRecordImpl(UUID session, URI endpoint, TupleExpr query, Collection<String> bindingNames) {
        this.session = session;
        this.endpoint = endpoint;
        this.query = query;
        this.bindingNames = new LinkedList<String>(bindingNames);
    }

    @Override
    public URI getEndpoint() { return endpoint; }

    @Override
    public TupleExpr getQuery() { return query; }

    @Override
    public UUID getSession() { return session; }

    @Override
    public List<String> getBindingNames() { return bindingNames; }

    @Override
    public void setCardinality(long card) { cardinality = card; }

    @Override
    public long getCardinality() { return cardinality; }

    @Override
    public void setDuration(long start, long end) {
        startTime = new Date(start);
        endTime = new Date(end);
        duration = end - start;
    }

    @Override
    public void setResults(URI handle) { results = handle; }

    @Override
    public Date getStartTime() { return startTime; }

    @Override
    public Date getEndTime() { return endTime; }

    @Override
    public long getDuration() { return duration; }

    @Override
    public URI getResults() { return results; }
}
