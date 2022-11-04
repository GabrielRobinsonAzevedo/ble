// ignore_for_file: avoid_print

import 'dart:convert';

import 'package:ble/model/bleDevice.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class PaginaProvision extends StatefulWidget {
  const PaginaProvision({Key? key}) : super(key: key);

  @override
  State<PaginaProvision> createState() => _PaginaProvisionState();
}

class _PaginaProvisionState extends State<PaginaProvision> {
  static const permissionPlatform = MethodChannel('tholz.com.br/permissions');
  static const platformBle = MethodChannel('com.ble/test3');
  static const bluetoothEventChannel =
      EventChannel('tholz.com.br/scanDeviceStream');
  bool isListening = true;

  static const bluetoothInitPlatform = MethodChannel('tholz.com.br/main');
  List<BleDevice> deviceList = [];
  bool isConnecting = false;

  Future<void> bluetoothInit() async {
    platformBle.invokeMethod('reloadDevices');
    setState(() {
      deviceList.clear();
    });
    try {
      String result = await bluetoothInitPlatform.invokeMethod('bluetoothInit');
      print(result);
    } on PlatformException catch (e) {
      print(e);
    }
  }

  Future<void> gpsPermission() async {
    platformBle.invokeMethod('registerEventBus');
    try {
      String result = await permissionPlatform.invokeMethod('gpsPermission');
      print(result);
    } on PlatformException catch (e) {
      print(e);
    }
  }

  Future<void> bleConnectPermission() async {
    try {
      String result =
          await permissionPlatform.invokeMethod('bleConnectPermission');
      print(result);
    } on PlatformException catch (e) {
      print(e);
    }
  }

  Future<bool> bleEnable() async {
    try {
      bool result =
          await permissionPlatform.invokeMethod('verifyEnableBluetooth');
      return result;
    } on PlatformException catch (e) {
      print(e);
      return false;
    }
  }

  Future<void> bluetoothConnect(int index) async {
    setState(() {
      isConnecting = true;
    });
    try {
      String result = await platformBle.invokeMethod(
          'bluetoothConnect', {"indice": deviceList[index].index});
      print(result);
    } on PlatformException catch (e) {
      print(e);
    }
  }

  Stream<Map<dynamic, dynamic>> get messageStream async* {
    await for (Map<dynamic, dynamic> message in bluetoothEventChannel
        .receiveBroadcastStream()
        .map((message) => message)) {
      String? productName;
      if (message['info'] != null) {
        message.forEach((key, value) {
          if (key.contains("info")) {
            Map<String, dynamic> teste = jsonDecode(value);
            teste.forEach((_key, _value) {
              if (_key.contains("Tholz_")) {
                productName = _key.replaceAll("Tholz_", "");
              }
            });
          }
        });
      }
      if (message['connect'] != null) {
        isListening = false;
        isConnecting = false;
        Navigator.of(context)
            .pushNamed("/DECIDIR", arguments: {"name": productName});
      } else {
        setState(() {
          deviceList
              .add(BleDevice(index: message['index'], name: message['name']));
          deviceList.sort((a, b) => a.index.compareTo(b.index));
        });
        yield message;
      }
    }
  }

  @override
  void initState() {
    super.initState();
    gpsPermission();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('BLE PROVISION'),
      ),
      body: Center(
          child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              ElevatedButton(
                  onPressed: bleConnectPermission,
                  child: const Text('PermissionConnect')),
              ElevatedButton(
                  onPressed: () async {
                    isConnecting = false;
                    bool isBleEnable = await bleEnable();
                    if (isBleEnable) bluetoothInit();
                  },
                  child: const Text('Scan device')),
              if (isListening)
                StreamBuilder<Map<dynamic, dynamic>>(
                    stream: messageStream,
                    builder: (BuildContext context,
                        AsyncSnapshot<Map<dynamic, dynamic>> snapshot) {
                      if (snapshot.hasData) {
                        return Text("Current Device: ${snapshot.data}");
                      } else {
                        return Text("Waiting for data...");
                      }
                    }),
            ],
          ),
          Container(
            color: Colors.lime,
            child: Column(
                children: List.generate(
                    deviceList.length,
                    (index) => Padding(
                          padding: const EdgeInsets.all(20),
                          child: Container(
                            height: 50,
                            width: double.infinity,
                            color: isConnecting ? Colors.grey : Colors.blue,
                            child: Center(
                              child: TextButton(
                                onPressed: () async => isConnecting
                                    ? () {}
                                    : bluetoothConnect(index),
                                child: Text(
                                  deviceList[index].name,
                                  style: TextStyle(color: Colors.white),
                                ),
                              ),
                            ),
                          ),
                        ))),
          )
        ],
      )),
    );
  }
}
