package com.example.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import com.espressif.provisioning.DeviceConnectionEvent
import com.espressif.provisioning.ESPConstants
import com.espressif.provisioning.ESPProvisionManager
import com.example.ble.models.BleDevice
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


open class BleScanListener(private var context: Context, var provisionManager: ESPProvisionManager, var wifiScanListener: WifiScanListener) : FlutterPlugin, MethodChannel.MethodCallHandler,
    EventChannel.StreamHandler,
    com.espressif.provisioning.listeners.BleScanListener {

    private var deviceList: ArrayList<BleDevice> = ArrayList<BleDevice>()
    private var bluetoothDevices: HashMap<BluetoothDevice, String> =  HashMap<BluetoothDevice, String>()


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: DeviceConnectionEvent): Unit{
        when (event.eventType) {
            ESPConstants.EVENT_DEVICE_CONNECTED -> {
                val map = mapOf("connect" to "true", "info" to provisionManager.espDevice.versionInfo)
                eventSink?.success(map)
                eventSink?.endOfStream()
                provisionManager.espDevice.scanNetworks(wifiScanListener)
                println("TESTE")
                println(provisionManager.espDevice.bluetoothDevice)
                provisionManager.espDevice.refreshServicesOfBleDevice()
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                println(provisionManager.espDevice.bluetoothDevice.uuids)
                println("TESTE")
            }
            ESPConstants.EVENT_DEVICE_CONNECTION_FAILED -> {
                println("Falhou")
            }
            ESPConstants.EVENT_DEVICE_DISCONNECTED -> {
                println("Desconectado")
            }

        }
    }
    private lateinit var channel: MethodChannel
    private var messageChannel: EventChannel? = null
    private var eventSink: EventChannel.EventSink? = null


    override fun onListen(arguments: Any?, eventSink: EventChannel.EventSink?) {
        this.eventSink = eventSink
    }

    override fun onCancel(arguments: Any?) {
        eventSink = null
        messageChannel = null
    }

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "com.ble/test3")
        channel.setMethodCallHandler(this)

        messageChannel = EventChannel(flutterPluginBinding.binaryMessenger, "tholz.com.br/scanDeviceStream")
        messageChannel?.setStreamHandler(this)
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: MethodChannel.Result) {
        when (call.method) {
            "bluetoothConnect" ->{
                val bleDevice: BleDevice = deviceList[call.argument<Int>("indice")!!]
                val uuid = bluetoothDevices[bleDevice.bluetoothDevice]
                println(bleDevice.bluetoothDevice)
                provisionManager.getEspDevice().connectBLEDevice(bleDevice.bluetoothDevice, uuid);
                provisionManager.espDevice.bluetoothDevice = bleDevice.bluetoothDevice
                provisionManager.espDevice.proofOfPossession = "abcd1234"
            }
            "reloadDevices" -> {
                deviceList.clear()
                bluetoothDevices.clear()
            }
            "registerEventBus" -> {
                EventBus.getDefault().register(this)
            }
            else -> {
                result.notImplemented()
            }
        }
    }


    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun scanStartFailed() {
        TODO("Not yet implemented")
    }

    @SuppressLint("MissingPermission")
    override fun onPeripheralFound(device: BluetoothDevice, scanResult: ScanResult) {
        var deviceExists = false

        var serviceUuid = ""

        if (scanResult.scanRecord!!.serviceUuids != null && scanResult.scanRecord!!
                .serviceUuids.size > 0
        ) {
            serviceUuid = scanResult.scanRecord!!.serviceUuids[0].toString()
        }
        if (bluetoothDevices.containsKey(device)) {
            deviceExists = true;
        }
        if (!deviceExists) {
            val bleDevice = BleDevice()
            bleDevice.name = scanResult.scanRecord!!.deviceName
           println("${bleDevice.name} NAME")
            bleDevice.bluetoothDevice = device
            bluetoothDevices.put(device, serviceUuid)
           println("${serviceUuid} serviceUuid")
            deviceList!!.add(bleDevice)
            var map = mapOf("name" to "${bleDevice.name}", "index" to (deviceList.size - 1))
            println("eventSink BLE  é $eventSink")
            eventSink?.success(map)
            println("depois do sink")
        }
    }

    override fun scanCompleted() {

    }

    override fun onFailure(e: Exception?) {
        println("${e!!.message} SOU EU")
        e.printStackTrace()
    }
}