#!/bin/bash
if [ ! -d /var/tmp/log4j_properties ]; then
	mkdir /var/tmp/log4j_properties
fi
touch /var/tmp/log4j_properties/log4j.properties

LOG4J_PATH='/var/tmp/log4j_properties'
EVAL_PATH=$STRHIST_PATH"/evaluation/target/sthist-eval-1.0-SNAPSHOT.jar"
DEPENDENCY_PATH=$STRHIST_PATH"/target/lib/*"
ALL_JARS=$EVAL_PATH
ALL_JARS+=":"
ALL_JARS+=$DEPENDENCY_PATH

#java -Dlog4j.configuration=file:${LOG4J_PATH}/log4j.properties -cp $ALL_JARS gr.demokritos.iit.irss.semagrow.tools.expirementfixedprefix.EvaluateOnVirtuoso -n $1 -v $2
java -cp $ALL_JARS gr.demokritos.iit.irss.semagrow.tools.expirementfixedprefix.EvaluateOnVirtuoso -n $1 -v $2

((counter++))
echo All done