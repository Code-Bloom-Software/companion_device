import 'package:flutter_test/flutter_test.dart';
import 'package:companion_device/companion_device.dart';
import 'package:companion_device/companion_device_platform_interface.dart';
import 'package:companion_device/companion_device_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockCompanionDevicePlatform
    with MockPlatformInterfaceMixin
    implements CompanionDevicePlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final CompanionDevicePlatform initialPlatform = CompanionDevicePlatform.instance;

  test('$MethodChannelCompanionDevice is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelCompanionDevice>());
  });

  test('getPlatformVersion', () async {
    CompanionDevice companionDevicePlugin = CompanionDevice();
    MockCompanionDevicePlatform fakePlatform = MockCompanionDevicePlatform();
    CompanionDevicePlatform.instance = fakePlatform;

    expect(await companionDevicePlugin.getPlatformVersion(), '42');
  });
}
