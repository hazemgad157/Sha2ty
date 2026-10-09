package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.*
import com.example.ui.canvas.FloorPlanCanvas
import com.example.ui.components.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.threed.Apartment3DViewer
import com.example.viewmodel.ApartmentPlannerViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ApartmentApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApartmentApp(
    viewModel: ApartmentPlannerViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val savedProjects by viewModel.savedProjects.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Dialog & Sheet States
    var showProjectsDialog by remember { mutableStateOf(false) }
    var showCatalogSheet by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var showComparisonDialog by remember { mutableStateOf(false) }
    var showWallSectionDialog by remember { mutableStateOf(false) }

    val selectedWall = state.walls.find { it.id == state.selectedWallId }
    val selectedFurniture = state.furniture.find { it.id == state.selectedFurnitureId }
    val selectedPortal = state.portals.find { it.id == state.selectedPortalId }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🏠 مصمم شقتي 3D",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "نسخة ${state.currentVariant}",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                },
                actions = {
                    // Undo & Redo
                    IconButton(
                        onClick = { viewModel.undo() },
                        enabled = state.canUndo
                    ) {
                        Icon(Icons.Default.Undo, contentDescription = "تراجع")
                    }
                    IconButton(
                        onClick = { viewModel.redo() },
                        enabled = state.canRedo
                    ) {
                        Icon(Icons.Default.Redo, contentDescription = "إعادة")
                    }

                    // MEP Layer Toggle
                    IconButton(
                        onClick = { viewModel.toggleMepLayer() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "كهربا وسباكة",
                            tint = if (state.showMepLayer) Color(0xFFEAB308) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Saved Projects (Room DB)
                    IconButton(onClick = { showProjectsDialog = true }) {
                        Icon(Icons.Default.Folder, contentDescription = "مشاريعي المحفوظة")
                    }

                    // Budget & Quantities Dialog
                    IconButton(onClick = {
                        viewModel.recalculateQuantities()
                        showBudgetDialog = true
                    }) {
                        Icon(Icons.Default.Calculate, contentDescription = "المقايسة والميزانية")
                    }

                    // A/B Comparison
                    IconButton(onClick = { showComparisonDialog = true }) {
                        Icon(Icons.Default.Compare, contentDescription = "مقارنة A/B")
                    }

                    // Share & Export
                    IconButton(onClick = { showShareDialog = true }) {
                        Icon(Icons.Default.Share, contentDescription = "مشاركة")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // View Mode Switcher + Tools
            Surface(
                tonalElevation = 4.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)) {
                    // View Mode Switcher: 2D Floor Plan vs 3D Orbit/Walkthrough
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                    ) {
                        SegmentedButton(
                            selected = state.viewMode == CanvasViewMode.PLAN_2D,
                            onClick = { viewModel.setViewMode(CanvasViewMode.PLAN_2D) },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                            icon = { Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        ) {
                            Text("🗺️ مخطط 2D", fontSize = 12.sp)
                        }
                        SegmentedButton(
                            selected = state.viewMode == CanvasViewMode.VIEW_3D,
                            onClick = { viewModel.setViewMode(CanvasViewMode.VIEW_3D) },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                            icon = { Icon(Icons.Default.ViewInAr, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        ) {
                            Text("🧊 شكل 3D", fontSize = 12.sp)
                        }
                    }

                    // When in 2D mode: Tool Ribbon
                    if (state.viewMode == CanvasViewMode.PLAN_2D) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ToolItem(
                                title = "حرّك/حدد",
                                icon = Icons.Default.TouchApp,
                                isSelected = state.toolMode == ToolMode.SELECT_MOVE,
                                onClick = { viewModel.setToolMode(ToolMode.SELECT_MOVE) }
                            )

                            ToolItem(
                                title = "ارسم حائط",
                                icon = Icons.Default.Edit,
                                isSelected = state.toolMode == ToolMode.DRAW_WALL,
                                onClick = { viewModel.setToolMode(ToolMode.DRAW_WALL) }
                            )

                            ToolItem(
                                title = "قسّم حائط",
                                icon = Icons.Default.ContentCut,
                                isSelected = state.toolMode == ToolMode.SPLIT_WALL,
                                onClick = { viewModel.setToolMode(ToolMode.SPLIT_WALL) }
                            )

                            ToolItem(
                                title = "أبواب وشبابيك",
                                icon = Icons.Default.MeetingRoom,
                                isSelected = false,
                                onClick = { showCatalogSheet = true }
                            )

                            ToolItem(
                                title = "ضيف عفش",
                                icon = Icons.Default.Weekend,
                                isSelected = false,
                                onClick = { showCatalogSheet = true }
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (state.viewMode) {
                CanvasViewMode.PLAN_2D -> {
                    FloorPlanCanvas(
                        walls = state.walls,
                        portals = state.portals,
                        rooms = state.rooms,
                        furniture = state.furniture,
                        columns = state.columns,
                        mepItems = state.mepItems,
                        toolMode = state.toolMode,
                        showMepLayer = state.showMepLayer,
                        selectedWallId = state.selectedWallId,
                        selectedFurnitureId = state.selectedFurnitureId,
                        selectedPortalId = state.selectedPortalId,
                        zoomScale = state.zoomScale,
                        panOffset = state.panOffset,
                        onZoomPanChange = { z, p -> viewModel.updateZoomPan(z, p) },
                        onSelectWall = { viewModel.selectWall(it) },
                        onSelectFurniture = { viewModel.selectFurniture(it) },
                        onSelectPortal = { viewModel.selectPortal(it) },
                        onWallEndpointsChange = { wId, s, e -> viewModel.updateWallEndpoints(wId, s, e) },
                        onFurniturePositionChange = { fId, x, y -> viewModel.updateFurniturePosition(fId, x, y) },
                        onCanvasTap = { pt -> viewModel.handleCanvasTap(pt) },
                        onSplitWallTap = { wId, dist -> viewModel.splitWall(wId, dist) }
                    )

                    // Zoom Controls overlay (+, -, reset)
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalIconButton(
                            onClick = {
                                viewModel.updateZoomPan(
                                    (state.zoomScale * 1.25f).coerceIn(0.4f, 4.0f),
                                    state.panOffset
                                )
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "تكبير")
                        }

                        FilledTonalIconButton(
                            onClick = {
                                viewModel.updateZoomPan(
                                    (state.zoomScale / 1.25f).coerceIn(0.4f, 4.0f),
                                    state.panOffset
                                )
                            }
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "تصغير")
                        }

                        FilledTonalIconButton(
                            onClick = {
                                viewModel.updateZoomPan(1.0f, Offset(60f, 60f))
                            }
                        ) {
                            Icon(Icons.Default.CenterFocusStrong, contentDescription = "إعادة ضبط المنظور")
                        }
                    }

                    // Mode Hint Badge
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                        shadowElevation = 2.dp
                    ) {
                        val hint = when (state.toolMode) {
                            ToolMode.DRAW_WALL -> "✏️ المس نقطتين على الشاشة لرسم حائط جديد"
                            ToolMode.SPLIT_WALL -> "✂️ المس أي حائط بالمسافة المطلوبة لتقسيمه"
                            ToolMode.SELECT_MOVE -> "✋ المس أي حائط أو عفش لتعديل مقاساته ولونه"
                            else -> "مسطرة وتحديد الأبعاد بالمتر"
                        }
                        Text(
                            text = hint,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                CanvasViewMode.VIEW_3D -> {
                    Apartment3DViewer(
                        walls = state.walls,
                        portals = state.portals,
                        rooms = state.rooms,
                        furniture = state.furniture,
                        columns = state.columns
                    )
                }

                CanvasViewMode.SECTION_CUT -> {
                    if (selectedWall != null) {
                        WallSectionDialog(
                            wall = selectedWall,
                            onDismiss = { viewModel.setViewMode(CanvasViewMode.PLAN_2D) }
                        )
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("يرجى اختيار حائط أولاً لعرض مقطعه التفصيلي.")
                        }
                    }
                }
            }

            // Bottom Sheets / Inspectors when elements are selected
            if (selectedWall != null) {
                Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                    WallInspectorSheet(
                        wall = selectedWall,
                        onClose = { viewModel.selectWall(null) },
                        onUpdateWall = { viewModel.updateWall(it) },
                        onResizeWall = { viewModel.resizeWall(selectedWall.id, it) },
                        onSplitWall = { viewModel.splitWall(selectedWall.id, it) },
                        onDeleteWall = { viewModel.deleteWall(selectedWall.id) },
                        onShowSectionView = { showWallSectionDialog = true }
                    )
                }
            }

            if (selectedFurniture != null) {
                Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                    FurnitureInspectorSheet(
                        item = selectedFurniture,
                        onClose = { viewModel.selectFurniture(null) },
                        onUpdateItem = { viewModel.updateFurniture(it) },
                        onDuplicateItem = { viewModel.duplicateFurniture(selectedFurniture.id) },
                        onDeleteItem = { viewModel.deleteFurniture(selectedFurniture.id) }
                    )
                }
            }

            if (selectedPortal != null) {
                Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                    PortalInspectorSheet(
                        portal = selectedPortal,
                        onClose = { viewModel.selectPortal(null) },
                        onUpdatePortal = { viewModel.updatePortal(it) },
                        onDeletePortal = { viewModel.deletePortal(selectedPortal.id) }
                    )
                }
            }
        }
    }

    // Projects Manager Dialog (Room Database)
    if (showProjectsDialog) {
        ProjectsManagerDialog(
            savedProjects = savedProjects,
            onDismiss = { showProjectsDialog = false },
            onSaveCurrent = { pName, cName, notes ->
                viewModel.saveCurrentProject(pName, cName, notes)
                Toast.makeText(context, "تم حفظ المشروع في قاعدة البيانات بنجاح!", Toast.LENGTH_SHORT).show()
            },
            onLoadProject = { p ->
                viewModel.loadSavedProject(p)
                Toast.makeText(context, "تم فتح المشروع: ${p.name}", Toast.LENGTH_SHORT).show()
            },
            onDeleteProject = { pId ->
                viewModel.deleteSavedProject(pId)
                Toast.makeText(context, "تم حذف المشروع.", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Catalog Bottom Sheet
    if (showCatalogSheet) {
        CatalogBottomSheet(
            onDismiss = { showCatalogSheet = false },
            onAddFurniture = { viewModel.addFurniture(it) },
            onAddPortal = { viewModel.addPortal(it) }
        )
    }

    // Budget Dialog
    if (showBudgetDialog && state.quantitiesEstimate != null) {
        BudgetDialog(
            estimate = state.quantitiesEstimate!!,
            furniture = state.furniture,
            onDismiss = { showBudgetDialog = false },
            onExportTable = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("مقايسة شقتي", viewModel.getExportReportText())
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "تم نسخ جدول المقايسة للحافظة!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Share & Export Dialog
    if (showShareDialog) {
        ShareExportDialog(
            planCode = viewModel.getShareableCode(),
            reportText = viewModel.getExportReportText(),
            onDismiss = { showShareDialog = false },
            onCopyCode = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("كود التصميم", viewModel.getShareableCode())
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "تم نسخ كود التصميم! ابعته لحبيبتك", Toast.LENGTH_SHORT).show()
            },
            onCopyReport = {
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, viewModel.getExportReportText())
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, "مشاركة تقرير المقايسة"))
            },
            onLoadCode = {
                viewModel.loadFromCode(it)
                showShareDialog = false
                Toast.makeText(context, "تم فتح التصميم بنجاح!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // A/B Comparison Dialog
    if (showComparisonDialog) {
        ComparisonDialog(
            currentVariant = state.currentVariant,
            onSelectVariant = {
                viewModel.switchVariant(it)
                showComparisonDialog = false
            },
            onDismiss = { showComparisonDialog = false }
        )
    }

    // Wall Section Dialog
    if (showWallSectionDialog && selectedWall != null) {
        WallSectionDialog(
            wall = selectedWall,
            onDismiss = { showWallSectionDialog = false }
        )
    }
}

@Composable
fun ToolItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(2.dp)
    ) {
        FilledTonalIconButton(
            onClick = onClick,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.size(44.dp)
        ) {
            Icon(icon, contentDescription = title, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}
