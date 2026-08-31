class ChallengeSummaryModel {
  final String id;
  final String trackingNumber;
  final String title;
  final String? description;
  final String? domainCode;
  final String? domainName;
  final String? district;
  final String? state;
  final String severityLevel;
  final String urgencyLevel;
  final int estimatedAffectedPopulation;
  final double priorityScore;
  final String status;
  final String? resolutionPath;
  final String? assignedDepartmentName;
  final int endorsementCount;
  final int attachmentCount;
  final DateTime? createdAt;

  ChallengeSummaryModel({
    required this.id,
    required this.trackingNumber,
    required this.title,
    this.description,
    this.domainCode,
    this.domainName,
    this.district,
    this.state,
    required this.severityLevel,
    required this.urgencyLevel,
    required this.estimatedAffectedPopulation,
    required this.priorityScore,
    required this.status,
    this.resolutionPath,
    this.assignedDepartmentName,
    required this.endorsementCount,
    required this.attachmentCount,
    this.createdAt,
  });

  factory ChallengeSummaryModel.fromJson(Map<String, dynamic> json) {
    return ChallengeSummaryModel(
      id: json['id']?.toString() ?? '',
      trackingNumber: json['trackingNumber']?.toString() ?? '',
      title: json['title']?.toString() ?? '',
      description: json['description']?.toString(),
      domainCode: json['domainCode']?.toString(),
      domainName: json['domainName']?.toString(),
      district: json['district']?.toString(),
      state: json['state']?.toString(),
      severityLevel: json['severityLevel']?.toString() ?? 'MEDIUM',
      urgencyLevel: json['urgencyLevel']?.toString() ?? 'NORMAL',
      estimatedAffectedPopulation: json['estimatedAffectedPopulation'] ?? 0,
      priorityScore: (json['priorityScore'] is num)
          ? (json['priorityScore'] as num).toDouble()
          : 0.0,
      status: json['status']?.toString() ?? 'SUBMITTED',
      resolutionPath: json['resolutionPath']?.toString(),
      assignedDepartmentName: json['assignedDepartmentName']?.toString(),
      endorsementCount: json['endorsementCount'] ?? 0,
      attachmentCount: json['attachmentCount'] ?? 0,
      createdAt: json['createdAt'] != null
          ? DateTime.tryParse(json['createdAt'].toString())
          : null,
    );
  }
}

class AttachmentModel {
  final String id;
  final String mediaType;
  final String fileName;
  final String fileUrl;
  final int? fileSizeBytes;
  final String? caption;
  final double? geoLatitude;
  final double? geoLongitude;
  final DateTime? createdAt;

  AttachmentModel({
    required this.id,
    required this.mediaType,
    required this.fileName,
    required this.fileUrl,
    this.fileSizeBytes,
    this.caption,
    this.geoLatitude,
    this.geoLongitude,
    this.createdAt,
  });

  factory AttachmentModel.fromJson(Map<String, dynamic> json) {
    return AttachmentModel(
      id: json['id']?.toString() ?? '',
      mediaType: json['mediaType']?.toString() ?? 'IMAGE',
      fileName: json['fileName']?.toString() ?? '',
      fileUrl: json['fileUrl']?.toString() ?? '',
      fileSizeBytes: json['fileSizeBytes'] is num ? (json['fileSizeBytes'] as num).toInt() : null,
      caption: json['caption']?.toString(),
      geoLatitude: (json['geoLatitude'] is num) ? (json['geoLatitude'] as num).toDouble() : null,
      geoLongitude: (json['geoLongitude'] is num) ? (json['geoLongitude'] as num).toDouble() : null,
      createdAt: json['createdAt'] != null ? DateTime.tryParse(json['createdAt'].toString()) : null,
    );
  }
}

class TimelineEventModel {
  final String id;
  final String? previousStatus;
  final String? newStatus;
  final String? eventType;
  final String? eventTitle;
  final String? eventDescription;
  final String? actorName;
  final String? actorRole;
  final DateTime? createdAt;

  TimelineEventModel({
    required this.id,
    this.previousStatus,
    this.newStatus,
    this.eventType,
    this.eventTitle,
    this.eventDescription,
    this.actorName,
    this.actorRole,
    this.createdAt,
  });

  factory TimelineEventModel.fromJson(Map<String, dynamic> json) {
    return TimelineEventModel(
      id: json['id']?.toString() ?? '',
      previousStatus: json['previousStatus']?.toString(),
      newStatus: json['newStatus']?.toString(),
      eventType: json['eventType']?.toString(),
      eventTitle: json['eventTitle']?.toString() ?? json['eventDescription']?.toString() ?? 'Progress Event',
      eventDescription: json['eventDescription']?.toString() ?? json['eventMessage']?.toString(),
      actorName: json['actorName']?.toString(),
      actorRole: json['actorRole']?.toString(),
      createdAt: json['createdAt'] != null ? DateTime.tryParse(json['createdAt'].toString()) : null,
    );
  }
}

class ChallengeDetailModel {
  final String id;
  final String trackingNumber;
  final String title;
  final String description;
  final String? submitterName;
  final String? submitterEmail;
  final String? submitterType;
  final String? domainCode;
  final String? domainName;
  final String? subCategory;
  final String? aiPredictedDomainCode;
  final double? aiConfidenceScore;
  final String? aiKeywords;
  final double? latitude;
  final double? longitude;
  final String? addressLine;
  final String? locality;
  final String? district;
  final String? state;
  final String? pincode;
  final String? jurisdictionLevel;
  final String severityLevel;
  final String urgencyLevel;
  final int estimatedAffectedPopulation;
  final double priorityScore;
  final int endorsementCount;
  final bool duplicate;
  final double? duplicateSimilarity;
  final String status;
  final String? resolutionPath;
  final String? assignedDepartmentName;
  final String? routingRationale;
  final DateTime? targetResolutionDate;
  final DateTime? resolvedAt;
  final String? resolutionSummary;
  final String? measurableImpactDescription;
  final List<AttachmentModel> attachments;
  final List<TimelineEventModel> timeline;
  final DateTime? createdAt;
  final DateTime? updatedAt;

  ChallengeDetailModel({
    required this.id,
    required this.trackingNumber,
    required this.title,
    required this.description,
    this.submitterName,
    this.submitterEmail,
    this.submitterType,
    this.domainCode,
    this.domainName,
    this.subCategory,
    this.aiPredictedDomainCode,
    this.aiConfidenceScore,
    this.aiKeywords,
    this.latitude,
    this.longitude,
    this.addressLine,
    this.locality,
    this.district,
    this.state,
    this.pincode,
    this.jurisdictionLevel,
    required this.severityLevel,
    required this.urgencyLevel,
    required this.estimatedAffectedPopulation,
    required this.priorityScore,
    required this.endorsementCount,
    required this.duplicate,
    this.duplicateSimilarity,
    required this.status,
    this.resolutionPath,
    this.assignedDepartmentName,
    this.routingRationale,
    this.targetResolutionDate,
    this.resolvedAt,
    this.resolutionSummary,
    this.measurableImpactDescription,
    required this.attachments,
    required this.timeline,
    this.createdAt,
    this.updatedAt,
  });

  factory ChallengeDetailModel.fromJson(Map<String, dynamic> json) {
    List<AttachmentModel> atts = [];
    if (json['attachments'] is List) {
      atts = (json['attachments'] as List)
          .map((a) => AttachmentModel.fromJson(a as Map<String, dynamic>))
          .toList();
    }

    List<TimelineEventModel> tEvents = [];
    if (json['timeline'] is List) {
      tEvents = (json['timeline'] as List)
          .map((t) => TimelineEventModel.fromJson(t as Map<String, dynamic>))
          .toList();
    }

    return ChallengeDetailModel(
      id: json['id']?.toString() ?? '',
      trackingNumber: json['trackingNumber']?.toString() ?? '',
      title: json['title']?.toString() ?? '',
      description: json['description']?.toString() ?? '',
      submitterName: json['submitterName']?.toString(),
      submitterEmail: json['submitterEmail']?.toString(),
      submitterType: json['submitterType']?.toString(),
      domainCode: json['domainCode']?.toString(),
      domainName: json['domainName']?.toString(),
      subCategory: json['subCategory']?.toString(),
      aiPredictedDomainCode: json['aiPredictedDomainCode']?.toString(),
      aiConfidenceScore: (json['aiConfidenceScore'] is num)
          ? (json['aiConfidenceScore'] as num).toDouble()
          : null,
      aiKeywords: json['aiKeywords']?.toString(),
      latitude: (json['latitude'] is num) ? (json['latitude'] as num).toDouble() : null,
      longitude: (json['longitude'] is num) ? (json['longitude'] as num).toDouble() : null,
      addressLine: json['addressLine']?.toString(),
      locality: json['locality']?.toString(),
      district: json['district']?.toString(),
      state: json['state']?.toString(),
      pincode: json['pincode']?.toString(),
      jurisdictionLevel: json['jurisdictionLevel']?.toString(),
      severityLevel: json['severityLevel']?.toString() ?? 'MEDIUM',
      urgencyLevel: json['urgencyLevel']?.toString() ?? 'NORMAL',
      estimatedAffectedPopulation: json['estimatedAffectedPopulation'] ?? 0,
      priorityScore: (json['priorityScore'] is num)
          ? (json['priorityScore'] as num).toDouble()
          : 0.0,
      endorsementCount: json['endorsementCount'] ?? 0,
      duplicate: json['duplicate'] ?? false,
      duplicateSimilarity: (json['duplicateSimilarity'] is num)
          ? (json['duplicateSimilarity'] as num).toDouble()
          : null,
      status: json['status']?.toString() ?? 'SUBMITTED',
      resolutionPath: json['resolutionPath']?.toString(),
      assignedDepartmentName: json['assignedDepartmentName']?.toString(),
      routingRationale: json['routingRationale']?.toString(),
      targetResolutionDate: json['targetResolutionDate'] != null
          ? DateTime.tryParse(json['targetResolutionDate'].toString())
          : null,
      resolvedAt: json['resolvedAt'] != null
          ? DateTime.tryParse(json['resolvedAt'].toString())
          : null,
      resolutionSummary: json['resolutionSummary']?.toString(),
      measurableImpactDescription: json['measurableImpactDescription']?.toString(),
      attachments: atts,
      timeline: tEvents,
      createdAt: json['createdAt'] != null
          ? DateTime.tryParse(json['createdAt'].toString())
          : null,
      updatedAt: json['updatedAt'] != null
          ? DateTime.tryParse(json['updatedAt'].toString())
          : null,
    );
  }
}

class AttachmentDto {
  final String mediaType;
  final String fileName;
  final String fileUrl;
  final int? fileSizeBytes;
  final String? caption;
  final double? geoLatitude;
  final double? geoLongitude;

  AttachmentDto({
    required this.mediaType,
    required this.fileName,
    required this.fileUrl,
    this.fileSizeBytes,
    this.caption,
    this.geoLatitude,
    this.geoLongitude,
  });

  Map<String, dynamic> toJson() {
    return {
      'mediaType': mediaType,
      'fileName': fileName,
      'fileUrl': fileUrl,
      if (fileSizeBytes != null) 'fileSizeBytes': fileSizeBytes,
      if (caption != null) 'caption': caption,
      if (geoLatitude != null) 'geoLatitude': geoLatitude,
      if (geoLongitude != null) 'geoLongitude': geoLongitude,
    };
  }
}

class SubmitChallengeDto {
  final String title;
  final String description;
  final String? domainCode;
  final String? subCategory;
  final String submitterType;
  final double latitude;
  final double longitude;
  final String? addressLine;
  final String? locality;
  final String district;
  final String state;
  final String pincode;
  final String jurisdictionLevel;
  final String severityLevel;
  final String urgencyLevel;
  final int estimatedAffectedPopulation;
  final List<AttachmentDto> attachments;

  SubmitChallengeDto({
    required this.title,
    required this.description,
    this.domainCode,
    this.subCategory,
    this.submitterType = 'CITIZEN',
    required this.latitude,
    required this.longitude,
    this.addressLine,
    this.locality,
    required this.district,
    required this.state,
    required this.pincode,
    this.jurisdictionLevel = 'PANCHAYAT_PRI',
    required this.severityLevel,
    required this.urgencyLevel,
    required this.estimatedAffectedPopulation,
    required this.attachments,
  });

  Map<String, dynamic> toJson() {
    return {
      'title': title,
      'description': description,
      if (domainCode != null && domainCode!.isNotEmpty) 'domainCode': domainCode,
      if (subCategory != null && subCategory!.isNotEmpty) 'subCategory': subCategory,
      'submitterType': submitterType,
      'latitude': latitude,
      'longitude': longitude,
      if (addressLine != null) 'addressLine': addressLine,
      if (locality != null) 'locality': locality,
      'district': district,
      'state': state,
      'pincode': pincode,
      'jurisdictionLevel': jurisdictionLevel,
      'severityLevel': severityLevel,
      'urgencyLevel': urgencyLevel,
      'estimatedAffectedPopulation': estimatedAffectedPopulation,
      'attachments': attachments.map((a) => a.toJson()).toList(),
    };
  }
}
