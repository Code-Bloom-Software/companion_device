class AssociatedDevice {
  final int? associationId;
  final String? name;
  final String? deviceAddress;

  AssociatedDevice({
    required this.associationId,
    required this.name,
    required this.deviceAddress
  });

  @override
  String toString() {
    return "associationId: $associationId, name: $name, deviceAddress: $deviceAddress";
  }
}