package se.comerit.avanza.nativebridge;

import com.sun.jna.Library;
import com.sun.jna.Native;

import java.io.File;

public final class NativeLibraryLoader {

    private NativeLibraryLoader() {
    }

    public static <T extends Library> T load(String libraryName, Class<T> libraryType) {
        try {
            File libraryFile = Native.extractFromResourcePath(libraryName, libraryType.getClassLoader());
            return Native.load(libraryFile.getAbsolutePath(), libraryType);
        } catch (Exception | UnsatisfiedLinkError exception) {
            return Native.load(libraryName, libraryType);
        }
    }
}
