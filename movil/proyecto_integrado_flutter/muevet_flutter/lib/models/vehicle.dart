class Vehicle {
  final String licensePlate;
  final String brand;
  final String model;
  final int year;
  final double rentalPricePerDay;
  final bool available;
  final String type;

  Vehicle({
    required this.licensePlate,
    required this.brand,
    required this.model,
    required this.year,
    required this.rentalPricePerDay,
    required this.available,
    required this.type,
  });

  factory Vehicle.fromJson(Map<String, dynamic> json) {
    return Vehicle(
      licensePlate: json['licensePlate'],
      brand: json['brand'],
      model: json['model'],
      year: json['year'],
      rentalPricePerDay: (json['rentalPricePerDay'] as num).toDouble(),
      available: json['available'],
      type: json['type'], 
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'licensePlate': licensePlate,
      'brand': brand,
      'model': model,
      'year': year,
      'rentalPricePerDay': rentalPricePerDay,
      'available': available,
      'type': type,
    };
  }
}
