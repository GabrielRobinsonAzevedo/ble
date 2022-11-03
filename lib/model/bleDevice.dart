class BleDevice {
  String name;
  int index;
  BleDevice({required this.index, required this.name});

  BleDevice copyWith(Map<dynamic, dynamic> event) {
    return BleDevice(
        index: event['index'] ?? index, name: event['name'] ?? name);
  }
}
