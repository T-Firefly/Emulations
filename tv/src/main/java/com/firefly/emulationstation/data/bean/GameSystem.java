package com.firefly.emulationstation.data.bean;

import com.firefly.emulationstation.utils.Sha1Helper;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rany on 17-10-31.
 */

@Root(name = "system")
public class GameSystem implements Serializable {

    private static Map<String, String> mPlatformMap = new HashMap<String, String>() {{
        put("3do", "3DO");
        put("amiga", "Amiga");
        put("amstradcpc", "Amstrad CPC");
        // missing apple2
        put("arcade", "Arcade");
        // missing atari 800
        put("atari2600", "Atari 2600");
        put("atari5200", "Atari 5200");
        put("atari7800", "Atari 7800");
        put("atarijaguar", "Atari Jaguar");
        put("atarijaguarcd", "Atari Jaguar CD");
        put("atarilynx", "Atari Lynx");
        // missing atari ST/STE/Falcon
        put("atarixe", "Atari XE");
        put("colecovision", "Colecovision");
        put("c64", "Commodore 64");
        put("intellivision", "Intellivision");
        put("macintosh", "Mac OS");
        put("xbox", "Microsoft Xbox");
        put("xbox360", "Microsoft Xbox 360");
        put("msx", "MSX");
        put("neogeo", "Neo Geo");
        put("ngp", "Neo Geo Pocket");
        put("ngpc", "Neo Geo Pocket Color");
        put("n3ds", "Nintendo 3DS");
        put("n64", "Nintendo 64");
        put("nds", "Nintendo DS");
        put("fds", "Famicom Disk System");
        put("nes", "Nintendo Entertainment System (NES)");
        put("gb", "Nintendo Game Boy");
        put("gba", "Nintendo Game Boy Advance");
        put("gbc", "Nintendo Game Boy Color");
        put("gc", "Nintendo GameCube");
        put("wii", "Nintendo Wii");
        put("wiiu", "Nintendo Wii U");
        put("virtualboy", "Nintendo Virtual Boy");
        put("gameandwatch", "Game & Watch");
        put("pc", "PC");
        put("sega32x", "Sega 32X");
        put("segacd", "Sega CD");
        put("dreamcast", "Sega Dreamcast");
        put("gamegear", "Sega Game Gear");
        put("genesis", "Sega Genesis");
        put("mastersystem", "Sega Master System");
        put("megadrive", "Sega Mega Drive");
        put("saturn", "Sega Saturn");
        put("sg-1000", "SEGA SG-1000");
        put("psx", "Sony Playstation");
        put("ps2", "Sony Playstation 2");
        put("ps3", "Sony Playstation 3");
        put("ps4", "Sony Playstation 4");
        put("psvita", "Sony Playstation Vita");
        put("psp", "Sony Playstation Portable");
        put("snes", "Super Nintendo (SNES)");
        put("pcengine", "TurboGrafx 16");
        put("wonderswan", "WonderSwan");
        put("wonderswancolor", "WonderSwan Color");
        put("zxspectrum", "Sinclair ZX Spectrum");
        put("videopac", "Magnavox Odyssey 2");
        put("vectrex", "Vectrex");
        put("trs-80", "TRS-80 Color Computer");
        put("coco", "TRS-80 Color Computer");
    }};

    @Element(required = false)
    private String name;
    @Element(name = "platform", required = false)
    private String platformId;
    private String platform;
    @Element(required = false)
    private String romPath;
    @Element(required = false)
    private Emulator emulator;
    @Element(required = false)
    private String extension;
    @Element(required = false)
    private boolean enable = true;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlatform() {
        if (platform == null) {
            platform = mPlatformMap.get(platformId);
        }

        return platform;
    }

    private void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getPlatformId() {
        return platformId;
    }

    public void setPlatformId(String platformId) {
        String p = mPlatformMap.get(platformId.toLowerCase());
        if (p != null) {
            this.platform = p;
        }

        this.platformId = platformId;
    }

    public String getRomPath() {
        return romPath;
    }

    public void setRomPath(String romPath) {
        this.romPath = romPath;
    }

    public Emulator getEmulator() {
        return emulator;
    }

    public void setEmulator(Emulator emulator) {
        this.emulator = emulator;
    }

    public String getExtension() {
        return extension;
    }

    public String[] getExtensions() {
        return extension.split("\\s+");
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public void merge(GameSystem system) throws Exception {
        if (system.getRomPath() != null) {
            setRomPath(system.getRomPath());
        }

        if (system.getExtension() != null) {
            setExtension(system.getExtension());
        }

        if (system.getEmulator() != null) {
            Emulator emulator = system.getEmulator();

            if (emulator.getName() == null
                    || emulator.getName().equals(getEmulator().getName())) {
                if (emulator.getConfig() != null) {
                    getEmulator().setConfig(emulator.getConfig());
                }
                if (emulator.getCore() != null) {
                    getEmulator().setCore(emulator.getCore());
                }
            } else {
                throw new Exception("Not a support emulator.");
//                setEmulator(system.getEmulator());
            }
        }

        setEnable(system.isEnable());

        if (system.getPlatformId() != null
                && mPlatformMap.containsKey(system.getPlatformId())) {
            setPlatformId(system.getPlatformId());
            setPlatform(mPlatformMap.get(getPlatformId()));
        }
    }

    public String getRomPathID() {
        String id = Sha1Helper.md5(getRomPath());

        if (id.isEmpty()) {
            return getRomPath();
        }

        return id;
    }

    public static String getPlatformName(String platformId) {
        return mPlatformMap.get(platformId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof GameSystem)) {
            return false;
        }
        GameSystem gameSystem = (GameSystem) obj;

        return gameSystem.getName().equals(getName())
                && gameSystem.getPlatformId().equals(getPlatformId())
                && gameSystem.getExtension().equals(getExtension())
                && gameSystem.getRomPath().equals(getRomPath());

    }
}
