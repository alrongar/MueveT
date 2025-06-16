import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:muevet_flutter/models/booking.dart';
import 'package:muevet_flutter/services/user_service.dart';
import 'package:shared_preferences/shared_preferences.dart';

class BookingService {
  final String baseUrl = 'http://16.171.42.106:8080/api';
  Future<List<Booking>> fetchUserBookings() async {
    final prefs = await SharedPreferences.getInstance();
    final token = prefs.getString('jwt');

    final response = await http.get(
      Uri.parse(baseUrl + '/getBookings'),
      headers: {'Authorization': 'Bearer $token'},
    );

    if (response.statusCode == 200) {
      final List<dynamic> jsonList = jsonDecode(response.body);
      final bookings = jsonList.map((e) => Booking.fromJson(e)).toList();
      print('Bookings cargadas: ${bookings.length}');
      return bookings;
    } else {
      throw Exception('no se encontraron reservas');
    }
  }

  Future<bool> cancelBooking(int bookingId) async {
    try {
      SharedPreferences prefs = await SharedPreferences.getInstance();
      final token = prefs.getString('jwt');

      if (token == null) return false;

      final response = await http.delete(
        Uri.parse('$baseUrl/deleteBooking/$bookingId'),
        headers: {'Authorization': 'Bearer $token'},
      );

      return response.statusCode == 200;
    } catch (_) {
      return false;
    }
  }

  Future<List<Booking>> fetchBookingHistory() async {
    final prefs = await SharedPreferences.getInstance();
    final token = prefs.getString('jwt');

    if (token == null) {
      throw Exception('No hay token disponible');
    }

    final response = await http.get(
      Uri.parse('$baseUrl/getBookingHistory'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode == 200) {
      final List<dynamic> jsonData = jsonDecode(response.body);
      return jsonData.map((item) => Booking.fromJson(item)).toList();
    } else {
      throw Exception('Error al cargar historial: ${response.statusCode}');
    }
  }

  Future<void> createBooking({
    required String licensePlate,
    required DateTime startDate,
    required DateTime endDate,
  }) async {
    final prefs = await SharedPreferences.getInstance();
    final jwt = prefs.getString('jwt');

    if (jwt == null) throw Exception('Token no encontrado');

    final user = await UserService().getUserFromJwt();
    final userId = user['id'];

    final bookingData = {
      'user': {'id': userId},
      'vehicle': {'licensePlate': licensePlate},
      'startDate': startDate.toIso8601String().substring(0, 10),
      'endDate': endDate.toIso8601String().substring(0, 10),
      'status': 'ACTIVE',
    };

    final response = await http.post(
      Uri.parse('$baseUrl/createBooking'),
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer $jwt',
      },
      body: jsonEncode(bookingData),
    );

    if (response.statusCode != 200) {
      final error = jsonDecode(utf8.decode(response.bodyBytes))['error'];
      throw Exception(error ?? 'Error al crear la reserva.');
    }
  }
}
