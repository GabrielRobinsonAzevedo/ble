import 'package:flutter/material.dart';

class WifiOrComunicationPage extends StatelessWidget {
  const WifiOrComunicationPage({super.key});

  @override
  Widget build(BuildContext context) {
    Map? name = ModalRoute.of(context)?.settings.arguments as Map?;
    String name1 = name?['name'] ?? "Não encontrado";
    return Scaffold(
      appBar: AppBar(title: const Text('')),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            ElevatedButton(
                onPressed: () {
                  Navigator.of(context)
                      .pushNamed("/COMUNICATION", arguments: {"name": name1});
                },
                child: const Text('Comunicação')),
            const SizedBox(height: 60),
            ElevatedButton(
                onPressed: () {
                  Navigator.of(context)
                      .pushNamed("/WIFI", arguments: {"name": name1});
                },
                child: const Text('Wifi')),
          ],
        ),
      ),
    );
  }
}
