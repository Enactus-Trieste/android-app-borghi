#!/bin/bash
MAX_TRIES=2
run_with_retry() {
   n=1
   until [ $n -gt $MAX_TRIES ]
   do
      echo "Starting test attempt $n"
      ./gradlew connectedDebugAndroidTest && break
      n=$[$n+1]
      sleep 5
   done
   if [ $n -gt $MAX_TRIES ]; then
     echo "Max tries reached ($MAX_TRIES)"
     exit 1
   fi
}
run_with_retry