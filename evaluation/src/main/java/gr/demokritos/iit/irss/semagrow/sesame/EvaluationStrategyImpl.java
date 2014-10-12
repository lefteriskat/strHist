package gr.demokritos.iit.irss.semagrow.sesame;

import info.aduna.iteration.CloseableIteration;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.sail.federation.evaluation.RepositoryTripleSource;

/**
 * Created by angel on 10/11/14.
 */
public class EvaluationStrategyImpl extends org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl {


    public EvaluationStrategyImpl(final RepositoryConnection cnx) {
        super(new RepositoryTripleSource(cnx));
    }

    @Override
    public CloseableIteration<BindingSet, QueryEvaluationException>
        evaluate(StatementPattern pattern, BindingSet bindings) throws QueryEvaluationException {

        //TODO: encapsulate with a logger.
        return super.evaluate(pattern, bindings);
    }

}