package com.tontine.service;

/** Erreur metier : le message est destine a etre affiche tel quel a l'utilisateur. */
public class ServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ServiceException(String message) {
        super(message);
    }
}
