package com.base580.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.base580.backend.model.AppStateDocument;
import com.base580.backend.repository.AppStateRepository;

class AppStateVersionTest {
    @Test
    void onlyOneConcurrentPublisherCanUseTheSameVersion() throws Exception {
        AppStateRepository repository = mock(AppStateRepository.class);
        AppStateDocument initial = AppStateDocument.sample();
        initial.setUpdatedAt("initial");
        AtomicReference<AppStateDocument> persisted = new AtomicReference<>(initial);
        when(repository.findById("default")).thenAnswer(invocation -> Optional.of(persisted.get()));
        when(repository.save(any(AppStateDocument.class))).thenAnswer(invocation -> {
            AppStateDocument state = invocation.getArgument(0);
            persisted.set(state);
            return state;
        });
        AppStateService service = new AppStateService(repository);
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var tasks = java.util.stream.IntStream.range(0, 2).mapToObj(i -> executor.submit(() -> {
                start.await();
                try { service.saveIfCurrent(AppStateDocument.sample(), "initial"); return 200; }
                catch (ResponseStatusException error) { return error.getStatusCode().value(); }
            })).toList();
            start.countDown();
            var codes = java.util.List.of(tasks.get(0).get(), tasks.get(1).get());
            assertTrue(codes.contains(200));
            assertTrue(codes.contains(409));
            verify(repository, times(1)).save(any(AppStateDocument.class));
        }
    }

    @Test
    void rejectsUnversionedWrites() {
        var service = new AppStateService(mock(AppStateRepository.class));
        var error = assertThrows(ResponseStatusException.class, () -> service.saveIfCurrent(AppStateDocument.sample(), null));
        assertEquals(HttpStatus.PRECONDITION_REQUIRED, error.getStatusCode());
    }
}
