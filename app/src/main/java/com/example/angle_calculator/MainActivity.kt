package com.example.angle_calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.angle_calculator.ui.theme.AngleCalculatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AngleCalculatorTheme {
                MainApp()
            }
        }
    }
}

enum class Screen {
    HOME, CALCULATOR, SETTINGS
}

@Composable
fun MainApp() {
    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    
    when (currentScreen) {
        Screen.HOME -> HomeScreen(
            onNavigateToCalculator = { currentScreen = Screen.CALCULATOR },
            onNavigateToSettings = { currentScreen = Screen.SETTINGS }
        )
        Screen.CALCULATOR -> CalculatorScreen(
            onBack = { currentScreen = Screen.HOME }
        )
        Screen.SETTINGS -> SettingsScreen(
            onBack = { currentScreen = Screen.HOME }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCalculator: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("三角形計算機") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "三角形計算機",
                style = MaterialTheme.typography.headlineLarge
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = onNavigateToCalculator,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                Icon(Icons.Default.Calculate, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("計算機", style = MaterialTheme.typography.titleMedium)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = onNavigateToSettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                Icon(Icons.Default.Settings, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("設定", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var angleDecimals by remember { mutableStateOf(SettingsManager.getAngleDecimals(context)) }
    var sideDecimals by remember { mutableStateOf(SettingsManager.getSideDecimals(context)) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("設定") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text("表示精度", style = MaterialTheme.typography.titleMedium)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Angle decimals
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("角度の小数点以下桁数: $angleDecimals")
                    Slider(
                        value = angleDecimals.toFloat(),
                        onValueChange = { 
                            angleDecimals = it.toInt()
                            SettingsManager.setAngleDecimals(context, angleDecimals)
                        },
                        valueRange = 0f..5f,
                        steps = 4
                    )
                    Text(
                        "例: ${String.format("%.${angleDecimals}f", 45.12345)}°",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Side decimals
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("辺の長さの小数点以下桁数: $sideDecimals")
                    Slider(
                        value = sideDecimals.toFloat(),
                        onValueChange = { 
                            sideDecimals = it.toInt()
                            SettingsManager.setSideDecimals(context, sideDecimals)
                        },
                        valueRange = 0f..5f,
                        steps = 4
                    )
                    Text(
                        "例: ${String.format("%.${sideDecimals}f", 123.45678)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val angleDecimals = remember { SettingsManager.getAngleDecimals(context) }
    val sideDecimals = remember { SettingsManager.getSideDecimals(context) }
    
    // State for triangle values (Angle A is fixed at 90)
    var sideA by remember { mutableStateOf("") }  // Hypotenuse
    var sideB by remember { mutableStateOf("") }  // Base (horizontal)
    var sideC by remember { mutableStateOf("") }  // Height (vertical)
    var angleB by remember { mutableStateOf("") }  // Angle at bottom-right
    var angleC by remember { mutableStateOf("") }  // Angle at top-left

    var resultMessage by remember { mutableStateOf("値を入力して計算ボタンを押してください") }
    var isError by remember { mutableStateOf(false) }
    
    // Save dialog state
    var showSaveDialog by remember { mutableStateOf(false) }
    var saveName by remember { mutableStateOf("") }
    
    // History dialog state
    var showHistoryDialog by remember { mutableStateOf(false) }
    var savedTriangles by remember { mutableStateOf(MemoryManager.loadAllTriangles(context)) }
    
    // Save Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("保存名を入力") },
            text = {
                OutlinedTextField(
                    value = saveName,
                    onValueChange = { saveName = it },
                    singleLine = true,
                    placeholder = { Text("例: 3-4-5三角形") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (saveName.isNotBlank()) {
                        val triangle = SavedTriangle(
                            id = System.currentTimeMillis(),
                            name = saveName,
                            sideA = sideA.toDoubleOrNull(),
                            sideB = sideB.toDoubleOrNull(),
                            sideC = sideC.toDoubleOrNull(),
                            angleA = 90.0,
                            angleB = angleB.toDoubleOrNull(),
                            angleC = angleC.toDoubleOrNull()
                        )
                        MemoryManager.saveTriangle(context, triangle)
                        savedTriangles = MemoryManager.loadAllTriangles(context)
                        resultMessage = "保存しました: $saveName"
                        isError = false
                        saveName = ""
                    }
                    showSaveDialog = false
                }) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) { Text("キャンセル") }
            }
        )
    }
    
    // History Dialog
    if (showHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showHistoryDialog = false },
            title = { Text("保存履歴") },
            text = {
                if (savedTriangles.isEmpty()) {
                    Text("保存データがありません")
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(savedTriangles) { triangle ->
                            Card(
                                onClick = {
                                    sideA = triangle.sideA?.toString() ?: ""
                                    sideB = triangle.sideB?.toString() ?: ""
                                    sideC = triangle.sideC?.toString() ?: ""
                                    angleB = triangle.angleB?.toString() ?: ""
                                    angleC = triangle.angleC?.toString() ?: ""
                                    resultMessage = "読込: ${triangle.name}"
                                    isError = false
                                    showHistoryDialog = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(triangle.name)
                                    TextButton(onClick = {
                                        MemoryManager.deleteTriangle(context, triangle.id)
                                        savedTriangles = MemoryManager.loadAllTriangles(context)
                                    }) {
                                        Text("削除", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHistoryDialog = false }) { Text("閉じる") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("計算機") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Visual Triangle Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(8.dp)
            ) {
                TriangleDiagram(
                    sideA = sideA.toDoubleOrNull(),
                    sideB = sideB.toDoubleOrNull(),
                    sideC = sideC.toDoubleOrNull(),
                    angleB = angleB.toDoubleOrNull(),
                    angleC = angleC.toDoubleOrNull(),
                    sideDecimals = sideDecimals,
                    angleDecimals = angleDecimals
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            // Direct Input Fields
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("辺の長さ", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = sideB,
                            onValueChange = { sideB = it },
                            label = { Text("辺 b (対辺)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = sideC,
                            onValueChange = { sideC = it },
                            label = { Text("辺 c (隣辺)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = sideA,
                        onValueChange = { sideA = it },
                        label = { Text("辺 a (斜辺)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("角度 (角A = 90° 固定)", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = angleB,
                            onValueChange = { angleB = it },
                            label = { Text("角 B / θ (左下)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = angleC,
                            onValueChange = { angleC = it },
                            label = { Text("角 C (右上)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons Row 1
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = {
                    try {
                        val input = TriangleSolver.Triangle(
                            sideA = sideA.toDoubleOrNull(),
                            sideB = sideB.toDoubleOrNull(),
                            sideC = sideC.toDoubleOrNull(),
                            angleA = 90.0,
                            angleB = angleB.toDoubleOrNull(),
                            angleC = angleC.toDoubleOrNull()
                        )
                        val result = TriangleSolver.solve(input)
                        
                        sideA = String.format("%.${sideDecimals}f", result.sideA)
                        sideB = String.format("%.${sideDecimals}f", result.sideB)
                        sideC = String.format("%.${sideDecimals}f", result.sideC)
                        angleB = String.format("%.${angleDecimals}f", result.angleB)
                        angleC = String.format("%.${angleDecimals}f", result.angleC)
                        
                        resultMessage = "計算完了"
                        isError = false
                        
                    } catch (e: Exception) {
                        resultMessage = e.message ?: "エラー"
                        isError = true
                    }
                }) {
                    Text("計算")
                }
                
                OutlinedButton(onClick = {
                    sideA = ""; sideB = ""; sideC = ""
                    angleB = ""; angleC = ""
                    resultMessage = "値を入力して計算ボタンを押してください"
                    isError = false
                }) {
                    Text("クリア")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Action Buttons Row 2 (Memory)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { showSaveDialog = true }) {
                    Text("保存")
                }
                OutlinedButton(onClick = {
                    savedTriangles = MemoryManager.loadAllTriangles(context)
                    showHistoryDialog = true
                }) {
                    Text("履歴")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = resultMessage,
                    modifier = Modifier.padding(16.dp),
                    color = if (isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun TriangleDiagram(
    sideA: Double?, sideB: Double?, sideC: Double?,
    angleB: Double?, angleC: Double?,
    sideDecimals: Int = 2,
    angleDecimals: Int = 2
) {
    val textMeasurer = rememberTextMeasurer()
    val primaryColor = MaterialTheme.colorScheme.primary
    val labelColor = MaterialTheme.colorScheme.onSurface
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        
        // Textbook-style right triangle (horizontally flipped)
        // A = bottom-right (right angle 90°), B = bottom-left (θ), C = top-right
        val pA = Offset(w * 0.85f, h * 0.85f)  // Right angle corner (bottom-right)
        val pB = Offset(w * 0.15f, h * 0.85f)  // θ angle (bottom-left)
        val pC = Offset(w * 0.85f, h * 0.15f)  // Top-right

        // Draw Triangle
        val path = Path().apply {
            moveTo(pA.x, pA.y)
            lineTo(pB.x, pB.y)
            lineTo(pC.x, pC.y)
            close()
        }
        
        drawPath(
            path = path,
            color = primaryColor,
            style = Stroke(width = 4f)
        )
        
        // Draw right-angle marker at pA (bottom-right)
        val markerSize = 20f
        val rightAnglePath = Path().apply {
            moveTo(pA.x - markerSize, pA.y)
            lineTo(pA.x - markerSize, pA.y - markerSize)
            lineTo(pA.x, pA.y - markerSize)
        }
        drawPath(
            path = rightAnglePath,
            color = primaryColor,
            style = Stroke(width = 2f)
        )

        // Helper to format values
        fun fmtSide(v: Double?, label: String) = if (v != null) "%.${sideDecimals}f".format(v) else label
        fun fmtAngle(v: Double?, label: String) = if (v != null) "%.${angleDecimals}f".format(v) else label

        // Draw Text Labels
        // Angle A (90° fixed) - bottom-right
        val measureA = textMeasurer.measure("90°", style = TextStyle(fontSize = 14.sp, color = labelColor))
        drawText(measureA, topLeft = Offset(pA.x - 60f, pA.y - 50f))

        // Angle B (θ) - bottom-left
        val measureB = textMeasurer.measure(fmtAngle(angleB, "θ"), style = TextStyle(fontSize = 14.sp, color = labelColor))
        drawText(measureB, topLeft = Offset(pB.x + 20f, pB.y - 30f))

        // Angle C - top-right
        val measureC = textMeasurer.measure(fmtAngle(angleC, "C"), style = TextStyle(fontSize = 14.sp, color = labelColor))
        drawText(measureC, topLeft = Offset(pC.x - 50f, pC.y + 30f))

        // Side a (Hypotenuse - diagonal from B to C)
        val measureSa = textMeasurer.measure(fmtSide(sideA, "a"), style = TextStyle(fontSize = 14.sp, color = labelColor))
        drawText(measureSa, topLeft = Offset((pB.x + pC.x)/2 - 30f, (pB.y + pC.y)/2 - 10f))

        // Side b (対辺 - vertical from A to C, opposite to θ)
        val measureSb = textMeasurer.measure(fmtSide(sideB, "b"), style = TextStyle(fontSize = 14.sp, color = labelColor))
        drawText(measureSb, topLeft = Offset(pA.x + 10f, (pA.y + pC.y)/2))

        // Side c (隣辺 - horizontal from B to A, adjacent to θ)
        val measureSc = textMeasurer.measure(fmtSide(sideC, "c"), style = TextStyle(fontSize = 14.sp, color = labelColor))
        drawText(measureSc, topLeft = Offset((pA.x + pB.x)/2 - measureSc.size.width/2, pA.y + 10f))
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    AngleCalculatorTheme {
        HomeScreen(onNavigateToCalculator = {}, onNavigateToSettings = {})
    }
}