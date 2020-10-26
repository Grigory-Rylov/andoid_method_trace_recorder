## CLI
1) create App.kt
```
fun main(args: Array<String>) {
    val launcher = Launcher(args)
    launcher.launch()
}

```

2) Create Launcher.kt
```

import com.github.grishberg.tracerecorder.common.ConsoleLogger
import com.github.grishberg.tracerecorder.exceptions.MethodTraceRecordException
import org.apache.commons.cli.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.exitProcess

private const val PACKAGE_OPT_NAME = "p"
private const val RECORD_DURATION_OPT_NAME = "t"
private const val ACTIVITY_OPT_NAME = "a"
private const val OUTPUT_FILE_NAME_OPT_NAME = "o"
private const val ENABLE_SYSTRACE = "s"
private const val ENABLE_METHOD_TRACING = "m"
private const val RECORDING_MODE = "mode"
private const val SERIAL_NUMBER = "serial"

class Launcher(
    private val args: Array<String>
) {
    @Volatile
    var shouldWaitForSystrace = false

    @Volatile
    var shouldWaitForMethodTrace = false

    fun launch(): Int {
        val options = Options()
        options.addRequiredOption(PACKAGE_OPT_NAME, "package", true, "Target application package")
        options.addRequiredOption(RECORD_DURATION_OPT_NAME, "timeout", true, "Recording duration in seconds")
        options.addOption(ACTIVITY_OPT_NAME, "activity", true, "Application entry point activity")
        options.addOption(OUTPUT_FILE_NAME_OPT_NAME, "outFile", true, "Output trace file name")
        options.addOption(ENABLE_SYSTRACE, "systrace", false, "Should record systrace")
        options.addOption(ENABLE_METHOD_TRACING, "methods", false, "Should record method tracing")
        options.addOption(RECORDING_MODE, "recording_mode", true, "Record mode sample/trace, sample as default")
        options.addOption(SERIAL_NUMBER, true, "Device serial number to connect.")

        val parser = DefaultParser()
        val formatter = HelpFormatter()
        return try {
            val cmd = parser.parse(options, args)
            initAndLaunch(cmd)
            0
        } catch (e: ParseException) {
            println(e.message)
            formatter.printHelp("Method trace recorder help:", options)
            1
        }
    }

    private fun initAndLaunch(cmd: CommandLine) {
        val packageName = cmd.getOptionValue(PACKAGE_OPT_NAME)
        val duration = Integer.valueOf(cmd.getOptionValue(RECORD_DURATION_OPT_NAME))
        val activity = cmd.getOptionValue(ACTIVITY_OPT_NAME)
        var outputFileName = cmd.getOptionValue(OUTPUT_FILE_NAME_OPT_NAME)
        val methodTrace = cmd.hasOption(ENABLE_METHOD_TRACING)
        val systrace = cmd.hasOption(ENABLE_SYSTRACE)
        val isSampleMode = "trace" != cmd.getOptionValue(RECORDING_MODE)
        val serialNumber = cmd.getOptionValue(SERIAL_NUMBER)

        if (!methodTrace && !systrace) {
            println("You must enter at least -m or -s argument.")
            exitProcess(1)
        }
        if (outputFileName == null) {
            val sdf = SimpleDateFormat("yyyyMMdd_HH-mm-ss.SSS")
            val formattedTime = sdf.format(Date())
            outputFileName = "trace-$formattedTime.trace"
        }
        shouldWaitForSystrace = systrace
        shouldWaitForMethodTrace = methodTrace

        val listener = object : MethodTraceEventListener {
            override fun onStartedRecording() {
                println("Start recording...")
            }

            override fun onMethodTraceReceived(traceFile: File) {
                println("trace file saved at $traceFile")
                shouldWaitForMethodTrace = false
                if (!shouldWaitForSystrace) {
                    exitProcess(0)
                }
            }

            override fun onMethodTraceReceived(remoteFilePath: String) {
                println("trace file saved in remote device at $remoteFilePath")
                shouldWaitForSystrace = false
                if (!shouldWaitForMethodTrace) {
                    exitProcess(0)
                }
            }

            override fun fail(throwable: Throwable) {
                println(throwable.message)
                exitProcess(1)
            }

            override fun onSystraceReceived(result: SystraceRecordResult) {
                val values = result.records

                println("SYSTRACE: parent_ts = ${result.parentTs}")
                for (record in values) {
                    println("${record.name} - ${String.format("%.06f", (record.endTime - record.startTime) * 1000)} ms")
                }
                exitProcess(0)
            }
        }

        val recorder = MethodTraceRecorderImpl(
            listener,
            methodTrace,
            systrace,
            ConsoleLogger(),
            serialNumber = serialNumber?.let(::SerialNumber)
        )
        try {
            recorder.startRecording(
                outputFileName,
                packageName,
                activity,
                if (isSampleMode) RecordMode.METHOD_SAMPLE else RecordMode.METHOD_TRACES
            )
        } catch (e: MethodTraceRecordException) {
            println(e.message)
            exitProcess(1)
        }

        Thread.sleep(duration * 1000L)

        recorder.stopRecording()
        Thread.sleep(500L)
        recorder.disconnect()
    }
}

```
