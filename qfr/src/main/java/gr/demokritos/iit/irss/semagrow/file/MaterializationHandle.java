package gr.demokritos.iit.irss.semagrow.file;

import org.openrdf.model.URI;
import org.openrdf.query.QueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;

import java.io.IOException;

/**
 * A QueryResultHandler that is used as materialization point
 * The results are passed to the handler using the handleSolution
 * and the results are committed/saved using the endQueryResults().
 * To discard the handle use destroy.
 * Created by angel on 10/20/14.
 */
public interface MaterializationHandle extends QueryResultHandler {

    URI getId();

    void destroy() throws IOException;
}
