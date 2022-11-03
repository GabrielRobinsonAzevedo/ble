import 'dart:ui';

import 'package:ble/model/wifi.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class WifiPage extends StatefulWidget {
  const WifiPage({Key? key}) : super(key: key);

  @override
  State<WifiPage> createState() => _WifiPageState();
}

class _WifiPageState extends State<WifiPage> {
  static const wifiEventChannel = EventChannel('tholz.com.br/scanWifiStream');

  static const initPlatform = MethodChannel('tholz.com.br/test5');
  static const provisionPlatform = MethodChannel('tholz.com.br/main');
  final TextEditingController _controller = TextEditingController();
  List<Wifi> listWifi = [];
  String? password;
  bool isSending = false;
  bool isLoading = true;
  @override
  void initState() {
    handlerScan();
    broadCastInit();
    super.initState();
  }

  void broadCastInit() {
    wifiEventChannel
        .receiveBroadcastStream()
        .listen((message) => message)
        .onData((message) {
      setState(() {
        isLoading = false;
        listWifi.add(Wifi(
            rssi: message['rssi'],
            name: message['name'],
            security: message['security']));
        listWifi.sort((a, b) => a.rssi.compareTo(b.rssi));
      });
    });
  }

  Future<String> handlerScan() async {
    String? finalResult;
    while (isLoading) {
      await Future.delayed(const Duration(seconds: 1));
      String result = await initPlatform.invokeMethod('TESTE');
      finalResult = result;
    }
    return finalResult ?? "carregando";
  }

  @override
  Widget build(BuildContext context) {
    Map? name = ModalRoute.of(context)?.settings.arguments as Map?;
    String name1 = name?['name'] ?? "Não encontrado";
    return Scaffold(
      appBar: AppBar(
        title: const Text("Selecione o Wi-fi"),
      ),
      body: isLoading
          ? const Center(
              child: CircularProgressIndicator(),
            )
          : SingleChildScrollView(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.spaceAround,
                children: [
                  Text(
                    'PDX$name1',
                    style: TextStyle(fontSize: 30),
                  ),
                  Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    crossAxisAlignment: CrossAxisAlignment.center,
                    children: [
                      const Text(
                        'Selecione o Wi-fi que deseja conectar',
                        style: TextStyle(fontSize: 20),
                      ),
                      Column(
                        children: List.generate(
                            listWifi.length,
                            (index) => Padding(
                                  padding: const EdgeInsets.all(8.0),
                                  child: InkWell(
                                    onTap: () => dialog(context, index),
                                    child: Container(
                                      height: 35,
                                      color: Colors.blue,
                                      child: Row(
                                        mainAxisAlignment:
                                            MainAxisAlignment.spaceEvenly,
                                        children: [
                                          Text(listWifi[index].name),
                                          Divider(),
                                          Text(listWifi[index].rssi.toString()),
                                          Divider(),
                                          Text(listWifi[index]
                                              .security
                                              .toString())
                                        ],
                                      ),
                                    ),
                                  ),
                                )),
                      ),
                    ],
                  ),
                ],
              ),
            ),
    );
  }

  Future<void> dialog(BuildContext context, int index) async {
    bool isObscure = true;

    showDialog(
      context: context,
      builder: (_) {
        return AlertDialog(
          content: Padding(
            padding: const EdgeInsets.all(8),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [Text(listWifi[index].name)],
                ),
                TextFormField(
                  controller: _controller,
                  keyboardType: TextInputType.text,
                  obscureText: false,
                  decoration: const InputDecoration(
                    hintText: "Digite a senha da rede",
                    hintStyle: TextStyle(
                      fontWeight: FontWeight.normal,
                      color: Colors.grey,
                    ),
                    enabledBorder: UnderlineInputBorder(
                      borderSide: BorderSide(
                        color: Colors.blue,
                      ),
                    ),
                    focusedBorder: UnderlineInputBorder(
                      borderSide: BorderSide(
                        color: Colors.blue,
                      ),
                    ),
                    contentPadding: EdgeInsets.all(16),
                    fillColor: Colors.white,
                    filled: true,
                  ),
                ),
                const SizedBox(
                  height: 30,
                ),
                ElevatedButton(
                    onPressed: () {
                      password = _controller.text;
                      isSending = true;
                      provisionPlatform.invokeMethod('startProvision',
                          {"name": listWifi[index].name, "password": password});
                      Navigator.of(context).pushReplacementNamed('/VERIFY');
                    },
                    child: const Text('Conectar'))
              ],
            ),
          ),
        );
      },
    );
  }
}
