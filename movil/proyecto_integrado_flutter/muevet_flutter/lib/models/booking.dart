import 'package:muevet_flutter/models/user.dart';

import 'vehicle.dart';

class Booking {
  final int id;
  final User user;
  final Vehicle vehicle;
  final String startDate;
  final String endDate;
  final String status;

  Booking({
    required this.id,
    required this.user,
    required this.vehicle,
    required this.startDate,
    required this.endDate,
    required this.status,
  });

  factory Booking.fromJson(Map<String, dynamic> json) {
    return Booking(
      id: json['id'],
      user: User.fromJson(json['user']),
      vehicle: Vehicle.fromJson(json['vehicle']),
      startDate: json['startDate'],
      endDate: json['endDate'],
      status: json['status'],
    );
  }
}
