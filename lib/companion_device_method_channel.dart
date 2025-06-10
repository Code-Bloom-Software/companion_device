import 'package:companion_device/associated_device.dart';
import 'package:companion_device/companion_device_filter.dart';
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
  Future<AssociatedDevice> associate({
    required bool isSingleDevice,
    required Duration timeout,
    CompanionDeviceFilter? filter
  }) async {
    final result = await methodChannel.invokeMethod<Map>('associate',
        [isSingleDevice, timeout.inMilliseconds,
          filter?.nameRegex, filter?.deviceAddress, filter?.uuids]);

    return AssociatedDevice(
        associationId: result?['association_id'],
        name: result?['name'],
        deviceAddress: result?['mac_address']
    );
  }

  @override
  Future<void> disassociate({int? associationId, String? deviceAddress}) async {
    await methodChannel.invokeMethod<void>('disassociate', [associationId, deviceAddress]);
  }

  @override
  Future<List<AssociatedDevice>> getAssociations() async {
    final result =  await methodChannel.invokeMethod<List>('associations');

    return result?.map((e) => AssociatedDevice(
        associationId: e['association_id'],
        name: e['name'],
        deviceAddress: e['mac_address']
    )).toList() ?? [];
  }
}
