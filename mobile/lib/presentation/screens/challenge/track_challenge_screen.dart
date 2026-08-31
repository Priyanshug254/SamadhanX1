import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../../core/constants/app_colors.dart';
import '../../../data/models/challenge_models.dart';
import '../../../providers/challenge_provider.dart';
import '../../widgets/custom_button.dart';
import '../../widgets/custom_text_field.dart';
import '../../widgets/priority_meter.dart';
import '../../widgets/status_badge.dart';
import 'challenge_detail_screen.dart';

class TrackChallengeScreen extends StatefulWidget {
  const TrackChallengeScreen({super.key});

  @override
  State<TrackChallengeScreen> createState() => _TrackChallengeScreenState();
}

class _TrackChallengeScreenState extends State<TrackChallengeScreen> {
  final _trackingController = TextEditingController();
  ChallengeDetailModel? _result;

  @override
  void dispose() {
    _trackingController.dispose();
    super.dispose();
  }

  Future<void> _handleTrack() async {
    final query = _trackingController.text.trim();
    if (query.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please enter a tracking number')),
      );
      return;
    }

    final challengeProvider = context.read<ChallengeProvider>();
    final found = await challengeProvider.trackByNumber(query);

    setState(() {
      _result = found;
    });

    if (found == null && mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(challengeProvider.trackingError ?? 'No challenge found with tracking number'),
          backgroundColor: AppColors.ruby,
          behavior: SnackBarBehavior.floating,
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final challengeProvider = context.watch<ChallengeProvider>();

    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        title: const Text('Track Civic Challenge'),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back_ios_new_rounded, size: 20),
          onPressed: () => Navigator.of(context).pop(),
        ),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(20.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Tracking Banner Card
            Container(
              padding: const EdgeInsets.all(20),
              decoration: BoxDecoration(
                color: AppColors.surface,
                borderRadius: BorderRadius.circular(16),
                border: Border.all(color: AppColors.divider),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Row(
                    children: [
                      Icon(Icons.qr_code_scanner_rounded, color: AppColors.primary, size: 22),
                      SizedBox(width: 10),
                      Text(
                        'Public Challenge Tracking',
                        style: TextStyle(
                          fontSize: 16,
                          fontWeight: FontWeight.bold,
                          color: AppColors.textPrimary,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 8),
                  const Text(
                    'Enter your 16-character tracking number (e.g. SMX-2026-08-12345) to inspect real-time departmental and academic status.',
                    style: TextStyle(fontSize: 13, color: AppColors.textSecondary, height: 1.4),
                  ),
                  const SizedBox(height: 18),
                  CustomTextField(
                    controller: _trackingController,
                    hintText: 'SMX-YYYY-MM-XXXXX',
                    textCapitalization: TextCapitalization.characters,
                    prefixIcon: const Icon(Icons.search_rounded, color: AppColors.textMuted),
                  ),
                  const SizedBox(height: 14),
                  CustomButton(
                    text: 'Search Tracking Ledger',
                    icon: Icons.track_changes_rounded,
                    isLoading: challengeProvider.isTracking,
                    onPressed: _handleTrack,
                  ),
                ],
              ),
            ),
            const SizedBox(height: 24),

            // Search Result Card
            if (_result != null) ...[
              const Text(
                'Tracking Result',
                style: TextStyle(
                  fontSize: 16,
                  fontWeight: FontWeight.bold,
                  color: AppColors.textPrimary,
                ),
              ),
              const SizedBox(height: 12),
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
                            _result!.trackingNumber,
                            style: const TextStyle(
                              fontSize: 14,
                              fontWeight: FontWeight.bold,
                              color: AppColors.primary,
                            ),
                          ),
                          StatusBadge(status: _result!.status),
                        ],
                      ),
                      const SizedBox(height: 12),
                      Text(
                        _result!.title,
                        style: const TextStyle(
                          fontSize: 16,
                          fontWeight: FontWeight.bold,
                          color: AppColors.textPrimary,
                        ),
                      ),
                      const SizedBox(height: 8),
                      Text(
                        'Location: ${_result!.district ?? "Local Area"}, ${_result!.state ?? ""}',
                        style: const TextStyle(fontSize: 12, color: AppColors.textSecondary),
                      ),
                      const SizedBox(height: 14),
                      const Divider(height: 1),
                      const SizedBox(height: 12),
                      PriorityMeter(score: _result!.priorityScore),
                      const SizedBox(height: 16),
                      CustomButton(
                        text: 'View Full Timeline & Audit Log',
                        icon: Icons.timeline_rounded,
                        isOutlined: true,
                        height: 44,
                        onPressed: () {
                          Navigator.of(context).push(
                            MaterialPageRoute(
                              builder: (_) => ChallengeDetailScreen(challengeId: _result!.id),
                            ),
                          );
                        },
                      ),
                    ],
                  ),
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }
}
