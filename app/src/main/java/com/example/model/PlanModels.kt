package com.example.model

import kotlin.math.*

/**
 * 2D Point on the floor plan plane (in meters).
 */
data class Point2D(val x: Float, val y: Float) {
    fun distanceTo(other: Point2D): Float =
        hypot(other.x - x, other.y - y)

    fun angleTo(other: Point2D): Float =
        atan2(other.y - y, other.x - x)

    operator fun plus(other: Point2D) = Point2D(x + other.x, y + other.y)
    operator fun minus(other: Point2D) = Point2D(x - other.x, y - other.y)
    operator fun times(factor: Float) = Point2D(x * factor, y * factor)

    fun snapTo(gridStep: Float = 0.10f): Point2D {
        return Point2D(
            (round(x / gridStep) * gridStep),
            (round(y / gridStep) * gridStep)
        )
    }

    fun lerp(other: Point2D, t: Float): Point2D {
        return Point2D(x + (other.x - x) * t, y + (other.y - y) * t)
    }
}

/**
 * Wall Finishes / Materials
 */
enum class WallFinish(val titleAr: String, val patternCode: String) {
    PAINT("دهان أملس مط", "paint"),
    WALLPAPER("ورق حائط مودرن", "wallpaper"),
    STONE("تكسية حجر / طوب", "stone"),
    CERAMIC("سيراميك حوائط", "ceramic"),
    WOOD_PANEL("بديل خشب / بانوهات", "wood")
}

/**
 * Wall bump-out (إزاحة جزء من الحائط للداخل أو الخارج 50 سم للنيش أو الأعمدة مع تقفيل الجانبين تلقائياً)
 */
data class WallBumpOut(
    val id: String,
    val offsetMeters: Float, // Distance along wall where bump-out starts
    val widthMeters: Float,  // Width of indentation (e.g. 1.50m)
    val depthMeters: Float,  // Depth of indentation (e.g. 0.50m)
    val isOutward: Boolean = false // True = exterior bump, False = interior niche
)

/**
 * Wall Cutout / Opening without a door leaf (شيل جزء من الحيطة: فتحة ريسبشن / عقد / بار)
 */
data class WallOpening(
    val id: String,
    val offsetMeters: Float,
    val widthMeters: Float,
    val heightMeters: Float = 2.40f,
    val elevationMeters: Float = 0.0f,
    val isArched: Boolean = false // فتحة بقوس إسلامي/روماني أو مستطيلة
)

/**
 * Wall Segment (قطعة جدار مستقلة من نقطة أ إلى نقطة ب)
 */
data class WallSegment(
    val id: String,
    val start: Point2D,
    val end: Point2D,
    val thickness: Float = 0.15f,     // 0.10m, 0.12m, 0.20m, 0.25m
    val height: Float = 2.80f,        // 2.80m standard, or 0.90m/1.20m half-height bar
    // Dual-face colors & finishes
    val innerColor: Long = 0xFFF1ECE1, // وجه داخلي (جوتن تايم لس)
    val innerMaterial: WallFinish = WallFinish.PAINT,
    val outerColor: Long = 0xFFE2DDD4, // وجه خارجي
    val outerMaterial: WallFinish = WallFinish.PAINT,
    val isAccent: Boolean = false,     // حائط مميز (Accent Wall)
    val accentColor: Long = 0xFF1D3557,
    val isDado: Boolean = false,       // بوازيري (نصين بلونين مختلفين)
    val dadoBottomColor: Long = 0xFF5A4D41,
    val dadoTopColor: Long = 0xFFF1ECE1,
    val paintBrandCode: String = "Jotun 1024 Timeless",
    val bumpOut: WallBumpOut? = null,
    val openings: List<WallOpening> = emptyList()
) {
    val length: Float get() = start.distanceTo(end)
    val angleRad: Float get() = start.angleTo(end)
    val angleDeg: Float get() = Math.toDegrees(angleRad.toDouble()).toFloat()

    val normal: Point2D
        get() {
            val len = length
            if (len == 0f) return Point2D(0f, 1f)
            val dx = (end.x - start.x) / len
            val dy = (end.y - start.y) / len
            return Point2D(-dy, dx)
        }
}

/**
 * Door & Window types
 */
enum class PortalType(val titleAr: String, val isWindow: Boolean) {
    SINGLE_DOOR("باب غرفة عادي", false),
    DOUBLE_DOOR("باب صالون مزدوج", false),
    SLIDING_DOOR("باب سحاب جرار", false),
    BALCONY_DOOR("بلكونة زجاجية", false),
    SLIDING_WINDOW("شباك ألوميتال جرار", true),
    CASEMENT_WINDOW("شباك مفصلي", true),
    PANORAMIC_WINDOW("شباك بانوراما كبير", true)
}

enum class DoorSwing(val titleAr: String) {
    RIGHT_IN("يمين للداخل"),
    LEFT_IN("شمال للداخل"),
    RIGHT_OUT("يمين للخارج"),
    LEFT_OUT("شمال للخارج")
}

enum class GlassType(val titleAr: String) {
    CLEAR("شفاف كريستال"),
    FROSTED("مصنفر خصوصية"),
    TINTED("ملون عاكس")
}

/**
 * Physical Door / Window embedded into a wall segment
 */
data class PortalItem(
    val id: String,
    val wallId: String,
    val offsetMeters: Float,
    val width: Float = 0.90f,
    val height: Float = 2.10f,
    val elevation: Float = 0.0f, // 0 for door, 0.9m for window
    val type: PortalType = PortalType.SINGLE_DOOR,
    val swing: DoorSwing = DoorSwing.RIGHT_IN,
    val frameColor: Long = 0xFF4A3728, // خشب بني / ألوميتال أسود
    val glassColor: Long = 0x889FD3F0,
    val glassType: GlassType = GlassType.CLEAR
)

/**
 * Flooring Materials
 */
enum class FloorMaterial(val titleAr: String, val defaultTileSizeCm: Int, val defaultColor: Long) {
    PORCELAIN_TILES("بورسلين ليزر 60×60", 60, 0xFFEDE8DF),
    LARGE_PORCELAIN("بورسلين إسباني 80×80", 80, 0xFFE2DDD5),
    PARQUET_OAK("باركيه خشب أرو طبيعي", 40, 0xFFC69C6D),
    MARBLE_IMPERADOR("رخام إمبرادور فاخر", 100, 0xFFD7CCC8),
    HOTEL_CARPET("موكيت فندقي ناعم", 50, 0xFF7D8C99),
    DECORATIVE_TILES("بلاط مغربي ديكوري", 30, 0xFFD8E2DC)
}

/**
 * Room Zone (غرفة أو فراغ متعدد الأضلاع: مستطيل، L-shape، إلخ)
 */
data class RoomZone(
    val id: String,
    val name: String,
    val points: List<Point2D>,
    val floorMaterial: FloorMaterial = FloorMaterial.PORCELAIN_TILES,
    val floorColor: Long = 0xFFEDE8DF,
    val tileSizeCm: Int = 60,
    val ceilingHeight: Float = 2.80f,
    val hasGypsumCove: Boolean = true,
    val spotlightsCount: Int = 8,
    val hasCornice: Boolean = true
)

/**
 * Furniture Categories
 */
enum class FurnitureCategory(val titleAr: String, val icon: String) {
    BEDROOM("غرف نوم", "🛏️"),
    LIVING("معيشة وصالون", "🛋️"),
    DINING("سفرة وبوفيه", "🍽️"),
    KITCHEN_BATH("مطبخ وحمام", "🍳"),
    LIGHTING_DECOR("إضاءة وديكور", "🌿"),
    OFFICE_KIDS("مكتب وأطفال", "💼")
}

enum class PurchaseStatus(val titleAr: String, val colorHex: Long) {
    IDEA("فكرة مقترحة", 0xFF64748B),
    RESERVED("محجوزة / في السلة", 0xFFD97706),
    PURCHASED("تم الشراء والتوريد", 0xFF10B981)
}

/**
 * Detailed Furniture & Decor Item
 */
data class FurnitureItem(
    val id: String,
    val name: String,
    val category: FurnitureCategory,
    val x: Float,
    val y: Float,
    val elevation: Float = 0.0f,
    val width: Float,
    val depth: Float,
    val height: Float,
    val rotationDeg: Float = 0.0f,
    val primaryColor: Long,
    val secondaryColor: Long = 0xFF8D6E63,
    val fabricColor: Long = 0xFFD7CCC8,
    val materialName: String = "خشب زان + قماش مخملي",
    val isLocked: Boolean = false,
    val priceEgp: Double = 12000.0,
    val purchaseStatus: PurchaseStatus = PurchaseStatus.IDEA,
    val modelKey: String = "box"
)

/**
 * MEP (Mechanical, Electrical & Plumbing) Layer Item
 */
enum class MepType(val titleAr: String, val symbol: String, val color: Long) {
    LIGHT_SWITCH("مفتاح إنارة", "💡", 0xFFEAB308),
    SOCKET_OUTLET("بريزة كهرباء 220V", "🔌", 0xFF3B82F6),
    AC_UNIT("مأخذ تكييف سبليت", "❄️", 0xFF06B6D4),
    ROUTER_INTERNET("مخرج راوتر وإنترنت", "🌐", 0xFF8B5CF6),
    TV_SATELLITE("مخرج دش وتلفزيون", "📺", 0xFF6366F1),
    WATER_SUPPLY("نقطة تغذية مياه سخن/بارد", "🚰", 0xFF0284C7),
    DRAINAGE("نقطة صرف صحي", "🌀", 0xFF14B8A6),
    WATER_HEATER("سخان مياه", "♨️", 0xFFEF4444)
}

data class MepItem(
    val id: String,
    val nameAr: String,
    val type: MepType,
    val x: Float,
    val y: Float,
    val elevationMeters: Float = 0.40f,
    val notes: String = ""
)

/**
 * Structural Column (أعمدة خرسانية لا يمكن إزالتها)
 */
data class StructuralColumn(
    val id: String,
    val x: Float,
    val y: Float,
    val width: Float = 0.40f,
    val depth: Float = 0.40f,
    val isCircular: Boolean = false
)

/**
 * Tool Modes for the Designer Canvas
 */
enum class ToolMode(val titleAr: String, val iconName: String) {
    SELECT_MOVE("تحديد وحرّك", "Touch"),
    DRAW_WALL("ارسم حائط", "Edit"),
    SPLIT_WALL("قسّم الحائط", "ContentCut"),
    ADD_BUMPOUT("إزاحة / نيش", "ViewSidebar"),
    ADD_PORTAL("أبواب وشبابيك", "Door"),
    ADD_FURNITURE("إضافة عفش", "Weekend"),
    MEP_LAYER("كهربا وسباكة", "Bolt"),
    RULER_MEASURE("مسطرة قياس", "Straighten")
}

enum class CanvasViewMode(val titleAr: String) {
    PLAN_2D("مخطط هندسي 2D"),
    VIEW_3D("عرض مجسم 3D"),
    SECTION_CUT("مقطع تفصيلي للحيطة")
}
