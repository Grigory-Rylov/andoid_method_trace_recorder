#Android Method Trace Recorder.

Used to record method `.trace` files which can be opened in [Android Trace Viewer](https://github.com/Grigory-Rylov/android-trace-viewer) or [Android Profiler](https://developer.android.com/studio/profile/android-profiler).


#CLI Usage

```
java -jar trace-recorder.java -p <package> [-a <activity>] [-o <output_file_name>]

```

#Usage in code

1) Create instance of `MethodTraceRecorder`
2) Call 
```
    try {
        recorder.startRecording(packageName, activityName)
    } catch (e: MethodTraceRecordException) {
        <handle exception>
    }
``` 
to start `activityName` and method trace recording.
3) Call `stopRecording()` to stop method trace recording


#Building CLI jar from sources
Clone sources and write in terminal `./gradlew fatJar` 
android-profiler-[VERSION].jar will be placed in `build/libs/`

Copyright [2020] [Grigory Rylov]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.