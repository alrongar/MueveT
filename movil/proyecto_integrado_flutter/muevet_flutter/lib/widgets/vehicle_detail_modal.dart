import 'package:flutter/material.dart';
import 'package:muevet_flutter/services/booking_service.dart';
import 'package:muevet_flutter/services/vehicle_service.dart';

class VehicleDetailModal extends StatefulWidget {
  final String licensePlate;

  const VehicleDetailModal({super.key, required this.licensePlate});

  @override
  State<VehicleDetailModal> createState() => _VehicleDetailModalState();
}

class _VehicleDetailModalState extends State<VehicleDetailModal> {
  Map<String, dynamic>? vehicleDetails;

  DateTime? selectedStartDate;
  DateTime? selectedEndDate;
  bool isLoading = true;
  final VehicleService vehicleService = VehicleService();
  @override
  void initState() {
    super.initState();
    _loadVehicleDetails();
  }

  Future<void> _selectStartDate() async {
    final picked = await showDatePicker(
      context: context,
      initialDate: selectedStartDate ?? DateTime.now(),
      firstDate: DateTime.now(),
      lastDate: DateTime(2100),
    );
    if (picked != null) {
      setState(() {
        selectedStartDate = picked;
      });
    }
  }

  Future<void> _selectEndDate() async {
    final picked = await showDatePicker(
      context: context,
      initialDate: selectedEndDate ?? (selectedStartDate ?? DateTime.now()),
      firstDate: selectedStartDate ?? DateTime.now(),
      lastDate: DateTime(2100),
    );
    if (picked != null) {
      setState(() {
        selectedEndDate = picked;
      });
    }
  }

  Future<void> _loadVehicleDetails() async {
    try {
      final data = await vehicleService.fetchVehicleDetails(
        widget.licensePlate,
      );
      setState(() {
        vehicleDetails = data;
        isLoading = false;
      });
    } catch (e) {
      setState(() {
        isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return DraggableScrollableSheet(
      initialChildSize: 0.5,
      minChildSize: 0.5,
      maxChildSize: 1.0,
      expand: false,
      builder: (_, scrollController) {
        return Container(
          decoration: const BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
          ),
          padding: const EdgeInsets.all(16),
          child:
              isLoading
                  ? const Center(child: CircularProgressIndicator())
                  : vehicleDetails == null
                  ? const Center(
                    child: Text('No se pudieron cargar los detalles.'),
                  )
                  : SingleChildScrollView(
                    controller: scrollController,
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Center(
                          child: Container(
                            width: 40,
                            height: 4,
                            margin: const EdgeInsets.only(bottom: 16),
                            decoration: BoxDecoration(
                              color: Colors.grey[400],
                              borderRadius: BorderRadius.circular(8),
                            ),
                          ),
                        ),
                        Row(
                          children: [
                            Text(
                              '${vehicleDetails!['brand']} ${vehicleDetails!['model']}',
                              style: Theme.of(context).textTheme.titleLarge,
                            ),
                            Align(
                              alignment: Alignment.centerRight,
                              child: ElevatedButton(
                                onPressed: () => Navigator.of(context).pop(),
                                child: const Text('Cerrar'),
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 12),
                        Text('Marca: ${vehicleDetails!['brand']}'),
                        Text('Modelo: ${vehicleDetails!['model']}'),
                        Text('Tipo: ${vehicleDetails!['type']}'),
                        Text('Año: ${vehicleDetails!['year']}'),
                        Text(
                          'Precio por día: ${vehicleDetails!['rentalPricePerDay']} €',
                        ),
                        Text(
                          'Disponibilidad: ${vehicleDetails!['available'] ? 'Disponible' : 'No Disponible'}',
                        ),
                        const SizedBox(height: 16),
                        TextField(
                          readOnly: true,
                          controller: TextEditingController(
                            text:
                                selectedStartDate != null
                                    ? '${selectedStartDate!.day}/${selectedStartDate!.month}/${selectedStartDate!.year}'
                                    : '',
                          ),
                          decoration: const InputDecoration(
                            labelText: 'Fecha inicio',
                            border: OutlineInputBorder(),
                            suffixIcon: Icon(Icons.calendar_today),
                          ),
                          onTap: _selectStartDate,
                        ),
                        const SizedBox(height: 16),
                        TextField(
                          readOnly: true,
                          controller: TextEditingController(
                            text:
                                selectedEndDate != null
                                    ? '${selectedEndDate!.day}/${selectedEndDate!.month}/${selectedEndDate!.year}'
                                    : '',
                          ),
                          decoration: const InputDecoration(
                            labelText: 'Fecha fin',
                            border: OutlineInputBorder(),
                            suffixIcon: Icon(Icons.calendar_today),
                          ),
                          onTap:
                              selectedStartDate != null ? _selectEndDate : null,
                        ),
                        const SizedBox(height: 24),

                        Align(
                          alignment: Alignment.centerRight,
                          child: ElevatedButton(
                            onPressed:
                                (selectedStartDate != null &&
                                        selectedEndDate != null)
                                    ? () async {
                                      setState(() => isLoading = true);
                                      try {
                                        await BookingService().createBooking(
                                          licensePlate:
                                              '${vehicleDetails!['licensePlate']}',
                                          startDate: selectedStartDate!,
                                          endDate: selectedEndDate!,
                                        );
                                        Navigator.of(context).pop();
                                        ScaffoldMessenger.of(
                                          context,
                                        ).showSnackBar(
                                          const SnackBar(
                                            content: Text(
                                              'Reserva creada correctamente.',
                                            ),
                                          ),
                                        );
                                      } catch (e) {
                                        ScaffoldMessenger.of(
                                          context,
                                        ).showSnackBar(
                                          SnackBar(
                                            content: Text(
                                              'Error: ${e.toString()}',
                                            ),
                                          ),
                                        );
                                      } finally {
                                        setState(() => isLoading = false);
                                      }
                                    }
                                    : null,
                            child:
                                isLoading
                                    ? const SizedBox(
                                      height: 20,
                                      width: 20,
                                      child: CircularProgressIndicator(
                                        strokeWidth: 2,
                                      ),
                                    )
                                    : const Text('Reservar'),
                          ),
                        ),
                      ],
                    ),
                  ),
        );
      },
    );
  }
}
