package gr.demokritos.iit.irss.semagrow.sesame;

import org.openrdf.query.algebra.TupleExpr;

/**
 * Created by angel on 10/11/14.
 */
public class CostEstimatorImpl implements CostEstimator {

    private CardinalityEstimator cardinalityEstimator;

    public CostEstimatorImpl(CardinalityEstimator cardEst) {
        this.cardinalityEstimator = cardEst;
    }

    @Override
    public double getCost(TupleExpr tupleExpr) {
        return 0;
    }


}
