package com.example.ble

import android.os.Handler
import android.os.Looper
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel
import java.lang.Exception
import java.nio.charset.StandardCharsets

open class ResponseListener: FlutterPlugin,
EventChannel.StreamHandler, com.espressif.provisioning.listeners.ResponseListener {
    private lateinit var channel: MethodChannel
    private var messageChannel1: EventChannel? = null
    private var eventSink1: EventChannel.EventSink? = null



    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        this.eventSink1 = events
    }

    override fun onCancel(arguments: Any?) {
        eventSink1 = null
        messageChannel1 = null
    }
    override fun onAttachedToEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        messageChannel1 = EventChannel(binding.binaryMessenger,"tholz.com.br/messageListener")
        messageChannel1!!.setStreamHandler(this)
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onSuccess(returnData: ByteArray?) {

        var data = String(returnData!!, StandardCharsets.UTF_8)
        println("-----------------------------")
        var message = data.removeRange(data.length-1, data.length )
        println(message)
        println("-----------------------------")
        println("eventSink1 é $eventSink1")
        try {
            var eventSinkTESTE = EventSinkOnMain(eventSink1!!)
            eventSinkTESTE.success(message)
        } catch (e: Exception){
            println(e)
        }

        println("-----------------------------")
        println(message)
        println("-----------------------------")
    }

    override fun onFailure(e: Exception?) {
        println("-----------------------------")
        println("--------TESTEE-----------")
        println("-----------------------------")
    }
}
fun EventChannel.EventSink.onMain(): EventSinkOnMain {
    return if (this is EventSinkOnMain) {
        this
    } else {
        EventSinkOnMain(this)
    }
}

class EventSinkOnMain internal constructor(private val result: EventChannel.EventSink) : EventChannel.EventSink {
    private val handler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }

    override fun success(res: Any?) {
        handler.post { result.success(res) }
    }

    override fun error(
        errorCode: String, errorMessage: String?, errorDetails: Any?) {
        handler.post { result.error(errorCode, errorMessage, errorDetails) }
    }

    override fun endOfStream() {
        TODO("Not yet implemented")
    }

}