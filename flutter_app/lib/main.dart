import 'package:flutter/material.dart';
import 'package:flutter_dotenv/flutter_dotenv.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

import 'core/constants/app_constants.dart';
import 'core/demo/demo_mode.dart';
import 'core/theme/app_theme.dart';
import 'routes/app_router.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // Load .env file first
  await dotenv.load(fileName: '.env');

  // Try to initialize Supabase. If credentials are missing/invalid, enable demo mode.
  try {
    final url = dotenv.env['SUPABASE_URL'] ?? '';
    final key = dotenv.env['SUPABASE_ANON_KEY'] ?? '';

    if (url.isEmpty || url.contains('your-project-id') ||
        key.isEmpty || key.contains('your-anon-key')) {
      DemoMode.enabled = true;
      await Supabase.initialize(
        url: 'https://placeholder.supabase.co',
        anonKey: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiYW5vbiJ9.ZopqoUt20nEV8rw6HtnRmaiXw',
      );
    } else {
      await Supabase.initialize(
        url: AppConstants.supabaseUrl,
        anonKey: AppConstants.supabaseAnonKey,
      );
    }
  } catch (_) {
    DemoMode.enabled = true;
  }

  runApp(
    // ProviderScope enables Riverpod state management throughout the app
    const ProviderScope(
      child: MgbHeightsApp(),
    ),
  );
}

class MgbHeightsApp extends ConsumerWidget {
  const MgbHeightsApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final router = ref.watch(appRouterProvider);

    return MaterialApp.router(
      title: 'MGB Heights',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.lightTheme,
      routerConfig: router,
    );
  }
}
