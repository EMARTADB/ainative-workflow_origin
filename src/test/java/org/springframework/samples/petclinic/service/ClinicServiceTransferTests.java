package org.springframework.samples.petclinic.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetTransfer;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.repository.PetRepository;
import org.springframework.samples.petclinic.repository.PetTransferRepository;
import org.springframework.samples.petclinic.repository.VetRepository;
import org.springframework.samples.petclinic.repository.VisitRepository;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the pet transfer logic in {@link ClinicServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class ClinicServiceTransferTests {

    @Mock private PetRepository petRepository;
    @Mock private VetRepository vetRepository;
    @Mock private OwnerRepository ownerRepository;
    @Mock private VisitRepository visitRepository;
    @Mock private PetTransferRepository petTransferRepository;

    private ClinicServiceImpl clinicService;

    private static final int PET_ID       = 1;
    private static final int OWNER_A_ID   = 10;
    private static final int OWNER_B_ID   = 20;

    @BeforeEach
    void setUp() {
        clinicService = new ClinicServiceImpl(
            petRepository, vetRepository, ownerRepository, visitRepository, petTransferRepository);
    }

    // --- helpers ---

    private Pet petOwnedBy(int ownerId) {
        Owner owner = new Owner();
        owner.setId(ownerId);
        Pet pet = new Pet();
        pet.setId(PET_ID);
        owner.addPet(pet);
        return pet;
    }

    // --- 7.1 Happy path ---

    @Test
    void shouldTransferPet_happyPath() {
        Pet pet = petOwnedBy(OWNER_A_ID);
        Owner newOwner = new Owner();
        newOwner.setId(OWNER_B_ID);

        given(petRepository.findById(PET_ID)).willReturn(pet);
        given(visitRepository.findByPetId(PET_ID)).willReturn(Collections.emptyList());
        given(ownerRepository.findById(OWNER_B_ID)).willReturn(newOwner);

        clinicService.transferPet(PET_ID, OWNER_B_ID, "receptionist");

        // pet owner updated
        assertThat(pet.getOwner().getId()).isEqualTo(OWNER_B_ID);

        // audit record saved
        ArgumentCaptor<PetTransfer> captor = ArgumentCaptor.forClass(PetTransfer.class);
        verify(petTransferRepository).save(captor.capture());
        PetTransfer saved = captor.getValue();
        assertThat(saved.getPetId()).isEqualTo(PET_ID);
        assertThat(saved.getFromOwnerId()).isEqualTo(OWNER_A_ID);
        assertThat(saved.getToOwnerId()).isEqualTo(OWNER_B_ID);
        assertThat(saved.getPerformedBy()).isEqualTo("receptionist");
        assertThat(saved.getTransferredAt()).isNotNull();
    }

    // --- 7.2 SELF_TRANSFER ---

    @Test
    void shouldThrowPetTransferException_selfTransfer() {
        Pet pet = petOwnedBy(OWNER_A_ID);
        given(petRepository.findById(PET_ID)).willReturn(pet);

        assertThatExceptionOfType(PetTransferException.class)
            .isThrownBy(() -> clinicService.transferPet(PET_ID, OWNER_A_ID, "receptionist"))
            .satisfies(e -> assertThat(e.getReason()).isEqualTo(PetTransferException.Reason.SELF_TRANSFER));

        verifyNoInteractions(petTransferRepository);
        verify(petRepository, never()).save(any());
    }

    // --- 7.3 PENDING_VISITS ---

    @Test
    void shouldThrowPetTransferException_pendingVisits() {
        Pet pet = petOwnedBy(OWNER_A_ID);
        given(petRepository.findById(PET_ID)).willReturn(pet);

        Visit futureVisit = new Visit();
        futureVisit.setDate(LocalDate.now().plusDays(7));
        given(visitRepository.findByPetId(PET_ID)).willReturn(List.of(futureVisit));

        assertThatExceptionOfType(PetTransferException.class)
            .isThrownBy(() -> clinicService.transferPet(PET_ID, OWNER_B_ID, "receptionist"))
            .satisfies(e -> assertThat(e.getReason()).isEqualTo(PetTransferException.Reason.PENDING_VISITS));

        verifyNoInteractions(petTransferRepository);
        verify(petRepository, never()).save(any());
    }

    // --- 7.4 Rollback-on-audit-failure ---

    @Test
    void shouldPropagateException_whenAuditSaveFails() {
        Pet pet = petOwnedBy(OWNER_A_ID);
        Owner newOwner = new Owner();
        newOwner.setId(OWNER_B_ID);

        given(petRepository.findById(PET_ID)).willReturn(pet);
        given(visitRepository.findByPetId(PET_ID)).willReturn(Collections.emptyList());
        given(ownerRepository.findById(OWNER_B_ID)).willReturn(newOwner);
        doThrow(new RuntimeException("DB failure")).when(petTransferRepository).save(any());

        assertThatExceptionOfType(RuntimeException.class)
            .isThrownBy(() -> clinicService.transferPet(PET_ID, OWNER_B_ID, "receptionist"));

        // The @Transactional rollback is managed by Spring; in unit test without transaction
        // we verify the exception propagates so the caller knows transfer failed.
    }

}
