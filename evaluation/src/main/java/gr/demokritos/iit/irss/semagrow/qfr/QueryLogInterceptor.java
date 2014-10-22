package gr.demokritos.iit.irss.semagrow.qfr;

import gr.demokritos.iit.irss.semagrow.file.MaterializationHandle;
import gr.demokritos.iit.irss.semagrow.file.ResultMaterializationManager;
import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.DelayedIteration;
import info.aduna.iteration.Iteration;
import info.aduna.iteration.Iterations;
import org.openrdf.model.URI;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.TupleExpr;

import java.io.IOException;
import java.util.*;

/**
 * Created by angel on 10/20/14.
 */
public class QueryLogInterceptor {

    private ResultMaterializationManager fileManager;

    private QueryLogHandler qfrHandler;

    public QueryLogInterceptor(QueryLogHandler qfrHandler, ResultMaterializationManager fileManager) {
        this.qfrHandler = qfrHandler;
        this.fileManager = fileManager;
    }

    public ResultMaterializationManager getFileManager() {
        return fileManager;
    }


    public CloseableIteration<BindingSet, QueryEvaluationException>
        afterExecution(URI endpoint, TupleExpr expr, BindingSet bindings, CloseableIteration<BindingSet, QueryEvaluationException> result) {

        QueryLogRecordImpl metadata = createMetadata(endpoint, expr, bindings.getBindingNames());
        return observe(metadata, result);
    }


    public CloseableIteration<BindingSet, QueryEvaluationException>
        afterExecution(URI endpoint, TupleExpr expr, CloseableIteration<BindingSet, QueryEvaluationException> bindingIter, CloseableIteration<BindingSet, QueryEvaluationException> result)
    {

        List<BindingSet> bindings = Collections.<BindingSet>emptyList();

        try {
            bindings = Iterations.asList(bindingIter);
        } catch (QueryEvaluationException e) {
            //?
        }

//        bindingIter = new CollectionIteration<BindingSet, QueryEvaluationException>(bindings);

        Set<String> bindingNames = (bindings.size() == 0) ? new HashSet<String>() : bindings.get(0).getBindingNames();

        QueryLogRecordImpl metadata = createMetadata(endpoint, expr, bindingNames);

        return observe(metadata, result);
    }

    protected QueryLogRecordImpl createMetadata(URI endpoint, TupleExpr expr, Set<String> bindingNames) {
        return new QueryLogRecordImpl(UUID.randomUUID(), endpoint, expr, bindingNames);
    }

    protected CloseableIteration<BindingSet, QueryEvaluationException>
        observe(QueryLogRecordImpl metadata, CloseableIteration<BindingSet, QueryEvaluationException> iter) {

        return new QueryObserver(metadata, iter);
    }

    protected class QueryObserver extends DelayedIteration<BindingSet, QueryEvaluationException> {

        private QueryLogRecord queryLogRecord;

        private Iteration<BindingSet, QueryEvaluationException> innerIter;

        private MeasuringIteration<BindingSet, QueryEvaluationException> measure;

        private MaterializationHandle handle;

        public QueryObserver(QueryLogRecordImpl metadata, Iteration<BindingSet, QueryEvaluationException> iter) {
            queryLogRecord = metadata;
            innerIter = iter;
        }

        @Override
        protected Iteration<? extends BindingSet, ? extends QueryEvaluationException>
            createIteration() throws QueryEvaluationException {

            ResultMaterializationManager manager = getFileManager();

            handle = manager.saveResult();

            Iteration<BindingSet, QueryEvaluationException> iter =
                    new QueryResultObservingIteration(handle, innerIter);

            measure = new MeasuringIteration<BindingSet,QueryEvaluationException>(iter);

            iter = measure;

            return iter;
        }

        @Override
        public BindingSet next() throws QueryEvaluationException {
            try {
                return super.next();
            } catch(QueryEvaluationException e) {
                try {
                    handle.destroy();
                } catch (IOException e2) {
                    throw new QueryEvaluationException(e2);
                }
                throw e;
            }
        }

        @Override
        public void handleClose() throws QueryEvaluationException {
            super.handleClose();

            queryLogRecord.setCardinality(measure.getCount());
            queryLogRecord.setDuration(measure.getStartTime(), measure.getEndTime());

            if (queryLogRecord.getCardinality() == 0) {
                try {
                    handle.destroy();
                } catch (IOException e) {
                    throw new QueryEvaluationException(e);
                }
            } else {
                queryLogRecord.setResults(handle);
            }

            try {
                qfrHandler.handleQueryRecord(queryLogRecord);
            } catch (QueryLogException e) {
                throw new QueryEvaluationException(e);
            }
        }

    }

}
