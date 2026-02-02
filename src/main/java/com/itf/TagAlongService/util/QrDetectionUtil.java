package com.itf.TagAlongService.util;

import com.google.zxing.*;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.multi.GenericMultipleBarcodeReader;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.cos.COSName;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.*;
import java.util.List;

public class QrDetectionUtil {

    private static final int DEFAULT_DPI = 400;
    private static final int HIGH_DPI = 600;
    private static final float DEFAULT_MARGIN_PTS = 150f;
    private static final float LARGE_MARGIN_PTS = 250f;
    private static final int VARIANT_PADDING_PX = 80;
    private static final double VARIANT_SCALE = 2.0;

    /**
     * Detects all QR codes in a PDF file, including those outside the page box.
     * Returns page index (0-based), QR text, and normalized coordinates (0..1).
     */
    public static List<QrCodeData> detectQrCodesInPdf(InputStream pdfStream) throws Exception {
        List<QrCodeData> results = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        int counter = 1;

        try (PDDocument doc = PDDocument.load(pdfStream)) {
            PDFRenderer renderer = new PDFRenderer(doc);
            int pageCount = doc.getNumberOfPages();

            for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
                PDPage page = doc.getPage(pageIndex);
                float pageWidthPts = page.getMediaBox().getWidth();
                float pageHeightPts = page.getMediaBox().getHeight();
                int beforeCount = results.size();

                // Pass 1: default dpi + default margin
                BufferedImage expandedImage = renderPageWithMargin(renderer, page, pageIndex, DEFAULT_DPI, DEFAULT_MARGIN_PTS);
                Result[] expandedResults = decodeQrFromImage(expandedImage);
                for (Result result : expandedResults) {
                    QrCodeData data = mapResultToPageCoordinates(result, pageIndex, pageWidthPts, pageHeightPts, DEFAULT_DPI, DEFAULT_MARGIN_PTS, counter++);
                    addIfNotDuplicate(results, seen, data);
                }

                // Pass 2: only if nothing found on this page yet
                if (results.size() == beforeCount) {
                    BufferedImage highDpiImage = renderPageWithMargin(renderer, page, pageIndex, HIGH_DPI, LARGE_MARGIN_PTS);
                    Result[] highDpiResults = decodeQrFromImage(highDpiImage);
                    for (Result result : highDpiResults) {
                        QrCodeData data = mapResultToPageCoordinates(result, pageIndex, pageWidthPts, pageHeightPts, HIGH_DPI, LARGE_MARGIN_PTS, counter++);
                        addIfNotDuplicate(results, seen, data);
                    }
                }

                // Pass 3: only if still nothing found, try embedded images
                if (results.size() == beforeCount) {
                    List<BufferedImage> extractedImages = extractImagesFromPageResources(page);
                    for (BufferedImage img : extractedImages) {
                        Result[] imgResults = decodeQrFromImage(img);
                        for (Result result : imgResults) {
                            QrCodeData data = mapResultToPageCoordinates(result, pageIndex, pageWidthPts, pageHeightPts, DEFAULT_DPI, 0, counter++);
                            addIfNotDuplicate(results, seen, data);
                        }
                    }
                }
            }
        }

        return results;
    }

    /**
     * Renders a page after temporarily expanding its MediaBox and CropBox.
     * This allows detection of content drawn outside the original page boundaries.
     */
    private static BufferedImage renderPageWithMargin(PDFRenderer renderer, PDPage page, int pageIndex, int dpi, float marginPts) throws Exception {
        PDRectangle origMedia = page.getMediaBox();
        PDRectangle origCrop = page.getCropBox();

        PDRectangle expanded = new PDRectangle(
                origMedia.getLowerLeftX() - marginPts,
                origMedia.getLowerLeftY() - marginPts,
                origMedia.getWidth() + (2 * marginPts),
                origMedia.getHeight() + (2 * marginPts)
        );

        page.setMediaBox(expanded);
        page.setCropBox(expanded);

        BufferedImage image;
        try {
            image = renderer.renderImageWithDPI(pageIndex, dpi, ImageType.RGB);
        } finally {
            page.setMediaBox(origMedia);
            page.setCropBox(origCrop);
        }
        return image;
    }

    /**
     * Attempts to decode QR codes from a BufferedImage using multiple strategies.
     */
    private static Result[] decodeQrFromImage(BufferedImage img) {
        try {
            List<BufferedImage> variants = buildImageVariants(img);

            for (BufferedImage variant : variants) {
                // Strategy 1: HybridBinarizer
                Result[] r = tryDecode(variant, true, false);
                if (r.length > 0) {
                    return r;
                }

                // Strategy 2: GlobalHistogramBinarizer
                r = tryDecode(variant, false, false);
                if (r.length > 0) {
                    return r;
                }

                // Strategy 3: Inverted + HybridBinarizer
                r = tryDecode(variant, true, true);
                if (r.length > 0) {
                    return r;
                }
            }

            return new Result[0];
        } catch (Exception ex) {
            return new Result[0];
        }
    }

    private static Result[] tryDecode(BufferedImage variant, boolean hybrid, boolean invertFirst) {
        try {
            BufferedImage working = invertFirst ? invert(variant) : variant;
            var src = new BufferedImageLuminanceSource(working);
            BinaryBitmap bitmap = hybrid
                    ? new BinaryBitmap(new HybridBinarizer(src))
                    : new BinaryBitmap(new GlobalHistogramBinarizer(src));
            return decodeMultipleWithFallback(bitmap);
        } catch (Exception ex) {
            return new Result[0];
        }
    }

    /**
     * Attempts multi-decode, falls back to single decode if needed.
     */
    private static Result[] decodeMultipleWithFallback(BinaryBitmap bitmap) throws Exception {
        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, Collections.singletonList(BarcodeFormat.QR_CODE));
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

        var reader = new MultiFormatReader();
        reader.setHints(hints);

        try {
            return new GenericMultipleBarcodeReader(reader).decodeMultiple(bitmap, hints);
        } catch (NotFoundException nf) {
            try {
                Result r = reader.decode(bitmap);
                return r == null ? new Result[0] : new Result[]{r};
            } catch (NotFoundException ex) {
                throw new Exception("No QR code found");
            }
        }
    }

    /**
     * Inverts the colors of a BufferedImage.
     */
    private static BufferedImage invert(BufferedImage src) {
        BufferedImage inv = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = inv.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        for (int y = 0; y < inv.getHeight(); y++) {
            for (int x = 0; x < inv.getWidth(); x++) {
                int rgba = inv.getRGB(x, y);
                int a = (rgba >> 24) & 0xff;
                int r = 255 - ((rgba >> 16) & 0xff);
                int gC = 255 - ((rgba >> 8) & 0xff);
                int b = 255 - (rgba & 0xff);
                inv.setRGB(x, y, (a << 24) | (r << 16) | (gC << 8) | b);
            }
        }
        return inv;
    }

    private static List<BufferedImage> buildImageVariants(BufferedImage img) {
        List<BufferedImage> variants = new ArrayList<>();
        variants.add(img);

        BufferedImage padded = addPadding(img, VARIANT_PADDING_PX);
        variants.add(padded);

        BufferedImage scaled2x = scaleImage(img, VARIANT_SCALE);
        variants.add(scaled2x);

        BufferedImage paddedScaled2x = addPadding(scaled2x, VARIANT_PADDING_PX);
        variants.add(paddedScaled2x);

        return variants;
    }

    private static BufferedImage addPadding(BufferedImage src, int pad) {
        int w = src.getWidth() + pad * 2;
        int h = src.getHeight() + pad * 2;
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);
        g.drawImage(src, pad, pad, null);
        g.dispose();
        return out;
    }

    private static BufferedImage scaleImage(BufferedImage src, double scale) {
        int w = Math.max(1, (int) Math.round(src.getWidth() * scale));
        int h = Math.max(1, (int) Math.round(src.getHeight() * scale));
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(src, 0, 0, w, h, null);
        g.dispose();
        return out;
    }

    /**
     * Recursively extracts images from page resources (including form XObjects).
     */
    private static List<BufferedImage> extractImagesFromPageResources(PDPage page) {
        List<BufferedImage> images = new ArrayList<>();
        PDResources resources = page.getResources();
        if (resources != null) {
            extractImagesFromResources(resources, images);
        }
        return images;
    }

    private static void extractImagesFromResources(PDResources resources, List<BufferedImage> out) {
        if (resources == null) return;
        for (COSName name : resources.getXObjectNames()) {
            try {
                var xobj = resources.getXObject(name);
                if (xobj instanceof PDImageXObject) {
                    PDImageXObject img = (PDImageXObject) xobj;
                    out.add(img.getImage());
                } else if (xobj instanceof PDFormXObject) {
                    PDFormXObject form = (PDFormXObject) xobj;
                    extractImagesFromResources(form.getResources(), out);
                }
            } catch (Exception ex) {
                // Skip problematic XObjects
            }
        }
    }

    /**
     * Maps a QR code result to page coordinates (normalized 0..1).
     */
    private static QrCodeData mapResultToPageCoordinates(Result result, int pageIndex, float pageWidthPts, float pageHeightPts, int dpi, float marginPts, int counter) {
        ResultPoint[] pts = result.getResultPoints();

        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, maxX = Float.MIN_VALUE, maxY = Float.MIN_VALUE;
        if (pts != null) {
            for (ResultPoint rp : pts) {
                if (rp.getX() < minX) minX = rp.getX();
                if (rp.getX() > maxX) maxX = rp.getX();
                if (rp.getY() < minY) minY = rp.getY();
                if (rp.getY() > maxY) maxY = rp.getY();
            }
        }

        double pxToPt = 72.0 / dpi;
        double minXPt = minX * pxToPt - marginPts;
        double maxXPt = maxX * pxToPt - marginPts;
        double minYPt = minY * pxToPt - marginPts;
        double maxYPt = maxY * pxToPt - marginPts;

        double leftNorm = clamp01(round(minXPt / pageWidthPts));
        double rightNorm = clamp01(round(maxXPt / pageWidthPts));
        double topNorm = clamp01(round(1.0 - (maxYPt / pageHeightPts)));
        double bottomNorm = clamp01(round(1.0 - (minYPt / pageHeightPts)));

        QrCodeData data = new QrCodeData();
        data.setFieldId("Barcode_" + counter);
        data.setFieldName("QR code " + counter);
        data.setFieldValue(result.getText());
        data.setFieldType(result.getBarcodeFormat().toString());
        data.setPageNumber(pageIndex + 1);
        data.setTopX(leftNorm);
        data.setTopY(topNorm);
        data.setBottomX(rightNorm);
        data.setBottomY(bottomNorm);
        data.setOcrConfidence(1.0);

        return data;
    }

    private static double round(double v) {
        return Math.round(v * 1_000_000d) / 1_000_000d;
    }

    private static double clamp01(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }

    /**
     * Data class for QR code detection results.
     */
    public static class QrCodeData {
        private String fieldId;
        private String fieldName;
        private String fieldValue;
        private String fieldType;
        private int pageNumber;
        private double topX;
        private double topY;
        private double bottomX;
        private double bottomY;
        private double ocrConfidence;

        // Getters and Setters
        public String getFieldId() { return fieldId; }
        public void setFieldId(String fieldId) { this.fieldId = fieldId; }

        public String getFieldName() { return fieldName; }
        public void setFieldName(String fieldName) { this.fieldName = fieldName; }

        public String getFieldValue() { return fieldValue; }
        public void setFieldValue(String fieldValue) { this.fieldValue = fieldValue; }

        public String getFieldType() { return fieldType; }
        public void setFieldType(String fieldType) { this.fieldType = fieldType; }

        public int getPageNumber() { return pageNumber; }
        public void setPageNumber(int pageNumber) { this.pageNumber = pageNumber; }

        public double getTopX() { return topX; }
        public void setTopX(double topX) { this.topX = topX; }

        public double getTopY() { return topY; }
        public void setTopY(double topY) { this.topY = topY; }

        public double getBottomX() { return bottomX; }
        public void setBottomX(double bottomX) { this.bottomX = bottomX; }

        public double getBottomY() { return bottomY; }
        public void setBottomY(double bottomY) { this.bottomY = bottomY; }

        public double getOcrConfidence() { return ocrConfidence; }
        public void setOcrConfidence(double ocrConfidence) { this.ocrConfidence = ocrConfidence; }
    }

    private static void addIfNotDuplicate(List<QrCodeData> results, Set<String> seen, QrCodeData data) {
        String key = data.getPageNumber() + "|" + data.getFieldValue() + "|" +
                round(data.getTopX()) + "|" + round(data.getTopY()) + "|" +
                round(data.getBottomX()) + "|" + round(data.getBottomY());
        if (seen.add(key)) {
            results.add(data);
        }
    }
}
