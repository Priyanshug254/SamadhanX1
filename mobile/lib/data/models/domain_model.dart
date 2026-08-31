class DomainModel {
  final String id;
  final String code;
  final String name;
  final String? description;
  final String? iconName;
  final int displayOrder;
  final bool active;

  DomainModel({
    required this.id,
    required this.code,
    required this.name,
    this.description,
    this.iconName,
    required this.displayOrder,
    required this.active,
  });

  factory DomainModel.fromJson(Map<String, dynamic> json) {
    return DomainModel(
      id: json['id']?.toString() ?? '',
      code: json['code']?.toString() ?? '',
      name: json['name']?.toString() ?? '',
      description: json['description']?.toString(),
      iconName: json['iconName']?.toString(),
      displayOrder: json['displayOrder'] ?? 0,
      active: json['active'] ?? true,
    );
  }
}
