#!/bin/bash
if [ ! -d /var/tmp/log4j_properties ]; then
	mkdir /var/tmp/log4j_properties
fi
touch /var/tmp/log4j_properties/log4j.properties
counter=1
LOG4J_PATH='/var/tmp/log4j_properties'
EVAL_PATH=$STRHIST_PATH"/evaluation/target/sthist-eval-1.0-SNAPSHOT.jar"
DEPENDENCY_PATH=$STRHIST_PATH"/target/lib/*"
ALL_JARS=$EVAL_PATH
ALL_JARS+=":"
ALL_JARS+=$DEPENDENCY_PATH
while [ $counter -le $1 ]
do
echo $counter
#java -Dlog4j.configuration=file:${LOG4J_PATH}/log4j.properties -cp $ALL_JARS gr.demokritos.iit.irss.semagrow.tools.expirementfixedprefix.PrepareTrainingWorkload -b $2 -v $3 -l $counter
java -cp $ALL_JARS gr.demokritos.iit.irss.semagrow.tools.expirementfixedprefix.PrepareTrainingWorkload -b $2 -v $3 -l $counter

((counter++))
done
echo All done
