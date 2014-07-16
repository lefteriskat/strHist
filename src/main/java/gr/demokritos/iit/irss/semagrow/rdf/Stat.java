package gr.demokritos.iit.irss.semagrow.rdf;

import java.util.List;

/**
 * Created by angel on 7/15/14.
 */
public class Stat {

    private Long frequency;

    private List<Long> distinctCount;

    public Stat(Long frequency, List<Long> distinctCount) {
        this.distinctCount = distinctCount;
        this.frequency = frequency;
    }

    public Long getFrequency() {
        return frequency;
    }

    public void setFrequency(Long frequency) {
        this.frequency = frequency;
    }

    public List<Long> getDistinctCount() {
        return distinctCount;
    }

    public void setDistinctCount(List<Long> distinctCount) {
        this.distinctCount = distinctCount;
    }

    public Double getDensity() {
        Long d = (long)1;
        for (Long l : getDistinctCount())
            if (l != null)
                d *= l;

        if (d != 0)
            return ((double)getFrequency()) / d;
        else
            return (double)0;
    }
}
