import 'package:flutter/material.dart';
import 'package:companion_device/companion_device.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final _companionDevicePlugin = CompanionDevice();

  @override
  void initState() {
    super.initState();
    _companionDevicePlugin.init();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: ElevatedButton(
              onPressed: () {
                _companionDevicePlugin.associate();
              },
              child: Text('Associate')
          ),
        ),
      ),
    );
  }
}
