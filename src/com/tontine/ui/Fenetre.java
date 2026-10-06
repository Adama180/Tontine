package com.tontine.ui;

import javafx.scene.Node;

/** Zone de contenu dans laquelle un ecran peut en afficher un autre (page publique ou espace connecte). */
public interface Fenetre {

    void afficher(String titre, Node contenu);
}
