import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/constants/app_colors.dart';
import '../../../models/shop_model.dart';
import '../../../providers/auth_provider.dart';
import '../../../routes/app_router.dart';
import '../../../services/database_service.dart';

// ─── Providers ────────────────────────────────────────────────────────────────

final _shopCategoryFilterProvider =
    StateProvider<ShopCategory?>((ref) => null);
final _shopFreeOnlyProvider = StateProvider<bool>((ref) => false);

final _shopListingsProvider =
    FutureProvider.autoDispose<List<ShopListingModel>>((ref) {
  final category = ref.watch(_shopCategoryFilterProvider);
  final freeOnly = ref.watch(_shopFreeOnlyProvider);
  return DatabaseService.instance.getActiveShopListings(
    category: category,
    freeOnly: freeOnly ? true : null,
  );
});

final _myShopListingsProvider =
    FutureProvider.autoDispose<List<ShopListingModel>>((ref) {
  final user = ref.watch(currentUserProvider);
  if (user == null) return Future.value([]);
  return DatabaseService.instance.getMyShopListings(user.id);
});

// ─── Shop Screen ─────────────────────────────────────────────────────────────

class ShopScreen extends ConsumerStatefulWidget {
  const ShopScreen({super.key});

  @override
  ConsumerState<ShopScreen> createState() => _ShopScreenState();
}

class _ShopScreenState extends ConsumerState<ShopScreen>
    with SingleTickerProviderStateMixin {
  late TabController _tabController;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 2, vsync: this);
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final user = ref.watch(currentUserProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Society Shop'),
        backgroundColor: AppColors.primary,
        foregroundColor: Colors.white,
        bottom: TabBar(
          controller: _tabController,
          tabs: const [
            Tab(text: 'Browse'),
            Tab(text: 'My Listings'),
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => context.push(AppRoutes.shopAddItem),
        backgroundColor: AppColors.primary,
        foregroundColor: Colors.white,
        icon: const Icon(Icons.add),
        label: const Text('Post Item'),
      ),
      body: TabBarView(
        controller: _tabController,
        children: [
          _BrowseTab(),
          _MyListingsTab(userId: user?.id ?? ''),
        ],
      ),
    );
  }
}

// ─── Browse Tab ───────────────────────────────────────────────────────────────

class _BrowseTab extends ConsumerWidget {
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final category = ref.watch(_shopCategoryFilterProvider);
    final freeOnly = ref.watch(_shopFreeOnlyProvider);
    final listingsAsync = ref.watch(_shopListingsProvider);
    final user = ref.watch(currentUserProvider);

    return Column(
      children: [
        // ── Filter bar ──────────────────────────────────────────────────────
        Container(
          color: Colors.grey.shade100,
          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              SingleChildScrollView(
                scrollDirection: Axis.horizontal,
                child: Row(
                  children: [
                    _FilterChip(
                      label: 'All',
                      selected: category == null,
                      onTap: () => ref
                          .read(_shopCategoryFilterProvider.notifier)
                          .state = null,
                    ),
                    const SizedBox(width: 8),
                    ...ShopCategory.values.map((cat) => Padding(
                          padding: const EdgeInsets.only(right: 8),
                          child: _FilterChip(
                            label: '${cat.emoji} ${cat.displayName}',
                            selected: category == cat,
                            onTap: () => ref
                                .read(_shopCategoryFilterProvider.notifier)
                                .state = (category == cat ? null : cat),
                          ),
                        )),
                  ],
                ),
              ),
              const SizedBox(height: 6),
              Row(
                children: [
                  FilterChip(
                    label: const Text('Free / Donation only'),
                    selected: freeOnly,
                    onSelected: (v) =>
                        ref.read(_shopFreeOnlyProvider.notifier).state = v,
                    selectedColor: Colors.green.shade100,
                    checkmarkColor: Colors.green.shade700,
                  ),
                ],
              ),
            ],
          ),
        ),

        // ── Listings grid ────────────────────────────────────────────────────
        Expanded(
          child: listingsAsync.when(
            loading: () =>
                const Center(child: CircularProgressIndicator()),
            error: (e, _) =>
                Center(child: Text('Error: $e')),
            data: (listings) {
              if (listings.isEmpty) {
                return const Center(
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Icon(Icons.storefront_outlined, size: 64, color: Colors.grey),
                      SizedBox(height: 12),
                      Text(
                        'No items listed yet.\nBe the first to post something!',
                        textAlign: TextAlign.center,
                        style: TextStyle(color: Colors.grey),
                      ),
                    ],
                  ),
                );
              }
              return RefreshIndicator(
                onRefresh: () async =>
                    ref.invalidate(_shopListingsProvider),
                child: GridView.builder(
                  padding: const EdgeInsets.all(12),
                  gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                    crossAxisCount: 2,
                    childAspectRatio: 0.72,
                    crossAxisSpacing: 12,
                    mainAxisSpacing: 12,
                  ),
                  itemCount: listings.length,
                  itemBuilder: (_, i) => _ListingCard(
                    listing: listings[i],
                    isOwner: listings[i].sellerId == user?.id,
                    onRefresh: () => ref.invalidate(_shopListingsProvider),
                  ),
                ),
              );
            },
          ),
        ),
      ],
    );
  }
}

// ─── My Listings Tab ─────────────────────────────────────────────────────────

class _MyListingsTab extends ConsumerWidget {
  final String userId;
  const _MyListingsTab({required this.userId});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final listingsAsync = ref.watch(_myShopListingsProvider);

    return listingsAsync.when(
      loading: () => const Center(child: CircularProgressIndicator()),
      error: (e, _) => Center(child: Text('Error: $e')),
      data: (listings) {
        if (listings.isEmpty) {
          return const Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Icon(Icons.inventory_2_outlined, size: 64, color: Colors.grey),
                SizedBox(height: 12),
                Text(
                  "You haven't posted anything yet.",
                  style: TextStyle(color: Colors.grey),
                ),
              ],
            ),
          );
        }
        return RefreshIndicator(
          onRefresh: () async => ref.invalidate(_myShopListingsProvider),
          child: ListView.separated(
            padding: const EdgeInsets.all(12),
            separatorBuilder: (_, __) => const SizedBox(height: 10),
            itemCount: listings.length,
            itemBuilder: (_, i) => _MyListingTile(
              listing: listings[i],
              onChanged: () {
                ref.invalidate(_myShopListingsProvider);
                ref.invalidate(_shopListingsProvider);
              },
            ),
          ),
        );
      },
    );
  }
}

// ─── Listing Card (grid) ──────────────────────────────────────────────────────

class _ListingCard extends ConsumerWidget {
  final ShopListingModel listing;
  final bool isOwner;
  final VoidCallback onRefresh;

  const _ListingCard({
    required this.listing,
    required this.isOwner,
    required this.onRefresh,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final isSold = listing.isSold;
    final isExpired = listing.isExpired;

    return Card(
      clipBehavior: Clip.antiAlias,
      elevation: 2,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      child: InkWell(
        onTap: () => _showDetailSheet(context, ref),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Image
            AspectRatio(
              aspectRatio: 1,
              child: Stack(
                fit: StackFit.expand,
                children: [
                  listing.imageUrls.isNotEmpty
                      ? Image.network(
                          listing.imageUrls.first,
                          fit: BoxFit.cover,
                          errorBuilder: (_, __, ___) =>
                              const _PlaceholderImage(),
                        )
                      : const _PlaceholderImage(),
                  if (isSold || isExpired)
                    Container(
                      color: Colors.black54,
                      child: Center(
                        child: Text(
                          isSold ? 'SOLD' : 'EXPIRED',
                          style: const TextStyle(
                            color: Colors.white,
                            fontWeight: FontWeight.bold,
                            fontSize: 18,
                            letterSpacing: 2,
                          ),
                        ),
                      ),
                    ),
                  Positioned(
                    top: 8,
                    left: 8,
                    child: _CategoryBadge(listing.category),
                  ),
                  if (listing.isFree)
                    Positioned(
                      top: 8,
                      right: 8,
                      child: Container(
                        padding: const EdgeInsets.symmetric(
                            horizontal: 6, vertical: 2),
                        decoration: BoxDecoration(
                          color: Colors.green,
                          borderRadius: BorderRadius.circular(6),
                        ),
                        child: const Text(
                          'FREE',
                          style: TextStyle(
                              color: Colors.white,
                              fontSize: 11,
                              fontWeight: FontWeight.bold),
                        ),
                      ),
                    ),
                ],
              ),
            ),

            // Details
            Padding(
              padding: const EdgeInsets.all(8),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    listing.title,
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis,
                    style: const TextStyle(
                        fontWeight: FontWeight.w600, fontSize: 13),
                  ),
                  const SizedBox(height: 4),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text(
                        listing.displayPrice,
                        style: TextStyle(
                          fontWeight: FontWeight.bold,
                          fontSize: 14,
                          color: listing.isFree
                              ? Colors.green.shade700
                              : AppColors.primary,
                        ),
                      ),
                      Text(
                        '${listing.daysLeft}d left',
                        style: TextStyle(
                            fontSize: 11,
                            color: listing.daysLeft <= 2
                                ? Colors.red
                                : Colors.grey),
                      ),
                    ],
                  ),
                  Text(
                    listing.locationTag,
                    style: const TextStyle(
                        fontSize: 11, color: Colors.grey),
                    overflow: TextOverflow.ellipsis,
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  void _showDetailSheet(BuildContext context, WidgetRef ref) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
          borderRadius: BorderRadius.vertical(top: Radius.circular(20))),
      builder: (_) =>
          _ListingDetailSheet(listing: listing, isOwner: isOwner, onChanged: onRefresh),
    );
  }
}

// ─── Listing Detail Bottom Sheet ──────────────────────────────────────────────

class _ListingDetailSheet extends ConsumerWidget {
  final ShopListingModel listing;
  final bool isOwner;
  final VoidCallback onChanged;

  const _ListingDetailSheet({
    required this.listing,
    required this.isOwner,
    required this.onChanged,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return DraggableScrollableSheet(
      initialChildSize: 0.7,
      maxChildSize: 0.95,
      minChildSize: 0.4,
      expand: false,
      builder: (_, controller) {
        return SingleChildScrollView(
          controller: controller,
          padding: const EdgeInsets.all(20),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Center(
                child: Container(
                  width: 40,
                  height: 4,
                  decoration: BoxDecoration(
                    color: Colors.grey.shade400,
                    borderRadius: BorderRadius.circular(2),
                  ),
                ),
              ),
              const SizedBox(height: 16),

              // Images row
              if (listing.imageUrls.isNotEmpty)
                SizedBox(
                  height: 200,
                  child: ListView.separated(
                    scrollDirection: Axis.horizontal,
                    itemCount: listing.imageUrls.length,
                    separatorBuilder: (_, __) => const SizedBox(width: 8),
                    itemBuilder: (_, i) => ClipRRect(
                      borderRadius: BorderRadius.circular(12),
                      child: Image.network(
                        listing.imageUrls[i],
                        width: 200,
                        fit: BoxFit.cover,
                        errorBuilder: (_, __, ___) =>
                            const SizedBox(width: 200, child: _PlaceholderImage()),
                      ),
                    ),
                  ),
                ),
              if (listing.imageUrls.isNotEmpty) const SizedBox(height: 16),

              Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Expanded(
                    child: Text(
                      listing.title,
                      style: const TextStyle(
                          fontSize: 20, fontWeight: FontWeight.bold),
                    ),
                  ),
                  _CategoryBadge(listing.category),
                ],
              ),
              const SizedBox(height: 8),
              Text(
                listing.displayPrice,
                style: TextStyle(
                  fontSize: 22,
                  fontWeight: FontWeight.bold,
                  color: listing.isFree
                      ? Colors.green.shade700
                      : AppColors.primary,
                ),
              ),
              const SizedBox(height: 8),
              Row(
                children: [
                  const Icon(Icons.person_outline, size: 16, color: Colors.grey),
                  const SizedBox(width: 4),
                  Text(
                    listing.sellerName.isNotEmpty
                        ? listing.sellerName
                        : 'Resident',
                    style: const TextStyle(color: Colors.grey),
                  ),
                  const SizedBox(width: 12),
                  const Icon(Icons.location_on_outlined,
                      size: 16, color: Colors.grey),
                  const SizedBox(width: 4),
                  Expanded(
                    child: Text(
                      listing.locationTag,
                      style: const TextStyle(color: Colors.grey),
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                ],
              ),
              if (listing.description.isNotEmpty) ...[
                const SizedBox(height: 16),
                const Text(
                  'Description',
                  style: TextStyle(fontWeight: FontWeight.w600, fontSize: 16),
                ),
                const SizedBox(height: 6),
                Text(listing.description),
              ],
              const SizedBox(height: 16),
              Row(
                children: [
                  const Icon(Icons.schedule_outlined,
                      size: 16, color: Colors.grey),
                  const SizedBox(width: 4),
                  Text(
                    listing.isSold
                        ? 'SOLD'
                        : listing.isExpired
                            ? 'EXPIRED'
                            : '${listing.daysLeft} day(s) remaining',
                    style: TextStyle(
                      color: listing.isSold
                          ? Colors.red
                          : listing.daysLeft <= 2
                              ? Colors.orange
                              : Colors.grey,
                    ),
                  ),
                ],
              ),
              if (isOwner && !listing.isSold) ...[
                const SizedBox(height: 20),
                const Divider(),
                const SizedBox(height: 8),
                const Text('Your listing',
                    style: TextStyle(
                        fontWeight: FontWeight.w600, color: Colors.grey)),
                const SizedBox(height: 12),
                Row(
                  children: [
                    Expanded(
                      child: OutlinedButton.icon(
                        onPressed: () => _markSold(context, ref),
                        icon: const Icon(Icons.check_circle_outline),
                        label: const Text('Mark as Sold'),
                        style: OutlinedButton.styleFrom(
                          foregroundColor: Colors.green,
                          side: const BorderSide(color: Colors.green),
                        ),
                      ),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: OutlinedButton.icon(
                        onPressed: () => _delete(context, ref),
                        icon: const Icon(Icons.delete_outline),
                        label: const Text('Remove'),
                        style: OutlinedButton.styleFrom(
                          foregroundColor: Colors.red,
                          side: const BorderSide(color: Colors.red),
                        ),
                      ),
                    ),
                  ],
                ),
              ],
              const SizedBox(height: 20),
            ],
          ),
        );
      },
    );
  }

  Future<void> _markSold(BuildContext context, WidgetRef ref) async {
    Navigator.pop(context);
    await DatabaseService.instance.markShopItemSold(listing.id);
    onChanged();
  }

  Future<void> _delete(BuildContext context, WidgetRef ref) async {
    final confirm = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Remove listing?'),
        content: const Text('This cannot be undone.'),
        actions: [
          TextButton(
              onPressed: () => Navigator.pop(ctx, false),
              child: const Text('Cancel')),
          TextButton(
              onPressed: () => Navigator.pop(ctx, true),
              child: const Text('Remove',
                  style: TextStyle(color: Colors.red))),
        ],
      ),
    );
    if (confirm == true && context.mounted) {
      Navigator.pop(context);
      await DatabaseService.instance.deleteShopListing(listing.id);
      onChanged();
    }
  }
}

// ─── My Listing Tile (list) ───────────────────────────────────────────────────

class _MyListingTile extends ConsumerWidget {
  final ShopListingModel listing;
  final VoidCallback onChanged;

  const _MyListingTile({required this.listing, required this.onChanged});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final isSold = listing.isSold;
    final isExpired = listing.isExpired;

    return Card(
      child: ListTile(
        contentPadding:
            const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
        leading: ClipRRect(
          borderRadius: BorderRadius.circular(8),
          child: SizedBox(
            width: 60,
            height: 60,
            child: listing.imageUrls.isNotEmpty
                ? Image.network(listing.imageUrls.first,
                    fit: BoxFit.cover,
                    errorBuilder: (_, __, ___) => const _PlaceholderImage())
                : const _PlaceholderImage(),
          ),
        ),
        title: Text(
          listing.title,
          style: const TextStyle(fontWeight: FontWeight.w600),
          maxLines: 1,
          overflow: TextOverflow.ellipsis,
        ),
        subtitle: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(listing.displayPrice,
                style: TextStyle(
                    color: listing.isFree
                        ? Colors.green.shade700
                        : AppColors.primary,
                    fontWeight: FontWeight.bold)),
            Text(
              isSold
                  ? '✓ Sold'
                  : isExpired
                      ? '✗ Expired'
                      : '${listing.daysLeft}d remaining',
              style: TextStyle(
                  fontSize: 12,
                  color: isSold
                      ? Colors.green
                      : isExpired
                          ? Colors.red
                          : Colors.grey),
            ),
          ],
        ),
        trailing: (!isSold && !isExpired)
            ? PopupMenuButton<String>(
                onSelected: (v) async {
                  if (v == 'sold') {
                    await DatabaseService.instance
                        .markShopItemSold(listing.id);
                    onChanged();
                  } else if (v == 'delete') {
                    await DatabaseService.instance
                        .deleteShopListing(listing.id);
                    onChanged();
                  }
                },
                itemBuilder: (_) => [
                  const PopupMenuItem(
                    value: 'sold',
                    child: Row(children: [
                      Icon(Icons.check_circle_outline, color: Colors.green),
                      SizedBox(width: 8),
                      Text('Mark as Sold'),
                    ]),
                  ),
                  const PopupMenuItem(
                    value: 'delete',
                    child: Row(children: [
                      Icon(Icons.delete_outline, color: Colors.red),
                      SizedBox(width: 8),
                      Text('Remove', style: TextStyle(color: Colors.red)),
                    ]),
                  ),
                ],
              )
            : null,
      ),
    );
  }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

class _PlaceholderImage extends StatelessWidget {
  const _PlaceholderImage();

  @override
  Widget build(BuildContext context) {
    return Container(
      color: Colors.grey.shade200,
      child: const Center(
        child: Icon(Icons.image_outlined, color: Colors.grey, size: 36),
      ),
    );
  }
}

class _CategoryBadge extends StatelessWidget {
  final ShopCategory category;
  const _CategoryBadge(this.category);

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
      decoration: BoxDecoration(
        color: Colors.black54,
        borderRadius: BorderRadius.circular(6),
      ),
      child: Text(
        category.emoji,
        style: const TextStyle(fontSize: 14),
      ),
    );
  }
}

class _FilterChip extends StatelessWidget {
  final String label;
  final bool selected;
  final VoidCallback onTap;

  const _FilterChip({
    required this.label,
    required this.selected,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 150),
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
        decoration: BoxDecoration(
          color: selected ? AppColors.primary : Colors.white,
          border: Border.all(
              color: selected ? AppColors.primary : Colors.grey.shade300),
          borderRadius: BorderRadius.circular(20),
        ),
        child: Text(
          label,
          style: TextStyle(
            fontSize: 12,
            color: selected ? Colors.white : Colors.black87,
            fontWeight:
                selected ? FontWeight.w600 : FontWeight.normal,
          ),
        ),
      ),
    );
  }
}
