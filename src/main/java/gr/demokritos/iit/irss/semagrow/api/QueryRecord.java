package gr.demokritos.iit.irss.semagrow.api;

/**
 * a query feedback record is essentially a query together with its resultset
 * Created by angel on 7/11/14.
 */
public interface QueryRecord {

    long getCardinality();

    void getResultSet();

}
