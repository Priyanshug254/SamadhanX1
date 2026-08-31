import 'package:intl/intl.dart';

class Formatters {
  static String formatDate(DateTime? date) {
    if (date == null) return 'N/A';
    return DateFormat('dd MMM yyyy').format(date);
  }

  static String formatDateTime(DateTime? date) {
    if (date == null) return 'N/A';
    return DateFormat('dd MMM yyyy, hh:mm a').format(date);
  }

  static String formatTimeAgo(DateTime? date) {
    if (date == null) return '';
    final difference = DateTime.now().difference(date);

    if (difference.inDays > 30) {
      return DateFormat('dd MMM yyyy').format(date);
    } else if (difference.inDays > 0) {
      return '${difference.inDays}d ago';
    } else if (difference.inHours > 0) {
      return '${difference.inHours}h ago';
    } else if (difference.inMinutes > 0) {
      return '${difference.inMinutes}m ago';
    } else {
      return 'Just now';
    }
  }

  static String formatNumber(num? value) {
    if (value == null) return '0';
    return NumberFormat.decimalPattern().format(value);
  }

  static String formatCurrency(num? value) {
    if (value == null) return '₹0';
    return '₹${NumberFormat.currency(locale: 'en_IN', symbol: '', decimalDigits: 0).format(value).trim()}';
  }

  static String sanitizeEnumString(String? text) {
    if (text == null || text.isEmpty) return 'Unknown';
    return text
        .replaceAll('_', ' ')
        .split(' ')
        .map((word) => word.isNotEmpty
            ? '${word[0].toUpperCase()}${word.substring(1).toLowerCase()}'
            : '')
        .join(' ');
  }
}
