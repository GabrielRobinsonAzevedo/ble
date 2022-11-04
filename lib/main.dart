import 'package:ble/pages/comunication.dart';
import 'package:ble/pages/pg.dart';
import 'package:ble/pages/verification.dart';
import 'package:ble/pages/wifi.dart';
import 'package:ble/pages/wifi_or_comunication.dart';
import 'package:flutter/material.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      routes: {
        '/': (context) => const PaginaProvision(),
        '/DECIDIR': (context) => const WifiOrComunicationPage(),
        '/WIFI': (context) => const WifiPage(),
        '/COMUNICATION': (context) => const ComunicationPage(),
        '/VERIFY': (context) => const VerificationPage(),
      },
    );
  }
}
