import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';

class AuthService {
  final String baseUrl = 'http://localhost:8080/api/auth'; // cambiar la url cuando este subido

 Future<Map<String, dynamic>> register({
  required String name,
  required String surname,
  required String email,
  required String password,
  String role = 'ROLE_USER',
}) async {
  try {
    final response = await http.post(
      Uri.parse('${baseUrl}/register'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({
        'email': email,
        'name': name,
        'surname': surname,
        'role': role,
        'active': true,
        'password': password,
      }),
    );

    print('Status code: ${response.statusCode}');
    print('Response body: ${response.body}');

    return jsonDecode(response.body);
  } catch (e) {
    print('Error en AuthService.register: $e');
    return {'error': 'No se pudo conectar con el servidor.'};
  }
}

  Future<Map<String, dynamic>> login({
  required String email,
  required String password,
}) async {
  final url = Uri.parse('http://localhost:8080/api/auth/login');

  final response = await http.post(
    url,
    headers: {'Content-Type': 'application/json'},
    body: jsonEncode({
      'email': email,
      'password': password,
    }),
  );

  if (response.statusCode == 200) {
    return jsonDecode(response.body);
  } else {
    return {'error': jsonDecode(response.body)['error'] ?? 'Error desconocido'};
  }
}

  Future<void> saveToken(String jwt) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('jwt', jwt);
  }

  Future<String?> getToken() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString('jwt');
  }

  Future<void> logout() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove('jwt');
  }
}
