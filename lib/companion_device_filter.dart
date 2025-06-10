class CompanionDeviceFilter {
  final String? nameRegex;
  final String? deviceAddress;
  final List<String> uuids;

  CompanionDeviceFilter({
    this.nameRegex,
    this.deviceAddress,
    this.uuids = const []
  });
}