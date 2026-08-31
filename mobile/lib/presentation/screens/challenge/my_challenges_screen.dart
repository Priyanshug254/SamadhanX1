import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../../core/constants/app_colors.dart';
import '../../../data/models/challenge_models.dart';
import '../../../providers/challenge_provider.dart';
import '../../widgets/challenge_card.dart';
import '../../widgets/empty_state_view.dart';
import '../../widgets/error_state_view.dart';
import 'challenge_detail_screen.dart';
import 'submit_challenge_screen.dart';

class MyChallengesScreen extends StatefulWidget {
  const MyChallengesScreen({super.key});

  @override
  State<MyChallengesScreen> createState() => _MyChallengesScreenState();
}

class _MyChallengesScreenState extends State<MyChallengesScreen> with SingleTickerProviderStateMixin {
  late TabController _tabController;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 4, vsync: this);
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<ChallengeProvider>().fetchMySubmissions();
    });
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  List<ChallengeSummaryModel> _filterList(List<ChallengeSummaryModel> list, int tabIndex) {
    switch (tabIndex) {
      case 1: // In Progress / Triage
        return list.where((c) =>
            c.status == 'SUBMITTED' ||
            c.status == 'AI_PROCESSED' ||
            c.status == 'ROUTED_TO_DEPARTMENT' ||
            c.status == 'UNDER_DEPARTMENT_TRIAGE' ||
            c.status == 'DEPARTMENT_IN_PROGRESS').toList();
      case 2: // Resolved
        return list.where((c) =>
            c.status == 'RESOLVED' ||
            c.status == 'RESOLVED_BY_DEPARTMENT').toList();
      case 3: // Innovation Pipeline
        return list.where((c) =>
            c.status == 'INNOVATION_REQUIRED' ||
            c.status == 'OPEN_FOR_ACADEMIC_PROPOSALS' ||
            c.status == 'SOLUTION_PROTOTYPING' ||
            c.status == 'FIELD_PILOT_TESTING').toList();
      default:
        return list;
    }
  }

  @override
  Widget build(BuildContext context) {
    final challengeProvider = context.watch<ChallengeProvider>();
    final myChallenges = challengeProvider.mySubmissions;

    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        title: const Text('My Submissions'),
        bottom: TabBar(
          controller: _tabController,
          labelColor: AppColors.primary,
          unselectedLabelColor: AppColors.textSecondary,
          indicatorColor: AppColors.primary,
          indicatorWeight: 3,
          labelStyle: const TextStyle(fontSize: 13, fontWeight: FontWeight.bold),
          unselectedLabelStyle: const TextStyle(fontSize: 13, fontWeight: FontWeight.normal),
          tabs: const [
            Tab(text: 'All'),
            Tab(text: 'In Progress'),
            Tab(text: 'Resolved'),
            Tab(text: 'Innovation'),
          ],
        ),
      ),
      body: challengeProvider.isLoadingMy
          ? const Center(child: CircularProgressIndicator())
          : challengeProvider.myError != null
              ? ErrorStateView(
                  message: challengeProvider.myError!,
                  onRetry: () => challengeProvider.fetchMySubmissions(refresh: true),
                )
              : TabBarView(
                  controller: _tabController,
                  children: List.generate(4, (index) {
                    final filtered = _filterList(myChallenges, index);

                    if (filtered.isEmpty) {
                      return EmptyStateView(
                        icon: Icons.playlist_add_check_rounded,
                        title: 'No Submissions in this Tab',
                        message: index == 0
                            ? 'You have not submitted any societal challenges yet.'
                            : 'No challenges matching this status tab.',
                        actionText: index == 0 ? 'Report a Challenge' : null,
                        onAction: index == 0
                            ? () {
                                Navigator.of(context).push(
                                  MaterialPageRoute(
                                    builder: (_) => const SubmitChallengeScreen(),
                                  ),
                                );
                              }
                            : null,
                      );
                    }

                    return RefreshIndicator(
                      onRefresh: () => challengeProvider.fetchMySubmissions(refresh: true),
                      child: ListView.builder(
                        padding: const EdgeInsets.all(16.0),
                        itemCount: filtered.length,
                        itemBuilder: (context, idx) {
                          final item = filtered[idx];
                          return ChallengeCard(
                            challenge: item,
                            onTap: () {
                              Navigator.of(context).push(
                                MaterialPageRoute(
                                  builder: (_) => ChallengeDetailScreen(challengeId: item.id),
                                ),
                              );
                            },
                          );
                        },
                      ),
                    );
                  }),
                ),
    );
  }
}
