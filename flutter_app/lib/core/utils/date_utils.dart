import 'package:intl/intl.dart';

/// Date / time helpers mirroring DateTimeUtil in the Kotlin shared module.
class AppDateUtils {
  AppDateUtils._();

  static String formatDate(DateTime date) =>
      DateFormat('dd MMM yyyy').format(date);

  static String formatDateTime(DateTime dt) =>
      DateFormat('dd MMM yyyy, hh:mm a').format(dt);

  static String formatTime(DateTime dt) =>
      DateFormat('hh:mm a').format(dt);

  static String formatMonth(DateTime dt) =>
      DateFormat('MMMM yyyy').format(dt);

  /// Converts epoch millis → formatted date string.
  static String fromEpoch(int epochMs) {
    if (epochMs == 0) return '—';
    final dt = DateTime.fromMillisecondsSinceEpoch(epochMs);
    return formatDate(dt);
  }

  static String fromEpochFull(int epochMs) {
    if (epochMs == 0) return '—';
    final dt = DateTime.fromMillisecondsSinceEpoch(epochMs);
    return formatDateTime(dt);
  }

  static int nowEpoch() => DateTime.now().millisecondsSinceEpoch;

  /// Returns true if the visitor entry has auto-expired (>30 min since entry).
  static bool isVisitorExpired(int entryTimeEpoch, {int expiryMinutes = 30}) {
    final entry = DateTime.fromMillisecondsSinceEpoch(entryTimeEpoch);
    return DateTime.now().difference(entry).inMinutes >= expiryMinutes;
  }

  static String timeAgo(int epochMs) {
    final dt = DateTime.fromMillisecondsSinceEpoch(epochMs);
    final diff = DateTime.now().difference(dt);
    if (diff.inSeconds < 60) return 'Just now';
    if (diff.inMinutes < 60) return '${diff.inMinutes}m ago';
    if (diff.inHours < 24) return '${diff.inHours}h ago';
    if (diff.inDays < 7) return '${diff.inDays}d ago';
    return formatDate(dt);
  }
}
