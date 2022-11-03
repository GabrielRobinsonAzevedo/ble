package com.example.ble

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import com.espressif.provisioning.ESPConstants
import com.espressif.provisioning.ESPProvisionManager
import com.espressif.provisioning.listeners.ResponseListener
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel
import org.greenrobot.eventbus.EventBus
import java.lang.Exception
import java.nio.charset.StandardCharsets


class MainActivity: FlutterActivity() {
    private val MAIN_CHANNEL = "tholz.com.br/main"
    private val PERMISSION_CHANNEL = "tholz.com.br/permissions"
    private val PROVISION_LISTENER_EVENT_CHANNEL = "tholz.com.br/provisionListener"
    private val SCAN_DEVICE_EVENT_CHANNEL = "tholz.com.br/scanDeviceStream"
    private val SCAN_WIFI_EVENT_CHANNEL = "tholz.com.br/scanWifiStream"
    private val CHANNEL2 = "com.ble/test3"
    private val CHANNEL3 = "tholz.com.br/test5"
    private val HANDLER_PROVISION_CHANNEL = "tholz.com.br/handlerProvision"
    private val MESSAGE_LISTENER_EVENT_CHANNEL = "tholz.com.br/messageListener"


    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {


        super.configureFlutterEngine(flutterEngine)
        val provisionManager: ESPProvisionManager = ESPProvisionManager.getInstance(applicationContext)
        val wifiScanListener = WifiScanListener(context = this, provisionManager = provisionManager)
        val bleScanListener = BleScanListener(context = this, provisionManager = provisionManager, wifiScanListener = wifiScanListener)
        val provisionListener = ProvisionListener()
        val responseListener = ResponseListener()

        /*
        *
        EVENT CHANNEL begin
        *
        */
        EventChannel(flutterEngine.dartExecutor.binaryMessenger, MESSAGE_LISTENER_EVENT_CHANNEL).setStreamHandler(responseListener)
        EventChannel(flutterEngine.dartExecutor.binaryMessenger, SCAN_WIFI_EVENT_CHANNEL).setStreamHandler(wifiScanListener)
        EventChannel(flutterEngine.dartExecutor.binaryMessenger, SCAN_DEVICE_EVENT_CHANNEL).setStreamHandler(bleScanListener)
        EventChannel(flutterEngine.dartExecutor.binaryMessenger, PROVISION_LISTENER_EVENT_CHANNEL).setStreamHandler(provisionListener)
        /*
        *
        EVENT CHANNEL end
        *
        */

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL3).setMethodCallHandler(wifiScanListener)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL2).setMethodCallHandler(bleScanListener)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, HANDLER_PROVISION_CHANNEL).setMethodCallHandler(provisionListener)
        fun byteArrayOfInts(vararg ints: Int) = ByteArray(ints.size) { pos -> ints[pos].toByte() }
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, MAIN_CHANNEL).setMethodCallHandler {
                call, result ->
            when (call.method) {
                "bluetoothInit" -> {
                    if(ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED){
                        provisionManager?.createESPDevice(ESPConstants.TransportType.TRANSPORT_BLE,ESPConstants.SecurityType.SECURITY_1)
                        provisionManager.searchBleEspDevices("THOLZ", bleScanListener)
                        result.success("ok")
                    }
                    else{
                        result.success("Sem permissão")
                    }
                }
                "startProvision" ->{
                    provisionManager.getEspDevice().provision(call.argument<String>("name")!!, call.argument<String>("password")!!, provisionListener)
                }
                "disconnect" -> {
                    provisionManager.getEspDevice().disconnectDevice()
                }
                "sendDataDeviceInfo" -> {
                    val arr = call.argument<String>("message")!!.toByteArray()
                    provisionManager.getEspDevice().sendDataToCustomEndPoint("deviceInfo", arr, responseListener)
                    println(provisionManager.espDevice.bluetoothDevice.uuids)
                }
                "sendDataParameterConfig" -> {
                    val arr = call.argument<String>("message")!!.toByteArray()
                    provisionManager.getEspDevice().sendDataToCustomEndPoint("parameterConfig", arr, responseListener)
                    println(provisionManager.espDevice.bluetoothDevice.uuids)
                }
                else -> result.notImplemented()
            }
        }
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, PERMISSION_CHANNEL).setMethodCallHandler {
          call, result ->
            when (call.method) {
                "bleConnectPermission" -> requestBluetoothPermission {
                        res -> result.success(res)
                }
                "verifyEnableBluetooth" -> verifyEnableBluetooth{
                        res -> result.success(res)
                }
                "gpsPermission" -> requestLocationPermission {
                        res -> result.success(res)
                }
                else -> result.notImplemented()
            }
        }

      }
    private fun requestBluetoothPermission(callback: (String) -> Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S){
            requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_CONNECT,Manifest.permission.BLUETOOTH_SCAN) , 0)
            callback("ANDROID 12")
        } else{
            requestPermissions(arrayOf(Manifest.permission.BLUETOOTH,Manifest.permission.BLUETOOTH_ADMIN) , 0)
            callback("ANDROID MENOR QUE 12")
        }


    }
    private fun requestLocationPermission(callback: (String) -> Unit) {
        requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0)
        callback("GPS")
    }
    private fun verifyEnableBluetooth(callback: (Boolean) -> Unit) {
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val bleAdapter: BluetoothAdapter = bluetoothManager.getAdapter()
        val noHasPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
        if(!noHasPermission){
        if(!bleAdapter.isEnabled){
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, 2)
            callback(false)
        }else{
            callback(true)
        }

    }
}}