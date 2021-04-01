#!/bin/bash

cp build/distributions/ssg-1.0-SNAPSHOT.tar .
tar -xf ssg-1.0-SNAPSHOT.tar

echo "Beginning: help command"
help_result=$(sh ssg-1.0-SNAPSHOT/bin/ssg help)
if [ $? -ne 0 ]; then
    err="ERROR: help command returned non-zero exit status"
    echo $err
else
    echo "---------Help: Passed 1/2"
    pattern='usage: ssg <build \| help> \[args\] \[Options\].*build'
    [[ $help_result =~ $pattern ]]
    if [ ${#BASH_REMATCH[0]} -gt 0 ]; then
        msg="---------Help: Passed 2/2"
        text='\e[0;32m $msg \e[m'
        echo $msg
    else
        err="FAILED 2/2 help command did not match the pattern"
        text='\e[0;31m $(err) \e[m'
        echo $text
        exit 1
    fi
fi
echo "Beginning: Test minimal"
file_index=$RANDOM
sh ssg-1.0-SNAPSHOT/bin/ssg build -i buildsite/src/test/resources/minimal/ -o out_${file_index}/
diff out_${file_index}/index.html buildsite/src/test/resources/out/correct/_output/minimal.html
if [ $? -ne 0 ]; then
  echo "ERROR: Minimal yielding incorrect output"
  exit 1
else
  echo "---------Minimal: Passed 1/1"
fi

echo "Tests completed"

echo "Beginning: Test HTTP Server"

