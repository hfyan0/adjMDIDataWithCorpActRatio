#!/bin/bash

JAVA_OPTS="-Xmx6G -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=6G -Xss2M -Duser.timezone=GMT"
./target/universal/stage/bin/adjmdidatawithcorpactratio 132.properties
