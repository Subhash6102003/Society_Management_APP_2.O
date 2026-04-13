import 'package:flutter/material.dart';

/// Brand colours for MGB Heights.
/// Matches the Android Kotlin app's color palette exactly.
class AppColors {
  AppColors._();

  // ── Brand ────────────────────────────────────────────────────────────────────
  static const Color primary = Color(0xFFE65100);      // Deep Orange 900
  static const Color primaryDark = Color(0xFFBF360C);  // Deep Orange 900 dark
  static const Color primaryHeader = Color(0xFFEF6C00); // Orange 800
  static const Color primaryLight = Color(0xFFFF8A65);  // Deep Orange 300

  // ── Backgrounds ───────────────────────────────────────────────────────────────
  static const Color background = Color(0xFFF8F6F6);
  static const Color surface = Color(0xFFFFFFFF);
  static const Color surfaceVariant = Color(0xFFFBE9E7); // Orange tint

  // ── Text ─────────────────────────────────────────────────────────────────────
  static const Color textPrimary = Color(0xFF212121);
  static const Color textSecondary = Color(0xFF757575);
  static const Color textHint = Color(0xFFBDBDBD);

  // ── Status colours (Android: success #2E7D32, warning #F57F17, info #1565C0) ─
  static const Color success = Color(0xFF2E7D32);
  static const Color warning = Color(0xFFF57F17);
  static const Color error = Color(0xFFE53935);
  static const Color info = Color(0xFF1565C0);

  // ── Stat card tint colours (Android dashboard cards) ─────────────────────────
  static const Color statPinkBg = Color(0xFFFFF3E0);   // Total users card
  static const Color statPinkText = Color(0xFFBF360C);
  static const Color statOrange = Color(0xFFFF9800);    // Pending card
  static const Color statGreen = Color(0xFF388E3C);     // Collected card

  // ── Role badge colours ───────────────────────────────────────────────────────
  static const Color admin = Color(0xFFE65100);
  static const Color resident = Color(0xFF2E7D32);
  static const Color tenant = Color(0xFF1565C0);
  static const Color guard = Color(0xFFF57F17);
  static const Color worker = Color(0xFF8E24AA);
  static const Color maid = Color(0xFFE91E63);

  // ── Misc ─────────────────────────────────────────────────────────────────────
  static const Color divider = Color(0xFFE0E0E0);
  static const Color disabled = Color(0xFFBDBDBD);
  static const Color shimmerBase = Color(0xFFEEEEEE);
  static const Color shimmerHighlight = Color(0xFFFAFAFA);
}
