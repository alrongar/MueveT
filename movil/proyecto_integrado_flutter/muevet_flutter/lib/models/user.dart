class User {
  final int? id;
  final String? name;
  final String? surname;
  final String? email;
  final String? password;
  final String? role;
  final bool? active;
  final String? jwt;

  User({
    this.id,
    this.name,
    this.surname,
    this.email,
    this.password,
    this.role,
    this.active,
    this.jwt,
  });

  factory User.fromJson(Map<String, dynamic> json) {
    return User(
      id: json['id'],
      name: json['name'],
      surname: json['surname'],
      email: json['email'],
      role: json['role'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'surname': surname,
      'email': email,
      'password': password,
      'role': role,
      'active': active,
      'jwt': jwt,
    };
  }
}
