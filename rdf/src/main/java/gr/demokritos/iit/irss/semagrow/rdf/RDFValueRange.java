package gr.demokritos.iit.irss.semagrow.rdf;

import gr.demokritos.iit.irss.semagrow.api.range.RangeLength;
import gr.demokritos.iit.irss.semagrow.api.range.Rangeable;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 * Created by angel on 10/25/14.
 */
public class RDFValueRange implements RangeLength<Value>, Rangeable<RDFValueRange> {

    private RDFURIRange uriRange;

    private RDFLiteralRange literalRange;

    public RDFValueRange() {
        this(new RDFURIRange(), new RDFLiteralRange());
    }

    public RDFValueRange(RDFURIRange uriRange, RDFLiteralRange literalRange) {
        assert uriRange != null;
        assert literalRange != null;
        this.uriRange = uriRange;
        this.literalRange = literalRange;
    }

    public RDFValueRange(RDFURIRange uriRange) {
        this(uriRange, new RDFLiteralRange());
        assert uriRange != null;
    }

    public RDFValueRange(RDFLiteralRange literalRange) {
        this(new RDFURIRange(), literalRange);
        assert literalRange != null;
    }

    public long getLength() {
        long length = 0;

        if (literalRange != null)
            length += literalRange.getLength();

        if (uriRange != null)
            length += uriRange.getLength();

        return length;
    }

    public boolean isUnit() {
        return (uriRange != null && literalRange == null && uriRange.isUnit())
            || (uriRange == null && literalRange != null && literalRange.isUnit());
    }

    public boolean isEmpty() {
        //FIXME: wrong definition uriRange.isEmpty() && literalRange.isEmpty();
        return uriRange.isEmpty() || literalRange.isEmpty();
    }

    public boolean isInfinite() { return uriRange.isInfinite() && literalRange.isInfinite(); }

    public boolean includes(Value elem) {
        if (elem instanceof URI)
            return uriRange.includes((URI)elem);
        else if (elem instanceof Literal)
            return literalRange.includes((Literal)elem);
        else
            return false;
    }

    public void expand(Value elem) {
        if (elem instanceof URI)
            uriRange.expand((URI)elem);
        else if (elem instanceof Literal)
            literalRange.expand((Literal) elem);
    }

    public RDFValueRange intersection(RDFValueRange rdfValueRange) {
        return new RDFValueRange(
                uriRange.intersection(rdfValueRange.uriRange),
                literalRange.intersection(rdfValueRange.literalRange));
    }

    public RDFValueRange minus(RDFValueRange rdfValueRange) {
        return new RDFValueRange(
                uriRange.minus(rdfValueRange.uriRange),
                literalRange.minus(rdfValueRange.literalRange));
    }

    public boolean contains(RDFValueRange rdfValueRange) {
        return uriRange.contains(rdfValueRange.uriRange) &&
               literalRange.contains(rdfValueRange.literalRange);
    }

    public boolean intersects(RDFValueRange rdfValueRange) {
        return uriRange.intersects(rdfValueRange.uriRange) ||
                literalRange.intersects(rdfValueRange.literalRange);
    }

    public RDFValueRange tightRange(RDFValueRange rdfValueRange) {
        return new RDFValueRange(uriRange.tightRange(rdfValueRange.uriRange),
                literalRange.tightRange(literalRange));
    }

    public boolean hasSameType(RDFValueRange rdfValueRange) {
        if (this.isInfinite() && rdfValueRange.isInfinite())
            return true;
        else
            return literalRange.hasSameType(rdfValueRange.literalRange);
    }

    public RDFLiteralRange getLiteralRange() { return literalRange; }

    public RDFURIRange getUriRange() { return uriRange; }

    @Override
    public String toString()  {
        if (uriRange != null && !uriRange.getPrefixList().isEmpty())
            return uriRange.toString();
        else if (literalRange != null)
            return literalRange.toString();

        return "{}";
    }
}
