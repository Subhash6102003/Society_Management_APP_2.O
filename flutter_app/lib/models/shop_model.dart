import 'dart:math';

// ─── Shop Category ────────────────────────────────────────────────────────────

enum ShopCategory {
  books,
  electronics,
  furniture,
  clothing,
  kitchenware,
  toys,
  sports,
  other;

  String get displayName => switch (this) {
        ShopCategory.books => 'Books',
        ShopCategory.electronics => 'Electronics',
        ShopCategory.furniture => 'Furniture',
        ShopCategory.clothing => 'Clothing',
        ShopCategory.kitchenware => 'Kitchenware',
        ShopCategory.toys => 'Toys',
        ShopCategory.sports => 'Sports',
        ShopCategory.other => 'Other',
      };

  String get emoji => switch (this) {
        ShopCategory.books => '📚',
        ShopCategory.electronics => '📱',
        ShopCategory.furniture => '🪑',
        ShopCategory.clothing => '👕',
        ShopCategory.kitchenware => '🍳',
        ShopCategory.toys => '🧸',
        ShopCategory.sports => '⚽',
        ShopCategory.other => '🏷️',
      };

  static ShopCategory fromString(String value) => ShopCategory.values.firstWhere(
        (e) => e.name.toLowerCase() == value.toLowerCase(),
        orElse: () => ShopCategory.other,
      );
}

// ─── Shop Listing Model ───────────────────────────────────────────────────────

class ShopListingModel {
  final String id;
  final String sellerId;
  final String sellerName;
  final String flatNumber;
  final String towerBlock;
  final String title;
  final String description;
  final double price;
  final bool isFree;       // true = free donation (price is ignored)
  final List<String> imageUrls;
  final ShopCategory category;
  final bool isAvailable;
  final bool isSold;
  final int expiresAt;     // auto-delete after 15 days
  final int createdAt;
  final int updatedAt;

  const ShopListingModel({
    required this.id,
    required this.sellerId,
    this.sellerName = '',
    this.flatNumber = '',
    this.towerBlock = '',
    required this.title,
    this.description = '',
    this.price = 0.0,
    this.isFree = false,
    this.imageUrls = const [],
    this.category = ShopCategory.other,
    this.isAvailable = true,
    this.isSold = false,
    required this.expiresAt,
    required this.createdAt,
    required this.updatedAt,
  });

  /// Returns true if the listing has passed its 15-day expiry.
  bool get isExpired =>
      DateTime.now().millisecondsSinceEpoch > expiresAt && !isSold;

  /// Days remaining before automatic deletion (0 if expired).
  int get daysLeft =>
      max(0, ((expiresAt - DateTime.now().millisecondsSinceEpoch) / 86400000).ceil());

  /// Display price: "FREE" for donations, "₹X" for paid items.
  String get displayPrice => isFree ? 'FREE' : '₹${price.toStringAsFixed(price.truncateToDouble() == price ? 0 : 2)}';

  String get locationTag =>
      towerBlock.isNotEmpty ? 'Flat $flatNumber · $towerBlock' : 'Flat $flatNumber';

  factory ShopListingModel.fromJson(Map<String, dynamic> json) {
    final rawUrls = json['image_urls'];
    List<String> urls = [];
    if (rawUrls is List) {
      urls = rawUrls.map((e) => e.toString()).toList();
    }

    return ShopListingModel(
      id: json['id'] as String? ?? '',
      sellerId: json['seller_id'] as String? ?? '',
      sellerName: json['seller_name'] as String? ?? '',
      flatNumber: json['flat_number'] as String? ?? '',
      towerBlock: json['tower_block'] as String? ?? '',
      title: json['title'] as String? ?? '',
      description: json['description'] as String? ?? '',
      price: (json['price'] as num?)?.toDouble() ?? 0.0,
      isFree: json['is_free'] as bool? ?? false,
      imageUrls: urls,
      category: ShopCategory.fromString(json['category'] as String? ?? 'other'),
      isAvailable: json['is_available'] as bool? ?? true,
      isSold: json['is_sold'] as bool? ?? false,
      expiresAt: (json['expires_at'] as num?)?.toInt() ?? 0,
      createdAt: (json['created_at'] as num?)?.toInt() ?? 0,
      updatedAt: (json['updated_at'] as num?)?.toInt() ?? 0,
    );
  }

  Map<String, dynamic> toJson() => {
        'id': id,
        'seller_id': sellerId,
        'seller_name': sellerName,
        'flat_number': flatNumber,
        'tower_block': towerBlock,
        'title': title,
        'description': description,
        'price': price,
        'is_free': isFree,
        'image_urls': imageUrls,
        'category': category.name,
        'is_available': isAvailable,
        'is_sold': isSold,
        'expires_at': expiresAt,
        'created_at': createdAt,
        'updated_at': updatedAt,
      };

  ShopListingModel copyWith({
    bool? isAvailable,
    bool? isSold,
  }) {
    return ShopListingModel(
      id: id,
      sellerId: sellerId,
      sellerName: sellerName,
      flatNumber: flatNumber,
      towerBlock: towerBlock,
      title: title,
      description: description,
      price: price,
      isFree: isFree,
      imageUrls: imageUrls,
      category: category,
      isAvailable: isAvailable ?? this.isAvailable,
      isSold: isSold ?? this.isSold,
      expiresAt: expiresAt,
      createdAt: createdAt,
      updatedAt: updatedAt,
    );
  }
}
