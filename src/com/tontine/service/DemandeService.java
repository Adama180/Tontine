package com.tontine.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import com.tontine.dao.AdhesionDao;
import com.tontine.dao.DemandeAdhesionDao;
import com.tontine.dao.DemandeCreationDao;
import com.tontine.dao.TontineDao;
import com.tontine.dao.UtilisateurDao;
import com.tontine.model.DemandeAdhesion;
import com.tontine.model.DemandeCreation;
import com.tontine.model.Frequence;
import com.tontine.model.StatutDemande;
import com.tontine.model.StatutTontine;
import com.tontine.model.Tontine;
import com.tontine.model.TypeTontine;
import com.tontine.model.Utilisateur;
import com.tontine.util.HashUtil;

/** Demandes d'un utilisateur : rejoindre une tontine, ou en creer une (devenir gerant). */
public class DemandeService {

    private static final long TAILLE_MAX_PIECE = 5L * 1024 * 1024;
    /** Dossier (a la racine du projet) ou sont copiees les pieces d'identite. */
    public static final String DOSSIER_PIECES = "pieces";

    private final AuthService auth = new AuthService();
    private final UtilisateurDao utilisateurDao = new UtilisateurDao();
    private final TontineDao tontineDao = new TontineDao();
    private final AdhesionDao adhesionDao = new AdhesionDao();
    private final DemandeAdhesionDao demandeAdhesionDao = new DemandeAdhesionDao();
    private final DemandeCreationDao demandeCreationDao = new DemandeCreationDao();

    // ---------------------------------------------------------------- adhesion

    /**
     * Visiteur qui clique sur "Rejoindre cette tontine" : cree son compte ET sa demande en une seule action.
     */
    public Utilisateur inscrireEtDemanderAdhesion(String nom, String prenom, String email, String telephone,
            String motDePasse, int tontineId, File pieceIdentite) {
        Tontine t = Controles.tontine(tontineId);
        Controles.exiger(t.getStatut() == StatutTontine.ACTIVE, "Cette tontine n'accepte plus de demandes.");
        controlerPiece(pieceIdentite, -1); // verifie avant de creer le compte
        Utilisateur u = auth.inscrire(nom, prenom, email, telephone, motDePasse);
        try {
            demanderAdhesion(u.getId(), tontineId, pieceIdentite);
        } catch (RuntimeException e) {
            utilisateurDao.delete(u.getId()); // on ne garde pas un compte a moitie cree
            throw e;
        }
        return u;
    }

    /** Utilisateur deja inscrit qui demande a rejoindre une (autre) tontine. */
    public DemandeAdhesion demanderAdhesion(int userId, int tontineId, File pieceIdentite) {
        Utilisateur u = Controles.utilisateur(userId);
        Tontine t = Controles.tontine(tontineId);
        Controles.exiger(t.getStatut() == StatutTontine.ACTIVE, "Cette tontine n'accepte plus de demandes.");
        Controles.exiger(!adhesionDao.estMembre(tontineId, userId), "Vous etes deja membre de cette tontine.");
        Controles.exiger(!demandeAdhesionDao.demandeEnAttenteExiste(tontineId, userId),
                "Vous avez deja une demande en attente pour cette tontine.");

        String[] piece = preparerPiece(pieceIdentite, userId);

        DemandeAdhesion d = new DemandeAdhesion();
        d.setTontineId(tontineId);
        d.setUserId(userId);
        d.setNom(u.getNomComplet());
        d.setTelephone(u.getTelephone());
        d.setEmail(u.getEmail());
        d.setPieceHash(piece[0]);
        d.setPieceChemin(piece[1]);
        demandeAdhesionDao.insert(d);

        NotificationService.notifier(t.getGerantId(),
                "Nouvelle demande d'adhesion de " + u.getNomComplet() + " pour la tontine " + t.getNom() + ".",
                "ADHESION");
        return d;
    }

    // ---------------------------------------------------------------- creation

    /** Utilisateur qui souhaite devenir gerant : la demande est validee par l'administrateur. */
    public DemandeCreation soumettreDemandeCreation(int userId, String nomSouhaite, TypeTontine type, double montant,
            Frequence frequence, int nbMembres, File pieceIdentite) {
        Utilisateur u = Controles.utilisateur(userId);
        nomSouhaite = Controles.texte(nomSouhaite, "nom de la tontine");
        Controles.exiger(type != null, "Choisissez un type de tontine.");
        Controles.exiger(frequence != null, "Choisissez une frequence de cotisation.");
        Controles.exiger(montant > 0, "Le montant de la cotisation doit etre positif.");
        Controles.exiger(nbMembres >= 2, "Une tontine doit avoir au moins 2 membres.");
        Controles.exiger(!tontineDao.nomExiste(nomSouhaite), "Une tontine porte deja ce nom.");
        for (DemandeCreation autre : demandeCreationDao.findByStatut(StatutDemande.EN_ATTENTE)) {
            Controles.exiger(!autre.getNomSouhaite().equalsIgnoreCase(nomSouhaite),
                    "Une demande en attente utilise deja ce nom.");
        }

        String[] piece = preparerPiece(pieceIdentite, userId);

        DemandeCreation d = new DemandeCreation();
        d.setDemandeurId(userId);
        d.setPieceHash(piece[0]);
        d.setPieceChemin(piece[1]);
        d.setNomSouhaite(nomSouhaite);
        d.setTypeSouhaite(type);
        d.setMontant(montant);
        d.setFrequence(frequence);
        d.setNbMembre(nbMembres);
        demandeCreationDao.insert(d);

        NotificationService.notifierAdmins(
                "Nouvelle demande de creation de tontine '" + nomSouhaite + "' par " + u.getNomComplet() + ".",
                "CREATION");
        return d;
    }

    /** Visiteur qui clique sur "Creer ma propre tontine" : compte + demande en une action. */
    public Utilisateur inscrireEtDemanderCreation(String nom, String prenom, String email, String telephone,
            String motDePasse, String nomTontine, TypeTontine type, double montant, Frequence frequence,
            int nbMembres, File pieceIdentite) {
        controlerPiece(pieceIdentite, -1); // verifie avant de creer le compte
        Utilisateur u = auth.inscrire(nom, prenom, email, telephone, motDePasse);
        try {
            soumettreDemandeCreation(u.getId(), nomTontine, type, montant, frequence, nbMembres, pieceIdentite);
        } catch (RuntimeException e) {
            utilisateurDao.delete(u.getId());
            throw e;
        }
        return u;
    }

    // ---------------------------------------------------------------- suivi

    public List<DemandeAdhesion> suivreDemandesAdhesion(int userId) {
        return demandeAdhesionDao.findByUser(userId);
    }

    public List<DemandeCreation> suivreDemandesCreation(int userId) {
        return demandeCreationDao.findByDemandeur(userId);
    }

    // ---------------------------------------------------------------- piece d'identite

    /** Vrai si l'utilisateur a deja fourni une piece d'identite (il n'a pas besoin de la renvoyer). */
    public boolean aPieceEnregistree(int userId) {
        return pieceEnregistree(userId) != null;
    }

    /** Derniere piece d'identite fournie par l'utilisateur : {empreinte, chemin}, ou null. */
    private String[] pieceEnregistree(int userId) {
        for (DemandeAdhesion d : demandeAdhesionDao.findByUser(userId)) {
            if (d.getPieceHash() != null && !d.getPieceHash().isEmpty()) {
                return new String[] { d.getPieceHash(), d.getPieceChemin() };
            }
        }
        for (DemandeCreation d : demandeCreationDao.findByDemandeur(userId)) {
            if (d.getPieceHash() != null && !d.getPieceHash().isEmpty()) {
                return new String[] { d.getPieceHash(), d.getPieceChemin() };
            }
        }
        return null;
    }

    /**
     * Nouvelle piece fournie : elle est controlee et copiee. Sinon, on reutilise celle deja enregistree
     * (pas besoin de tout ressaisir) ; s'il n'y en a pas, la piece est obligatoire.
     */
    private String[] preparerPiece(File piece, int userId) {
        if (piece != null) {
            String hash = controlerPiece(piece, userId);
            return new String[] { hash, copierPiece(piece, hash) };
        }
        String[] existante = pieceEnregistree(userId);
        Controles.exiger(existante != null, "La piece d'identite est obligatoire.");
        return existante;
    }

    /** Verifie la piece et retourne son empreinte. userId = -1 pour une personne sans compte. */
    private String controlerPiece(File piece, int userId) {
        Controles.exiger(piece != null && piece.isFile(), "La piece d'identite est obligatoire.");
        String nom = piece.getName().toLowerCase();
        Controles.exiger(nom.endsWith(".pdf") || nom.endsWith(".jpg") || nom.endsWith(".jpeg") || nom.endsWith(".png"),
                "Format de piece accepte : PDF, JPG ou PNG.");
        Controles.exiger(piece.length() <= TAILLE_MAX_PIECE, "La piece d'identite ne doit pas depasser 5 Mo.");
        String hash;
        try {
            hash = HashUtil.sha256Fichier(piece);
        } catch (IOException e) {
            throw new ServiceException("Impossible de lire la piece d'identite.");
        }
        // protection anti-doublon : la meme piece ne peut pas servir a deux personnes differentes
        DemandeAdhesion deja = demandeAdhesionDao.findByPieceHash(hash);
        DemandeCreation dejaCreation = demandeCreationDao.findByPieceHash(hash);
        Controles.exiger((deja == null || deja.getUserId() == userId)
                && (dejaCreation == null || dejaCreation.getDemandeurId() == userId),
                "Cette piece d'identite a deja ete utilisee par un autre compte.");
        return hash;
    }

    private String copierPiece(File piece, String hash) {
        try {
            String nom = piece.getName();
            String ext = nom.substring(nom.lastIndexOf('.')).toLowerCase();
            Path dossier = Paths.get(DOSSIER_PIECES);
            Files.createDirectories(dossier);
            Path cible = dossier.resolve(hash + ext);
            if (!Files.exists(cible)) {
                Files.copy(piece.toPath(), cible, StandardCopyOption.REPLACE_EXISTING);
            }
            return cible.toString();
        } catch (IOException e) {
            throw new ServiceException("Impossible d'enregistrer la piece d'identite : " + e.getMessage());
        }
    }
}
