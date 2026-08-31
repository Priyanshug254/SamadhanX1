import 'package:flutter_test/flutter_test.dart';
import 'package:samadhanx_mobile/core/services/location_service.dart';
import 'package:samadhanx_mobile/data/models/api_response.dart';
import 'package:samadhanx_mobile/data/models/auth_models.dart';
import 'package:samadhanx_mobile/data/models/challenge_models.dart';
import 'package:samadhanx_mobile/data/models/domain_model.dart';
import 'package:samadhanx_mobile/providers/notification_provider.dart';

void main() {
  group('SamadhanX Citizen Models & Services Test Suite', () {
    test('UserModel and AuthResponseModel deserialization', () {
      final userJson = {
        'id': '123e4567-e89b-12d3-a456-426614174000',
        'email': 'rahul@example.com',
        'firstName': 'Rahul',
        'lastName': 'Verma',
        'phoneNumber': '9876543210',
        'isActive': true,
        'roles': ['ROLE_CITIZEN'],
        'createdAt': '2026-08-30T09:00:00Z',
      };

      final user = UserModel.fromJson(userJson);
      expect(user.id, '123e4567-e89b-12d3-a456-426614174000');
      expect(user.fullName, 'Rahul Verma');
      expect(user.isCitizen, isTrue);

      final authJson = {
        'accessToken': 'jwt.token.here',
        'tokenType': 'Bearer',
        'expiresIn': 86400,
        'user': userJson,
      };

      final authResp = AuthResponseModel.fromJson(authJson);
      expect(authResp.accessToken, 'jwt.token.here');
      expect(authResp.user.email, 'rahul@example.com');
    });

    test('DomainModel deserialization', () {
      final domainJson = {
        'id': 'domain-1',
        'code': 'WATER_SANITATION',
        'name': 'Water & Sanitation',
        'description': 'Clean water and sanitation innovation',
        'iconName': 'water_drop',
        'displayOrder': 1,
        'active': true,
      };

      final domain = DomainModel.fromJson(domainJson);
      expect(domain.code, 'WATER_SANITATION');
      expect(domain.name, 'Water & Sanitation');
    });

    test('ChallengeSummaryModel and ChallengeDetailModel deserialization', () {
      final summaryJson = {
        'id': 'ch-1',
        'trackingNumber': 'SMX-2026-08-12345',
        'title': 'High Fluoride in Borewell',
        'domainCode': 'WATER_SANITATION',
        'domainName': 'Water & Sanitation',
        'district': 'Varanasi',
        'state': 'Uttar Pradesh',
        'severityLevel': 'CRITICAL',
        'urgencyLevel': 'IMMEDIATE',
        'estimatedAffectedPopulation': 3500,
        'priorityScore': 88.5,
        'status': 'INNOVATION_REQUIRED',
        'endorsementCount': 42,
        'attachmentCount': 2,
      };

      final summary = ChallengeSummaryModel.fromJson(summaryJson);
      expect(summary.trackingNumber, 'SMX-2026-08-12345');
      expect(summary.priorityScore, 88.5);
      expect(summary.status, 'INNOVATION_REQUIRED');

      final detailJson = {
        ...summaryJson,
        'description': 'Over 3,500 villagers affected by fluoride in drinking water.',
        'duplicate': false,
        'attachments': [
          {
            'id': 'att-1',
            'mediaType': 'IMAGE',
            'fileName': 'water_sample.jpg',
            'fileUrl': 'https://docs.samadhanx.org/evidence/water_sample.jpg',
            'caption': 'Water sample test site',
          }
        ],
        'timeline': [
          {
            'id': 'tl-1',
            'eventTitle': 'Challenge Registered',
            'eventDescription': 'AI categorized under Water & Sanitation',
            'actorName': 'AI Categorization Engine',
            'actorRole': 'SYSTEM',
          }
        ],
      };

      final detail = ChallengeDetailModel.fromJson(detailJson);
      expect(detail.description, contains('3,500 villagers'));
      expect(detail.attachments.length, 1);
      expect(detail.attachments.first.mediaType, 'IMAGE');
      expect(detail.timeline.length, 1);
      expect(detail.timeline.first.eventTitle, 'Challenge Registered');
    });

    test('SubmitChallengeDto serialization', () {
      final dto = SubmitChallengeDto(
        title: 'Ground Water Contamination',
        description: 'Detailed description exceeding 20 characters for validation',
        domainCode: 'WATER_SANITATION',
        latitude: 25.2820,
        longitude: 83.1150,
        district: 'Varanasi',
        state: 'Uttar Pradesh',
        pincode: '221005',
        severityLevel: 'CRITICAL',
        urgencyLevel: 'IMMEDIATE',
        estimatedAffectedPopulation: 1200,
        attachments: [
          AttachmentDto(
            mediaType: 'IMAGE',
            fileName: 'sample.jpg',
            fileUrl: 'https://docs.samadhanx.org/sample.jpg',
          ),
        ],
      );

      final json = dto.toJson();
      expect(json['title'], 'Ground Water Contamination');
      expect(json['latitude'], 25.2820);
      expect(json['district'], 'Varanasi');
      expect((json['attachments'] as List).length, 1);
    });

    test('NotificationItem model deserialization', () {
      final notifJson = {
        'id': 'notif-123',
        'title': 'Challenge Routed to Jal Sansthan',
        'body': 'Your challenge SMX-2026-08-11111 is under departmental triage.',
        'notificationType': 'CHALLENGE_ROUTED',
        'referenceId': 'ch-uuid-123',
        'referenceType': 'CHALLENGE',
        'isRead': false,
        'createdAt': '2026-08-30T10:00:00Z',
      };

      final item = NotificationItem.fromJson(notifJson);
      expect(item.title, 'Challenge Routed to Jal Sansthan');
      expect(item.notificationType, 'CHALLENGE_ROUTED');
      expect(item.isRead, isFalse);
      expect(item.referenceId, 'ch-uuid-123');
    });

    test('LocationResult model initialization', () {
      final loc = LocationResult(
        latitude: 25.2677,
        longitude: 82.9913,
        district: 'Varanasi',
        state: 'Uttar Pradesh',
      );

      expect(loc.latitude, 25.2677);
      expect(loc.district, 'Varanasi');
    });

    test('ApiResponse wrapper parsing', () {
      final json = {
        'success': true,
        'message': 'Challenge retrieved',
        'data': {
          'id': 'domain-1',
          'code': 'WATER_SANITATION',
          'name': 'Water & Sanitation',
          'displayOrder': 1,
          'active': true,
        },
      };

      final resp = ApiResponse.fromJson(
        json,
        (data) => DomainModel.fromJson(data as Map<String, dynamic>),
      );

      expect(resp.success, isTrue);
      expect(resp.data?.code, 'WATER_SANITATION');
    });
  });
}
