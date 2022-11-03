package com.example.ble

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
        var teste = String(returnData!!, StandardCharsets.UTF_8)
        println("-----------------------------")
        println(teste.removeRange(teste.length-1, teste.length ))
        println("-----------------------------")
        println("eventSink1 é $eventSink1")
        var teste1 = teste.removeRange(teste.length-1, teste.length )
        eventSink1?.success(teste1)
        println("-----------------------------")
        println(teste.removeRange(teste.length-1, teste.length ))
        println("-----------------------------")
    }

    override fun onFailure(e: Exception?) {
        println("-----------------------------")
        println("--------TESTEE-----------")
        println("-----------------------------")
    }
}
