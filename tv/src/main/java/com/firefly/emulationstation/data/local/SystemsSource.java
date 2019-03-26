package com.firefly.emulationstation.data.local;

import com.firefly.emulationstation.data.bean.GameSystem;
import com.firefly.emulationstation.data.bean.GameSystems;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;

import java.io.File;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by rany on 17-10-31.
 */

@Singleton
public class SystemsSource {
    @Inject
    public SystemsSource() {
    }

    public List<GameSystem> getGameSystems(File file) throws Exception {
        return fileToObj(file);
    }

    public void saveGameSystem(List<GameSystem> systems, File out) throws Exception {
        if (systems != null) {
            Strategy strategy = new AnnotationStrategy();
            Serializer serializer = new Persister(strategy);
            GameSystems gameSystems = new GameSystems();

            gameSystems.setSystems(systems);

            serializer.write(gameSystems, out);
        }
    }

    public static List<GameSystem> fileToObj(File file) throws Exception {
        Strategy strategy = new AnnotationStrategy();
        Serializer serializer = new Persister(strategy);
        GameSystems gameSystems = serializer.read(GameSystems.class, file);

        return gameSystems.getSystems();
    }
}
