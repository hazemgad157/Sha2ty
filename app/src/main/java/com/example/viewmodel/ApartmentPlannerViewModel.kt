package com.example.viewmodel

import android.util.Base64
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import com.example.data.CatalogData
import com.example.engine.FloorPlanEngine
import com.example.engine.QuantitiesEstimate
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.util.UUID

data class PlanSnapshot(
    val walls: List<WallSegment>,
    val portals: List<PortalItem>,
    val rooms: List<RoomZone>,
    val furniture: List<FurnitureItem>,
    val columns: List<StructuralColumn>,
    val mepItems: List<MepItem>
)

data class ApartmentUiState(
    val walls: List<WallSegment> = emptyList(),
    val portals: List<PortalItem> = emptyList(),
    val rooms: List<RoomZone> = emptyList(),
    val furniture: List<FurnitureItem> = emptyList(),
    val columns: List<StructuralColumn> = emptyList(),
    val mepItems: List<MepItem> = emptyList(),
    val selectedWallId: String? = null,
    val selectedFurnitureId: String? = null,
    val selectedPortalId: String? = null,
    val selectedRoomId: String? = null,
    val toolMode: ToolMode = ToolMode.SELECT_MOVE,
    val viewMode: CanvasViewMode = CanvasViewMode.PLAN_2D,
    val showMepLayer: Boolean = false,
    val zoomScale: Float = 1.0f,
    val panOffset: Offset = Offset(60f, 60f),
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val currentVariant: String = "A",
    val quantitiesEstimate: QuantitiesEstimate? = null
)

class ApartmentPlannerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ApartmentUiState())
    val uiState: StateFlow<ApartmentUiState> = _uiState.asStateFlow()

    // Undo / Redo Stacks
    private val undoStack = mutableListOf<PlanSnapshot>()
    private val redoStack = mutableListOf<PlanSnapshot>()

    // Memory variants for A/B comparison
    private var variantA: PlanSnapshot? = null
    private var variantB: PlanSnapshot? = null

    // Temp drawing wall point
    private var drawWallStartPoint: Point2D? = null

    init {
        // Load default complete sample apartment
        val sample = CatalogData.createSampleApartment()
        val snapshot = PlanSnapshot(
            walls = sample.walls,
            portals = sample.portals,
            rooms = sample.rooms,
            furniture = sample.furniture,
            columns = sample.columns,
            mepItems = sample.mepItems
        )
        variantA = snapshot

        _uiState.update {
            it.copy(
                walls = snapshot.walls,
                portals = snapshot.portals,
                rooms = snapshot.rooms,
                furniture = snapshot.furniture,
                columns = snapshot.columns,
                mepItems = snapshot.mepItems
            )
        }
        recalculateQuantities()
    }

    private fun pushUndo() {
        val current = _uiState.value
        val snapshot = PlanSnapshot(
            walls = current.walls,
            portals = current.portals,
            rooms = current.rooms,
            furniture = current.furniture,
            columns = current.columns,
            mepItems = current.mepItems
        )
        undoStack.add(snapshot)
        if (undoStack.size > 50) undoStack.removeAt(0)
        redoStack.clear()
        _uiState.update { it.copy(canUndo = true, canRedo = false) }
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val current = _uiState.value
        val currentSnap = PlanSnapshot(
            walls = current.walls,
            portals = current.portals,
            rooms = current.rooms,
            furniture = current.furniture,
            columns = current.columns,
            mepItems = current.mepItems
        )
        redoStack.add(currentSnap)

        val previous = undoStack.removeAt(undoStack.lastIndex)
        _uiState.update {
            it.copy(
                walls = previous.walls,
                portals = previous.portals,
                rooms = previous.rooms,
                furniture = previous.furniture,
                columns = previous.columns,
                mepItems = previous.mepItems,
                canUndo = undoStack.isNotEmpty(),
                canRedo = true
            )
        }
        recalculateQuantities()
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val next = redoStack.removeAt(redoStack.lastIndex)
        pushUndo()
        _uiState.update {
            it.copy(
                walls = next.walls,
                portals = next.portals,
                rooms = next.rooms,
                furniture = next.furniture,
                columns = next.columns,
                mepItems = next.mepItems,
                canRedo = redoStack.isNotEmpty()
            )
        }
        recalculateQuantities()
    }

    fun setToolMode(mode: ToolMode) {
        drawWallStartPoint = null
        _uiState.update { it.copy(toolMode = mode) }
    }

    fun setViewMode(mode: CanvasViewMode) {
        _uiState.update { it.copy(viewMode = mode) }
    }

    fun toggleMepLayer() {
        _uiState.update { it.copy(showMepLayer = !it.showMepLayer) }
    }

    fun updateZoomPan(zoom: Float, pan: Offset) {
        _uiState.update { it.copy(zoomScale = zoom, panOffset = pan) }
    }

    fun selectWall(wallId: String?) {
        _uiState.update {
            it.copy(
                selectedWallId = wallId,
                selectedFurnitureId = if (wallId != null) null else it.selectedFurnitureId,
                selectedPortalId = if (wallId != null) null else it.selectedPortalId
            )
        }
    }

    fun selectFurniture(furnitureId: String?) {
        _uiState.update {
            it.copy(
                selectedFurnitureId = furnitureId,
                selectedWallId = if (furnitureId != null) null else it.selectedWallId,
                selectedPortalId = if (furnitureId != null) null else it.selectedPortalId
            )
        }
    }

    fun selectPortal(portalId: String?) {
        _uiState.update {
            it.copy(
                selectedPortalId = portalId,
                selectedFurnitureId = if (portalId != null) null else it.selectedFurnitureId
            )
        }
    }

    // Canvas tap in Draw Wall mode
    fun handleCanvasTap(pos: Point2D) {
        val mode = _uiState.value.toolMode
        if (mode == ToolMode.DRAW_WALL) {
            val snapped = pos.snapTo(0.10f)
            if (drawWallStartPoint == null) {
                drawWallStartPoint = snapped
            } else {
                val start = drawWallStartPoint!!
                if (start.distanceTo(snapped) >= 0.5f) {
                    pushUndo()
                    val newWall = WallSegment(
                        id = "w_" + UUID.randomUUID().toString().take(6),
                        start = start,
                        end = snapped,
                        thickness = 0.15f
                    )
                    _uiState.update {
                        it.copy(
                            walls = it.walls + newWall,
                            selectedWallId = newWall.id
                        )
                    }
                    recalculateQuantities()
                }
                drawWallStartPoint = null
            }
        }
    }

    fun updateWallEndpoints(wallId: String, newStart: Point2D, newEnd: Point2D) {
        _uiState.update { state ->
            state.copy(
                walls = state.walls.map {
                    if (it.id == wallId) it.copy(start = newStart, end = newEnd) else it
                }
            )
        }
        recalculateQuantities()
    }

    fun updateWall(updated: WallSegment) {
        pushUndo()
        _uiState.update { state ->
            state.copy(
                walls = state.walls.map { if (it.id == updated.id) updated else it }
            )
        }
        recalculateQuantities()
    }

    fun resizeWall(wallId: String, newLength: Float) {
        pushUndo()
        _uiState.update { state ->
            val wall = state.walls.find { it.id == wallId } ?: return@update state
            val resized = FloorPlanEngine.resizeWall(wall, newLength)
            state.copy(walls = state.walls.map { if (it.id == wallId) resized else it })
        }
        recalculateQuantities()
    }

    fun splitWall(wallId: String, distanceMeters: Float) {
        pushUndo()
        _uiState.update { state ->
            val wall = state.walls.find { it.id == wallId } ?: return@update state
            val (w1, w2) = FloorPlanEngine.splitWallAt(wall, distanceMeters)
            val updatedList = state.walls.filter { it.id != wallId } + listOf(w1, w2)
            state.copy(walls = updatedList, selectedWallId = w1.id)
        }
        recalculateQuantities()
    }

    fun deleteWall(wallId: String) {
        pushUndo()
        _uiState.update { state ->
            state.copy(
                walls = state.walls.filter { it.id != wallId },
                portals = state.portals.filter { it.wallId != wallId },
                selectedWallId = null
            )
        }
        recalculateQuantities()
    }

    fun updateFurniturePosition(furnitureId: String, newX: Float, newY: Float) {
        _uiState.update { state ->
            state.copy(
                furniture = state.furniture.map {
                    if (it.id == furnitureId) it.copy(x = newX, y = newY) else it
                }
            )
        }
    }

    fun updateFurniture(updated: FurnitureItem) {
        pushUndo()
        _uiState.update { state ->
            state.copy(
                furniture = state.furniture.map { if (it.id == updated.id) updated else it }
            )
        }
        recalculateQuantities()
    }

    fun duplicateFurniture(furnitureId: String) {
        pushUndo()
        _uiState.update { state ->
            val item = state.furniture.find { it.id == furnitureId } ?: return@update state
            val copy = item.copy(
                id = "furn_" + UUID.randomUUID().toString().take(6),
                x = item.x + 0.35f,
                y = item.y + 0.35f
            )
            state.copy(
                furniture = state.furniture + copy,
                selectedFurnitureId = copy.id
            )
        }
        recalculateQuantities()
    }

    fun deleteFurniture(furnitureId: String) {
        pushUndo()
        _uiState.update { state ->
            state.copy(
                furniture = state.furniture.filter { it.id != furnitureId },
                selectedFurnitureId = null
            )
        }
        recalculateQuantities()
    }

    fun addFurniture(item: FurnitureItem) {
        pushUndo()
        _uiState.update { state ->
            state.copy(
                furniture = state.furniture + item,
                selectedFurnitureId = item.id
            )
        }
        recalculateQuantities()
    }

    fun updatePortal(updated: PortalItem) {
        pushUndo()
        _uiState.update { state ->
            state.copy(
                portals = state.portals.map { if (it.id == updated.id) updated else it }
            )
        }
        recalculateQuantities()
    }

    fun deletePortal(portalId: String) {
        pushUndo()
        _uiState.update { state ->
            state.copy(
                portals = state.portals.filter { it.id != portalId },
                selectedPortalId = null
            )
        }
        recalculateQuantities()
    }

    fun addPortal(type: PortalType) {
        val walls = _uiState.value.walls
        if (walls.isEmpty()) return
        val targetWall = walls.find { it.id == _uiState.value.selectedWallId } ?: walls.first()
        pushUndo()
        val newPortal = PortalItem(
            id = "port_" + UUID.randomUUID().toString().take(6),
            wallId = targetWall.id,
            offsetMeters = (targetWall.length / 2f - 0.45f).coerceAtLeast(0.1f),
            width = if (type == PortalType.PANORAMIC_WINDOW) 1.80f else 0.90f,
            height = if (type.isWindow) 1.20f else 2.10f,
            elevation = if (type.isWindow) 0.90f else 0.0f,
            type = type
        )
        _uiState.update { state ->
            state.copy(
                portals = state.portals + newPortal,
                selectedPortalId = newPortal.id
            )
        }
        recalculateQuantities()
    }

    fun updateRoom(updated: RoomZone) {
        pushUndo()
        _uiState.update { state ->
            state.copy(
                rooms = state.rooms.map { if (it.id == updated.id) updated else it }
            )
        }
        recalculateQuantities()
    }

    fun switchVariant(variantKey: String) {
        val current = _uiState.value
        val currentSnap = PlanSnapshot(
            walls = current.walls,
            portals = current.portals,
            rooms = current.rooms,
            furniture = current.furniture,
            columns = current.columns,
            mepItems = current.mepItems
        )

        if (current.currentVariant == "A") {
            variantA = currentSnap
        } else {
            variantB = currentSnap
        }

        val targetSnap = if (variantKey == "A") {
            variantA ?: currentSnap
        } else {
            variantB ?: currentSnap.copy(
                // create a modern alternative for B
                walls = currentSnap.walls.map { it.copy(innerColor = 0xFF1D3557, isAccent = true) }
            )
        }

        _uiState.update {
            it.copy(
                walls = targetSnap.walls,
                portals = targetSnap.portals,
                rooms = targetSnap.rooms,
                furniture = targetSnap.furniture,
                columns = targetSnap.columns,
                mepItems = targetSnap.mepItems,
                currentVariant = variantKey
            )
        }
        recalculateQuantities()
    }

    fun recalculateQuantities() {
        val state = _uiState.value
        val estimate = FloorPlanEngine.calculateQuantities(
            walls = state.walls,
            portals = state.portals,
            rooms = state.rooms,
            furniture = state.furniture
        )
        _uiState.update { it.copy(quantitiesEstimate = estimate) }
    }

    fun getExportReportText(): String {
        val state = _uiState.value
        val est = state.quantitiesEstimate ?: return "لا يوجد بيانات مقايسة متاحة."
        val sb = StringBuilder()
        sb.append("📋 تقرير المقايسة والمقاسات - مصمم شقتي\n")
        sb.append("=========================================\n")
        sb.append("إجمالي مسطح الأرضيات: ${String.format("%.1f", est.totalFloorAreaM2)} م²\n")
        sb.append("صافي مسطح الحوائط: ${String.format("%.1f", est.totalWallNetAreaM2)} م²\n")
        sb.append("كمية الدهان المقدرة: ${String.format("%.1f", est.paintLitersNeeded)} لتر (${est.paintCans15L} بستلة 15L)\n")
        sb.append("عدد كراتين السيراميك: ${est.ceramicBoxesNeeded} كرتونة\n")
        sb.append("أطوال الكرانيش والوزرات: ${String.format("%.1f", est.cornicesLinearMeters)} م طولي\n\n")

        sb.append("🏠 تفاصيل الغرف:\n")
        est.roomEstimates.forEach { r ->
            sb.append("- ${r.roomName}: مساحة ${String.format("%.1f", r.areaM2)} م² | أرضية: ${r.floorMaterial} (${r.tileBoxes} كرتونة)\n")
        }

        sb.append("\n🛋️ بيان العفش والميزانية:\n")
        state.furniture.forEach { f ->
            sb.append("- ${f.name} [${f.width}×${f.depth}×${f.height}م]: ${f.priceEgp.toInt()} ج.م (${f.purchaseStatus.titleAr})\n")
        }
        sb.append("\nالإجمالي العام التقديري: ${est.furnitureTotalEgp.toInt()} ج.م\n")
        return sb.toString()
    }

    fun getShareableCode(): String {
        return try {
            val s = _uiState.value
            val root = JSONObject()
            root.put("v", 1)
            val wallsArr = JSONArray()
            for (w in s.walls) {
                val o = JSONObject()
                o.put("id", w.id)
                o.put("sx", w.start.x.toDouble())
                o.put("sy", w.start.y.toDouble())
                o.put("ex", w.end.x.toDouble())
                o.put("ey", w.end.y.toDouble())
                o.put("t", w.thickness.toDouble())
                o.put("h", w.height.toDouble())
                o.put("ic", w.innerColor)
                o.put("oc", w.outerColor)
                o.put("acc", w.isAccent)
                wallsArr.put(o)
            }
            root.put("walls", wallsArr)
            val jsonStr = root.toString()
            Base64.encodeToString(jsonStr.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
        } catch (e: Exception) {
            "ERR_CODE"
        }
    }

    fun loadFromCode(code: String) {
        try {
            val bytes = Base64.decode(code.trim(), Base64.DEFAULT)
            val jsonStr = String(bytes, StandardCharsets.UTF_8)
            val root = JSONObject(jsonStr)
            val wallsArr = root.getJSONArray("walls")
            val newWalls = mutableListOf<WallSegment>()
            for (i in 0 until wallsArr.length()) {
                val o = wallsArr.getJSONObject(i)
                newWalls.add(
                    WallSegment(
                        id = o.optString("id", "w_$i"),
                        start = Point2D(o.getDouble("sx").toFloat(), o.getDouble("sy").toFloat()),
                        end = Point2D(o.getDouble("ex").toFloat(), o.getDouble("ey").toFloat()),
                        thickness = o.optDouble("t", 0.15).toFloat(),
                        height = o.optDouble("h", 2.80).toFloat(),
                        innerColor = o.optLong("ic", 0xFFEAE6DC),
                        outerColor = o.optLong("oc", 0xFFB0BEC5),
                        isAccent = o.optBoolean("acc", false)
                    )
                )
            }
            if (newWalls.isNotEmpty()) {
                pushUndo()
                _uiState.update { it.copy(walls = newWalls) }
                recalculateQuantities()
            }
        } catch (_: Exception) {
        }
    }

    fun clearAll() {
        pushUndo()
        _uiState.update {
            it.copy(
                walls = emptyList(),
                portals = emptyList(),
                rooms = emptyList(),
                furniture = emptyList(),
                columns = emptyList(),
                mepItems = emptyList(),
                selectedWallId = null,
                selectedFurnitureId = null,
                selectedPortalId = null
            )
        }
        recalculateQuantities()
    }
}
