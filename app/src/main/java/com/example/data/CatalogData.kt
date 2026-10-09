package com.example.data

import com.example.model.*

/**
 * Real Paint Shade specification
 */
data class PaintColorSpec(
    val brand: String,
    val code: String,
    val nameAr: String,
    val colorHex: Long,
    val categoryAr: String
)

object CatalogData {

    // 🎨 مكتبة ألوان دهانات حقيقية بأسماء وكودات جوتن وسيبس معتمدة
    val REAL_PAINTS = listOf(
        PaintColorSpec("جوتن فينومستيك", "1024", "تايم لس (أبيض دافئ)", 0xFFEAE6DC, "بيج ونيوترال"),
        PaintColorSpec("جوتن ليدي", "9918", "كلاسيك وايت (أبيض ناصع)", 0xFFFAF9F6, "بيج ونيوترال"),
        PaintColorSpec("جوتن فينومستيك", "1875", "سينس (بيج ناعم)", 0xFFE4DACB, "بيج ونيوترال"),
        PaintColorSpec("جوتن ليدي", "10679", "واشد أوكر (خردلي هادئ)", 0xFFD1B280, "ألوان دافئة"),
        PaintColorSpec("جوتن ليدي", "20047", "بلشينج بيتش (خوخي ترابي)", 0xFFD8B1A2, "ألوان دافئة"),
        PaintColorSpec("جوتن ليدي", "10961", "سموكد أوك (بني كاكاو)", 0xFF58483B, "ألوان دافئة"),
        PaintColorSpec("جوتن فينومستيك", "7628", "تريجر (أخضر ميرمية)", 0xFF9CA999, "أخضر وأزرق"),
        PaintColorSpec("جوتن ليدي", "6350", "سوفت تيل (تيل مهدئ)", 0xFF7A9392, "أخضر وأزرق"),
        PaintColorSpec("جوتن ليدي", "4618", "إيفنينج لايت (أزرق غامق)", 0xFF2B3A4A, "أخضر وأزرق"),
        PaintColorSpec("جوتن فينومستيك", "9913", "ماتريكس (رمادي خرساني)", 0xFF8A8F93, "رماديات مودرن"),
        PaintColorSpec("جوتن ليدي", "1434", "إليجانت (فحم داكن)", 0xFF35393D, "رماديات مودرن"),
        PaintColorSpec("سيبس رويال", "SC-7030", "عاجي مصري كلاسيك", 0xFFF2ECE1, "بيج ونيوترال"),
        PaintColorSpec("سيبس سافانا", "SV-2015", "طوبي نوبي دافئ", 0xFFBD634C, "ألوان دافئة"),
        PaintColorSpec("كابسي فاخر", "KP-440", "زيتي ملكي للأبواب", 0xFF344E41, "أخضر وأزرق")
    )

    // 🛋️ كتالوج العفش والديكور المفصل بمقاسات حقيقية وأسعار تقريبية (جنيه مصري)
    val FURNITURE_CATALOG = listOf(
        // غرف نوم
        FurnitureItem(
            id = "cat_bed_king",
            name = "سرير ماستر كينج بظهر كابوتونيه",
            category = FurnitureCategory.BEDROOM,
            x = 0f, y = 0f, width = 1.80f, depth = 2.05f, height = 1.10f,
            primaryColor = 0xFF5D4037, secondaryColor = 0xFFE0D7C6, fabricColor = 0xFFECE7DE,
            materialName = "خشب زان + قماش بوكليه",
            priceEgp = 18500.0, modelKey = "bed"
        ),
        FurnitureItem(
            id = "cat_bed_single",
            name = "سرير فردي شبابي / أطفال",
            category = FurnitureCategory.BEDROOM,
            x = 0f, y = 0f, width = 1.20f, depth = 2.00f, height = 0.90f,
            primaryColor = 0xFF795548, secondaryColor = 0xFFEDE7F6, fabricColor = 0xFF90CAF9,
            materialName = "خشب MDF إسباني مقاوم",
            priceEgp = 8200.0, modelKey = "bed"
        ),
        FurnitureItem(
            id = "cat_nightstand",
            name = "كومودينو بدرجين وقاعدة معدنية",
            category = FurnitureCategory.BEDROOM,
            x = 0f, y = 0f, width = 0.50f, depth = 0.45f, height = 0.55f,
            primaryColor = 0xFF4E342E, secondaryColor = 0xFFD4AF37, fabricColor = 0xFF4E342E,
            materialName = "خشب قشرة أرو + أرجل ذهبية",
            priceEgp = 3200.0, modelKey = "box"
        ),
        FurnitureItem(
            id = "cat_wardrobe_large",
            name = "دولاب ملابس جرار 3 ضلف مع مرايات",
            category = FurnitureCategory.BEDROOM,
            x = 0f, y = 0f, width = 2.60f, depth = 0.65f, height = 2.40f,
            primaryColor = 0xFF3E2723, secondaryColor = 0xFFCFD8DC, fabricColor = 0xFF3E2723,
            materialName = "خشب كونتر أرو وقواطيع زان",
            priceEgp = 36000.0, modelKey = "box"
        ),
        FurnitureItem(
            id = "cat_dresser",
            name = "تسريحة مع مرآة ليد دائرية وكرسي",
            category = FurnitureCategory.BEDROOM,
            x = 0f, y = 0f, width = 1.30f, depth = 0.45f, height = 1.65f,
            primaryColor = 0xFF4E342E, secondaryColor = 0xFFE0E0E0, fabricColor = 0xFFD7CCC8,
            materialName = "خشب زان أحمر + زجاج سيكوريت",
            priceEgp = 11500.0, modelKey = "box"
        ),

        // معيشة وصالون
        FurnitureItem(
            id = "cat_sofa_l",
            name = "كنبة ركنة مودرن حرف L مريحة",
            category = FurnitureCategory.LIVING,
            x = 0f, y = 0f, width = 2.80f, depth = 1.80f, height = 0.85f,
            primaryColor = 0xFF37474F, secondaryColor = 0xFF455A64, fabricColor = 0xFF78909C,
            materialName = "إسفنج كثافة 38 + قماش ووتر بروف",
            priceEgp = 24000.0, modelKey = "sofa_l"
        ),
        FurnitureItem(
            id = "cat_sofa_3seater",
            name = "كنبة صالون 3 مقاعد فخمة",
            category = FurnitureCategory.LIVING,
            x = 0f, y = 0f, width = 2.20f, depth = 0.90f, height = 0.85f,
            primaryColor = 0xFF1D3557, secondaryColor = 0xFFD4AF37, fabricColor = 0xFF457B9D,
            materialName = "خشب زان + قماش قطيفة",
            priceEgp = 16500.0, modelKey = "sofa"
        ),
        FurnitureItem(
            id = "cat_armchair",
            name = "فوتيه مفرد مريح (Armchair)",
            category = FurnitureCategory.LIVING,
            x = 0f, y = 0f, width = 0.85f, depth = 0.85f, height = 0.90f,
            primaryColor = 0xFFB45309, secondaryColor = 0xFF78350F, fabricColor = 0xFFD97706,
            materialName = "قماش كتان معالج + أرجل زان",
            priceEgp = 6800.0, modelKey = "sofa"
        ),
        FurnitureItem(
            id = "cat_coffee_table",
            name = "طاولة وسط رخامية مزدوجة",
            category = FurnitureCategory.LIVING,
            x = 0f, y = 0f, width = 1.20f, depth = 0.70f, height = 0.45f,
            primaryColor = 0xFFE2DDD5, secondaryColor = 0xFF1E293B, fabricColor = 0xFFE2DDD5,
            materialName = "سطح رخام كلكتا + قاعدة ستيل",
            priceEgp = 7500.0, modelKey = "table"
        ),
        FurnitureItem(
            id = "cat_tv_unit",
            name = "وحدة تلفزيون جدارية مع رفوف ونيش",
            category = FurnitureCategory.LIVING,
            x = 0f, y = 0f, width = 2.20f, depth = 0.40f, height = 1.80f,
            primaryColor = 0xFF3E2723, secondaryColor = 0xFF1E293B, fabricColor = 0xFF3E2723,
            materialName = "خشب أرو ماسيف + إضاءة مخفية",
            priceEgp = 14500.0, modelKey = "box"
        ),

        // سفرة وبوفيه
        FurnitureItem(
            id = "cat_dining_rect",
            name = "طاولة طعام سفرة 8 كراسي",
            category = FurnitureCategory.DINING,
            x = 0f, y = 0f, width = 2.00f, depth = 1.00f, height = 0.78f,
            primaryColor = 0xFF4E342E, secondaryColor = 0xFF8D6E63, fabricColor = 0xFFD7CCC8,
            materialName = "خشب زان أحمر مجفف",
            priceEgp = 28000.0, modelKey = "table"
        ),
        FurnitureItem(
            id = "cat_dining_round",
            name = "سفرة دائرية مودرن 4 كراسي",
            category = FurnitureCategory.DINING,
            x = 0f, y = 0f, width = 1.30f, depth = 1.30f, height = 0.78f,
            primaryColor = 0xFF3E2723, secondaryColor = 0xFFE0E0E0, fabricColor = 0xFFBCAAA4,
            materialName = "قشرة أرو طبيعي",
            priceEgp = 15000.0, modelKey = "round"
        ),
        FurnitureItem(
            id = "cat_buffet",
            name = "بوفيه سفرة بـ 4 درف مع مرايا كبيرة",
            category = FurnitureCategory.DINING,
            x = 0f, y = 0f, width = 2.10f, depth = 0.50f, height = 0.90f,
            primaryColor = 0xFF4E342E, secondaryColor = 0xFFD4AF37, fabricColor = 0xFF4E342E,
            materialName = "خشب زان + رخام إيطالي",
            priceEgp = 19500.0, modelKey = "box"
        ),

        // مطبخ وحمام
        FurnitureItem(
            id = "cat_kitchen_l",
            name = "وحدات مطبخ كاملة خشب بولي لاك حرف L",
            category = FurnitureCategory.KITCHEN_BATH,
            x = 0f, y = 0f, width = 3.20f, depth = 1.80f, height = 2.20f,
            primaryColor = 0xFF2A3439, secondaryColor = 0xFFC69C6D, fabricColor = 0xFF2A3439,
            materialName = "شاسيه كونتر + ضلف بولي لاك لامع",
            priceEgp = 52000.0, modelKey = "box"
        ),
        FurnitureItem(
            id = "cat_refrigerator",
            name = "ثلاجة دولابي سعة 580 لتر إنفرتر",
            category = FurnitureCategory.KITCHEN_BATH,
            x = 0f, y = 0f, width = 0.90f, depth = 0.75f, height = 1.85f,
            primaryColor = 0xFF37474F, secondaryColor = 0xFFB0BEC5, fabricColor = 0xFF37474F,
            materialName = "ستانلس ستيل أسود ديجيتال",
            priceEgp = 42000.0, modelKey = "box"
        ),
        FurnitureItem(
            id = "cat_cooktop",
            name = "بوتاجاز بلت إن مسطح 5 شعلة + فرن",
            category = FurnitureCategory.KITCHEN_BATH,
            x = 0f, y = 0f, width = 0.90f, depth = 0.60f, height = 0.85f,
            primaryColor = 0xFF263238, secondaryColor = 0xFF78909C, fabricColor = 0xFF263238,
            materialName = "زجاج أسود حراري ومقابض ألومنيوم",
            priceEgp = 18000.0, modelKey = "box"
        ),
        FurnitureItem(
            id = "cat_sink",
            name = "حوض مطبخ حلتين جرانيت أسود مع خلاط شلال",
            category = FurnitureCategory.KITCHEN_BATH,
            x = 0f, y = 0f, width = 1.10f, depth = 0.55f, height = 0.85f,
            primaryColor = 0xFF1E293B, secondaryColor = 0xFFCBD5E1, fabricColor = 0xFF1E293B,
            materialName = "جرانيت تركي مضاد للخدش",
            priceEgp = 8500.0, modelKey = "box"
        ),
        FurnitureItem(
            id = "cat_bathtub",
            name = "بانيو جاكوزي ديورافيت زاوية",
            category = FurnitureCategory.KITCHEN_BATH,
            x = 0f, y = 0f, width = 1.70f, depth = 0.80f, height = 0.60f,
            primaryColor = 0xFFFFFFFF, secondaryColor = 0xFFB0BEC5, fabricColor = 0xFFFFFFFF,
            materialName = "أكريليك نقي مقوى بألياف زجاجية",
            priceEgp = 16000.0, modelKey = "box"
        ),
        FurnitureItem(
            id = "cat_shower_cabin",
            name = "كابينة شاور زجاج سيكوريت 10 مم جرار",
            category = FurnitureCategory.KITCHEN_BATH,
            x = 0f, y = 0f, width = 1.00f, depth = 1.00f, height = 2.00f,
            primaryColor = 0xFF1E293B, secondaryColor = 0x889FD3F0, fabricColor = 0xFF1E293B,
            materialName = "زجاج سيكوريت عازل للمياه + ستيل أسود",
            priceEgp = 9200.0, modelKey = "box"
        ),
        FurnitureItem(
            id = "cat_toilet",
            name = "قاعدة حمام معلقة صندوق دفن جروهي",
            category = FurnitureCategory.KITCHEN_BATH,
            x = 0f, y = 0f, width = 0.40f, depth = 0.55f, height = 0.42f,
            primaryColor = 0xFFFFFFFF, secondaryColor = 0xFF90A4AE, fabricColor = 0xFFFFFFFF,
            materialName = "خزف صحي مضاد للبكتيريا",
            priceEgp = 7400.0, modelKey = "box"
        ),

        // إضاءة وديكور
        FurnitureItem(
            id = "cat_carpet_large",
            name = "سجادة نساجون شرقيون حرير مودرن 2×3 م",
            category = FurnitureCategory.LIGHTING_DECOR,
            x = 0f, y = 0f, width = 2.00f, depth = 3.00f, height = 0.02f,
            primaryColor = 0xFFD7CCC8, secondaryColor = 0xFF6D4C41, fabricColor = 0xFFBCAAA4,
            materialName = "صوف هولندي ناعم مع حرير صناعي",
            priceEgp = 8800.0, modelKey = "rug"
        ),
        FurnitureItem(
            id = "cat_indoor_plant",
            name = "نبات فيكس استوائي في أصيص فخاري كبير",
            category = FurnitureCategory.LIGHTING_DECOR,
            x = 0f, y = 0f, width = 0.55f, depth = 0.55f, height = 1.40f,
            primaryColor = 0xFF2E7D32, secondaryColor = 0xFF8D6E63, fabricColor = 0xFF388E3C,
            materialName = "أصيص خرساني معماري + نبات طبيعي",
            priceEgp = 1800.0, modelKey = "plant"
        ),
        FurnitureItem(
            id = "cat_floor_lamp",
            name = "أباجورة أرضية مودرن بذراع نحاسي مقوس",
            category = FurnitureCategory.LIGHTING_DECOR,
            x = 0f, y = 0f, width = 0.45f, depth = 0.45f, height = 1.70f,
            primaryColor = 0xFFD4AF37, secondaryColor = 0xFF1E293B, fabricColor = 0xFFFFF9C4,
            materialName = "نحاس مطلي + لمبة إديسون دافئة",
            priceEgp = 3400.0, modelKey = "lamp"
        ),
        FurnitureItem(
            id = "cat_chandelier",
            name = "نجفة صالون مودرن ليد بتصميم حلقي",
            category = FurnitureCategory.LIGHTING_DECOR,
            x = 0f, y = 0f, elevation = 2.30f, width = 0.80f, depth = 0.80f, height = 0.35f,
            primaryColor = 0xFFD4AF37, secondaryColor = 0xFFE0F7FA, fabricColor = 0xFFFFF59D,
            materialName = "ألومنيوم مذهب مع كريستال K9",
            priceEgp = 6200.0, modelKey = "round"
        )
    )

    /**
     * Initial Complete Sample Apartment Plan
     * Features:
     * - Independent segmented walls with real thicknesses and heights
     * - Master bedroom, Living room, Kitchen, Bathroom, Balcony
     * - Real physical doors with swing arcs and physical windows
     * - Recessed wall niche (تدخيل 50 سم للنيش)
     * - Structural column (عمود خرساني)
     * - Real paint shades, real flooring, MEP points
     */
    fun createSampleApartment(): InitialPlanState {
        // Points
        val p0 = Point2D(1.0f, 1.0f)   // Top-left
        val p1 = Point2D(5.5f, 1.0f)   // Living top-right
        val p2 = Point2D(9.5f, 1.0f)   // Bed top-right
        val p3 = Point2D(9.5f, 5.5f)   // Bed bottom-right
        val p4 = Point2D(5.5f, 5.5f)   // Mid junction
        val p5 = Point2D(5.5f, 8.5f)   // Bath/Kitchen bottom-right
        val p6 = Point2D(1.0f, 8.5f)   // Kitchen bottom-left
        val p7 = Point2D(1.0f, 5.5f)   // Mid left

        // Partition points
        val pMidKitchenBath = Point2D(3.2f, 8.5f)
        val pMidWallKitchenBath = Point2D(3.2f, 5.5f)

        val walls = listOf(
            // Living room top wall (with large window)
            WallSegment(
                id = "w_living_top",
                start = p0,
                end = p1,
                thickness = 0.20f,
                innerColor = 0xFFEAE6DC, // Jotun Timeless
                outerColor = 0xFFD1B280,
                paintBrandCode = "Jotun 1024 Timeless"
            ),
            // Master Bedroom top wall (with window)
            WallSegment(
                id = "w_bed_top",
                start = p1,
                end = p2,
                thickness = 0.20f,
                innerColor = 0xFFEAE6DC,
                outerColor = 0xFFD1B280,
                paintBrandCode = "Jotun 1024 Timeless"
            ),
            // Master Bedroom right wall (accent wall!)
            WallSegment(
                id = "w_bed_right",
                start = p2,
                end = p3,
                thickness = 0.20f,
                innerColor = 0xFF2B3A4A, // Accent navy
                isAccent = true,
                accentColor = 0xFF2B3A4A,
                paintBrandCode = "Jotun 4618 Evening Light"
            ),
            // Master Bedroom bottom wall
            WallSegment(
                id = "w_bed_bottom",
                start = p3,
                end = p4,
                thickness = 0.15f,
                innerColor = 0xFFEAE6DC,
                outerColor = 0xFFEAE6DC,
                paintBrandCode = "Jotun 1024 Timeless"
            ),
            // Partition wall between Living and Bed (contains Bed door & living niche bumpout!)
            WallSegment(
                id = "w_mid_vert",
                start = p1,
                end = p4,
                thickness = 0.15f,
                innerColor = 0xFFEAE6DC,
                outerColor = 0xFFEAE6DC,
                bumpOut = WallBumpOut(
                    id = "bump_niche_1",
                    offsetMeters = 1.4f,
                    widthMeters = 1.2f,
                    depthMeters = 0.40f, // 40 cm niche into living wall
                    isOutward = false
                ),
                paintBrandCode = "Jotun 1024 Timeless"
            ),
            // Wall dividing Living from Kitchen/Bath (with open entrance opening)
            WallSegment(
                id = "w_mid_horiz",
                start = p7,
                end = p4,
                thickness = 0.15f,
                innerColor = 0xFFEAE6DC,
                outerColor = 0xFF9CA999, // Sage green
                openings = listOf(
                    WallOpening(
                        id = "opening_hall",
                        offsetMeters = 1.8f,
                        widthMeters = 1.2f,
                        heightMeters = 2.40f,
                        isArched = true // فتحة مقوسة بدون باب
                    )
                )
            ),
            // Bathroom / Kitchen separator
            WallSegment(
                id = "w_bath_sep",
                start = pMidWallKitchenBath,
                end = pMidKitchenBath,
                thickness = 0.12f,
                innerColor = 0xFFFFFFFF,
                innerMaterial = WallFinish.CERAMIC,
                outerColor = 0xFF2A3439,
                outerMaterial = WallFinish.CERAMIC
            ),
            // Outer bottom wall (Kitchen & Bath)
            WallSegment(
                id = "w_bottom_outer",
                start = p6,
                end = p5,
                thickness = 0.20f,
                innerColor = 0xFFF2ECE1,
                outerColor = 0xFFB0BEC5
            ),
            // Bottom-right vertical wall
            WallSegment(
                id = "w_bottom_right",
                start = p4,
                end = p5,
                thickness = 0.20f,
                innerColor = 0xFFF2ECE1,
                outerColor = 0xFFB0BEC5
            ),
            // Left exterior wall (Living & Kitchen)
            WallSegment(
                id = "w_left_outer",
                start = p0,
                end = p6,
                thickness = 0.20f,
                innerColor = 0xFFEAE6DC,
                outerColor = 0xFFB0BEC5,
                // نصف ارتفاع في جزء المطبخ (بار أمريكي 1.10م)
                isDado = true,
                dadoBottomColor = 0xFF58483B,
                dadoTopColor = 0xFFEAE6DC
            )
        )

        val portals = listOf(
            // باب الشقة الرئيسي
            PortalItem(
                id = "p_main_door",
                wallId = "w_living_top",
                offsetMeters = 0.8f,
                width = 1.00f,
                height = 2.15f,
                type = PortalType.SINGLE_DOOR,
                swing = DoorSwing.RIGHT_IN,
                frameColor = 0xFF3E2723
            ),
            // شباك الريسبشن الكبير
            PortalItem(
                id = "p_living_win",
                wallId = "w_living_top",
                offsetMeters = 2.4f,
                width = 1.80f,
                height = 1.40f,
                elevation = 0.90f,
                type = PortalType.PANORAMIC_WINDOW,
                frameColor = 0xFF1E293B
            ),
            // باب غرفة النوم الماستر
            PortalItem(
                id = "p_bed_door",
                wallId = "w_mid_vert",
                offsetMeters = 3.2f,
                width = 0.90f,
                height = 2.10f,
                type = PortalType.SINGLE_DOOR,
                swing = DoorSwing.LEFT_IN,
                frameColor = 0xFF4E342E
            ),
            // شباك غرفة النوم
            PortalItem(
                id = "p_bed_win",
                wallId = "w_bed_top",
                offsetMeters = 1.8f,
                width = 1.40f,
                height = 1.20f,
                elevation = 1.00f,
                type = PortalType.SLIDING_WINDOW,
                frameColor = 0xFF1E293B
            ),
            // باب الحمام
            PortalItem(
                id = "p_bath_door",
                wallId = "w_mid_horiz",
                offsetMeters = 3.4f,
                width = 0.80f,
                height = 2.10f,
                type = PortalType.SINGLE_DOOR,
                swing = DoorSwing.RIGHT_IN,
                frameColor = 0xFF37474F
            )
        )

        val rooms = listOf(
            RoomZone(
                id = "r_living",
                name = "صالون ومعيشة",
                points = listOf(p0, p1, p4, p7),
                floorMaterial = FloorMaterial.LARGE_PORCELAIN,
                floorColor = 0xFFEDE8DF,
                tileSizeCm = 80,
                ceilingHeight = 2.85f,
                hasGypsumCove = true,
                spotlightsCount = 10,
                hasCornice = true
            ),
            RoomZone(
                id = "r_master_bed",
                name = "غرفة نوم ماستر",
                points = listOf(p1, p2, p3, p4),
                floorMaterial = FloorMaterial.PARQUET_OAK,
                floorColor = 0xFFC69C6D,
                tileSizeCm = 40,
                ceilingHeight = 2.80f,
                hasGypsumCove = true,
                spotlightsCount = 6,
                hasCornice = true
            ),
            RoomZone(
                id = "r_kitchen",
                name = "مطبخ أمريكي",
                points = listOf(p7, pMidWallKitchenBath, pMidKitchenBath, p6),
                floorMaterial = FloorMaterial.PORCELAIN_TILES,
                floorColor = 0xFFDFD7CA,
                tileSizeCm = 60,
                ceilingHeight = 2.70f,
                hasGypsumCove = false,
                spotlightsCount = 4,
                hasCornice = false
            ),
            RoomZone(
                id = "r_bathroom",
                name = "حمام رئيسي",
                points = listOf(pMidWallKitchenBath, p4, p5, pMidKitchenBath),
                floorMaterial = FloorMaterial.DECORATIVE_TILES,
                floorColor = 0xFFC8D6D5,
                tileSizeCm = 30,
                ceilingHeight = 2.60f,
                hasGypsumCove = false,
                spotlightsCount = 4,
                hasCornice = false
            )
        )

        val furniture = listOf(
            // Living room
            FurnitureItem(
                id = "f_sofa",
                name = "كنبة ركنة مودرن حرف L",
                category = FurnitureCategory.LIVING,
                x = 2.5f, y = 3.6f, width = 2.60f, depth = 1.60f, height = 0.85f,
                rotationDeg = 0f, primaryColor = 0xFF37474F,
                priceEgp = 24000.0, purchaseStatus = PurchaseStatus.RESERVED,
                modelKey = "sofa_l"
            ),
            FurnitureItem(
                id = "f_coffee_table",
                name = "ترابيزة وسط رخام",
                category = FurnitureCategory.LIVING,
                x = 2.8f, y = 2.3f, width = 1.10f, depth = 0.65f, height = 0.45f,
                rotationDeg = 0f, primaryColor = 0xFFE2DDD5,
                priceEgp = 6500.0, purchaseStatus = PurchaseStatus.IDEA,
                modelKey = "table"
            ),
            FurnitureItem(
                id = "f_tv_unit",
                name = "وحدة تليفزيون ونيش جداري",
                category = FurnitureCategory.LIVING,
                x = 4.8f, y = 2.6f, width = 1.80f, depth = 0.40f, height = 1.60f,
                rotationDeg = 90f, primaryColor = 0xFF4E342E,
                priceEgp = 13500.0, purchaseStatus = PurchaseStatus.PURCHASED,
                modelKey = "box"
            ),
            FurnitureItem(
                id = "f_rug_living",
                name = "سجادة صالون حرير 2×3 م",
                category = FurnitureCategory.LIGHTING_DECOR,
                x = 2.8f, y = 2.8f, width = 2.00f, depth = 2.80f, height = 0.02f,
                rotationDeg = 0f, primaryColor = 0xFFBCAAA4,
                priceEgp = 7800.0, purchaseStatus = PurchaseStatus.RESERVED,
                modelKey = "rug"
            ),

            // Bedroom
            FurnitureItem(
                id = "f_master_bed",
                name = "سرير ماستر كينج 180 سم",
                category = FurnitureCategory.BEDROOM,
                x = 8.1f, y = 3.2f, width = 1.80f, depth = 2.05f, height = 1.10f,
                rotationDeg = 90f, primaryColor = 0xFF4E342E,
                priceEgp = 19000.0, purchaseStatus = PurchaseStatus.PURCHASED,
                modelKey = "bed"
            ),
            FurnitureItem(
                id = "f_nightstand_1",
                name = "كومودينو يمين",
                category = FurnitureCategory.BEDROOM,
                x = 8.1f, y = 1.9f, width = 0.45f, depth = 0.40f, height = 0.50f,
                rotationDeg = 90f, primaryColor = 0xFF4E342E,
                priceEgp = 2800.0, purchaseStatus = PurchaseStatus.PURCHASED,
                modelKey = "box"
            ),
            FurnitureItem(
                id = "f_nightstand_2",
                name = "كومودينو شمال",
                category = FurnitureCategory.BEDROOM,
                x = 8.1f, y = 4.5f, width = 0.45f, depth = 0.40f, height = 0.50f,
                rotationDeg = 90f, primaryColor = 0xFF4E342E,
                priceEgp = 2800.0, purchaseStatus = PurchaseStatus.PURCHASED,
                modelKey = "box"
            ),
            FurnitureItem(
                id = "f_wardrobe",
                name = "دولاب ملابس جرار كبير 2.4 م",
                category = FurnitureCategory.BEDROOM,
                x = 6.4f, y = 2.0f, width = 2.20f, depth = 0.65f, height = 2.30f,
                rotationDeg = 0f, primaryColor = 0xFF3E2723,
                priceEgp = 32000.0, purchaseStatus = PurchaseStatus.IDEA,
                modelKey = "box"
            ),

            // Kitchen & Bath
            FurnitureItem(
                id = "f_kitchen_counter",
                name = "رخامة ومطبخ بولي لاك",
                category = FurnitureCategory.KITCHEN_BATH,
                x = 2.1f, y = 7.7f, width = 2.00f, depth = 0.60f, height = 0.90f,
                rotationDeg = 0f, primaryColor = 0xFF263238,
                priceEgp = 28000.0, purchaseStatus = PurchaseStatus.RESERVED,
                modelKey = "box"
            ),
            FurnitureItem(
                id = "f_refrigerator",
                name = "ثلاجة 18 قدم شارب",
                category = FurnitureCategory.KITCHEN_BATH,
                x = 1.5f, y = 6.4f, width = 0.75f, depth = 0.70f, height = 1.80f,
                rotationDeg = 90f, primaryColor = 0xFF37474F,
                priceEgp = 38000.0, purchaseStatus = PurchaseStatus.PURCHASED,
                modelKey = "box"
            ),
            FurnitureItem(
                id = "f_shower",
                name = "كابينة شاور زجاج سيكوريت",
                category = FurnitureCategory.KITCHEN_BATH,
                x = 4.7f, y = 7.7f, width = 1.00f, depth = 1.00f, height = 2.00f,
                rotationDeg = 0f, primaryColor = 0xFF1E293B,
                priceEgp = 8500.0, purchaseStatus = PurchaseStatus.IDEA,
                modelKey = "box"
            ),
            FurnitureItem(
                id = "f_toilet",
                name = "قاعدة حمام معلقة جروهي",
                category = FurnitureCategory.KITCHEN_BATH,
                x = 3.8f, y = 6.2f, width = 0.40f, depth = 0.55f, height = 0.42f,
                rotationDeg = 0f, primaryColor = 0xFFFFFFFF,
                priceEgp = 6800.0, purchaseStatus = PurchaseStatus.PURCHASED,
                modelKey = "box"
            )
        )

        val columns = listOf(
            StructuralColumn(id = "col_1", x = 5.5f, y = 5.5f, width = 0.40f, depth = 0.30f)
        )

        val mepItems = listOf(
            MepItem("mep_ac_living", "مأخذ تكييف 2.25 حصان", MepType.AC_UNIT, 4.8f, 1.3f, 2.30f),
            MepItem("mep_ac_bed", "مأخذ تكييف 1.5 حصان", MepType.AC_UNIT, 8.8f, 1.3f, 2.30f),
            MepItem("mep_router", "نقطة راوتر فايبر وتليفون", MepType.ROUTER_INTERNET, 4.5f, 2.5f, 0.40f),
            MepItem("mep_heater", "تغذية سخان مياه الحمام", MepType.WATER_HEATER, 4.8f, 6.2f, 1.80f),
            MepItem("mep_switch_living", "مفاتيح إنارة صالون 3 خط", MepType.LIGHT_SWITCH, 1.2f, 1.2f, 1.20f),
            MepItem("mep_water_sink", "محبس مياه وصرف الحوض", MepType.WATER_SUPPLY, 2.2f, 8.2f, 0.50f)
        )

        return InitialPlanState(
            walls = walls,
            portals = portals,
            rooms = rooms,
            furniture = furniture,
            columns = columns,
            mepItems = mepItems
        )
    }
}

data class InitialPlanState(
    val walls: List<WallSegment>,
    val portals: List<PortalItem>,
    val rooms: List<RoomZone>,
    val furniture: List<FurnitureItem>,
    val columns: List<StructuralColumn>,
    val mepItems: List<MepItem>
)
