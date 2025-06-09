
import 'companion_device_platform_interface.dart';

class CompanionDevice {
  Future<String?> getPlatformVersion() {
    return CompanionDevicePlatform.instance.getPlatformVersion();
  }
}
