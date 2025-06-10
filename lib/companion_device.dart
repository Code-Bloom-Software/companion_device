
import 'companion_device_platform_interface.dart';

class CompanionDevice {

  Future<void> init() {
    return CompanionDevicePlatform.instance.init();
  }

  Future<void> associate() {
    return CompanionDevicePlatform.instance.associate();
  }
}
