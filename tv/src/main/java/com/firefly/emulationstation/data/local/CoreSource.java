package com.firefly.emulationstation.data.local;

import com.firefly.emulationstation.commom.Constants;
import com.firefly.emulationstation.data.bean.GamePlay;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;

import java.io.File;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CoreSource {
    private List<GamePlay> mCores = null;

    @Inject
    CoreSource() {
    }

    public List<GamePlay> getCores() {
        if (mCores == null) {
            Strategy strategy = new AnnotationStrategy();
            Serializer serializer = new Persister(strategy);
            File file = new File(Constants.ES_DIR, "download_source.xml");

            try {
                Res res = serializer.read(Res.class, file);
                mCores = res.cores;
                mCores.add(0, res.retroarch);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return mCores;
    }

    public static class Res {
        @Element(name = "retroarch")
        GamePlay retroarch;
        @ElementList(name = "cores")
        List<GamePlay> cores;
    }
}
