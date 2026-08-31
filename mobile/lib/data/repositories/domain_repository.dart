import 'package:supabase_flutter/supabase_flutter.dart';
import '../models/domain_model.dart';

class DomainRepository {
  DomainRepository();

  Future<List<DomainModel>> getAllActiveDomains() async {
    final res = await Supabase.instance.client
        .from('domains')
        .select('*')
        .eq('is_active', true)
        .order('name', ascending: true);

    return (res as List).map((item) => DomainModel(
      id: item['id']?.toString() ?? '',
      code: item['code']?.toString() ?? '',
      name: item['name']?.toString() ?? '',
      description: item['description']?.toString(),
      iconName: item['icon_name']?.toString(),
      displayOrder: 0,
      active: item['is_active'] ?? true,
    )).toList();
  }
}
