package org.springframework.samples.petclinic.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.samples.petclinic.service.PetTransferException;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link PetTransferController}.
 */
@SpringJUnitWebConfig(locations = {"classpath:spring/mvc-core-config.xml", "classpath:spring/mvc-test-config.xml"})
class PetTransferControllerTests {

    private static final int TEST_PET_ID     = 1;
    private static final int TEST_OWNER_ID   = 10;
    private static final int NEW_OWNER_ID    = 20;

    @Autowired
    private PetTransferController petTransferController;

    @Autowired
    private ClinicService clinicService;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        Mockito.reset(clinicService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(petTransferController).build();

        Owner currentOwner = new Owner();
        currentOwner.setId(TEST_OWNER_ID);
        currentOwner.setFirstName("Current");
        currentOwner.setLastName("Owner");

        Pet pet = new Pet();
        pet.setId(TEST_PET_ID);
        currentOwner.addPet(pet);

        given(clinicService.findPetById(TEST_PET_ID)).willReturn(pet);
    }

    // --- 7.5 Access (no existing auth — expect 200 for GET) ---

    @Test
    void shouldReturnTransferForm() throws Exception {
        mockMvc.perform(get("/pets/{petId}/transfer", TEST_PET_ID))
            .andExpect(status().isOk())
            .andExpect(view().name("pets/transferForm"))
            .andExpect(model().attributeExists("pet", "currentOwner"));
    }

    // --- 7.6 SELF_TRANSFER error rendered in form ---

    @Test
    void shouldShowError_whenSelfTransferOnConfirm() throws Exception {
        willThrow(new PetTransferException(PetTransferException.Reason.SELF_TRANSFER))
            .given(clinicService).transferPet(eq(TEST_PET_ID), eq(TEST_OWNER_ID), anyString());

        mockMvc.perform(post("/pets/{petId}/transfer/confirm", TEST_PET_ID)
                .param("newOwnerId", String.valueOf(TEST_OWNER_ID)))
            .andExpect(status().isOk())
            .andExpect(view().name("pets/transferForm"))
            .andExpect(model().attributeExists("transferError"));
    }

    // --- 7.7 PENDING_VISITS error rendered in form ---

    @Test
    void shouldShowError_whenPendingVisitsOnConfirm() throws Exception {
        willThrow(new PetTransferException(PetTransferException.Reason.PENDING_VISITS))
            .given(clinicService).transferPet(eq(TEST_PET_ID), eq(NEW_OWNER_ID), anyString());

        mockMvc.perform(post("/pets/{petId}/transfer/confirm", TEST_PET_ID)
                .param("newOwnerId", String.valueOf(NEW_OWNER_ID)))
            .andExpect(status().isOk())
            .andExpect(view().name("pets/transferForm"))
            .andExpect(model().attributeExists("transferError"));
    }

}
