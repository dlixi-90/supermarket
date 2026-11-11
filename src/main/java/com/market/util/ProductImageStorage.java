package com.market.util;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.servlet.http.Part;

public final class ProductImageStorage {
    public static final long MAX_FILE_SIZE = 5L * 1024 * 1024;
    private static final long MAX_PIXELS = 20_000_000;
    private static final int MAX_IMAGE_DIMENSION = 1600;

    private ProductImageStorage() { }

    public static Path directory() {
        String configured = System.getProperty("msm.upload.dir");
        if (configured == null || configured.isBlank()) configured = System.getenv("MSM_UPLOAD_DIR");
        if (configured != null && !configured.isBlank()) return Path.of(configured).toAbsolutePath().normalize();
        String base = System.getProperty("catalina.base", System.getProperty("user.home"));
        return Path.of(base, "msm-uploads", "products").toAbsolutePath().normalize();
    }

    public static String save(Part part) throws IOException {
        if (part == null || (part.getSize() == 0 && (part.getSubmittedFileName() == null || part.getSubmittedFileName().isBlank()))) return null;
        if (part.getSize() == 0) throw new IllegalArgumentException("The selected image is empty.");
        if (part.getSize() > MAX_FILE_SIZE) throw new IllegalArgumentException("Choose an image no larger than 5 MB.");

        BufferedImage image;
        String format;
        try (InputStream input = part.getInputStream(); ImageInputStream imageInput = ImageIO.createImageInputStream(input)) {
            if (imageInput == null) throw new IllegalArgumentException("Choose a valid JPG or PNG image.");
            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInput);
            if (!readers.hasNext()) throw new IllegalArgumentException("Choose a valid JPG or PNG image.");
            ImageReader reader = readers.next();
            try {
                format = reader.getFormatName().toLowerCase(Locale.ROOT);
                if (!format.equals("jpeg") && !format.equals("png")) throw new IllegalArgumentException("Only JPG and PNG images are supported.");
                reader.setInput(imageInput, true, true);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width <= 0 || height <= 0 || (long) width * height > MAX_PIXELS) throw new IllegalArgumentException("Choose an image with at most 20 million pixels.");
                image = reader.read(0);
            } finally {
                reader.dispose();
            }
        } catch (javax.imageio.IIOException e) {
            throw new IllegalArgumentException("The image could not be read. Choose another JPG or PNG file.", e);
        }
        if (image == null) throw new IllegalArgumentException("Choose a valid JPG or PNG image.");
        image = resizeForWeb(image);

        String name = UUID.randomUUID() + (format.equals("jpeg") ? ".jpg" : ".png");
        Path directory = directory();
        Files.createDirectories(directory);
        Path output = directory.resolve(name);
        try {
            if (!ImageIO.write(image, format, output.toFile())) throw new IOException("Could not save product image.");
        } catch (IOException e) {
            Files.deleteIfExists(output);
            throw e;
        }
        return "uploads/products/" + name;
    }

    private static BufferedImage resizeForWeb(BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();
        int longestSide = Math.max(width, height);
        if (longestSide <= MAX_IMAGE_DIMENSION) return source;

        double scale = (double) MAX_IMAGE_DIMENSION / longestSide;
        int scaledWidth = Math.max(1, (int) Math.round(width * scale));
        int scaledHeight = Math.max(1, (int) Math.round(height * scale));
        int imageType = source.getColorModel().hasAlpha()
                ? BufferedImage.TYPE_INT_ARGB
                : BufferedImage.TYPE_INT_RGB;
        BufferedImage resized = new BufferedImage(scaledWidth, scaledHeight, imageType);
        java.awt.Graphics2D graphics = resized.createGraphics();
        try {
            graphics.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                    java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING,
                    java.awt.RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.drawImage(source, 0, 0, scaledWidth, scaledHeight, null);
        } finally {
            graphics.dispose();
        }
        return resized;
    }

    public static Path find(String name) {
        if (name == null || !name.matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}\\.(png|jpg)")) return null;
        Path file = directory().resolve(name);
        return Files.isRegularFile(file) ? file : null;
    }
}
