/*package it.trenical.server.notification;

import it.trenical.common.grpc.ClienteDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NotificationDispatcherTest {

    private NotificationDispatcher dispatcher;

    @BeforeEach
    public void setup() {
        dispatcher = NotificationDispatcher.getInstance();
        dispatcher.reset(); // pulisce gli osservatori prima di ogni test
    }

    @Test
    public void testAttachObserver() {
        ClienteDTO cliente = ClienteDTO.newBuilder()
                .setId(99)
                .setNome("Mario")
                .setCognome("Rossi")
                .setEmail("mario.rossi@example.com")
                .build();

        dispatcher.attach(cliente);

        assertTrue(dispatcher.getClientiOsservatori().contains(cliente),
                "Il cliente dovrebbe essere registrato come osservatore.");
    }

    @Test
    public void testResetObservers() {
        ClienteDTO cliente = ClienteDTO.newBuilder().setId(1).build();
        dispatcher.attach(cliente);
        dispatcher.reset();

        assertTrue(dispatcher.getClientiOsservatori().isEmpty(),
                "Dopo il reset non devono esserci osservatori.");
    }
}
*/