package com.tontine.service;

import java.util.ArrayList;
import java.util.List;

import com.tontine.dao.AdhesionDao;
import com.tontine.dao.TontineDao;
import com.tontine.model.Adhesion;
import com.tontine.model.Tontine;
import com.tontine.model.TypeTontine;

/** Consultation publique (visiteur) et liste des tontines d'un utilisateur. */
public class ConsultationService {

    private final TontineDao tontineDao = new TontineDao();
    private final AdhesionDao adhesionDao = new AdhesionDao();

    public TypeTontine[] listerTypes() {
        return TypeTontine.values();
    }

    public List<Tontine> listerTontines(TypeTontine type) {
        return tontineDao.findByType(type);
    }

    public List<Tontine> listerToutesLesTontines() {
        return tontineDao.findActives();
    }

    public Tontine detail(int tontineId) {
        return Controles.tontine(tontineId);
    }

    public int nombreMembres(int tontineId) {
        return adhesionDao.compterActifs(tontineId);
    }

    /** Regles generales visibles par tout le monde (sans historique ni soldes). */
    public List<String> reglesGenerales(Tontine t) {
        List<String> r = new ArrayList<>();
        r.add("Type : " + Libelles.type(t.getType()));
        r.add("Cotisation : " + Format.montant(t.getMontantCotisation()) + " (" + Libelles.frequence(t.getFrequence()) + ")");
        r.add("Penalite de retard : " + Format.montant(t.getPenaliteRetard()));
        r.add("Membres : " + nombreMembres(t.getId()) + " / " + t.getNbMembreMax());
        if (t.isFondsCommun()) {
            r.add("Taux d'interet des prets : " + (t.getTauxInteret() == null ? "non defini" : t.getTauxInteret() + " %"));
            r.add("Duree maximale d'un pret : " + (t.getDureeMaxPret() == null ? "non definie" : t.getDureeMaxPret() + " jours"));
        }
        return r;
    }

    /** Tontines dont l'utilisateur est membre actif (ou gerant). */
    public List<Tontine> mesTontines(int userId) {
        return tontineDao.findByMembre(userId);
    }

    /** Participation de l'utilisateur a une tontine, ou null s'il n'en fait pas partie. */
    public Adhesion monAdhesion(int userId, int tontineId) {
        return adhesionDao.findByTontineEtUser(tontineId, userId);
    }
}
