package com.amdium.util;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.util.HashSet;
import java.util.Set;

public class GLUtils {

    private static Set<String> supportedExtensions = null;

    public static boolean isExtensionSupported(String extension) {
        if (supportedExtensions == null) {
            loadExtensions();
        }
        return supportedExtensions.contains(extension);
    }

    private static void loadExtensions() {
        supportedExtensions = new HashSet<>();
        try {
            int numExtensions = GL11.glGetInteger(GL30.GL_NUM_EXTENSIONS);
            for (int i = 0; i < numExtensions; i++) {
                String ext = GL30.glGetStringi(GL11.GL_EXTENSIONS, i);
                if (ext != null) {
                    supportedExtensions.add(ext);
                }
            }
        } catch (Exception e) {
            try {
                String extensions = GL11.glGetString(GL11.GL_EXTENSIONS);
                if (extensions != null) {
                    for (String ext : extensions.split(" ")) {
                        supportedExtensions.add(ext.trim());
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    public static Set<String> getAMDExtensions() {
        if (supportedExtensions == null) loadExtensions();
        Set<String> amdExts = new HashSet<>();
        for (String ext : supportedExtensions) {
            if (ext.contains("AMD") || ext.contains("ATI")) {
                amdExts.add(ext);
            }
        }
        return amdExts;
    }

    public static int getMaxTextureSize() {
        return GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE);
    }

    public static int getMaxVertexAttribs() {
        return GL11.glGetInteger(GL30.GL_MAX_VERTEX_ATTRIBS);
    }

    public static void checkGLError(String operation) {
        int error = GL11.glGetError();
        if (error != GL11.GL_NO_ERROR) {
            String errorName = switch (error) {
                case GL11.GL_INVALID_ENUM -> "GL_INVALID_ENUM";
                case GL11.GL_INVALID_VALUE -> "GL_INVALID_VALUE";
                case GL11.GL_INVALID_OPERATION -> "GL_INVALID_OPERATION";
                case GL11.GL_STACK_OVERFLOW -> "GL_STACK_OVERFLOW";
                case GL11.GL_STACK_UNDERFLOW -> "GL_STACK_UNDERFLOW";
                case GL11.GL_OUT_OF_MEMORY -> "GL_OUT_OF_MEMORY";
                case GL30.GL_INVALID_FRAMEBUFFER_OPERATION -> "GL_INVALID_FRAMEBUFFER_OPERATION";
                default -> "Unknown (0x" + Integer.toHexString(error) + ")";
            };
            com.amdium.Amdium.LOGGER.warn("GL Error during {}: {}", operation, errorName);
        }
    }

}
