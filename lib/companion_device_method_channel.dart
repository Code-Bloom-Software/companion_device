import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'companion_device_platform_interface.dart';

/// An implementation of [CompanionDevicePlatform] that uses method channels.
class MethodChannelCompanionDevice extends CompanionDevicePlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('companion_device');

  @override
  Future<void> init() async {
    await methodChannel.invokeMethod<void>('init');
  }

  @override
  Future<void> associate() async {
    await methodChannel.invokeMethod<void>('associate');
  }
}
