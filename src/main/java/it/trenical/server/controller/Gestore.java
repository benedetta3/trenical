package it.trenical.server.controller;

import it.trenical.common.grpc.RichiestaDTO;
import it.trenical.common.grpc.RispostaDTO;

public interface Gestore {
    RispostaDTO gestisci(RichiestaDTO richiesta);
}