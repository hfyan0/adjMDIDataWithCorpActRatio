#!/bin/bash

JAVA_OPTS="-Xmx8G -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=8G -Xss2M -Duser.timezone=GMT"
echo "Start: "$(date)
java -jar adjMDIDataWithCorpActRatio-assembly-1.0-SNAPSHOT.jar 17.properties
echo "End: "$(date)
