import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';
import '../../../core/constants/app_colors.dart';
import '../../../core/utils/formatters.dart';
import '../../../providers/challenge_provider.dart';
import '../../widgets/error_state_view.dart';
import '../../widgets/priority_meter.dart';
import '../../widgets/status_badge.dart';
import '../../widgets/timeline_stepper.dart';

class ChallengeDetailScreen extends StatefulWidget {
  final String challengeId;

  const ChallengeDetailScreen({
    super.key,
    required this.challengeId,
  });

  @override
  State<ChallengeDetailScreen> createState() => _ChallengeDetailScreenState();
}

class _ChallengeDetailScreenState extends State<ChallengeDetailScreen> {
  bool _isEndorsing = false;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<ChallengeProvider>().loadChallengeDetails(widget.challengeId);
    });
  }

  Future<void> _handleEndorse() async {
    setState(() => _isEndorsing = true);
    final success = await context.read<ChallengeProvider>().endorseChallenge(widget.challengeId);
    setState(() => _isEndorsing = false);

    if (!mounted) return;

    if (success) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Challenge endorsed! Community priority updated on server.'),
          backgroundColor: AppColors.emerald,
          behavior: SnackBarBehavior.floating,
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final challengeProvider = context.watch<ChallengeProvider>();
    final challenge = challengeProvider.currentChallenge;

    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        title: Text(challenge?.trackingNumber ?? 'Challenge Details'),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back_ios_new_rounded, size: 20),
          onPressed: () => Navigator.of(context).pop(),
        ),
        actions: [
          if (challenge != null)
            IconButton(
              icon: const Icon(Icons.share_outlined, size: 20),
              onPressed: () {
                Clipboard.setData(
                  ClipboardData(
                    text: 'SamadhanX Challenge [${challenge.trackingNumber}]: ${challenge.title}',
                  ),
                );
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(
                    content: Text('Challenge details copied for sharing'),
                    behavior: SnackBarBehavior.floating,
                  ),
                );
              },
            ),
        ],
      ),
      body: challengeProvider.isLoadingDetail
          ? const Center(child: CircularProgressIndicator())
          : challengeProvider.detailError != null
              ? ErrorStateView(
                  message: challengeProvider.detailError!,
                  onRetry: () => challengeProvider.loadChallengeDetails(widget.challengeId),
                )
              : challenge == null
                  ? const Center(child: Text('Challenge not found'))
                  : RefreshIndicator(
                      onRefresh: () => challengeProvider.loadChallengeDetails(widget.challengeId),
                      child: SingleChildScrollView(
                        padding: const EdgeInsets.all(16.0),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            // Main Card
                            Card(
                              child: Padding(
                                padding: const EdgeInsets.all(18.0),
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Row(
                                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                      children: [
                                        SelectableText(
                                          challenge.trackingNumber,
                                          style: const TextStyle(
                                            fontSize: 14,
                                            fontWeight: FontWeight.w800,
                                            color: AppColors.primary,
                                            letterSpacing: 0.5,
                                          ),
                                        ),
                                        StatusBadge(status: challenge.status, isLarge: true),
                                      ],
                                    ),
                                    const SizedBox(height: 12),
                                    Text(
                                      challenge.title,
                                      style: const TextStyle(
                                        fontSize: 18,
                                        fontWeight: FontWeight.bold,
                                        color: AppColors.textPrimary,
                                        height: 1.3,
                                      ),
                                    ),
                                    const SizedBox(height: 16),
                                    const Divider(height: 1),
                                    const SizedBox(height: 14),

                                    // SamadhanX Server-Calculated Priority
                                    const Text(
                                      'SamadhanX Calculated Priority (Multi-factor Engine)',
                                      style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold, color: AppColors.textSecondary),
                                    ),
                                    const SizedBox(height: 6),
                                    PriorityMeter(score: challenge.priorityScore),
                                    const SizedBox(height: 16),

                                    // Citizen Assessment vs Server AI Classification
                                    Container(
                                      padding: const EdgeInsets.all(12),
                                      decoration: BoxDecoration(
                                        color: AppColors.surfaceVariant,
                                        borderRadius: BorderRadius.circular(10),
                                      ),
                                      child: Column(
                                        crossAxisAlignment: CrossAxisAlignment.start,
                                        children: [
                                          const Text(
                                            'Citizen Input Assessment:',
                                            style: TextStyle(fontSize: 11, fontWeight: FontWeight.bold, color: AppColors.textMuted),
                                          ),
                                          const SizedBox(height: 4),
                                          Row(
                                            children: [
                                              _buildBadge('Severity: ${challenge.severityLevel}', AppColors.ruby),
                                              const SizedBox(width: 8),
                                              _buildBadge('Urgency: ${challenge.urgencyLevel}', AppColors.saffron),
                                            ],
                                          ),
                                          if (challenge.aiConfidenceScore != null) ...[
                                            const SizedBox(height: 8),
                                            const Divider(height: 1),
                                            const SizedBox(height: 6),
                                            Row(
                                              mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                              children: [
                                                const Text('AI Domain Confidence:', style: TextStyle(fontSize: 11, color: AppColors.textSecondary)),
                                                Text(
                                                  '${(challenge.aiConfidenceScore! * 100).toStringAsFixed(1)}%',
                                                  style: const TextStyle(fontSize: 11, fontWeight: FontWeight.bold, color: AppColors.sapphire),
                                                ),
                                              ],
                                            ),
                                          ],
                                        ],
                                      ),
                                    ),
                                    const SizedBox(height: 14),

                                    // Quick Metas Grid
                                    Row(
                                      children: [
                                        Expanded(
                                          child: _buildMetaItem(
                                            Icons.category_outlined,
                                            'Domain',
                                            challenge.domainName ?? 'General Sector',
                                            AppColors.saffron,
                                          ),
                                        ),
                                        Expanded(
                                          child: _buildMetaItem(
                                            Icons.people_outline,
                                            'Impacted Population',
                                            '${Formatters.formatNumber(challenge.estimatedAffectedPopulation)} People',
                                            AppColors.sapphire,
                                          ),
                                        ),
                                      ],
                                    ),
                                  ],
                                ),
                              ),
                            ),
                            const SizedBox(height: 16),

                            // Endorsement Card
                            Card(
                              color: AppColors.surfaceVariant,
                              child: Padding(
                                padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                                child: Row(
                                  children: [
                                    const Icon(Icons.thumb_up_alt_outlined, color: AppColors.primary, size: 22),
                                    const SizedBox(width: 12),
                                    Expanded(
                                      child: Column(
                                        crossAxisAlignment: CrossAxisAlignment.start,
                                        children: [
                                          Text(
                                            '${challenge.endorsementCount} Community Endorsements',
                                            style: const TextStyle(
                                              fontSize: 14,
                                              fontWeight: FontWeight.bold,
                                              color: AppColors.textPrimary,
                                            ),
                                          ),
                                          const Text(
                                            'Endorse to boost priority on the government triage queue',
                                            style: TextStyle(fontSize: 11, color: AppColors.textSecondary),
                                          ),
                                        ],
                                      ),
                                    ),
                                    ElevatedButton(
                                      onPressed: _isEndorsing ? null : _handleEndorse,
                                      style: ElevatedButton.styleFrom(
                                        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
                                        minimumSize: const Size(0, 36),
                                      ),
                                      child: _isEndorsing
                                          ? const SizedBox(
                                              width: 16,
                                              height: 16,
                                              child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
                                            )
                                          : const Text('Endorse', style: TextStyle(fontSize: 12)),
                                    ),
                                  ],
                                ),
                              ),
                            ),
                            const SizedBox(height: 16),

                            // Problem Description
                            _buildSectionHeader('Problem Description & Impact'),
                            Card(
                              child: Padding(
                                padding: const EdgeInsets.all(16.0),
                                child: Text(
                                  challenge.description,
                                  style: const TextStyle(
                                    fontSize: 14,
                                    color: AppColors.textPrimary,
                                    height: 1.5,
                                  ),
                                ),
                              ),
                            ),
                            const SizedBox(height: 16),

                            // Location & GIS Information
                            _buildSectionHeader('Location & Administrative Jurisdiction'),
                            Card(
                              child: Padding(
                                padding: const EdgeInsets.all(16.0),
                                child: Column(
                                  children: [
                                    _buildInfoRow('District & State', '${challenge.district ?? "N/A"}, ${challenge.state ?? "N/A"} (${challenge.pincode ?? ""})'),
                                    const Divider(height: 18),
                                    _buildInfoRow('Address', challenge.addressLine ?? challenge.locality ?? 'Local Area'),
                                    const Divider(height: 18),
                                    _buildInfoRow('Jurisdiction Level', Formatters.sanitizeEnumString(challenge.jurisdictionLevel)),
                                    if (challenge.latitude != null && challenge.longitude != null) ...[
                                      const Divider(height: 18),
                                      _buildInfoRow('GPS Coordinates', '${challenge.latitude!.toStringAsFixed(4)}° N, ${challenge.longitude!.toStringAsFixed(4)}° E'),
                                    ],
                                  ],
                                ),
                              ),
                            ),
                            const SizedBox(height: 16),

                            // Department Routing / Innovation Pipeline Info
                            if (challenge.assignedDepartmentName != null || challenge.routingRationale != null) ...[
                              _buildSectionHeader('Department Assignment & Routing'),
                              Card(
                                child: Padding(
                                  padding: const EdgeInsets.all(16.0),
                                  child: Column(
                                    crossAxisAlignment: CrossAxisAlignment.start,
                                    children: [
                                      if (challenge.assignedDepartmentName != null)
                                        _buildInfoRow('Assigned Authority', challenge.assignedDepartmentName!),
                                      if (challenge.routingRationale != null) ...[
                                        const SizedBox(height: 10),
                                        const Text('Routing Rationale:', style: TextStyle(fontSize: 12, fontWeight: FontWeight.w600, color: AppColors.textSecondary)),
                                        const SizedBox(height: 4),
                                        Text(challenge.routingRationale!, style: const TextStyle(fontSize: 13, color: AppColors.textPrimary)),
                                      ],
                                    ],
                                  ),
                                ),
                              ),
                              const SizedBox(height: 16),
                            ],

                            // Evidence Attachments
                            if (challenge.attachments.isNotEmpty) ...[
                              _buildSectionHeader('Evidence & Multimedia Attachments (${challenge.attachments.length})'),
                              ListView.builder(
                                shrinkWrap: true,
                                physics: const NeverScrollableScrollPhysics(),
                                itemCount: challenge.attachments.length,
                                itemBuilder: (context, idx) {
                                  final att = challenge.attachments[idx];
                                  return Card(
                                    margin: const EdgeInsets.only(bottom: 8),
                                    child: ListTile(
                                      leading: Icon(
                                        att.mediaType == 'IMAGE'
                                            ? Icons.image_outlined
                                            : att.mediaType == 'DOCUMENT'
                                                ? Icons.description_outlined
                                                : Icons.video_collection_outlined,
                                        color: AppColors.primary,
                                      ),
                                      title: Text(att.fileName, style: const TextStyle(fontSize: 13, fontWeight: FontWeight.bold)),
                                      subtitle: Text(att.caption ?? att.fileUrl, style: const TextStyle(fontSize: 11)),
                                    ),
                                  );
                                },
                              ),
                              const SizedBox(height: 16),
                            ],

                            // Live Activity Timeline
                            _buildSectionHeader('Real-Time Progress & Audit Timeline'),
                            Card(
                              child: Padding(
                                padding: const EdgeInsets.all(16.0),
                                child: TimelineStepper(events: challenge.timeline),
                              ),
                            ),
                            const SizedBox(height: 24),
                          ],
                        ),
                      ),
                    ),
    );
  }

  Widget _buildSectionHeader(String title) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8.0, left: 4.0),
      child: Text(
        title,
        style: const TextStyle(
          fontSize: 15,
          fontWeight: FontWeight.bold,
          color: AppColors.textPrimary,
        ),
      ),
    );
  }

  Widget _buildBadge(String text, Color color) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.12),
        borderRadius: BorderRadius.circular(6),
      ),
      child: Text(
        text,
        style: TextStyle(fontSize: 11, fontWeight: FontWeight.bold, color: color),
      ),
    );
  }

  Widget _buildMetaItem(IconData icon, String label, String value, Color color) {
    return Row(
      children: [
        Icon(icon, size: 16, color: color),
        const SizedBox(width: 8),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                label,
                style: const TextStyle(fontSize: 11, color: AppColors.textSecondary),
              ),
              Text(
                value,
                style: const TextStyle(fontSize: 13, fontWeight: FontWeight.w600, color: AppColors.textPrimary),
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
              ),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildInfoRow(String label, String value) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          label,
          style: const TextStyle(fontSize: 13, color: AppColors.textSecondary),
        ),
        const SizedBox(width: 16),
        Flexible(
          child: Text(
            value,
            style: const TextStyle(fontSize: 13, fontWeight: FontWeight.w600, color: AppColors.textPrimary),
            textAlign: TextAlign.end,
          ),
        ),
      ],
    );
  }
}
