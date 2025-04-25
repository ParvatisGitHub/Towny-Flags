package com.psmountains.townyflags;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AsciiFlagView {

	private static final int CHAT_WIDTH = 32; // number of characters per line
	private static final int CHAT_HEIGHT = 16; // number of lines in chat

	public static List<Component> generatePreviewRGB(File imageFile) {
    return generatePreviewRGB(imageFile, CHAT_WIDTH, CHAT_HEIGHT);
}

	
	public static List<Component> generatePreviewRGB(File imageFile, int maxWidth, int maxHeight) {
		try {
			BufferedImage original = ImageIO.read(imageFile);
			if (original == null) {
				return List.of(Component.text("§cInvalid image file."));
			}

			double originalWidth = original.getWidth();
			double originalHeight = original.getHeight();

			double scale = Math.min(
				(double) maxWidth / originalWidth,
				(double) maxHeight * 2 / originalHeight
			);

			int newWidth = Math.max(1, (int) (originalWidth * scale));
			int newHeight = Math.max(1, (int) (originalHeight * scale / 1.2));

			BufferedImage resized = resizeImage(original, newWidth, newHeight);
			List<Component> lines = new ArrayList<>();

			for (int y = 0; y < resized.getHeight(); y++) {
				Component line = Component.empty();
				for (int x = 0; x < resized.getWidth(); x++) {
					Color color = new Color(resized.getRGB(x, y), true);
					if (color.getAlpha() < 128) {
						line = line.append(Component.text(" "));
					} else {
						line = line.append(Component.text("█")
							.color(TextColor.color(color.getRGB())));
					}
				}
				lines.add(line);
			}

			return lines;

		} catch (IOException e) {
			e.printStackTrace();
			return List.of(Component.text("§cFailed to render image."));
		}
	}


	private static BufferedImage resizeImage(BufferedImage original, int targetWidth, int targetHeight) {
		BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = resized.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(original, 0, 0, targetWidth, targetHeight, null);
		g.dispose();
		return resized;
	}
}
