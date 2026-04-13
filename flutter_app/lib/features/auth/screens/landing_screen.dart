import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import '../../../core/constants/app_colors.dart';
import '../../../routes/app_router.dart';

/// First screen a new / logged-out user sees.
/// Matches the Kotlin fragment_landing.xml design in the screenshot:
///  - Rounded card header with building icon
///  - "Welcome to MGB Heights" headline
///  - Login and Sign Up actions
///  - Terms footer
class LandingScreen extends StatelessWidget {
  const LandingScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF5F5F5),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.symmetric(horizontal: 24),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const SizedBox(height: 32),

              // ── Hero card ────────────────────────────────────────────────────
              Container(
                width: double.infinity,
                height: 240,
                decoration: BoxDecoration(
                  color: const Color(0xFFFBE9E7),
                  borderRadius: BorderRadius.circular(24),
                ),
                child: Center(
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Container(
                        padding: const EdgeInsets.all(20),
                        decoration: BoxDecoration(
                          color: Colors.white.withOpacity(0.6),
                          shape: BoxShape.circle,
                        ),
                        child: const Icon(
                          Icons.apartment,
                          size: 72,
                          color: AppColors.primary,
                        ),
                      ),
                    ],
                  ),
                ),
              ),

              const SizedBox(height: 32),

              // ── Headline ─────────────────────────────────────────────────────
              const Text(
                'Welcome to',
                style: TextStyle(
                  fontSize: 28,
                  fontWeight: FontWeight.w400,
                  color: Color(0xFF212121),
                ),
              ),
              const Text(
                'MGB Heights',
                style: TextStyle(
                  fontSize: 32,
                  fontWeight: FontWeight.bold,
                  color: AppColors.primary,
                ),
              ),
              const SizedBox(height: 8),
              const Text(
                'Enter your email and password to continue',
                style: TextStyle(
                  fontSize: 15,
                  color: Color(0xFF757575),
                ),
              ),

              const SizedBox(height: 40),

              // ── Login button ─────────────────────────────────────────────────
              SizedBox(
                width: double.infinity,
                height: 52,
                child: ElevatedButton(
                  onPressed: () => context.push(AppRoutes.login),
                  child: const Text('Login'),
                ),
              ),

              const SizedBox(height: 16),

              // ── Sign Up text link ─────────────────────────────────────────────
              Center(
                child: TextButton(
                  onPressed: () => context.push(AppRoutes.selectRole),
                  child: const Text(
                    "Don't have an account? Sign Up",
                    style: TextStyle(
                      color: AppColors.primary,
                      fontSize: 15,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                ),
              ),

              const SizedBox(height: 24),

              // ── Terms footer ─────────────────────────────────────────────────
              const Center(
                child: Text(
                  'By continuing, you agree to MGB Heights\nTerms of Service and Privacy Policy',
                  textAlign: TextAlign.center,
                  style: TextStyle(
                    fontSize: 12,
                    color: Color(0xFF9E9E9E),
                  ),
                ),
              ),

              const SizedBox(height: 24),
            ],
          ),
        ),
      ),
    );
  }
}
