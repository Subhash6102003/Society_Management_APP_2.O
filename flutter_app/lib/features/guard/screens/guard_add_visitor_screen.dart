import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/constants/app_colors.dart';
import '../../../providers/auth_provider.dart';
import '../../../providers/data_providers.dart';
import '../../../services/database_service.dart';
import '../../../widgets/common_widgets.dart';

class GuardAddVisitorScreen extends ConsumerStatefulWidget {
  const GuardAddVisitorScreen({super.key});

  @override
  ConsumerState<GuardAddVisitorScreen> createState() => _GuardAddVisitorScreenState();
}

class _GuardAddVisitorScreenState extends ConsumerState<GuardAddVisitorScreen> {
  final _formKey = GlobalKey<FormState>();
  final _nameCtrl = TextEditingController();
  final _phoneCtrl = TextEditingController();
  final _flatCtrl = TextEditingController();
  final _purposeCtrl = TextEditingController();
  bool _loading = false;

  @override
  void dispose() {
    _nameCtrl.dispose();
    _phoneCtrl.dispose();
    _flatCtrl.dispose();
    _purposeCtrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Log Visitor Entry')),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(20),
        child: Form(
          key: _formKey,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const SectionHeader(title: 'Visitor Information'),
              const SizedBox(height: 16),
              AppTextField(
                controller: _nameCtrl,
                label: 'Visitor Name',
                prefixIcon: Icons.person,
                validator: (v) => v!.isEmpty ? 'Required' : null,
              ),
              const SizedBox(height: 12),
              AppTextField(
                controller: _phoneCtrl,
                label: 'Phone Number',
                prefixIcon: Icons.phone,
                keyboardType: TextInputType.phone,
              ),
              const SizedBox(height: 12),
              AppTextField(
                controller: _flatCtrl,
                label: 'Flat to Visit',
                prefixIcon: Icons.home,
                validator: (v) => v!.isEmpty ? 'Required' : null,
              ),
              const SizedBox(height: 12),
              AppTextField(
                controller: _purposeCtrl,
                label: 'Purpose of Visit',
                prefixIcon: Icons.notes,
              ),
              const SizedBox(height: 28),
              SizedBox(
                width: double.infinity,
                child: PrimaryButton(
                  label: 'Check In Visitor',
                  isLoading: _loading,
                  onPressed: _submit,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;
    final user = ref.read(currentUserProvider);
    if (user == null) return;
    setState(() => _loading = true);
    try {
      await DatabaseService.instance.createVisitor(
        residentId: user.id,
        flatNumber: _flatCtrl.text.trim(),
        name: _nameCtrl.text.trim(),
        phoneNumber: _phoneCtrl.text.trim().isEmpty ? null : _phoneCtrl.text.trim(),
        purpose: _purposeCtrl.text.trim().isEmpty ? null : _purposeCtrl.text.trim(),
        guardEntry: true,
      );
      ref.invalidate(visitorsProvider);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Visitor checked in!'), backgroundColor: AppColors.success),
        );
        _formKey.currentState!.reset();
        _nameCtrl.clear(); _phoneCtrl.clear(); _flatCtrl.clear(); _purposeCtrl.clear();
      }
    } catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Error: $e')));
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }
}
