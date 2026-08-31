import 'package:flutter/material.dart';
import '../../core/constants/app_colors.dart';
import '../../core/utils/formatters.dart';

class StatusBadge extends StatelessWidget {
  final String status;
  final bool isLarge;

  const StatusBadge({
    super.key,
    required this.status,
    this.isLarge = false,
  });

  @override
  Widget build(BuildContext context) {
    final config = _getStatusConfig(status);

    return Container(
      padding: EdgeInsets.symmetric(
        horizontal: isLarge ? 12 : 8,
        vertical: isLarge ? 6 : 4,
      ),
      decoration: BoxDecoration(
        color: config.backgroundColor,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: config.borderColor, width: 1),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
            width: isLarge ? 8 : 6,
            height: isLarge ? 8 : 6,
            decoration: BoxDecoration(
              color: config.dotColor,
              shape: BoxShape.circle,
            ),
          ),
          const SizedBox(width: 6),
          Text(
            config.label,
            style: TextStyle(
              fontSize: isLarge ? 13 : 11,
              fontWeight: FontWeight.w600,
              color: config.textColor,
            ),
          ),
        ],
      ),
    );
  }

  _StatusConfig _getStatusConfig(String status) {
    switch (status.toUpperCase()) {
      case 'SUBMITTED':
        return _StatusConfig(
          label: 'Submitted',
          dotColor: AppColors.statusSubmitted,
          textColor: AppColors.statusSubmitted,
          backgroundColor: AppColors.statusSubmitted.withValues(alpha: 0.1),
          borderColor: AppColors.statusSubmitted.withValues(alpha: 0.3),
        );
      case 'AI_PROCESSED':
        return _StatusConfig(
          label: 'AI Classified',
          dotColor: AppColors.statusAiProcessed,
          textColor: AppColors.statusAiProcessed,
          backgroundColor: AppColors.statusAiProcessed.withValues(alpha: 0.1),
          borderColor: AppColors.statusAiProcessed.withValues(alpha: 0.3),
        );
      case 'ROUTED_TO_DEPARTMENT':
        return _StatusConfig(
          label: 'Routed to Dept',
          dotColor: AppColors.statusRouted,
          textColor: AppColors.statusRouted,
          backgroundColor: AppColors.statusRouted.withValues(alpha: 0.1),
          borderColor: AppColors.statusRouted.withValues(alpha: 0.3),
        );
      case 'UNDER_DEPARTMENT_TRIAGE':
      case 'DEPARTMENT_IN_PROGRESS':
        return _StatusConfig(
          label: 'In Progress',
          dotColor: AppColors.statusTriage,
          textColor: const Color(0xFFB45309),
          backgroundColor: AppColors.statusTriage.withValues(alpha: 0.1),
          borderColor: AppColors.statusTriage.withValues(alpha: 0.3),
        );
      case 'RESOLVED':
      case 'RESOLVED_BY_DEPARTMENT':
        return _StatusConfig(
          label: 'Resolved',
          dotColor: AppColors.statusResolved,
          textColor: const Color(0xFF047857),
          backgroundColor: AppColors.statusResolved.withValues(alpha: 0.1),
          borderColor: AppColors.statusResolved.withValues(alpha: 0.3),
        );
      case 'INNOVATION_REQUIRED':
      case 'OPEN_FOR_ACADEMIC_PROPOSALS':
        return _StatusConfig(
          label: 'Innovation Required',
          dotColor: AppColors.statusInnovation,
          textColor: const Color(0xFF6D28D9),
          backgroundColor: AppColors.statusInnovation.withValues(alpha: 0.1),
          borderColor: AppColors.statusInnovation.withValues(alpha: 0.3),
        );
      case 'FLAGGED_DUPLICATE':
        return _StatusConfig(
          label: 'Duplicate Cluster',
          dotColor: AppColors.statusDuplicate,
          textColor: const Color(0xFFC2410C),
          backgroundColor: AppColors.statusDuplicate.withValues(alpha: 0.1),
          borderColor: AppColors.statusDuplicate.withValues(alpha: 0.3),
        );
      case 'REJECTED':
        return _StatusConfig(
          label: 'Closed / Rejected',
          dotColor: AppColors.statusRejected,
          textColor: AppColors.statusRejected,
          backgroundColor: AppColors.statusRejected.withValues(alpha: 0.1),
          borderColor: AppColors.statusRejected.withValues(alpha: 0.3),
        );
      default:
        return _StatusConfig(
          label: Formatters.sanitizeEnumString(status),
          dotColor: AppColors.primary,
          textColor: AppColors.primary,
          backgroundColor: AppColors.primary.withValues(alpha: 0.1),
          borderColor: AppColors.primary.withValues(alpha: 0.3),
        );
    }
  }
}

class _StatusConfig {
  final String label;
  final Color dotColor;
  final Color textColor;
  final Color backgroundColor;
  final Color borderColor;

  _StatusConfig({
    required this.label,
    required this.dotColor,
    required this.textColor,
    required this.backgroundColor,
    required this.borderColor,
  });
}
