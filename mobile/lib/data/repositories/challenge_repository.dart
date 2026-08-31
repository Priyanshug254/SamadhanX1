import 'package:supabase_flutter/supabase_flutter.dart';
import '../models/api_response.dart';
import '../models/challenge_models.dart';

class ChallengeRepository {
  ChallengeRepository();

  ChallengeDetailModel _mapDbToDetail(Map<String, dynamic> json) {
    List<AttachmentModel> atts = [];
    if (json['challenge_media'] is List) {
      atts = (json['challenge_media'] as List)
          .map((m) => AttachmentModel(
                id: m['id']?.toString() ?? '',
                mediaType: m['media_type']?.toString() ?? 'IMAGE',
                fileName: m['file_name']?.toString() ?? 'Attachment',
                fileUrl: m['media_url']?.toString() ?? '',
                createdAt: m['created_at'] != null ? DateTime.tryParse(m['created_at'].toString()) : null,
              ))
          .toList();
    }

    List<TimelineEventModel> tEvents = [];
    if (json['challenge_timeline'] is List) {
      tEvents = (json['challenge_timeline'] as List)
          .map((t) => TimelineEventModel(
                id: t['id']?.toString() ?? '',
                previousStatus: t['metadata'] is Map ? t['metadata']['old_status']?.toString() : null,
                newStatus: t['status']?.toString(),
                eventType: t['status']?.toString(),
                eventTitle: t['title']?.toString() ?? 'Status Update',
                eventDescription: t['description']?.toString(),
                actorName: t['profiles'] is Map ? '${t['profiles']['first_name']} ${t['profiles']['last_name']}'.trim() : 'Official',
                createdAt: t['created_at'] != null ? DateTime.tryParse(t['created_at'].toString()) : null,
              ))
          .toList();
    }

    return ChallengeDetailModel(
      id: json['id']?.toString() ?? '',
      trackingNumber: json['tracking_number']?.toString() ?? '',
      title: json['title']?.toString() ?? '',
      description: json['description']?.toString() ?? '',
      submitterName: json['profiles'] is Map ? '${json['profiles']['first_name']} ${json['profiles']['last_name']}'.trim() : 'Citizen',
      submitterEmail: json['profiles'] is Map ? json['profiles']['email']?.toString() : null,
      domainCode: json['domains'] is Map ? json['domains']['code']?.toString() : 'WATER_SANITATION',
      domainName: json['domains'] is Map ? json['domains']['name']?.toString() : 'Water & Sanitation',
      subCategory: json['problem_category']?.toString(),
      latitude: json['latitude'] is num ? (json['latitude'] as num).toDouble() : null,
      longitude: json['longitude'] is num ? (json['longitude'] as num).toDouble() : null,
      addressLine: json['address']?.toString() ?? json['location']?.toString(),
      district: json['district']?.toString(),
      state: json['state']?.toString(),
      severityLevel: json['severity']?.toString() ?? 'MEDIUM',
      urgencyLevel: json['urgency']?.toString() ?? 'MEDIUM',
      estimatedAffectedPopulation: int.tryParse(json['affected_population']?.toString() ?? '0') ?? 0,
      priorityScore: json['severity'] == 'CRITICAL' ? 95.0 : json['severity'] == 'HIGH' ? 80.0 : 50.0,
      endorsementCount: json['endorsement_count'] ?? 0,
      duplicate: false,
      status: json['status']?.toString() ?? 'SUBMITTED',
      assignedDepartmentName: json['organizations'] is Map ? json['organizations']['name']?.toString() : null,
      attachments: atts,
      timeline: tEvents,
      createdAt: json['created_at'] != null ? DateTime.tryParse(json['created_at'].toString()) : null,
      updatedAt: json['updated_at'] != null ? DateTime.tryParse(json['updated_at'].toString()) : null,
    );
  }

  ChallengeSummaryModel _mapDbToSummary(Map<String, dynamic> json) {
    return ChallengeSummaryModel(
      id: json['id']?.toString() ?? '',
      trackingNumber: json['tracking_number']?.toString() ?? '',
      title: json['title']?.toString() ?? '',
      description: json['description']?.toString(),
      domainCode: json['domains'] is Map ? json['domains']['code']?.toString() : 'WATER_SANITATION',
      domainName: json['domains'] is Map ? json['domains']['name']?.toString() : 'Water & Sanitation',
      district: json['district']?.toString(),
      state: json['state']?.toString(),
      severityLevel: json['severity']?.toString() ?? 'MEDIUM',
      urgencyLevel: json['urgency']?.toString() ?? 'MEDIUM',
      estimatedAffectedPopulation: int.tryParse(json['affected_population']?.toString() ?? '0') ?? 0,
      priorityScore: json['severity'] == 'CRITICAL' ? 95.0 : json['severity'] == 'HIGH' ? 80.0 : 50.0,
      status: json['status']?.toString() ?? 'SUBMITTED',
      assignedDepartmentName: json['organizations'] is Map ? json['organizations']['name']?.toString() : null,
      endorsementCount: json['endorsement_count'] ?? 0,
      attachmentCount: json['challenge_media'] is List ? (json['challenge_media'] as List).length : 0,
      createdAt: json['created_at'] != null ? DateTime.tryParse(json['created_at'].toString()) : null,
    );
  }

  Future<ChallengeDetailModel> submitChallenge(SubmitChallengeDto dto) async {
    // Look up domain ID by code if needed
    String domainId = '';
    if (dto.domainCode != null && dto.domainCode!.isNotEmpty) {
      final domainRes = await Supabase.instance.client
          .from('domains')
          .select('id')
          .eq('code', dto.domainCode!)
          .maybeSingle();
      if (domainRes != null) {
        domainId = domainRes['id'].toString();
      }
    }

    if (domainId.isEmpty) {
      final defaultDomain = await Supabase.instance.client
          .from('domains')
          .select('id')
          .limit(1)
          .single();
      domainId = defaultDomain['id'].toString();
    }

    final mediaUrls = dto.attachments.map((a) => a.fileUrl).toList();

    final result = await Supabase.instance.client.rpc('create_challenge', params: {
      'p_title': dto.title,
      'p_description': dto.description,
      'p_domain_id': domainId,
      'p_location': dto.addressLine ?? '${dto.district}, ${dto.state}',
      'p_latitude': dto.latitude,
      'p_longitude': dto.longitude,
      'p_address': dto.addressLine,
      'p_district': dto.district,
      'p_state': dto.state,
      'p_severity': dto.severityLevel,
      'p_urgency': dto.urgencyLevel,
      'p_affected_population': dto.estimatedAffectedPopulation.toString(),
      'p_problem_category': dto.subCategory,
      'p_media_urls': mediaUrls,
    });

    final challengeId = (result as Map<String, dynamic>)['id'].toString();
    return getChallengeById(challengeId);
  }

  Future<PageData<ChallengeSummaryModel>> getMySubmissions({
    int page = 0,
    int size = 20,
  }) async {
    final user = Supabase.instance.client.auth.currentUser;
    if (user == null) {
      return PageData(content: [], totalElements: 0, totalPages: 0, number: 0, size: size, last: true);
    }

    final from = page * size;
    final to = from + size - 1;

    final response = await Supabase.instance.client
        .from('challenges')
        .select('*, domains(*), challenge_media(id), organizations(name)')
        .eq('submitter_id', user.id)
        .order('created_at', ascending: false)
        .range(from, to);

    final List<dynamic> data = response;
    final int count = data.length;

    final items = data.map((item) => _mapDbToSummary(item as Map<String, dynamic>)).toList();

    return PageData(
      content: items,
      totalElements: count,
      totalPages: (count / size).ceil(),
      number: page,
      size: size,
      last: to >= count - 1,
    );
  }

  Future<PageData<ChallengeSummaryModel>> searchPublicChallenges({
    String? search,
    String? domainCode,
    String? status,
    String? state,
    String? district,
    int page = 0,
    int size = 20,
  }) async {
    final from = page * size;
    final to = from + size - 1;

    var query = Supabase.instance.client
        .from('challenges')
        .select('*, domains(*), challenge_media(id), organizations(name)');

    if (search != null && search.isNotEmpty) {
      query = query.or('title.ilike.%$search%,description.ilike.%$search%,tracking_number.ilike.%$search%');
    }
    if (status != null && status.isNotEmpty) {
      query = query.eq('status', status);
    }
    if (state != null && state.isNotEmpty) {
      query = query.eq('state', state);
    }
    if (district != null && district.isNotEmpty) {
      query = query.eq('district', district);
    }

    final response = await query.order('created_at', ascending: false).range(from, to);
    final List<dynamic> data = response;
    final int count = data.length;

    final items = data.map((item) => _mapDbToSummary(item as Map<String, dynamic>)).toList();

    return PageData(
      content: items,
      totalElements: count,
      totalPages: (count / size).ceil(),
      number: page,
      size: size,
      last: to >= count - 1,
    );
  }

  Future<ChallengeDetailModel> getChallengeById(String id) async {
    final res = await Supabase.instance.client
        .from('challenges')
        .select('*, domains(*), profiles!submitter_id(*), organizations(name), challenge_media(*), challenge_timeline(*, profiles(first_name, last_name, email))')
        .eq('id', id)
        .single();

    return _mapDbToDetail(res);
  }

  Future<ChallengeDetailModel> getChallengeByTrackingNumber(String trackingNumber) async {
    final res = await Supabase.instance.client
        .from('challenges')
        .select('*, domains(*), profiles!submitter_id(*), organizations(name), challenge_media(*), challenge_timeline(*, profiles(first_name, last_name, email))')
        .eq('tracking_number', trackingNumber.trim())
        .single();

    return _mapDbToDetail(res);
  }

  Future<List<TimelineEventModel>> getChallengeTimeline(String id) async {
    final res = await Supabase.instance.client
        .from('challenge_timeline')
        .select('*, profiles(first_name, last_name, email)')
        .eq('challenge_id', id)
        .order('created_at', ascending: true);

    return (res as List).map((t) => TimelineEventModel(
          id: t['id']?.toString() ?? '',
          previousStatus: t['metadata'] is Map ? t['metadata']['old_status']?.toString() : null,
          newStatus: t['status']?.toString(),
          eventType: t['status']?.toString(),
          eventTitle: t['title']?.toString() ?? 'Update',
          eventDescription: t['description']?.toString(),
          actorName: t['profiles'] is Map ? '${t['profiles']['first_name']} ${t['profiles']['last_name']}'.trim() : 'System',
          createdAt: t['created_at'] != null ? DateTime.tryParse(t['created_at'].toString()) : null,
        )).toList();
  }

  Future<void> endorseChallenge(String id, {String? comment}) async {
    await Supabase.instance.client.rpc('endorse_challenge', params: {'p_challenge_id': id});
  }
}
