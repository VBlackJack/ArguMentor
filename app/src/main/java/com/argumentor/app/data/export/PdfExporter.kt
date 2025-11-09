package com.argumentor.app.data.export

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.argumentor.app.data.model.Claim
import com.argumentor.app.data.model.Rebuttal
import com.argumentor.app.data.model.Topic
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Exports topics and their arguments to PDF format.
 */
class PdfExporter(private val context: Context) {

    companion object {
        private const val PAGE_WIDTH = 595 // A4 width in points
        private const val PAGE_HEIGHT = 842 // A4 height in points
        private const val MARGIN = 50
        private const val LINE_HEIGHT = 20
        private const val TITLE_SIZE = 24f
        private const val HEADING_SIZE = 18f
        private const val BODY_SIZE = 12f
    }

    /**
     * Export a single topic with all its claims and rebuttals to PDF via OutputStream (for SAF).
     * Use this method with Storage Access Framework (CreateDocument).
     */
    fun exportTopicToPdf(
        topic: Topic,
        claims: List<Claim>,
        rebuttals: Map<String, List<Rebuttal>>,
        outputStream: OutputStream
    ): Result<Unit> {
        return try {
            val document = PdfDocument()
            var pageNumber = 1
            var yPosition = MARGIN

            // Create first page
            var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            var page = document.startPage(pageInfo)
            var canvas = page.canvas

            // Title Paint
            val titlePaint = Paint().apply {
                textSize = TITLE_SIZE
                isFakeBoldText = true
            }

            // Heading Paint
            val headingPaint = Paint().apply {
                textSize = HEADING_SIZE
                isFakeBoldText = true
            }

            // Body Paint
            val bodyPaint = Paint().apply {
                textSize = BODY_SIZE
            }

            // Draw topic title
            canvas.drawText(topic.title, MARGIN.toFloat(), yPosition.toFloat(), titlePaint)
            yPosition += LINE_HEIGHT * 2

            // Draw topic summary
            val summaryLines = wrapText(topic.summary, PAGE_WIDTH - (MARGIN * 2), bodyPaint)
            summaryLines.forEach { line ->
                if (yPosition > PAGE_HEIGHT - MARGIN) {
                    document.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    yPosition = MARGIN
                }
                canvas.drawText(line, MARGIN.toFloat(), yPosition.toFloat(), bodyPaint)
                yPosition += LINE_HEIGHT
            }

            yPosition += LINE_HEIGHT

            // Draw posture
            canvas.drawText("Posture: ${topic.posture.name}", MARGIN.toFloat(), yPosition.toFloat(), bodyPaint)
            yPosition += LINE_HEIGHT * 2

            // Draw tags if any
            if (topic.tags.isNotEmpty()) {
                canvas.drawText("Tags: ${topic.tags.joinToString(", ")}", MARGIN.toFloat(), yPosition.toFloat(), bodyPaint)
                yPosition += LINE_HEIGHT * 2
            }

            // Draw claims
            canvas.drawText("Arguments", MARGIN.toFloat(), yPosition.toFloat(), headingPaint)
            yPosition += LINE_HEIGHT * 2

            claims.forEach { claim ->
                // Check if we need a new page
                if (yPosition > PAGE_HEIGHT - MARGIN * 3) {
                    document.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    yPosition = MARGIN
                }

                // Draw claim
                val claimHeader = "• [${claim.stance.name}] ${claim.text.take(40)}..."
                canvas.drawText(claimHeader, MARGIN.toFloat(), yPosition.toFloat(), bodyPaint)
                yPosition += LINE_HEIGHT

                val claimContentLines = wrapText(claim.text, PAGE_WIDTH - (MARGIN * 3), bodyPaint)
                claimContentLines.forEach { line ->
                    if (yPosition > PAGE_HEIGHT - MARGIN) {
                        document.finishPage(page)
                        pageNumber++
                        pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                        page = document.startPage(pageInfo)
                        canvas = page.canvas
                        yPosition = MARGIN
                    }
                    canvas.drawText(line, (MARGIN + 20).toFloat(), yPosition.toFloat(), bodyPaint)
                    yPosition += LINE_HEIGHT
                }

                // Draw rebuttals for this claim
                rebuttals[claim.id]?.forEach { rebuttal ->
                    if (yPosition > PAGE_HEIGHT - MARGIN) {
                        document.finishPage(page)
                        pageNumber++
                        pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                        page = document.startPage(pageInfo)
                        canvas = page.canvas
                        yPosition = MARGIN
                    }

                    canvas.drawText("  ↳ ${rebuttal.text.take(30)}...", (MARGIN + 40).toFloat(), yPosition.toFloat(), bodyPaint)
                    yPosition += LINE_HEIGHT

                    val rebuttalLines = wrapText(rebuttal.text, PAGE_WIDTH - (MARGIN * 4), bodyPaint)
                    rebuttalLines.forEach { line ->
                        if (yPosition > PAGE_HEIGHT - MARGIN) {
                            document.finishPage(page)
                            pageNumber++
                            pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                            page = document.startPage(pageInfo)
                            canvas = page.canvas
                            yPosition = MARGIN
                        }
                        canvas.drawText(line, (MARGIN + 60).toFloat(), yPosition.toFloat(), bodyPaint)
                        yPosition += LINE_HEIGHT
                    }
                }

                yPosition += LINE_HEIGHT
            }

            // Finish last page
            document.finishPage(page)

            // Write to OutputStream (SAF-compatible)
            document.writeTo(outputStream)
            document.close()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            try {
                outputStream.close()
            } catch (e: Exception) {
                // Ignore close errors
            }
        }
    }

    /**
     * Wrap text to fit within a specified width
     */
    private fun wrapText(text: String, maxWidth: Int, paint: Paint): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""

        words.forEach { word ->
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val width = paint.measureText(testLine)

            if (width > maxWidth && currentLine.isNotEmpty()) {
                lines.add(currentLine)
                currentLine = word
            } else {
                currentLine = testLine
            }
        }

        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }

        return lines
    }
}
