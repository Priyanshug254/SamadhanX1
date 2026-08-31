import 'package:flutter/foundation.dart';
import '../data/models/domain_model.dart';
import '../data/repositories/domain_repository.dart';

class DomainProvider extends ChangeNotifier {
  final DomainRepository _domainRepository;

  List<DomainModel> _domains = [];
  String? _selectedDomainCode;
  bool _isLoading = false;
  String? _errorMessage;

  DomainProvider(this._domainRepository);

  List<DomainModel> get domains => _domains;
  String? get selectedDomainCode => _selectedDomainCode;
  bool get isLoading => _isLoading;
  String? get errorMessage => _errorMessage;

  Future<void> fetchDomains() async {
    if (_domains.isNotEmpty) return; // cache in memory

    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    try {
      _domains = await _domainRepository.getAllActiveDomains();
      _isLoading = false;
    } catch (e) {
      _isLoading = false;
      _errorMessage = e.toString().replaceAll('Exception: ', '');
    }

    notifyListeners();
  }

  void selectDomain(String? domainCode) {
    _selectedDomainCode = domainCode;
    notifyListeners();
  }
}
