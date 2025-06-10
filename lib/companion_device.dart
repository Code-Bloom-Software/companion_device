import 'package:companion_device/associated_device.dart';
import 'package:companion_device/companion_device_filter.dart';

import 'companion_device_platform_interface.dart';

class CompanionDevice {

  Future<void> init() {
    return CompanionDevicePlatform.instance.init();
  }

  Future<AssociatedDevice> associate({
    required bool isSingleDevice,
    Duration timeout = const Duration(seconds: 15),
    CompanionDeviceFilter? filter
  }) {
    return CompanionDevicePlatform.instance.associate(
        isSingleDevice: isSingleDevice,
        timeout: timeout,
        filter: filter
    );
  }

  Future<void> disassociate({int? associationId, String? deviceAddress}) {
    return CompanionDevicePlatform.instance.disassociate(
        associationId: associationId,
        deviceAddress: deviceAddress
    );
  }

  Future<List<AssociatedDevice>> getAssociations() async {
    return CompanionDevicePlatform.instance.getAssociations();
  }
}
