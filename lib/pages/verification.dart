import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class VerificationPage extends StatefulWidget {
  const VerificationPage();

  @override
  State<VerificationPage> createState() => _VerificationPageState();
}

class _VerificationPageState extends State<VerificationPage> {
  final TextEditingController _controller = TextEditingController();
  static const _handlerProvision =
      MethodChannel('tholz.com.br/handlerProvision');
  static const _handlerMessage = EventChannel("tholz.com.br/messageListener");
  static const provisionPlatform = MethodChannel('tholz.com.br/main');
  bool isSended = true;
  bool isAplied = false;
  bool isChecked = false;
  bool handler = true;

  Future<String> handlerProvision() async {
    while (handler) {
      await Future.delayed(const Duration(seconds: 1));
      String result = await _handlerProvision.invokeMethod('handlerProvision');
      print(result);
      if (result == "finish") {
        setState(() {
          handler = false;
          isChecked = true;
        });
        showDialog(
            context: context,
            builder: (_) {
              return const AlertDialog(
                content: Text('Provisionamento concluído'),
              );
            });
        return 'ok';
      } else if (result == "not init") {
      } else if (result == "applied") {
        setState(() {
          isAplied = true;
        });
      } else if (result == "sent") {
        setState(() {
          isSended = true;
        });
      } else {
        handler = false;
        showDialog(
            context: context,
            builder: (_) {
              return AlertDialog(
                content: Text(result),
              );
            });
        return 'erro';
      }
    }
    return '';
  }

  Stream<String> streamTimeFromNative() {
    const _timeChannel = EventChannel('tholz.com.br/messageListener');
    return _timeChannel
        .receiveBroadcastStream()
        .map((event) => event.toString());
  }

  // Stream<String> get messageStream async* {
  //   await for (String message
  //       in _handlerMessage.receiveBroadcastStream().map((message) {
  //     print(message);
  //     return message;
  //   })) {
  //     setState(() {});
  //     yield message;
  //   }
  // }

  @override
  void initState() {
    super.initState();
    handlerProvision();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text("Verificação"),
      ),
      body: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(
                isSended ? Icons.check : Icons.error,
              ),
              Text("Dados enviados.")
            ],
          ),
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(
                isAplied ? Icons.check : Icons.error,
              ),
              Text("Dados recebidos pelo controlador.")
            ],
          ),
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(
                isChecked ? Icons.check : Icons.error,
              ),
              const Text("Concluído.")
            ],
          ),
          ElevatedButton(
              onPressed: () {
                provisionPlatform.invokeMethod('disconnect');
                Navigator.of(context).popAndPushNamed("/");
              },
              child: const Text('Voltar')),
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
          StreamBuilder<String>(
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
        ],
      ),
    );
  }
}
