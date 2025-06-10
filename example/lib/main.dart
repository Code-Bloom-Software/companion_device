import 'package:companion_device/associated_device.dart';
import 'package:companion_device/companion_device_filter.dart';
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
  bool _loading = false;
  List<AssociatedDevice> _associations = [];

  @override
  void initState() {
    super.initState();
    _companionDevicePlugin.init();
    _companionDevicePlugin.getAssociations().then((result) {
      setState(() {
        _loading = false;
        _associations = result;
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            children: [
              ElevatedButton(
                  onPressed: () async {
                    setState(() {
                      _loading = true;
                    });
                    try {
                      await _companionDevicePlugin.associate(
                          isSingleDevice: true,
                          filter: CompanionDeviceFilter(
                              uuids: ['0000FE40-CC7A-482A-984A-7F2ED5B3E58C'])
                      );
                      _companionDevicePlugin.getAssociations().then((result) {
                        setState(() {
                          _loading = false;
                          _associations = result;
                        });
                      });
                    } catch (e) {
                      print(e);
                      setState(() {
                        _loading = false;
                      });
                    }
                  },
                  child: Text('Associate')
              ),
              const SizedBox(height: 32),
              ElevatedButton(
                  onPressed: () async {
                    setState(() {
                      _loading = true;
                    });
                    await _companionDevicePlugin.disassociate(
                        associationId: _associations[0].associationId,
                        deviceAddress: _associations[0].deviceAddress
                    );
                    _companionDevicePlugin.getAssociations().then((result) {
                      setState(() {
                        _loading = false;
                        _associations = result;
                      });
                    });
                  },
                  child: Text('Disassociate')
              ),
              const SizedBox(height: 32),
              _loading ? SizedBox(
                width: 20,
                height: 20,
                child: CircularProgressIndicator(strokeWidth: 2),
              ) : ListView(
                shrinkWrap: true,
                children: _associations.map((e) => Text('$e')).toList(),
              )
            ],
          ),
        ),
      ),
    );
  }
}
