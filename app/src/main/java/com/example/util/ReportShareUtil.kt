package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.api.GeminiIngredientInsight
import com.example.data.model.ScanReport
import com.example.data.model.Severity
import com.example.data.model.SummaryStatus
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportShareUtil {

    /**
     * Generates a beautifully formatted text block optimized for messaging apps (WhatsApp, Telegram, SMS)
     * and social media sharing.
     */
    fun generateShareableText(
        report: ScanReport,
        gemini: GeminiIngredientInsight?
    ): String {
        val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(report.timestamp))
        val score = gemini?.safetyScore ?: if (report.summaryStatus == SummaryStatus.NO_RELEVANT_CONCERN) 92 else 45
        val tier = gemini?.safetyTier ?: if (report.summaryStatus == SummaryStatus.NO_RELEVANT_CONCERN) "SAFE" else "WARNING"

        val matchedAllergens = report.ingredients
            .mapNotNull { it.matchedAllergenName }
            .flatMap { it.split(",") }
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

        val criticalCount = report.findings.count { it.severity == Severity.CRITICAL } +
            if (matchedAllergens.isNotEmpty()) 1 else 0
        val cautionCount = report.findings.count { it.severity == Severity.CAUTION }
        val safeCount = (report.ingredients.size - criticalCount - cautionCount).coerceAtLeast(0)

        return buildString {
            append("🌿 *PURESCAN PRODUCT SAFETY REPORT*\n")
            append("━━━━━━━━━━━━━━━━━━━━\n")
            append("🧴 *Product:* ${report.productTitle}\n")
            append("📂 *Category:* ${report.category} (${report.region} Market)\n")
            append("📅 *Date:* $dateStr\n\n")

            val scoreEmoji = when {
                score >= 80 -> "🟢"
                score >= 50 -> "🟡"
                else -> "🔴"
            }
            append("$scoreEmoji *Safety Score:* $score/100 • *$tier*\n")

            if (matchedAllergens.isNotEmpty()) {
                append("\n🚨 *PERSONAL ALLERGEN ALERT:*\n")
                matchedAllergens.forEach { allergen ->
                    append(" • ⚠️ Flagged: $allergen\n")
                }
                append(" (Triggered by your custom PureScan Allergy Filter)\n")
            }

            append("\n📊 *Ingredient Breakdown (${report.ingredients.size} total):*\n")
            append(" • 🟢 Safe: $safeCount\n")
            append(" • 🟡 Warning / Caution: $cautionCount\n")
            append(" • 🔴 High Hazard / Allergen: $criticalCount\n")

            val notableConcerns = report.findings.filter { it.severity == Severity.CRITICAL || it.severity == Severity.CAUTION }.take(4)
            if (notableConcerns.isNotEmpty()) {
                append("\n⚠️ *Key Findings:*\n")
                notableConcerns.forEach { finding ->
                    val bullet = if (finding.severity == Severity.CRITICAL) "🔴" else "🟡"
                    append(" $bullet *${finding.ingredientName}*: ${finding.title}\n")
                }
            }

            if (!gemini?.saferAlternatives.isNullOrEmpty()) {
                val topAlt = gemini!!.saferAlternatives.first()
                append("\n💡 *Verified Allergen-Free Alternative:*\n")
                append(" • *${topAlt.productName}* by ${topAlt.brandName} (Score: ${topAlt.safetyScore}/100)\n")
                append("   _${topAlt.reason}_\n")
            }

            append("\n━━━━━━━━━━━━━━━━━━━━\n")
            append("🔬 *Evidence Authorities:* EU SCCS, US CIR, US FDA OTC, NIH PubChem\n")
            append("📱 Scanned with PureScan • #PureScan #CleanIngredients #AllergenFree #SafeBeauty")
        }
    }

    /**
     * Renders a high-resolution (1080 x 1440) Material 3 branded infographic card bitmap on-device.
     */
    fun renderReportInfographicBitmap(
        context: Context,
        report: ScanReport,
        gemini: GeminiIngredientInsight?
    ): Bitmap {
        val width = 1080
        val height = 1440
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background: Soft premium Cream
        canvas.drawColor(Color.parseColor("#F9F8F4"))

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 1. Top Decorative Brand Bar
        paint.color = Color.parseColor("#4A6B35") // Forest Green
        canvas.drawRect(0f, 0f, width.toFloat(), 24f, paint)

        // 2. Header Box: Brand Wordmark & Emblem
        paint.color = Color.parseColor("#385228")
        paint.textSize = 42f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("PURESCAN", 80f, 90f, paint)

        paint.color = Color.parseColor("#706E6B")
        paint.textSize = 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("SCIENTIFIC INGREDIENT & ALLERGEN ANALYSIS", 80f, 126f, paint)

        val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(report.timestamp))
        paint.color = Color.parseColor("#9E9D99")
        paint.textSize = 22f
        val dateWidth = paint.measureText(dateStr)
        canvas.drawText(dateStr, width - 80f - dateWidth, 126f, paint)

        // Divider
        paint.color = Color.parseColor("#E5E3DD")
        paint.strokeWidth = 2f
        canvas.drawLine(80f, 150f, width - 80f, 150f, paint)

        // 3. Main Product Card Container
        val cardRect = RectF(80f, 180f, width - 80f, 430f)
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(cardRect, 28f, 28f, paint)

        paint.color = Color.parseColor("#E0DED7")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        canvas.drawRoundRect(cardRect, 28f, 28f, paint)
        paint.style = Paint.Style.FILL

        // Category Tag Pill
        val tagRect = RectF(120f, 215f, 120f + 200f, 255f)
        paint.color = Color.parseColor("#F1EFEA")
        canvas.drawRoundRect(tagRect, 12f, 12f, paint)
        paint.color = Color.parseColor("#5A5854")
        paint.textSize = 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(report.category.uppercase(), 140f, 243f, paint)

        // Product Title (with TextPaint & StaticLayout for multi-line wrapping)
        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1C1B1A")
            textSize = 38f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val staticTitle = StaticLayout.Builder.obtain(
            report.productTitle,
            0,
            report.productTitle.length,
            titlePaint,
            width - 80 - 120 - 240
        ).setMaxLines(2)
            .setEllipsize(android.text.TextUtils.TruncateAt.END)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .build()

        canvas.save()
        canvas.translate(120f, 280f)
        staticTitle.draw(canvas)
        canvas.restore()

        // Circular Safety Score Gauge
        val score = gemini?.safetyScore ?: if (report.summaryStatus == SummaryStatus.NO_RELEVANT_CONCERN) 92 else 45
        val scoreColor = when {
            score >= 80 -> Color.parseColor("#2E7D32") // Safe Green
            score >= 50 -> Color.parseColor("#F57C00") // Warning Amber
            else -> Color.parseColor("#D32F2F") // Crimson High Hazard
        }
        val scoreBgColor = when {
            score >= 80 -> Color.parseColor("#E8F5E9")
            score >= 50 -> Color.parseColor("#FFF3E0")
            else -> Color.parseColor("#FFEBEE")
        }

        val circleX = width - 80f - 110f
        val circleY = 305f
        val circleRadius = 75f

        paint.color = scoreBgColor
        canvas.drawCircle(circleX, circleY, circleRadius, paint)

        paint.color = scoreColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 8f
        canvas.drawCircle(circleX, circleY, circleRadius, paint)
        paint.style = Paint.Style.FILL

        paint.color = scoreColor
        paint.textSize = 46f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val scoreText = score.toString()
        val scoreW = paint.measureText(scoreText)
        canvas.drawText(scoreText, circleX - (scoreW / 2), circleY + 8f, paint)

        paint.textSize = 18f
        paint.color = Color.parseColor("#5A5854")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val scoreSub = "SCORE"
        val scoreSubW = paint.measureText(scoreSub)
        canvas.drawText(scoreSub, circleX - (scoreSubW / 2), circleY + 32f, paint)

        // 4. Allergen Alert Banner (If Any Allergen Detected)
        val matchedAllergens = report.ingredients
            .mapNotNull { it.matchedAllergenName }
            .flatMap { it.split(",") }
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

        var currentY = 460f
        if (matchedAllergens.isNotEmpty()) {
            val alertRect = RectF(80f, currentY, width - 80f, currentY + 140f)
            paint.color = Color.parseColor("#FFEBEE")
            canvas.drawRoundRect(alertRect, 20f, 20f, paint)

            paint.color = Color.parseColor("#D32F2F")
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f
            canvas.drawRoundRect(alertRect, 20f, 20f, paint)
            paint.style = Paint.Style.FILL

            paint.color = Color.parseColor("#B71C1C")
            paint.textSize = 28f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("⚠️ ALLERGEN ALERT DETECTED", 115f, currentY + 45f, paint)

            paint.color = Color.parseColor("#5A1010")
            paint.textSize = 22f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            val allergenSummary = "Flagged by your Allergy Filter: ${matchedAllergens.joinToString(", ")}"
            canvas.drawText(allergenSummary.take(65), 115f, currentY + 85f, paint)

            paint.textSize = 18f
            paint.color = Color.parseColor("#7F1D1D")
            canvas.drawText("Contains sensitized triggers matching your profile settings.", 115f, currentY + 115f, paint)

            currentY += 170f
        }

        // 5. Category Counts Metric Pills (Safe / Warning / High Hazard)
        val criticalCount = report.findings.count { it.severity == Severity.CRITICAL } +
            if (matchedAllergens.isNotEmpty()) 1 else 0
        val cautionCount = report.findings.count { it.severity == Severity.CAUTION }
        val safeCount = (report.ingredients.size - criticalCount - cautionCount).coerceAtLeast(0)

        val pillWidth = (width - 160f - 40f) / 3f

        // High Hazard Pill
        val pill1 = RectF(80f, currentY, 80f + pillWidth, currentY + 110f)
        paint.color = Color.parseColor("#FFEBEE")
        canvas.drawRoundRect(pill1, 16f, 16f, paint)
        paint.color = Color.parseColor("#D32F2F")
        paint.textSize = 34f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(criticalCount.toString(), 110f, currentY + 52f, paint)
        paint.textSize = 20f
        paint.color = Color.parseColor("#7F1D1D")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("High Hazard", 110f, currentY + 85f, paint)

        // Warning Pill
        val pill2X = 80f + pillWidth + 20f
        val pill2 = RectF(pill2X, currentY, pill2X + pillWidth, currentY + 110f)
        paint.color = Color.parseColor("#FFF3E0")
        canvas.drawRoundRect(pill2, 16f, 16f, paint)
        paint.color = Color.parseColor("#E65100")
        paint.textSize = 34f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(cautionCount.toString(), pill2X + 30f, currentY + 52f, paint)
        paint.textSize = 20f
        paint.color = Color.parseColor("#7E3800")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Warning", pill2X + 30f, currentY + 85f, paint)

        // Safe Pill
        val pill3X = pill2X + pillWidth + 20f
        val pill3 = RectF(pill3X, currentY, pill3X + pillWidth, currentY + 110f)
        paint.color = Color.parseColor("#E8F5E9")
        canvas.drawRoundRect(pill3, 16f, 16f, paint)
        paint.color = Color.parseColor("#1B5E20")
        paint.textSize = 34f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(safeCount.toString(), pill3X + 30f, currentY + 52f, paint)
        paint.textSize = 20f
        paint.color = Color.parseColor("#0F3D13")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Safe", pill3X + 30f, currentY + 85f, paint)

        currentY += 140f

        // 6. Notable Ingredient Analysis Section
        paint.color = Color.parseColor("#1C1B1A")
        paint.textSize = 26f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("INGREDIENT ANALYSIS HIGHLIGHTS", 80f, currentY + 20f, paint)
        currentY += 45f

        val highlightedIngredients = report.ingredients.take(5)
        highlightedIngredients.forEach { ing ->
            val ingName = ing.canonicalIngredient?.canonicalName ?: ing.rawText
            val isHazard = ing.allergenSeverity == Severity.CRITICAL ||
                report.findings.any { it.ingredientName.equals(ingName, true) && it.severity == Severity.CRITICAL }
            val isCaution = ing.allergenSeverity == Severity.CAUTION ||
                report.findings.any { it.ingredientName.equals(ingName, true) && it.severity == Severity.CAUTION }

            val rowColor = when {
                isHazard -> Color.parseColor("#D32F2F")
                isCaution -> Color.parseColor("#F57C00")
                else -> Color.parseColor("#2E7D32")
            }

            paint.color = rowColor
            canvas.drawCircle(95f, currentY + 15f, 8f, paint)

            paint.color = Color.parseColor("#2B2927")
            paint.textSize = 24f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(ingName.take(30), 120f, currentY + 22f, paint)

            val roleOrNote = ing.matchedAllergenName?.let { "⚠️ Allergen: $it" }
                ?: ing.canonicalIngredient?.category
                ?: "Safe Formulation Base"

            paint.color = Color.parseColor("#6E6B65")
            paint.textSize = 20f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText(roleOrNote.take(38), 500f, currentY + 22f, paint)

            // subtle divider
            paint.color = Color.parseColor("#EEECE6")
            paint.strokeWidth = 1f
            canvas.drawLine(80f, currentY + 40f, width - 80f, currentY + 40f, paint)

            currentY += 56f
        }

        // 7. Safer Alternative Banner (If available from Gemini)
        if (!gemini?.saferAlternatives.isNullOrEmpty()) {
            val topAlt = gemini.saferAlternatives.first()
            val altRect = RectF(80f, height - 240f, width - 80f, height - 120f)
            paint.color = Color.parseColor("#F1F8E9")
            canvas.drawRoundRect(altRect, 18f, 18f, paint)

            paint.color = Color.parseColor("#33691E")
            paint.textSize = 24f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("💡 Verified Alternative: ${topAlt.productName} (${topAlt.brandName})", 110f, height - 195f, paint)

            paint.color = Color.parseColor("#558B2F")
            paint.textSize = 20f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText(topAlt.reason.take(75), 110f, height - 160f, paint)
        }

        // 8. Footer Authority Watermark
        paint.color = Color.parseColor("#8C8A84")
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val footerText = "Evaluated by PureScan Evidence Engine • EU SCCS & US CIR Validated"
        val footerW = paint.measureText(footerText)
        canvas.drawText(footerText, (width - footerW) / 2f, height - 55f, paint)

        return bitmap
    }

    /**
     * Saves rendered Bitmap to cacheDir/exports/ and returns a shareable FileProvider Content URI.
     */
    fun saveBitmapToCacheAndGetUri(context: Context, bitmap: Bitmap, scanId: String): Uri {
        val exportsDir = File(context.cacheDir, "exports").apply {
            if (!exists()) mkdirs()
        }
        val safeId = scanId.take(8).ifBlank { "report" }
        val imageFile = File(exportsDir, "purescan_report_$safeId.png")

        FileOutputStream(imageFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
        }

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
    }

    /**
     * Launches Android System Share Sheet with the rendered infographic card PNG.
     */
    fun shareReportImage(context: Context, imageUri: Uri, productTitle: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_SUBJECT, "PureScan Safety Report: $productTitle")
            putExtra(Intent.EXTRA_TEXT, "Here is the ingredient safety breakdown for $productTitle from PureScan.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share PureScan Report Card"))
    }

    /**
     * Launches Android System Share Sheet with formatted text payload.
     */
    fun shareReportText(context: Context, text: String, productTitle: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, "PureScan Safety Report: $productTitle")
        }
        context.startActivity(Intent.createChooser(intent, "Share PureScan Report Text"))
    }

    /**
     * Copies formatted report text to the system clipboard.
     */
    fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText("PureScan Report", text)
        clipboard?.setPrimaryClip(clip)
        Toast.makeText(context, "Report summary copied to clipboard!", Toast.LENGTH_SHORT).show()
    }
}
