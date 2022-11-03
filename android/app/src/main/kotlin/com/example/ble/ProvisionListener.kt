package com.example.ble

import androidx.annotation.NonNull
import com.espressif.provisioning.ESPConstants
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

class ProvisionListener : FlutterPlugin,
    EventChannel.StreamHandler,
    com.espressif.provisioning.listeners.ProvisionListener, MethodChannel.MethodCallHandler  {
    private lateinit var channel: MethodChannel
    private var messageChannel: EventChannel? = null
    private var eventSink: EventChannel.EventSink? = null

    private var provisioningMoment: String? = null

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "tholz.com.br/handlerProvision")
        channel.setMethodCallHandler(this)

        messageChannel = EventChannel(flutterPluginBinding.binaryMessenger, "tholz.com.br/provisionListener")
        messageChannel?.setStreamHandler(this)
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: MethodChannel.Result) {
        when (call.method) {
            "handlerProvision" ->{
                when(provisioningMoment) {
                    null -> {
                        result.success("not init")
                    }
                    "applied" ->{
                        result.success("applied")
                    }
                    "sent" ->{
                        result.success("sent")
                    }
                    "finish" ->{
                        eventSink?.success("finish")
                        result.success("finish")
                    }
                    else-> {
                        result.success(provisioningMoment)
                    }

                }
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        this.eventSink = eventSink
    }

    override fun onCancel(arguments: Any?) {
        eventSink = null
        messageChannel = null
    }
//Provision
    override fun createSessionFailed(e: Exception?) {
        println("createSessionFailed")
        provisioningMoment = "error"
    }

    override fun wifiConfigSent() {
        println("wifiConfigSent")
        provisioningMoment = "sent"
    }

    override fun wifiConfigFailed(e: Exception?) {
        println("wifiConfigFailed")
        provisioningMoment = "error"
    }

    override fun wifiConfigApplied() {
        println("wifiConfigApplied")
        provisioningMoment = "applied"
    }

    override fun wifiConfigApplyFailed(e: Exception?) {
        println("wifiConfigApplyFailed")
        provisioningMoment = "error"
    }

    override fun provisioningFailedFromDevice(failureReason: ESPConstants.ProvisionFailureReason?) {
        println("provisioningFailedFromDevice")
        provisioningMoment = "error"
        when (failureReason) {
            ESPConstants.ProvisionFailureReason.AUTH_FAILED -> {
                provisioningMoment = "erro autenticação"
                            }
            ESPConstants.ProvisionFailureReason.NETWORK_NOT_FOUND -> {
                provisioningMoment = "Rede não encontrada"
                            }
            ESPConstants.ProvisionFailureReason.DEVICE_DISCONNECTED -> {
                provisioningMoment = "device desconectado"
                            }
            ESPConstants.ProvisionFailureReason.UNKNOWN -> {
                provisioningMoment = "erro"
                            }
        }
    }

    override fun deviceProvisioningSuccess() {
        println("deviceProvisioningSuccess")
        provisioningMoment = "finish"
    }

    override fun onProvisioningFailed(e: Exception?) {
        println("onProvisioningFailed")
        provisioningMoment = "error"
    }
}