#!/bin/bash
if [ ! -d /var/tmp/log4j_properties ]; then
	mkdir /var/tmp/log4j_properties
fi
counter=1
STRHIST_PATH="/home/lefteris/GitHub/strHist"
EVAL_PATH=$STRHIST_PATH"/evaluation/target/sthist-eval-1.0-SNAPSHOT.jar"
DEPENDENCY_PATH=$STRHIST_PATH"/target/lib/*"
ALL_JARS=$EVAL_PATH
ALL_JARS+=":"
ALL_JARS+=$DEPENDENCY_PATH
while [ $counter -le 2 ]
do
echo $counter
echo $ALL_JARS
java -cp $ALL_JARS gr.demokritos.iit.irss.semagrow.tools.expirementfixedprefix.PrepareTrainingWorkload -b 25 -v 3.2
# java -cp "/home/lefteris/GitHub/strHist/evaluation/target/sthist-eval-1.0-SNAPSHOT.jar:/home/lefteris/GitHub/strHist/target/lib/*"  gr.demokritos.iit.irss.semagrow.tools.expirementfixedprefix.PrepareTrainingWorkload -b 25 -v 3.2
mv /var/tmp/_log.ser /var/tmp/${counter}.script_log.ser
((counter++))
done
echo All done

"/home/lefteris/GitHub/strHist/evaluation/target/sthist-eval-1.0-SNAPSHOT.jar:/home/lefteris/GitHub/strHist/target/lib/*"
"/home/lefteris/GitHub/strHist/evaluation/target/sthist-eval-1.0-SNAPSHOT.jar:/home/lefteris/GitHub/strHist/target/lib/*"
