import 'dart:io';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:image_picker/image_picker.dart';
import '../../../core/constants/app_colors.dart';
import '../../../core/utils/validators.dart';
import '../../../models/user_model.dart';
import '../../../providers/auth_provider.dart';
import '../../../services/storage_service.dart';
import '../../../widgets/common_widgets.dart';

/// Second step of sign-up: complete profile details.
/// Fields vary per role — Resident and Tenant additionally need flat number.
class CreateProfileScreen extends ConsumerStatefulWidget {
  const CreateProfileScreen({super.key});

  @override
  ConsumerState<CreateProfileScreen> createState() =>
      _CreateProfileScreenState();
}

class _CreateProfileScreenState extends ConsumerState<CreateProfileScreen> {
  final _formKey = GlobalKey<FormState>();
  final _nameCtrl = TextEditingController();
  final _phoneCtrl = TextEditingController();
  final _flatCtrl = TextEditingController();
  final _towerCtrl = TextEditingController();

  File? _profilePhoto;
  File? _idProof;
  bool _isLoading = false;

  @override
  void dispose() {
    _nameCtrl.dispose();
    _phoneCtrl.dispose();
    _flatCtrl.dispose();
    _towerCtrl.dispose();
    super.dispose();
  }

  UserRole get _role =>
      ref.read(currentUserProvider)?.role ?? UserRole.resident;

  Future<void> _pickImage(bool isProfile) async {
    final picker = ImagePicker();
    final picked = await picker.pickImage(source: ImageSource.gallery, imageQuality: 70);
    if (picked == null) return;
    setState(() {
      if (isProfile) {
        _profilePhoto = File(picked.path);
      } else {
        _idProof = File(picked.path);
      }
    });
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;
    setState(() => _isLoading = true);

    try {
      final user = ref.read(currentUserProvider)!;
      String profileUrl = '';
      String idProofUrl = '';

      if (_profilePhoto != null) {
        profileUrl = await StorageService.instance.uploadProfilePhoto(
          _profilePhoto!,
          user.id,
        );
      }
      if (_idProof != null) {
        idProofUrl = await StorageService.instance.uploadIdProof(
          _idProof!,
          user.id,
        );
      }

      await ref.read(authProvider.notifier).completeProfile(
            name: _nameCtrl.text.trim(),
            phoneNumber: _phoneCtrl.text.trim(),
            flatNumber: _flatCtrl.text.trim(),
            towerBlock: _towerCtrl.text.trim(),
            profilePhotoUrl: profileUrl,
            idProofUrl: idProofUrl,
          );

      // Check if completeProfile silently failed (state is still pendingProfile)
      final authState = ref.read(authProvider);
      if (mounted && authState.errorMessage != null) {
        _showError(authState.errorMessage!);
      }
    } catch (e) {
      if (mounted) {
        _showError(e.toString().replaceAll('Exception: ', ''));
      }
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
    // On success: router auto-redirects to PendingApprovalScreen
  }

  void _showError(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        backgroundColor: AppColors.error,
        duration: const Duration(seconds: 8),
        action: SnackBarAction(
          label: 'OK',
          textColor: Colors.white,
          onPressed: () =>
              ScaffoldMessenger.of(context).hideCurrentSnackBar(),
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    // Pre-fill name if already available from sign-up step
    final user = ref.watch(currentUserProvider);
    if (_nameCtrl.text.isEmpty && (user?.name.isNotEmpty ?? false)) {
      _nameCtrl.text = user!.name;
    }

    return Scaffold(
      backgroundColor: const Color(0xFFF5F5F5),
      appBar: AppBar(title: const Text('Complete Your Profile')),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(24),
        child: Form(
          key: _formKey,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // ── Profile photo picker ─────────────────────────────────────────
              Center(
                child: GestureDetector(
                  onTap: () => _pickImage(true),
                  child: CircleAvatar(
                    radius: 52,
                    backgroundColor: AppColors.surfaceVariant,
                    backgroundImage: _profilePhoto != null
                        ? FileImage(_profilePhoto!)
                        : null,
                    child: _profilePhoto == null
                        ? Column(
                            mainAxisSize: MainAxisSize.min,
                            children: const [
                              Icon(Icons.camera_alt_outlined,
                                  size: 28, color: AppColors.primary),
                              SizedBox(height: 4),
                              Text(
                                'Add Photo',
                                style: TextStyle(
                                  fontSize: 11,
                                  color: AppColors.primary,
                                ),
                              ),
                            ],
                          )
                        : null,
                  ),
                ),
              ),

              const SizedBox(height: 28),

              // ── Name ─────────────────────────────────────────────────────────
              AppTextField(
                controller: _nameCtrl,
                hint: 'Full Name',
                prefixIcon: Icons.badge_outlined,
                validator: (v) => AppValidators.required(v, label: 'Name'),
              ),

              const SizedBox(height: 16),

              // ── Phone ─────────────────────────────────────────────────────────
              AppTextField(
                controller: _phoneCtrl,
                hint: 'Phone Number',
                prefixIcon: Icons.phone_outlined,
                keyboardType: TextInputType.phone,
                validator: AppValidators.phone,
              ),

              // ── Flat fields (only for Resident / Tenant) ─────────────────────
              if (_role.requiresFlat) ...[
                const SizedBox(height: 16),
                AppTextField(
                  controller: _flatCtrl,
                  hint: 'Flat Number (e.g. A-101)',
                  prefixIcon: Icons.door_front_door_outlined,
                  validator: AppValidators.flatNumber,
                ),
                const SizedBox(height: 16),
                AppTextField(
                  controller: _towerCtrl,
                  hint: 'Tower / Block (e.g. Tower A)',
                  prefixIcon: Icons.apartment_outlined,
                  validator: (v) =>
                      AppValidators.required(v, label: 'Tower/Block'),
                ),
              ],

              const SizedBox(height: 24),

              // ── ID proof upload ───────────────────────────────────────────────
              _IdProofPicker(
                file: _idProof,
                onTap: () => _pickImage(false),
              ),

              const SizedBox(height: 32),

              // ── Submit ────────────────────────────────────────────────────────
              PrimaryButton(
                label: 'Submit for Approval',
                onPressed: _submit,
                isLoading: _isLoading,
              ),

              const SizedBox(height: 16),
              const Center(
                child: Text(
                  'Your profile will be reviewed by the society admin.\nYou\'ll be notified once approved.',
                  textAlign: TextAlign.center,
                  style: TextStyle(fontSize: 12, color: Color(0xFF9E9E9E)),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

// ─── ID Proof picker widget ────────────────────────────────────────────────────

class _IdProofPicker extends StatelessWidget {
  final File? file;
  final VoidCallback onTap;

  const _IdProofPicker({required this.file, required this.onTap});

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        height: 120,
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(12),
          border: Border.all(color: AppColors.divider),
        ),
        child: file != null
            ? ClipRRect(
                borderRadius: BorderRadius.circular(12),
                child: Image.file(file!, fit: BoxFit.cover, width: double.infinity),
              )
            : Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: const [
                  Icon(Icons.file_upload_outlined,
                      size: 36, color: AppColors.primary),
                  SizedBox(height: 8),
                  Text(
                    'Upload ID Proof',
                    style: TextStyle(color: AppColors.primary, fontWeight: FontWeight.w500),
                  ),
                  SizedBox(height: 4),
                  Text(
                    'Aadhaar / PAN / Voter ID / Passport',
                    style: TextStyle(fontSize: 12, color: AppColors.textSecondary),
                  ),
                ],
              ),
      ),
    );
  }
}
