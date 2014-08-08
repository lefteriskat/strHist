package gr.demokritos.iit.irss.semagrow.api.range;

import gr.demokritos.iit.irss.semagrow.api.range.Range;
import org.json.simple.JSONObject;

/**
 *
 * Created by angel on 7/16/14.
 */
public interface RangeLength<T> extends Range<T> {

    long getLength();
    JSONObject toJSON();
}
