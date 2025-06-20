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

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
