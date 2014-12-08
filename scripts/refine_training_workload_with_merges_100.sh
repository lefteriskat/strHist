ABS_PATH=/mnt/ocfs2/IDF_data/journals/exp_triples/histogram/test

OPTS="-o /mnt/ocfs2/IDF_data/journals/exp_triples/histogram/test/experiments/infinite_fixed_root_with_merges_100/"

CP=${ABS_PATH}/jars/refine_training_workload_with_merges_100.jar

for Y in {1977..2009}
do

FILE=log4j.$Y.properties
LOG4J=${ABS_PATH}/log4j_properties/$FILE
cat ${ABS_PATH}/log4j_properties/log4j.properties | sed "s|{year}|$Y|" > $LOG4J

java -Dlog4j.configuration=file:$LOG4J -cp $CP gr.demokritos.iit.irss.semagrow.tools.expirementfixedprefix.RefineTrainingWorkload -y $Y $OPTS

done

