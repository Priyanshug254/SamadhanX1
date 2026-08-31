import 'package:flutter/foundation.dart';
import '../data/models/challenge_models.dart';
import '../data/repositories/challenge_repository.dart';

class ChallengeProvider extends ChangeNotifier {
  final ChallengeRepository _challengeRepository;

  List<ChallengeSummaryModel> _publicChallenges = [];
  List<ChallengeSummaryModel> _mySubmissions = [];
  ChallengeDetailModel? _currentChallenge;
  List<TimelineEventModel> _currentTimeline = [];

  bool _isLoadingPublic = false;
  bool _isLoadingMy = false;
  bool _isLoadingDetail = false;
  bool _isSubmitting = false;
  bool _isTracking = false;

  String? _publicError;
  String? _myError;
  String? _detailError;
  String? _submitError;
  String? _trackingError;

  ChallengeProvider(this._challengeRepository);

  List<ChallengeSummaryModel> get publicChallenges => _publicChallenges;
  List<ChallengeSummaryModel> get mySubmissions => _mySubmissions;
  List<ChallengeSummaryModel> get myChallenges => _mySubmissions;
  ChallengeDetailModel? get currentChallenge => _currentChallenge;
  List<TimelineEventModel> get currentTimeline => _currentTimeline;

  bool get isLoadingPublic => _isLoadingPublic;
  bool get isLoadingMy => _isLoadingMy;
  bool get isLoadingDetail => _isLoadingDetail;
  bool get isSubmitting => _isSubmitting;
  bool get isTracking => _isTracking;

  String? get publicError => _publicError;
  String? get myError => _myError;
  String? get detailError => _detailError;
  String? get submitError => _submitError;
  String? get trackingError => _trackingError;

  Future<void> fetchPublicChallenges({
    bool refresh = false,
    String? search,
    String? domainCode,
    String? status,
  }) async {
    if (_publicChallenges.isNotEmpty && !refresh && search == null && domainCode == null) {
      return;
    }

    _isLoadingPublic = true;
    _publicError = null;
    notifyListeners();

    try {
      final page = await _challengeRepository.searchPublicChallenges(
        search: search,
        domainCode: domainCode,
        status: status,
        page: 0,
        size: 30,
      );
      _publicChallenges = page.content;
      _isLoadingPublic = false;
    } catch (e) {
      _isLoadingPublic = false;
      _publicError = e.toString().replaceAll('Exception: ', '');
    }

    notifyListeners();
  }

  Future<void> fetchMySubmissions({bool refresh = false}) async {
    _isLoadingMy = true;
    _myError = null;
    notifyListeners();

    try {
      final page = await _challengeRepository.getMySubmissions(
        page: 0,
        size: 30,
      );
      _mySubmissions = page.content;
      _isLoadingMy = false;
    } catch (e) {
      _isLoadingMy = false;
      _myError = e.toString().replaceAll('Exception: ', '');
    }

    notifyListeners();
  }

  Future<ChallengeDetailModel?> submitChallenge(SubmitChallengeDto dto) async {
    _isSubmitting = true;
    _submitError = null;
    notifyListeners();

    try {
      final created = await _challengeRepository.submitChallenge(dto);
      _isSubmitting = false;
      _currentChallenge = created;
      // Refresh my submissions list in background
      fetchMySubmissions(refresh: true);
      notifyListeners();
      return created;
    } catch (e) {
      _isSubmitting = false;
      _submitError = e.toString().replaceAll('Exception: ', '');
      notifyListeners();
      return null;
    }
  }

  Future<ChallengeDetailModel?> loadChallengeDetails(String id) async {
    _isLoadingDetail = true;
    _detailError = null;
    notifyListeners();

    try {
      final detail = await _challengeRepository.getChallengeById(id);
      _currentChallenge = detail;
      _currentTimeline = detail.timeline;
      _isLoadingDetail = false;
      notifyListeners();
      return detail;
    } catch (e) {
      _isLoadingDetail = false;
      _detailError = e.toString().replaceAll('Exception: ', '');
      notifyListeners();
      return null;
    }
  }

  Future<ChallengeDetailModel?> trackByNumber(String trackingNumber) async {
    _isTracking = true;
    _trackingError = null;
    notifyListeners();

    try {
      final detail = await _challengeRepository.getChallengeByTrackingNumber(trackingNumber);
      _currentChallenge = detail;
      _currentTimeline = detail.timeline;
      _isTracking = false;
      notifyListeners();
      return detail;
    } catch (e) {
      _isTracking = false;
      _trackingError = e.toString().replaceAll('Exception: ', '');
      notifyListeners();
      return null;
    }
  }

  Future<bool> endorseChallenge(String id, {String? comment}) async {
    try {
      await _challengeRepository.endorseChallenge(id, comment: comment);
      // Increment local count if currently viewed
      if (_currentChallenge != null && _currentChallenge!.id == id) {
        // reload details
        loadChallengeDetails(id);
      }
      return true;
    } catch (_) {
      return false;
    }
  }

  void clearCurrentChallenge() {
    _currentChallenge = null;
    _currentTimeline = [];
    notifyListeners();
  }
}
