package com.tontine.service;

import com.tontine.dao.AdhesionDao;
import com.tontine.dao.DistributionDao;
import com.tontine.dao.TontineDao;
import com.tontine.model.Distribution;
import com.tontine.model.Tontine;

/** Chiffres globaux de la plateforme, visibles par tous sur la page d'accueil. */
public class StatsPubliquesService {

    public ResumePublic resume() {
        AdhesionDao adhesions = new AdhesionDao();
        int tontines = 0;
        int membres = 0;
        for (Tontine t : new TontineDao().findActives()) {
            tontines++;
            membres += adhesions.compterActifs(t.getId());
        }
        double verse = 0;
        for (Distribution d : new DistributionDao().findAll()) {
            verse += d.getMontantTotal();
        }
        return new ResumePublic(tontines, membres, verse);
    }
}
