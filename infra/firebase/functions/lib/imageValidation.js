"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.validateImage = validateImage;
// @ts-ignore
const sharp = require("sharp");
async function validateImage(imageBuffer) {
    try {
        const MIN_IMAGE_SIZE = 10000;
        if (imageBuffer.length < MIN_IMAGE_SIZE) {
            return {
                isValid: false,
                safetyLevel: "unknown",
                reason: "image_too_small",
            };
        }
        const metadata = await sharp(imageBuffer).metadata();
        const { width = 0, height = 0 } = metadata;
        if (width < 100 || height < 100) {
            return {
                isValid: false,
                safetyLevel: "unknown",
                reason: "image_too_small_dimensions",
            };
        }
        const stats = await sharp(imageBuffer)
            .greyscale()
            .stats();
        const totalPixels = width * height;
        const veryDarkPixels = stats.channels[0]?.histogram
            ?.slice(0, 6)
            ?.reduce((sum, count) => sum + (count || 0), 0) || 0;
        const veryDarkRatio = veryDarkPixels / totalPixels;
        if (veryDarkRatio > 0.99) {
            return {
                isValid: false,
                safetyLevel: "unknown",
                reason: "too_dark",
            };
        }
        return {
            isValid: true,
            safetyLevel: "ok",
            reason: "ok",
        };
    }
    catch (error) {
        console.error("Image validation error:", error);
        return {
            isValid: false,
            safetyLevel: "unknown",
            reason: "validation_error",
        };
    }
}
//# sourceMappingURL=imageValidation.js.map