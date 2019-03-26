package com.firefly.emulationstation.gamerepo.data;

import com.firefly.emulationstation.data.bean.Game;
import com.firefly.emulationstation.data.bean.GameSystem;
import com.firefly.emulationstation.gamerepo.data.bean.Repo;
import com.firefly.emulationstation.gamerepo.data.bean.Rom;

import java.util.List;
import java.util.Set;

import io.reactivex.Observable;

/**
 * Created by rany on 18-4-24.
 *
 * Repository file structure:
 *      repository/
 *          |__ com.example/
 *              |__ repo.json
 *              |__ platforms/
 *                  |__ nes.json
 *                  |__ psp.json
 *                      ...
 *          |__ com.example1/
 *              ...
 * The parent directory name of repo.json file is the id of this repository.
 *
 */

public interface IGameRepoSource {
    /**
     * Get all repositories
     * @return {@link Observable} Repos set.
     */
    Observable<Set<Repo>> repos();

    /**
     * Add or update a game repository
     * @param path can be a http(s) scheme url or a path of local zip file
     * @return new {@link Repo}
     */
    Observable<Repo> addOrUpdateRepo(String path, boolean isNew);

    /**
     * Remove a exists repository
     * @param repo the Repo object which will be removed.
     * @return the new repositories after removing.
     */
    Observable<Set<Repo>> deleteRepo(Repo repo);


    Observable<List<Rom>> getRoms(GameSystem gameSystem, int filterStatus);

    /**
     * Get roms from a repo by id
     * @param repoId game repo id
     * @return List of Rom
     */
    Observable<List<Rom>> getRomsFromRepo(String repoId);
}
