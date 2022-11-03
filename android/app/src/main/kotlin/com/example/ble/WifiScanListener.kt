package com.example.ble


import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.annotation.NonNull
import com.espressif.provisioning.ESPProvisionManager
import com.espressif.provisioning.WiFiAccessPoint
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

open class WifiScanListener(private var context: Context, var provisionManager: ESPProvisionManager): com.espressif.provisioning.listeners.WiFiScanListener, FlutterPlugin, MethodChannel.MethodCallHandler,
    EventChannel.StreamHandler {
    private lateinit var channel: MethodChannel
    private var messageChannel: EventChannel? = null
    private var eventSink: EventChannel.EventSink? = null
    private  var wifiList:ArrayList<WiFiAccessPoint>? = null


    override fun onListen(arguments: Any?, eventSink: EventChannel.EventSink?) {
        this.eventSink = eventSink
    }

    override fun onCancel(arguments: Any?) {
        eventSink = null
        messageChannel = null
    }

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {

        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "tholz.com.br/test5")
        channel.setMethodCallHandler(this)

        messageChannel = EventChannel(flutterPluginBinding.binaryMessenger, "tholz.com.br/scanWifiStream")
        messageChannel?.setStreamHandler(this)
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: MethodChannel.Result) {
        when (call.method) {
            "TESTE" ->{
                if (wifiList != null){
                    for (i in wifiList!!){
                        var map = mapOf("name" to i.wifiName, "rssi" to (i.rssi), "security" to (i.security))
                        eventSink?.success(map)
                    }
                    result.success("teste2")
                }else{
                    result.success("teste")
                }

            }
            else -> {
                result.notImplemented()
            }
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }


    override fun onWifiListReceived(wifiList1: ArrayList<WiFiAccessPoint>?) {
        if (wifiList1 != null) {
            wifiList = wifiList1
        }
        if (wifiList != null) {
            for (i in wifiList!!) {
                var map = mapOf("name" to i.wifiName)
                eventSink?.success(map)
            }
        }
    }
    override fun onWiFiScanFailed(e: Exception?) {
        TODO("Not yet implemented")
    }
}


