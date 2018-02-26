STRHIST_PATH='/home/lefteris/GitHub/strHist'
mvn install:install-file -Dfile=${STRHIST_PATH}/custom_dependencies/virtjdbc4.jar -DgroupId=custom -DartifactId=virtjdbc4 -Dversion=1 -Dpackaging=jar
mvn install:install-file -Dfile=${STRHIST_PATH}/custom_dependencies/virtjdbc4.jar -DgroupId=custom -DartifactId=virt_sesame2 -Dversion=1 -Dpackaging=jar
