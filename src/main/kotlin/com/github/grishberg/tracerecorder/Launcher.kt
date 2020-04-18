package com.github.grishberg.tracerecorder

import org.apache.commons.cli.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private const val PACKAGE_OPT_NAME = "p"
private const val RECORD_DURATION_OPT_NAME = "t"
private const val ACTIVITY_OPT_NAME = "a"
private const val OUTPUT_FILE_NAME_OPT_NAME = "o"

class Launcher(
    private val args: Array<String>
) {
    fun launch(): Int {
        val options = Options()
        options.addRequiredOption(PACKAGE_OPT_NAME, "package", true, "Target application package")
        options.addRequiredOption(RECORD_DURATION_OPT_NAME, "timeout", true, "Recording duration in seconds")
        options.addOption(ACTIVITY_OPT_NAME, "activity", true, "Application entry point activity")
        options.addOption(OUTPUT_FILE_NAME_OPT_NAME, "outFile", true, "Output trace file name")

        val parser = DefaultParser()
        val formatter = HelpFormatter()
        try {
            val cmd = parser.parse(options, args)
            initAndLaunch(cmd)
            return 0
        } catch (e: ParseException) {
            println(e.message)
            formatter.printHelp("Method trace recorder help:", options)

            return 1
        }
    }

    private fun initAndLaunch(cmd: CommandLine) {
        val packageName = cmd.getOptionValue(PACKAGE_OPT_NAME)
        val duration = Integer.valueOf(cmd.getOptionValue(RECORD_DURATION_OPT_NAME))
        val activity = cmd.getOptionValue(ACTIVITY_OPT_NAME)
        var outputFileName = cmd.getOptionValue(OUTPUT_FILE_NAME_OPT_NAME)
        if (outputFileName == null) {
            val sdf = SimpleDateFormat("yyyyMMdd_HH-mm-ss.SSS")
            val formattedTime = sdf.format(Date())
            outputFileName = "trace-$formattedTime.trace"
        }

        val listener = object : MethodTraceEventListener {
            override fun success(traceFile: File) {
                println("trace file saved at $traceFile")
                System.exit(0)
            }

            override fun fail(throwable: Throwable) {
                println(throwable.message)
                System.exit(1)
            }
        }

        val recorder = MethodTraceRecorder(packageName, outputFileName, listener)
        recorder.startRecording(activity)

        Thread.sleep(duration * 1000L)

        recorder.stopRecording()
    }
}