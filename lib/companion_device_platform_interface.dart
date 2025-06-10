import 'package:companion_device/associated_device.dart';
import 'package:companion_device/companion_device_filter.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'companion_device_method_channel.dart';

abstract class CompanionDevicePlatform extends PlatformInterface {
  /// Constructs a CompanionDevicePlatform.
  CompanionDevicePlatform() : super(token: _token);

  static final Object _token = Object();

  static CompanionDevicePlatform _instance = MethodChannelCompanionDevice();

  /// The default instance of [CompanionDevicePlatform] to use.
  ///
  /// Defaults to [MethodChannelCompanionDevice].
  static CompanionDevicePlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [CompanionDevicePlatform] when
  /// they register themselves.
  static set instance(CompanionDevicePlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<void> init() {
    throw UnimplementedError('init() has not been implemented.');
  }

  Future<AssociatedDevice> associate({
    required bool isSingleDevice,
    required Duration timeout,
    CompanionDeviceFilter? filter
  }) {
    throw UnimplementedError('associate() has not been implemented.');
  }

  Future<void> disassociate({int? associationId, String? deviceAddress}) {
    throw UnimplementedError('disassociate() has not been implemented.');
  }

  Future<List<AssociatedDevice>> getAssociations() {
    throw UnimplementedError('getAssociations() has not been implemented.');
  }
}
