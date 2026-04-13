import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:image_picker/image_picker.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

import '../../../core/constants/app_colors.dart';
import '../../../core/constants/app_constants.dart';
import '../../../models/shop_model.dart';
import '../../../providers/auth_provider.dart';
import '../../../services/database_service.dart';

class AddShopItemScreen extends ConsumerStatefulWidget {
  const AddShopItemScreen({super.key});

  @override
  ConsumerState<AddShopItemScreen> createState() => _AddShopItemScreenState();
}

class _AddShopItemScreenState extends ConsumerState<AddShopItemScreen> {
  final _formKey = GlobalKey<FormState>();
  final _titleController = TextEditingController();
  final _descController = TextEditingController();
  final _priceController = TextEditingController();

  ShopCategory _category = ShopCategory.other;
  bool _isFree = false;
  final List<XFile> _pickedImages = [];
  bool _isSubmitting = false;

  @override
  void dispose() {
    _titleController.dispose();
    _descController.dispose();
    _priceController.dispose();
    super.dispose();
  }

  // ── Pick images ────────────────────────────────────────────────────────────

  Future<void> _pickImages() async {
    final picker = ImagePicker();
    final picked = await picker.pickMultiImage(
      imageQuality: 70,
      maxWidth: 1080,
      maxHeight: 1080,
    );
    if (picked.isEmpty) return;
    setState(() {
      // Keep max 4 images total
      final remaining = 4 - _pickedImages.length;
      _pickedImages.addAll(picked.take(remaining));
    });
  }

  // ── Upload images to Supabase Storage ─────────────────────────────────────

  Future<List<String>> _uploadImages(String listingId) async {
    final client = Supabase.instance.client;
    final urls = <String>[];
    for (int i = 0; i < _pickedImages.length; i++) {
      final file = File(_pickedImages[i].path);
      final ext = _pickedImages[i].name.split('.').last.toLowerCase();
      final path = '$listingId/image_$i.$ext';
      await client.storage
          .from(AppConstants.bucketShop)
          .upload(path, file,
              fileOptions: FileOptions(contentType: 'image/$ext', upsert: true));
      final url = client.storage
          .from(AppConstants.bucketShop)
          .getPublicUrl(path);
      urls.add(url);
    }
    return urls;
  }

  // ── Submit ─────────────────────────────────────────────────────────────────

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;
    if (!_isFree) {
      final price = double.tryParse(_priceController.text.trim());
      if (price == null || price <= 0) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Enter a valid price or mark as free.')),
        );
        return;
      }
    }

    setState(() => _isSubmitting = true);

    try {
      final user = ref.read(currentUserProvider);
      if (user == null) throw Exception('Not logged in');

      final now = DateTime.now().millisecondsSinceEpoch;
      final expiresAt =
          now + (AppConstants.shopExpireDays * 24 * 60 * 60 * 1000);

      // First add listing with no images to get the ID
      final tempListing = ShopListingModel(
        id: '',
        sellerId: user.id,
        sellerName: user.name,
        flatNumber: user.flatNumber,
        towerBlock: user.towerBlock,
        title: _titleController.text.trim(),
        description: _descController.text.trim(),
        price: _isFree ? 0.0 : (double.tryParse(_priceController.text.trim()) ?? 0.0),
        isFree: _isFree,
        category: _category,
        expiresAt: expiresAt,
        createdAt: now,
        updatedAt: now,
      );

      final saved = await DatabaseService.instance.addShopListing(tempListing);

      // Upload images (if any) and update listing
      if (_pickedImages.isNotEmpty) {
        final urls = await _uploadImages(saved.id);
        await Supabase.instance.client
            .from(AppConstants.tableShopListings)
            .update({'image_urls': urls, 'updated_at': now})
            .eq('id', saved.id);
      }

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Item posted! It will be auto-removed after 15 days.'),
            backgroundColor: Colors.green,
          ),
        );
        Navigator.pop(context);
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Failed to post: $e'), backgroundColor: Colors.red),
        );
      }
    } finally {
      if (mounted) setState(() => _isSubmitting = false);
    }
  }

  // ── Build ──────────────────────────────────────────────────────────────────

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Post an Item'),
        backgroundColor: AppColors.primary,
        foregroundColor: Colors.white,
      ),
      body: Form(
        key: _formKey,
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            // ── Images ────────────────────────────────────────────────────
            const Text('Photos (optional, max 4)',
                style: TextStyle(fontWeight: FontWeight.w600)),
            const SizedBox(height: 8),
            SizedBox(
              height: 100,
              child: ListView(
                scrollDirection: Axis.horizontal,
                children: [
                  ..._pickedImages.asMap().entries.map((e) => Padding(
                        padding: const EdgeInsets.only(right: 8),
                        child: Stack(
                          children: [
                            ClipRRect(
                              borderRadius: BorderRadius.circular(8),
                              child: Image.file(
                                File(e.value.path),
                                width: 100,
                                height: 100,
                                fit: BoxFit.cover,
                              ),
                            ),
                            Positioned(
                              top: 2,
                              right: 2,
                              child: InkWell(
                                onTap: () => setState(
                                    () => _pickedImages.removeAt(e.key)),
                                child: Container(
                                  decoration: const BoxDecoration(
                                    color: Colors.black54,
                                    shape: BoxShape.circle,
                                  ),
                                  padding: const EdgeInsets.all(2),
                                  child: const Icon(Icons.close,
                                      size: 14, color: Colors.white),
                                ),
                              ),
                            ),
                          ],
                        ),
                      )),
                  if (_pickedImages.length < 4)
                    GestureDetector(
                      onTap: _pickImages,
                      child: Container(
                        width: 100,
                        height: 100,
                        decoration: BoxDecoration(
                          border: Border.all(color: Colors.grey.shade300),
                          borderRadius: BorderRadius.circular(8),
                          color: Colors.grey.shade100,
                        ),
                        child: const Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Icon(Icons.add_a_photo_outlined, color: Colors.grey),
                            SizedBox(height: 4),
                            Text('Add Photo',
                                style: TextStyle(
                                    fontSize: 11, color: Colors.grey)),
                          ],
                        ),
                      ),
                    ),
                ],
              ),
            ),
            const SizedBox(height: 20),

            // ── Title ──────────────────────────────────────────────────────
            TextFormField(
              controller: _titleController,
              decoration: _inputDecoration('Item Title *'),
              textCapitalization: TextCapitalization.sentences,
              validator: (v) =>
                  (v == null || v.trim().isEmpty) ? 'Title is required' : null,
            ),
            const SizedBox(height: 14),

            // ── Description ────────────────────────────────────────────────
            TextFormField(
              controller: _descController,
              decoration: _inputDecoration('Description (condition, usage, etc.)'),
              maxLines: 3,
              textCapitalization: TextCapitalization.sentences,
            ),
            const SizedBox(height: 14),

            // ── Category ───────────────────────────────────────────────────
            DropdownButtonFormField<ShopCategory>(
              value: _category,
              decoration: _inputDecoration('Category'),
              items: ShopCategory.values
                  .map((c) => DropdownMenuItem(
                        value: c,
                        child: Text('${c.emoji}  ${c.displayName}'),
                      ))
                  .toList(),
              onChanged: (v) => setState(() => _category = v!),
            ),
            const SizedBox(height: 14),

            // ── Free toggle ────────────────────────────────────────────────
            SwitchListTile(
              title: const Text('Offering for FREE (donation)'),
              subtitle:
                  const Text('Enable this if you want to give the item away'),
              value: _isFree,
              onChanged: (v) => setState(() => _isFree = v),
              activeColor: AppColors.primary,
              contentPadding: EdgeInsets.zero,
            ),

            // ── Price ──────────────────────────────────────────────────────
            if (!_isFree) ...[
              const SizedBox(height: 6),
              TextFormField(
                controller: _priceController,
                decoration: _inputDecoration('Asking Price (₹) *'),
                keyboardType:
                    const TextInputType.numberWithOptions(decimal: true),
              ),
            ],
            const SizedBox(height: 6),

            const Padding(
              padding: EdgeInsets.symmetric(vertical: 8),
              child: Text(
                '⏱ Your listing will be automatically removed after 15 days.',
                style: TextStyle(fontSize: 12, color: Colors.grey),
              ),
            ),
            const SizedBox(height: 20),

            // ── Submit ─────────────────────────────────────────────────────
            SizedBox(
              height: 52,
              child: ElevatedButton(
                onPressed: _isSubmitting ? null : _submit,
                style: ElevatedButton.styleFrom(
                  backgroundColor: AppColors.primary,
                  foregroundColor: Colors.white,
                  shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(12)),
                ),
                child: _isSubmitting
                    ? const SizedBox(
                        width: 24,
                        height: 24,
                        child: CircularProgressIndicator(
                            color: Colors.white, strokeWidth: 2))
                    : const Text('Post Item',
                        style: TextStyle(
                            fontSize: 16, fontWeight: FontWeight.bold)),
              ),
            ),
            const SizedBox(height: 40),
          ],
        ),
      ),
    );
  }

  InputDecoration _inputDecoration(String label) => InputDecoration(
        labelText: label,
        border: OutlineInputBorder(borderRadius: BorderRadius.circular(10)),
        contentPadding:
            const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
      );
}
