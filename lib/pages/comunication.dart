import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class ComunicationPage extends StatefulWidget {
  const ComunicationPage({super.key});

  @override
  State<ComunicationPage> createState() => _ComunicationPageState();
}

class _ComunicationPageState extends State<ComunicationPage> {
  final TextEditingController _controller = TextEditingController();
  static const provisionPlatform = MethodChannel('tholz.com.br/main');
  bool isSended = true;
  bool isAplied = false;
  bool isChecked = false;
  bool handler = true;
  Stream<String> streamTimeFromNative() {
    const _timeChannel = EventChannel('tholz.com.br/messageListener');
    return _timeChannel
        .receiveBroadcastStream()
        .map((event) => event.toString());
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Comunicação'),
      ),
      body: Center(
        child: Column(
          children: [
            Container(
              width: 300,
              child: TextField(
                controller: _controller,
              ),
            ),
            ElevatedButton(
                onPressed: () {
                  provisionPlatform.invokeMethod('sendDataDeviceInfo',
                      {"message": _controller.text.toString()});
                },
                child: const Text('Teste envia Device')),
            ElevatedButton(
                onPressed: () {
                  provisionPlatform.invokeMethod('sendDataParameterConfig',
                      {"message": _controller.text.toString()});
                },
                child: const Text('Teste envia Parameter')),
            Center(
              child: StreamBuilder<String>(
                stream: streamTimeFromNative(),
                builder: (context, snapshot) {
                  if (snapshot.hasData) {
                    return Text(
                      '${snapshot.data}',
                      style: Theme.of(context).textTheme.headline4,
                    );
                  } else {
                    return CircularProgressIndicator();
                  }
                },
              ),
            ),
          ],
        ),
      ),
    );
  }
}
