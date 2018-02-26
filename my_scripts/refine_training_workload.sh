#!/bin/bash
if [ ! -d /var/tmp/log4j_properties ]; then
	mkdir /var/tmp/log4j_properties
fi
touch /var/tmp/log4j_properties/log4j.properties
counter=1
LOG4J_PATH='/var/tmp/log4j_properties'
STRHIST_PATH="/home/lefteris/GitHub/strHist"
EVAL_PATH=$STRHIST_PATH"/evaluation/target/sthist-eval-1.0-SNAPSHOT.jar"
DEPENDENCY_PATH=$STRHIST_PATH"/target/lib/*"
ALL_JARS=$EVAL_PATH
ALL_JARS+=":"
ALL_JARS+=$DEPENDENCY_PATH
while [ $counter -le 2 ]
do
echo $counter
java -Dlog4j.configuration=file:${LOG4J_PATH}/log4j.properties -cp $ALL_JARS gr.demokritos.iit.irss.semagrow.tools.expirementfixedprefix.RefineTrainingWorkload -b 50 -v 3.2 -l $counter -o /var/tmp/strHist/
((counter++))
done
echo All done