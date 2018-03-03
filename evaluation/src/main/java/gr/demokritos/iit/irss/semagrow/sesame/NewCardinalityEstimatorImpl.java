package gr.demokritos.iit.irss.semagrow.sesame;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.Slice;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.UnaryTupleOperator;
import org.openrdf.query.algebra.Union;

import eu.semagrow.core.plan.Plan;
import gr.demokritos.iit.irss.semagrow.api.Histogram;
import gr.demokritos.iit.irss.semagrow.api.range.Range;
import gr.demokritos.iit.irss.semagrow.api.range.RangeLength;
import gr.demokritos.iit.irss.semagrow.base.Estimation;
import gr.demokritos.iit.irss.semagrow.base.range.CalendarRange;
import gr.demokritos.iit.irss.semagrow.base.range.ExplicitSetRange;
import gr.demokritos.iit.irss.semagrow.base.range.IntervalRange;
import gr.demokritos.iit.irss.semagrow.base.range.PrefixRange;
import gr.demokritos.iit.irss.semagrow.rdf.RDFLiteralRange;
import gr.demokritos.iit.irss.semagrow.rdf.RDFRectangle;
import gr.demokritos.iit.irss.semagrow.rdf.RDFURIRange;
import gr.demokritos.iit.irss.semagrow.rdf.RDFValueRange;

public class NewCardinalityEstimatorImpl {
	private Histogram<RDFRectangle> histogram;

    public NewCardinalityEstimatorImpl(Histogram<RDFRectangle> histogram) {
        this.histogram = histogram;
    }

    public Estimation getCardinality(TupleExpr expr, BindingSet bindings) {

        if (expr instanceof StatementPattern)
            return getCardinality((StatementPattern)expr, bindings);
        else if (expr instanceof Union)
            return getCardinality((Union)expr, bindings);
        else if (expr instanceof Filter)
            return getCardinality((Filter)expr, bindings);
        else if (expr instanceof Projection)
            return getCardinality((Projection)expr, bindings);
        else if (expr instanceof Slice)
            return getCardinality((Slice)expr, bindings);
        else if (expr instanceof Join)
            return getCardinality((Join)expr, bindings);
        else if (expr instanceof LeftJoin)
            return getCardinality((LeftJoin)expr, bindings);
        else if (expr instanceof Plan)
            return getCardinality((Plan)expr, bindings);
        else
            return new Estimation();
    }

    public Estimation getCardinality(Plan plan, BindingSet bindings) {
        return new Estimation(plan.getProperties().getCardinality());
    }

    public Estimation getCardinality(Slice slice, BindingSet bindings) {
        Estimation card = getCardinality(slice.getArg(), bindings);
        return Estimation.min(card, slice.getLimit());
    }

    public Estimation getCardinality(Join join, BindingSet bindings) {
    	Estimation card1 = getCardinality(join.getLeftArg(), bindings);
    	Estimation card2 = getCardinality(join.getRightArg(), bindings);
        double sel = 0.5;
        return Estimation.multiplyByDouble( Estimation.multiply(card1, card2), sel);
    }

    public Estimation getCardinality(LeftJoin join, BindingSet bindings) {
        Estimation card1 = getCardinality(join.getLeftArg(), bindings);
        Estimation card2 = getCardinality(join.getRightArg(), bindings);
        Estimation card12 = Estimation.multiply(card1, card2);
        double sel = 0.5;
        Estimation card = Estimation.multiplyByDouble( card12, sel );
        return Estimation.max(card1, card);
    }

    public Estimation getCardinality(Filter filter, BindingSet bindings) {
    	double sel = 0.5;
        return Estimation.multiplyByDouble( getCardinality(filter.getArg(), bindings), sel );
    }

    public Estimation getCardinality(UnaryTupleOperator op, BindingSet bindings) {
        return getCardinality(op.getArg(), bindings);
    }

    public Estimation getCardinality(Union union, BindingSet bindings) {
        return Estimation.add( getCardinality(union.getLeftArg(), bindings), getCardinality(union.getRightArg(), bindings));
    }

    public Estimation getCardinality(StatementPattern pattern, BindingSet bindings) {

        return histogram.newEstimate(toRectangle(pattern, bindings));
    }

    private RDFRectangle toRectangle(StatementPattern pattern, BindingSet bindings) {
        Value sVal = pattern.getSubjectVar().getValue();
        Value pVal = pattern.getPredicateVar().getValue();
        Value oVal = pattern.getObjectVar().getValue();

        RDFURIRange subjectRange;
        List<String> list = new ArrayList<String>();
        if (sVal == null) {
            if (bindings.hasBinding(pattern.getSubjectVar().getName()))
                list.add(bindings.getValue(pattern.getSubjectVar().getName()).stringValue());
        } else
            list.add(sVal.stringValue());

        if (!list.isEmpty())
            subjectRange = new RDFURIRange(list);
        else
            subjectRange = new RDFURIRange();

        ExplicitSetRange<URI> predicateRange;
        Set<URI> set = new HashSet<URI>();
        if (pVal == null) {
            if (bindings.hasBinding(pattern.getPredicateVar().getName()))
                set.add((URI)bindings.getValue(pattern.getPredicateVar().getName()));
        } else
            set.add((URI) pVal);

        if (!set.isEmpty())
            predicateRange = new ExplicitSetRange<URI>(set);
        else
            predicateRange = new ExplicitSetRange<>();

        RDFValueRange objectRange = new RDFValueRange();
        if (oVal == null) {
            if (bindings.hasBinding(pattern.getObjectVar().getName()))
                objectRange = fillObjectRange(bindings.getValue(pattern.getObjectVar().getName()));
        } else
            objectRange = fillObjectRange(oVal);

        return new RDFRectangle(subjectRange, predicateRange, objectRange);
    }

    private RDFValueRange fillObjectRange(Value oVal) {
        if (oVal instanceof URI) {
            URI uri = (URI) oVal;
            PrefixRange pr = new PrefixRange();
            pr.getPrefixList().add(uri.stringValue());


            return new RDFValueRange(new RDFURIRange(pr.getPrefixList()),
                                     new RDFLiteralRange(Collections.<URI, RangeLength<?>>emptyMap()));
        } else if (oVal instanceof Literal) {
            Literal l = (Literal) oVal;
            Range literalRange = null;

            if (l.getDatatype().equals(XMLSchema.INT))
                literalRange = new RDFLiteralRange(XMLSchema.INT, new IntervalRange(l.intValue(), l.intValue()));
            else if (l.getDatatype().equals(XMLSchema.LONG))
                literalRange = new RDFLiteralRange(XMLSchema.LONG, new IntervalRange((int) l.longValue(), (int) l.longValue()));
            else if (l.getDatatype().equals(XMLSchema.STRING)) {
                PrefixRange pr = new PrefixRange();
                pr.getPrefixList().add(l.stringValue());
                literalRange = new RDFLiteralRange(XMLSchema.STRING, pr);
            } else if (l.getDatatype().equals(XMLSchema.DATETIME)) {
                Calendar cal = l.calendarValue().toGregorianCalendar();
                CalendarRange cr = new CalendarRange(cal.getTime(), cal.getTime());
                literalRange = new RDFLiteralRange(XMLSchema.DATETIME, cr);
            }

            if (literalRange != null)
                return new RDFValueRange(new RDFURIRange(Collections.<String>emptyList()),
                                        (RDFLiteralRange)literalRange);
        }

        return new RDFValueRange();
    }

}
