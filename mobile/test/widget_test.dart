import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:samadhanx_mobile/presentation/widgets/custom_button.dart';
import 'package:samadhanx_mobile/presentation/widgets/domain_chip.dart';
import 'package:samadhanx_mobile/presentation/widgets/priority_meter.dart';
import 'package:samadhanx_mobile/presentation/widgets/status_badge.dart';

void main() {
  group('SamadhanX Citizen Widget Tests', () {
    testWidgets('CustomButton renders and handles tap callback', (WidgetTester tester) async {
      bool tapped = false;

      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: CustomButton(
              text: 'Report Problem',
              onPressed: () {
                tapped = true;
              },
            ),
          ),
        ),
      );

      expect(find.text('Report Problem'), findsOneWidget);
      await tester.tap(find.text('Report Problem'));
      expect(tapped, isTrue);
    });

    testWidgets('StatusBadge renders correct status and label', (WidgetTester tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: Scaffold(
            body: Column(
              children: [
                StatusBadge(status: 'INNOVATION_REQUIRED'),
                StatusBadge(status: 'RESOLVED_BY_DEPARTMENT'),
                StatusBadge(status: 'SUBMITTED'),
              ],
            ),
          ),
        ),
      );

      expect(find.text('Innovation Required'), findsOneWidget);
      expect(find.text('Resolved'), findsOneWidget);
      expect(find.text('Submitted'), findsOneWidget);
    });

    testWidgets('PriorityMeter displays score and label correctly', (WidgetTester tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: Scaffold(
            body: PriorityMeter(score: 88.5),
          ),
        ),
      );

      expect(find.text('Priority Score: 88.5'), findsOneWidget);
      expect(find.text('CRITICAL'), findsOneWidget);
    });

    testWidgets('DomainChip renders and handles selection', (WidgetTester tester) async {
      bool clicked = false;

      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: DomainChip(
              label: 'Water & Sanitation',
              isSelected: true,
              onTap: () {
                clicked = true;
              },
            ),
          ),
        ),
      );

      expect(find.text('Water & Sanitation'), findsOneWidget);
      await tester.tap(find.text('Water & Sanitation'));
      expect(clicked, isTrue);
    });
  });
}
