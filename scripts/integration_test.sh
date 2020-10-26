#!/bin/bash
# Integration tests of Android Methods Tracer Recorder
set -x

# TODO(geaden): Assign version dynamically
COMMAND="java -jar app/build/libs/android-profiler-2.0.0.jar -m -a com.github.grishberg.testapp.MainActivity -p com.github.grishberg.testapp -t 15"

echo '1. Compile and build latest version of AMTR'
./gradlew app:fatJar

echo '2. Compile and build latest version of testapp'
pushd testapp
./gradlew :app:installDebug
popd

echo '3. Clean test trace files'
rm /tmp/*.trace

echo '4. Start trace for first device'
FIRST_TRACE=/tmp/first.trace
eval "$COMMAND -o $FIRST_TRACE"
test -f "$FIRST_TRACE" || {
  echo 'Failed to create trace file'
  exit 1
}

echo '5. Start trace for device serial'
adb devices | while read line
do
    if [ ! "$line" = "" ] && [ `echo $line | awk '{print $2}'` = "device" ]
    then
        device=`echo $line | awk '{print $1}'`
        SERIAL_TRACE="/tmp/$device.trace"
        eval "$COMMAND -serial $device -o $SERIAL_TRACE"
        test -f "$SERIAL_TRACE" || {
          echo 'Failed to create trace file'
          # TODO(geaden): This doesn't stop the execution. Probably must be extracted into a function
          exit 1
        }
    fi
done

echo "PASSED."
