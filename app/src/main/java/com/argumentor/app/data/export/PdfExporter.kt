package com.argumentor.app.data.export

import android.content.Context
import android.graphics.Paint
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
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
class PdfExporter @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        /** A4 page width in points (595pt = 210mm) */
        private const val PAGE_WIDTH = 595

        /** A4 page height in points (842pt = 297mm) */
        private const val PAGE_HEIGHT = 842

        /** Page margin in points (50pt ≈ 17.6mm) */
        private const val MARGIN = 50

        /** Line height for text in points */
        private const val LINE_HEIGHT = 20

        /** Font size for document title in points */
        private const val TITLE_SIZE = 24f

        /** Font size for section headings in points */
        private const val HEADING_SIZE = 18f

        /** Font size for body text in points */
        private const val BODY_SIZE = 12f

        /** Indentation for claim content (level 1) */
        private const val CLAIM_INDENT = 20

        /** Indentation for rebuttal header (level 2) */
        private const val REBUTTAL_HEADER_INDENT = 40

        /** Indentation for rebuttal content (level 3) */
        private const val REBUTTAL_CONTENT_INDENT = 60
    }

    /**
     * Internal data class to track PDF rendering state across pages.
     */
    private data class PdfRenderContext(
        val document: PdfDocument,
        var page: PdfDocument.Page,
        var canvas: android.graphics.Canvas,
        var yPosition: Int,
        var pageNumber: Int,
        val titlePaint: Paint,
        val headingPaint: Paint,
        val bodyPaint: Paint
    )

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
            val context = createRenderContext(document)

            // Render topic header (title, summary, posture, tags)
            renderTopicHeader(context, topic)

            // Render arguments section
            renderArgumentsSection(context, claims, rebuttals)

            // BUG-008: Finish last page and write to output
            // Note: We don't close the stream here as it's passed by the caller
            // who is responsible for its lifecycle management
            document.finishPage(context.page)
            document.writeTo(outputStream)
            document.close()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Creates initial PDF render context with first page and paint objects.
     */
    private fun createRenderContext(document: PdfDocument): PdfRenderContext {
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)

        val titlePaint = Paint().apply {
            textSize = TITLE_SIZE
            isFakeBoldText = true
        }

        val headingPaint = Paint().apply {
            textSize = HEADING_SIZE
            isFakeBoldText = true
        }

        val bodyPaint = Paint().apply {
            textSize = BODY_SIZE
        }

        return PdfRenderContext(
            document = document,
            page = page,
            canvas = page.canvas,
            yPosition = MARGIN,
            pageNumber = 1,
            titlePaint = titlePaint,
            headingPaint = headingPaint,
            bodyPaint = bodyPaint
        )
    }

    /**
     * Renders the topic header including title, summary, posture, and tags.
     */
    private fun renderTopicHeader(context: PdfRenderContext, topic: Topic) {
        // Draw topic title
        context.canvas.drawText(topic.title, MARGIN.toFloat(), context.yPosition.toFloat(), context.titlePaint)
        context.yPosition += LINE_HEIGHT * 2

        // Draw topic summary
        val summaryLines = wrapText(topic.summary, PAGE_WIDTH - (MARGIN * 2), context.bodyPaint)
        summaryLines.forEach { line ->
            checkAndCreateNewPage(context)
            context.canvas.drawText(line, MARGIN.toFloat(), context.yPosition.toFloat(), context.bodyPaint)
            context.yPosition += LINE_HEIGHT
        }
        context.yPosition += LINE_HEIGHT

        // Draw posture
        context.canvas.drawText("Posture: ${topic.posture.name}", MARGIN.toFloat(), context.yPosition.toFloat(), context.bodyPaint)
        context.yPosition += LINE_HEIGHT * 2

        // Draw tags if any
        if (topic.tags.isNotEmpty()) {
            context.canvas.drawText("Tags: ${topic.tags.joinToString(", ")}", MARGIN.toFloat(), context.yPosition.toFloat(), context.bodyPaint)
            context.yPosition += LINE_HEIGHT * 2
        }
    }

    /**
     * Renders the arguments section with all claims and their rebuttals.
     */
    private fun renderArgumentsSection(
        context: PdfRenderContext,
        claims: List<Claim>,
        rebuttals: Map<String, List<Rebuttal>>
    ) {
        context.canvas.drawText("Arguments", MARGIN.toFloat(), context.yPosition.toFloat(), context.headingPaint)
        context.yPosition += LINE_HEIGHT * 2

        claims.forEach { claim ->
            renderClaim(context, claim)
            rebuttals[claim.id]?.forEach { rebuttal ->
                renderRebuttal(context, rebuttal)
            }
            context.yPosition += LINE_HEIGHT
        }
    }

    /**
     * Renders a single claim with its header and content.
     */
    private fun renderClaim(context: PdfRenderContext, claim: Claim) {
        // Check if we need a new page for the claim
        if (context.yPosition > PAGE_HEIGHT - MARGIN * 3) {
            createNewPage(context)
        }

        // Draw claim header
        val claimHeader = "• [${claim.stance.name}] ${claim.text.take(40)}..."
        context.canvas.drawText(claimHeader, MARGIN.toFloat(), context.yPosition.toFloat(), context.bodyPaint)
        context.yPosition += LINE_HEIGHT

        // Draw claim content with indentation
        val claimContentLines = wrapText(claim.text, PAGE_WIDTH - (MARGIN * 2 + CLAIM_INDENT), context.bodyPaint)
        claimContentLines.forEach { line ->
            checkAndCreateNewPage(context)
            context.canvas.drawText(line, (MARGIN + CLAIM_INDENT).toFloat(), context.yPosition.toFloat(), context.bodyPaint)
            context.yPosition += LINE_HEIGHT
        }
    }

    /**
     * Renders a single rebuttal with its header and content.
     */
    private fun renderRebuttal(context: PdfRenderContext, rebuttal: Rebuttal) {
        checkAndCreateNewPage(context)

        // Draw rebuttal header
        context.canvas.drawText(
            "  ↳ ${rebuttal.text.take(30)}...",
            (MARGIN + REBUTTAL_HEADER_INDENT).toFloat(),
            context.yPosition.toFloat(),
            context.bodyPaint
        )
        context.yPosition += LINE_HEIGHT

        // Draw rebuttal content with deeper indentation
        val rebuttalLines = wrapText(rebuttal.text, PAGE_WIDTH - (MARGIN * 2 + REBUTTAL_CONTENT_INDENT), context.bodyPaint)
        rebuttalLines.forEach { line ->
            checkAndCreateNewPage(context)
            context.canvas.drawText(
                line,
                (MARGIN + REBUTTAL_CONTENT_INDENT).toFloat(),
                context.yPosition.toFloat(),
                context.bodyPaint
            )
            context.yPosition += LINE_HEIGHT
        }
    }

    /**
     * Checks if a new page is needed and creates one if necessary.
     */
    private fun checkAndCreateNewPage(context: PdfRenderContext) {
        if (context.yPosition > PAGE_HEIGHT - MARGIN) {
            createNewPage(context)
        }
    }

    /**
     * Creates a new page and updates the context.
     */
    private fun createNewPage(context: PdfRenderContext) {
        context.document.finishPage(context.page)
        context.pageNumber++
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, context.pageNumber).create()
        context.page = context.document.startPage(pageInfo)
        context.canvas = context.page.canvas
        context.yPosition = MARGIN
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
