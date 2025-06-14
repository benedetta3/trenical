package it.trenical.server.controller;

import it.trenical.common.grpc.*;
import it.trenical.server.observer.ClienteOsservatore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

public class GestoreNotificaClientTest {

    private GestoreNotificaClient gestore;

    @BeforeEach
    public void setUp() {
        resetTratteFile();
        gestore = new GestoreNotificaClient();
        ClienteOsservatore.resetNotifiche();
    }

    private void resetTratteFile() {
        try (
                BufferedReader reader = new BufferedReader(new FileReader("tratte_originale.txt"));
                BufferedWriter writer = new BufferedWriter(new FileWriter("tratte.txt"))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            fail("Errore nel reset del file tratte.txt: " + e.getMessage());
        }
    }

    @Test
    public void testClienteSenzaEmail() {
        ClienteDTO cliente = ClienteDTO.newBuilder()
                .setNome("Mario")
                .setCognome("Rossi")
                .build();

        RichiestaDTO richiesta = RichiestaDTO.newBuilder()
                .setCliente(cliente)
                .build();

        RispostaDTO risposta = gestore.gestisci(richiesta);

        assertFalse(risposta.getEsito());
        assertEquals("Email cliente mancante per controllo notifiche.", risposta.getMessaggio());
    }

    @Test
    public void testClienteSenzaNotifiche() {
        ClienteDTO cliente = ClienteDTO.newBuilder()
                .setEmail("test@email.com")
                .build();

        RichiestaDTO richiesta = RichiestaDTO.newBuilder()
                .setCliente(cliente)
                .build();

        RispostaDTO risposta = gestore.gestisci(richiesta);

        assertTrue(risposta.getEsito());
        assertEquals("Nessuna notifica", risposta.getMessaggio());
    }

    @Test
    public void testClienteConNotifiche() {
        String email = "utente@fedelta.com";
        String descrizionePromo = "Hai una nuova promozione!";
        ClienteOsservatore.inviaNotificaPromozione(email, descrizionePromo);

        ClienteDTO cliente = ClienteDTO.newBuilder()
                .setEmail(email)
                .setIsFedelta(true)
                .setRiceviPromo(true)
                .build();

        RichiestaDTO richiesta = RichiestaDTO.newBuilder()
                .setCliente(cliente)
                .build();

        RispostaDTO risposta = gestore.gestisci(richiesta);

        assertTrue(risposta.getEsito());
        assertEquals(1, risposta.getNotificheCount());

        String messaggio = risposta.getNotifiche(0).getMessaggio();
        assertTrue(messaggio.contains("NUOVA PROMOZIONE DEDICATA"));
        assertTrue(messaggio.contains(descrizionePromo));
    }
}