package com.example.viewmodel

import android.app.Application
import android.util.Base64
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CatalogData
import com.example.data.local.ApartmentDatabase
import com.example.data.local.ApartmentRepository
import com.example.data.local.entities.ProjectEntity
import com.example.data.local.entities.RoomLayoutEntity
import com.example.data.local.entities.WallDimensionEntity
import com.example.engine.FloorPlanEngine
import com.example.engine.QuantitiesEstimate
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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

class ApartmentPlannerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ApartmentRepository(
        ApartmentDatabase.getDatabase(application).apartmentDao()
    )

    // Flow of saved projects from Room Database
    val savedProjects: StateFlow<List<ProjectEntity>> = repository.allProjects
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

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

    // --- Room Database Operations ---

    fun saveCurrentProject(projectName: String, clientName: String, notes: String) {
        viewModelScope.launch {
            val state = _uiState.value
            val projectId = "proj_" + UUID.randomUUID().toString().take(8)
            val jsonSnapshot = serializeFullSnapshot(state)

            val totalArea = state.quantitiesEstimate?.totalFloorAreaM2 ?: 0f
            val totalBudget = state.quantitiesEstimate?.furnitureTotalEgp ?: 0.0

            val projectEntity = ProjectEntity(
                projectId = projectId,
                name = projectName,
                clientName = clientName,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                totalFloorAreaM2 = totalArea,
                totalEstimatedBudgetEgp = totalBudget,
                notes = notes,
                snapshotJson = jsonSnapshot
            )

            // Normalized Room Layout Entities
            val roomEntities = state.rooms.map { r ->
                val pointsArr = JSONArray()
                r.points.forEach { pt ->
                    val pObj = JSONObject()
                    pObj.put("x", pt.x)
                    pObj.put("y", pt.y)
                    pointsArr.put(pObj)
                }
                RoomLayoutEntity(
                    roomId = r.id,
                    projectId = projectId,
                    roomName = r.name,
                    floorMaterial = r.floorMaterial.name,
                    tileSizeCm = r.tileSizeCm,
                    floorColorHex = r.floorColor,
                    ceilingHeightM = r.ceilingHeight,
                    hasGypsumCove = r.hasGypsumCove,
                    calculatedAreaM2 = FloorPlanEngine.calculatePolygonArea(r.points),
                    polygonPointsJson = pointsArr.toString()
                )
            }

            // Normalized Wall Dimension Entities
            val wallEntities = state.walls.map { w ->
                WallDimensionEntity(
                    wallId = w.id,
                    projectId = projectId,
                    lengthMeters = w.length,
                    thicknessMeters = w.thickness,
                    heightMeters = w.height,
                    startX = w.start.x,
                    startY = w.start.y,
                    endX = w.end.x,
                    endY = w.end.y,
                    innerColorHex = w.innerColor,
                    outerColorHex = w.outerColor,
                    paintBrandCode = w.paintBrandCode,
                    isAccent = w.isAccent,
                    hasBumpOut = w.bumpOut != null,
                    bumpOutDepthM = w.bumpOut?.depthMeters ?: 0f,
                    openingsCount = w.openings.size
                )
            }

            repository.saveProject(projectEntity, roomEntities, wallEntities)
        }
    }

    fun loadSavedProject(project: ProjectEntity) {
        val snapshot = deserializeFullSnapshot(project.snapshotJson)
        if (snapshot != null) {
            pushUndo()
            _uiState.update {
                it.copy(
                    walls = snapshot.walls,
                    portals = snapshot.portals,
                    rooms = snapshot.rooms,
                    furniture = snapshot.furniture,
                    columns = snapshot.columns,
                    mepItems = snapshot.mepItems,
                    selectedWallId = null,
                    selectedFurnitureId = null,
                    selectedPortalId = null
                )
            }
            recalculateQuantities()
        }
    }

    fun deleteSavedProject(projectId: String) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
        }
    }

    // --- State & History Operations ---

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
            val jsonStr = serializeFullSnapshot(_uiState.value)
            Base64.encodeToString(jsonStr.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
        } catch (_: Exception) {
            "ERR_CODE"
        }
    }

    fun loadFromCode(code: String) {
        try {
            val bytes = Base64.decode(code.trim(), Base64.DEFAULT)
            val jsonStr = String(bytes, StandardCharsets.UTF_8)
            val snapshot = deserializeFullSnapshot(jsonStr)
            if (snapshot != null) {
                pushUndo()
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
        } catch (_: Exception) {
        }
    }

    // --- JSON Serialization Helpers ---

    private fun serializeFullSnapshot(state: ApartmentUiState): String {
        val root = JSONObject()
        root.put("v", 2)

        // Walls
        val wallsArr = JSONArray()
        for (w in state.walls) {
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
            o.put("acCol", w.accentColor)
            o.put("brand", w.paintBrandCode)
            if (w.bumpOut != null) {
                val bo = JSONObject()
                bo.put("id", w.bumpOut.id)
                bo.put("off", w.bumpOut.offsetMeters.toDouble())
                bo.put("w", w.bumpOut.widthMeters.toDouble())
                bo.put("d", w.bumpOut.depthMeters.toDouble())
                bo.put("out", w.bumpOut.isOutward)
                o.put("bump", bo)
            }
            wallsArr.put(o)
        }
        root.put("walls", wallsArr)

        // Portals
        val portalsArr = JSONArray()
        for (p in state.portals) {
            val o = JSONObject()
            o.put("id", p.id)
            o.put("wallId", p.wallId)
            o.put("off", p.offsetMeters.toDouble())
            o.put("w", p.width.toDouble())
            o.put("h", p.height.toDouble())
            o.put("el", p.elevation.toDouble())
            o.put("type", p.type.name)
            o.put("swing", p.swing.name)
            o.put("fc", p.frameColor)
            o.put("gt", p.glassType.name)
            portalsArr.put(o)
        }
        root.put("portals", portalsArr)

        // Rooms
        val roomsArr = JSONArray()
        for (r in state.rooms) {
            val o = JSONObject()
            o.put("id", r.id)
            o.put("name", r.name)
            o.put("mat", r.floorMaterial.name)
            o.put("col", r.floorColor)
            o.put("tile", r.tileSizeCm)
            o.put("ch", r.ceilingHeight.toDouble())
            o.put("cove", r.hasGypsumCove)
            val ptsArr = JSONArray()
            r.points.forEach { pt ->
                val po = JSONObject()
                po.put("x", pt.x.toDouble())
                po.put("y", pt.y.toDouble())
                ptsArr.put(po)
            }
            o.put("pts", ptsArr)
            roomsArr.put(o)
        }
        root.put("rooms", roomsArr)

        // Furniture
        val furnArr = JSONArray()
        for (f in state.furniture) {
            val o = JSONObject()
            o.put("id", f.id)
            o.put("name", f.name)
            o.put("cat", f.category.name)
            o.put("x", f.x.toDouble())
            o.put("y", f.y.toDouble())
            o.put("w", f.width.toDouble())
            o.put("d", f.depth.toDouble())
            o.put("h", f.height.toDouble())
            o.put("rot", f.rotationDeg.toDouble())
            o.put("pc", f.primaryColor)
            o.put("sc", f.secondaryColor)
            o.put("fc", f.fabricColor)
            o.put("mat", f.materialName)
            o.put("lock", f.isLocked)
            o.put("price", f.priceEgp)
            o.put("stat", f.purchaseStatus.name)
            o.put("model", f.modelKey)
            furnArr.put(o)
        }
        root.put("furniture", furnArr)

        // Columns
        val colsArr = JSONArray()
        for (c in state.columns) {
            val o = JSONObject()
            o.put("id", c.id)
            o.put("x", c.x.toDouble())
            o.put("y", c.y.toDouble())
            o.put("w", c.width.toDouble())
            o.put("d", c.depth.toDouble())
            colsArr.put(o)
        }
        root.put("columns", colsArr)

        // MEP Items
        val mepArr = JSONArray()
        for (m in state.mepItems) {
            val o = JSONObject()
            o.put("id", m.id)
            o.put("name", m.nameAr)
            o.put("type", m.type.name)
            o.put("x", m.x.toDouble())
            o.put("y", m.y.toDouble())
            o.put("el", m.elevationMeters.toDouble())
            mepArr.put(o)
        }
        root.put("mep", mepArr)

        return root.toString()
    }

    private fun deserializeFullSnapshot(jsonStr: String): PlanSnapshot? {
        return try {
            val root = JSONObject(jsonStr)

            // Walls
            val walls = mutableListOf<WallSegment>()
            val wallsArr = root.optJSONArray("walls")
            if (wallsArr != null) {
                for (i in 0 until wallsArr.length()) {
                    val o = wallsArr.getJSONObject(i)
                    var bo: WallBumpOut? = null
                    if (o.has("bump")) {
                        val bObj = o.getJSONObject("bump")
                        bo = WallBumpOut(
                            id = bObj.optString("id", "bo_$i"),
                            offsetMeters = bObj.getDouble("off").toFloat(),
                            widthMeters = bObj.getDouble("w").toFloat(),
                            depthMeters = bObj.getDouble("d").toFloat(),
                            isOutward = bObj.optBoolean("out", false)
                        )
                    }
                    walls.add(
                        WallSegment(
                            id = o.optString("id", "w_$i"),
                            start = Point2D(o.getDouble("sx").toFloat(), o.getDouble("sy").toFloat()),
                            end = Point2D(o.getDouble("ex").toFloat(), o.getDouble("ey").toFloat()),
                            thickness = o.optDouble("t", 0.15).toFloat(),
                            height = o.optDouble("h", 2.80).toFloat(),
                            innerColor = o.optLong("ic", 0xFFEAE6DC),
                            outerColor = o.optLong("oc", 0xFFB0BEC5),
                            isAccent = o.optBoolean("acc", false),
                            accentColor = o.optLong("acCol", 0xFF1D3557),
                            paintBrandCode = o.optString("brand", "Jotun 1024 Timeless"),
                            bumpOut = bo
                        )
                    )
                }
            }

            // Portals
            val portals = mutableListOf<PortalItem>()
            val portalsArr = root.optJSONArray("portals")
            if (portalsArr != null) {
                for (i in 0 until portalsArr.length()) {
                    val o = portalsArr.getJSONObject(i)
                    portals.add(
                        PortalItem(
                            id = o.optString("id", "p_$i"),
                            wallId = o.optString("wallId", ""),
                            offsetMeters = o.getDouble("off").toFloat(),
                            width = o.getDouble("w").toFloat(),
                            height = o.getDouble("h").toFloat(),
                            elevation = o.optDouble("el", 0.0).toFloat(),
                            type = try { PortalType.valueOf(o.getString("type")) } catch (_: Exception) { PortalType.SINGLE_DOOR },
                            swing = try { DoorSwing.valueOf(o.getString("swing")) } catch (_: Exception) { DoorSwing.RIGHT_IN },
                            frameColor = o.optLong("fc", 0xFF4A3728),
                            glassType = try { GlassType.valueOf(o.getString("gt")) } catch (_: Exception) { GlassType.CLEAR }
                        )
                    )
                }
            }

            // Rooms
            val rooms = mutableListOf<RoomZone>()
            val roomsArr = root.optJSONArray("rooms")
            if (roomsArr != null) {
                for (i in 0 until roomsArr.length()) {
                    val o = roomsArr.getJSONObject(i)
                    val pts = mutableListOf<Point2D>()
                    val ptsArr = o.getJSONArray("pts")
                    for (k in 0 until ptsArr.length()) {
                        val po = ptsArr.getJSONObject(k)
                        pts.add(Point2D(po.getDouble("x").toFloat(), po.getDouble("y").toFloat()))
                    }
                    rooms.add(
                        RoomZone(
                            id = o.optString("id", "r_$i"),
                            name = o.optString("name", "غرفة"),
                            points = pts,
                            floorMaterial = try { FloorMaterial.valueOf(o.getString("mat")) } catch (_: Exception) { FloorMaterial.PORCELAIN_TILES },
                            floorColor = o.optLong("col", 0xFFEDE8DF),
                            tileSizeCm = o.optInt("tile", 60),
                            ceilingHeight = o.optDouble("ch", 2.80).toFloat(),
                            hasGypsumCove = o.optBoolean("cove", true)
                        )
                    )
                }
            }

            // Furniture
            val furniture = mutableListOf<FurnitureItem>()
            val furnArr = root.optJSONArray("furniture")
            if (furnArr != null) {
                for (i in 0 until furnArr.length()) {
                    val o = furnArr.getJSONObject(i)
                    furniture.add(
                        FurnitureItem(
                            id = o.optString("id", "f_$i"),
                            name = o.optString("name", "قطعة عفش"),
                            category = try { FurnitureCategory.valueOf(o.getString("cat")) } catch (_: Exception) { FurnitureCategory.LIVING },
                            x = o.getDouble("x").toFloat(),
                            y = o.getDouble("y").toFloat(),
                            width = o.getDouble("w").toFloat(),
                            depth = o.getDouble("d").toFloat(),
                            height = o.getDouble("h").toFloat(),
                            rotationDeg = o.optDouble("rot", 0.0).toFloat(),
                            primaryColor = o.optLong("pc", 0xFF37474F),
                            secondaryColor = o.optLong("sc", 0xFF8D6E63),
                            fabricColor = o.optLong("fc", 0xFFD7CCC8),
                            materialName = o.optString("mat", "خشب زان"),
                            isLocked = o.optBoolean("lock", false),
                            priceEgp = o.optDouble("price", 10000.0),
                            purchaseStatus = try { PurchaseStatus.valueOf(o.getString("stat")) } catch (_: Exception) { PurchaseStatus.IDEA },
                            modelKey = o.optString("model", "box")
                        )
                    )
                }
            }

            // Columns
            val columns = mutableListOf<StructuralColumn>()
            val colsArr = root.optJSONArray("columns")
            if (colsArr != null) {
                for (i in 0 until colsArr.length()) {
                    val o = colsArr.getJSONObject(i)
                    columns.add(
                        StructuralColumn(
                            id = o.optString("id", "c_$i"),
                            x = o.getDouble("x").toFloat(),
                            y = o.getDouble("y").toFloat(),
                            width = o.getDouble("w").toFloat(),
                            depth = o.getDouble("d").toFloat()
                        )
                    )
                }
            }

            // MEP
            val mep = mutableListOf<MepItem>()
            val mepArr = root.optJSONArray("mep")
            if (mepArr != null) {
                for (i in 0 until mepArr.length()) {
                    val o = mepArr.getJSONObject(i)
                    mep.add(
                        MepItem(
                            id = o.optString("id", "m_$i"),
                            nameAr = o.optString("name", "نقطة"),
                            type = try { MepType.valueOf(o.getString("type")) } catch (_: Exception) { MepType.SOCKET_OUTLET },
                            x = o.getDouble("x").toFloat(),
                            y = o.getDouble("y").toFloat(),
                            elevationMeters = o.optDouble("el", 0.40).toFloat()
                        )
                    )
                }
            }

            PlanSnapshot(
                walls = walls,
                portals = portals,
                rooms = rooms,
                furniture = furniture,
                columns = columns,
                mepItems = mep
            )
        } catch (_: Exception) {
            null
        }
    }
}
