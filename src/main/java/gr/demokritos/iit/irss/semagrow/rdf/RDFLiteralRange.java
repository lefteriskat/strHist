package gr.demokritos.iit.irss.semagrow.rdf;

import gr.demokritos.iit.irss.semagrow.api.Range;
import gr.demokritos.iit.irss.semagrow.api.Rangeable;

/**
 * Created by angel on 7/15/14.
 */
public class RDFLiteralRange
        implements Range<Object>, Rangeable<RDFLiteralRange>
{


    public long getLength() {
        return 0;
    }

    public boolean isUnit() {
        return false;
    }

    public RDFLiteralRange intersection(RDFLiteralRange rect) {
        return null;
    }

    public boolean contains(RDFLiteralRange rect) {
        return false;
    }
}
