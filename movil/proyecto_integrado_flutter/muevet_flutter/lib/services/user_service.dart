import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:muevet_flutter/models/user.dart';
import 'package:shared_preferences/shared_preferences.dart';

class UserService {
  final String baseUrl = 'http://16.171.42.106:8080/api';

  Future<Map<String, dynamic>> getUserByEmail(String email) async {
    final response = await http.get(Uri.parse('$baseUrl/auth/getUser/$email'));

    if (response.statusCode == 200) {
      return json.decode(response.body);
    } else {
      throw Exception('No se pudo obtener el usuario');
    }
  }

  Future<Map<String, dynamic>> getUserFromJwt() async {
    final prefs = await SharedPreferences.getInstance();
    final jwt = prefs.getString('jwt');
    if (jwt == null) throw Exception('No hay token JWT guardado');

    final email = _extractEmailFromJwt(jwt);
    if (email.isEmpty) throw Exception('No se pudo extraer el email del JWT');

    final url = Uri.parse(baseUrl + '/getUser/$email');

    final response = await http.get(
      url,
      headers: {'Authorization': 'Bearer $jwt'},
    );

    if (response.statusCode == 200) {
      return json.decode(response.body);
    } else {
      throw Exception(
        'No se pudo obtener el usuario. Código: ${response.statusCode}',
      );
    }
  }

  String _extractEmailFromJwt(String jwt) {
    try {
      final parts = jwt.split('.');
      if (parts.length != 3) throw Exception('Token JWT inválido');

      final payload = utf8.decode(
        base64Url.decode(base64Url.normalize(parts[1])),
      );
      final payloadMap = json.decode(payload);
      return payloadMap['sub'] ?? '';
    } catch (e) {
      print('Error al extraer el email del JWT: $e');
      return '';
    }
  }

  Future<bool> updateUser(User user) async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final jwt = prefs.getString('jwt') ?? '';

      final response = await http.post(
        Uri.parse(baseUrl + '/user/update'),
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer $jwt',
        },
        body: jsonEncode(user.toJson()),
      );

      return response.statusCode == 200;
    } catch (e) {
      print('Error actualizando usuario: $e');
      return false;
    }
  }
}
