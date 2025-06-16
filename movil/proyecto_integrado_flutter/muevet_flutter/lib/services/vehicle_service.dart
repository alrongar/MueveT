import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:muevet_flutter/models/vehicle.dart';
import 'package:shared_preferences/shared_preferences.dart';

class VehicleService {
  final String baseUrl = 'localhost:8080';

  Future<List<Vehicle>> fetchVehicles({
    String? searchField,
    String? searchValue,
    int? yearMin,
    int? yearMax,
    bool? available,
  }) async {
    final prefs = await SharedPreferences.getInstance();
    final jwt = prefs.getString('jwt');
    if (jwt == null) throw Exception('Token no encontrado');

    final uri = Uri.http(baseUrl, '/api/getAllVehicles', {
      if (searchValue != null && searchValue.isNotEmpty)
        if (searchField == 'brand')
          'brand': searchValue
        else if (searchField == 'model')
          'model': searchValue
        else if (searchField == 'type')
          'type': searchValue,
      if (yearMin != null) 'yearMin': yearMin.toString(),
      if (yearMax != null) 'yearMax': yearMax.toString(),
      if (available != null && available) 'available': 'true',
    });

    final response = await http.get(
      uri,
      headers: {'Authorization': 'Bearer $jwt'},
    );

    if (response.statusCode == 200) {
      final List<dynamic> jsonList = json.decode(
        utf8.decode(response.bodyBytes),
      );
      return jsonList.map((e) => Vehicle.fromJson(e)).toList();
    } else {
      throw Exception('Error al cargar vehículos: ${response.statusCode}');
    }
  }

  Future<Map<String, dynamic>> fetchVehicleDetails(String licensePlate) async {
    final prefs = await SharedPreferences.getInstance();
    final jwt = prefs.getString('jwt');
    final response = await http.get(
      Uri.parse('http://$baseUrl/api/getVehicle/$licensePlate'),
      headers: {'Authorization': 'Bearer $jwt'},
    );

    if (response.statusCode == 200) {
      return jsonDecode(response.body);
    } else {
      throw Exception('Error al obtener detalles del vehículo');
    }
  }
}
