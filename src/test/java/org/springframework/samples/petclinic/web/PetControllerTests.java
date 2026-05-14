package org.springframework.samples.petclinic.web;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the {@link PetController}
 *
 * @author Colin But
 */
@SpringJUnitWebConfig(locations = {"classpath:spring/mvc-core-config.xml", "classpath:spring/mvc-test-config.xml"})
class PetControllerTests {

    private static final int TEST_OWNER_ID = 1;
    private static final int TEST_PET_ID = 1;

    @Autowired
    private PetController petController;

    @Autowired
    private FormattingConversionServiceFactoryBean formattingConversionServiceFactoryBean;

    @Autowired
    private ClinicService clinicService;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        Mockito.reset(this.clinicService);
        this.mockMvc = MockMvcBuilders
            .standaloneSetup(petController)
            .setConversionService(formattingConversionServiceFactoryBean.getObject())
            .build();

        PetType cat = new PetType();
        cat.setId(3);
        cat.setName("hamster");
        given(this.clinicService.findPetTypes()).willReturn(Lists.newArrayList(cat));
        given(this.clinicService.findOwnerById(TEST_OWNER_ID)).willReturn(new Owner());
        given(this.clinicService.findPetById(TEST_PET_ID)).willReturn(new Pet());
    }

    @Test
    void testInitCreationForm() throws Exception {
        mockMvc.perform(get("/owners/{ownerId}/pets/new", TEST_OWNER_ID))
            .andExpect(status().isOk())
            .andExpect(view().name("pets/createOrUpdatePetForm"))
            .andExpect(model().attributeExists("pet"));
    }

    @Test
    void testProcessCreationFormSuccess() throws Exception {
        mockMvc.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_ID)
            .param("name", "Betty")
            .param("type", "hamster")
            .param("birthDate", "2015/02/12")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/owners/{ownerId}"));
    }

    @Test
    void testProcessCreationFormHasErrors() throws Exception {
        mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID)
            .param("name", "Betty")
            .param("birthDate", "2015/02/12")
        )
            .andExpect(model().attributeHasNoErrors("owner"))
            .andExpect(model().attributeHasErrors("pet"))
            .andExpect(status().isOk())
            .andExpect(view().name("pets/createOrUpdatePetForm"));
    }

    @Test
    void testInitUpdateForm() throws Exception {
        mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("pet"))
            .andExpect(view().name("pets/createOrUpdatePetForm"));
    }

    @Test
    void testProcessUpdateFormSuccess() throws Exception {
        mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID)
            .param("name", "Betty")
            .param("type", "hamster")
            .param("birthDate", "2015/02/12")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/owners/{ownerId}"));
    }

    @Test
    void testProcessUpdateFormHasErrors() throws Exception {
        mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID)
            .param("name", "Betty")
            .param("birthDate", "2015/02/12")
        )
            .andExpect(model().attributeHasNoErrors("owner"))
            .andExpect(model().attributeHasErrors("pet"))
            .andExpect(status().isOk())
            .andExpect(view().name("pets/createOrUpdatePetForm"));
    }

    @Test
    void testProcessCreationFormWithValidMicrochipId() throws Exception {
        mockMvc.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_ID)
            .param("name", "Chipped")
            .param("type", "hamster")
            .param("birthDate", "2020/01/01")
            .param("microchipId", "123456789012345")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/owners/{ownerId}"));
    }

    @Test
    void testProcessCreationFormWithInvalidMicrochipId() throws Exception {
        mockMvc.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_ID)
            .param("name", "Chipped")
            .param("type", "hamster")
            .param("birthDate", "2020/01/01")
            .param("microchipId", "12345")
        )
            .andExpect(status().isOk())
            .andExpect(model().attributeHasFieldErrors("pet", "microchipId"))
            .andExpect(view().name("pets/createOrUpdatePetForm"));
    }

    @Test
    void testProcessCreationFormWithEmptyMicrochipId() throws Exception {
        mockMvc.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_ID)
            .param("name", "Unchipped")
            .param("type", "hamster")
            .param("birthDate", "2020/01/01")
            .param("microchipId", "")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/owners/{ownerId}"));
    }

    @Test
    void testProcessCreationFormWithDuplicateMicrochipId() throws Exception {
        willThrow(new DataIntegrityViolationException("duplicate"))
            .given(this.clinicService).savePet(any(Pet.class));

        mockMvc.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_ID)
            .param("name", "Duplicate")
            .param("type", "hamster")
            .param("birthDate", "2020/01/01")
            .param("microchipId", "123456789012345")
        )
            .andExpect(status().isOk())
            .andExpect(model().attributeHasFieldErrors("pet", "microchipId"))
            .andExpect(view().name("pets/createOrUpdatePetForm"));
    }

    // ---- Delete pet tests (8.2 – 8.5) ----

    @Test
    void testDeletePetSuccess() throws Exception {
        // 8.2: DELETE endpoint returns redirect to owner profile on success
        Pet pet = new Pet();
        pet.setId(TEST_PET_ID);
        given(this.clinicService.findPetById(TEST_PET_ID)).willReturn(pet);

        mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/delete", TEST_OWNER_ID, TEST_PET_ID))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/owners/" + TEST_OWNER_ID));

        verify(this.clinicService).deletePet(pet);
    }

    @Test
    void testDeletePetNotFound() throws Exception {
        // 8.3: DELETE endpoint returns redirect gracefully when pet does not exist
        given(this.clinicService.findPetById(TEST_PET_ID)).willReturn(null);

        mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/delete", TEST_OWNER_ID, TEST_PET_ID))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/owners/" + TEST_OWNER_ID));

        verify(this.clinicService, never()).deletePet(any(Pet.class));
    }

    @Test
    void testDeletePetRequiresPost() throws Exception {
        // 8.4: CSRF is enforced by Spring Security (not configured in this project).
        // This test verifies the endpoint is POST-only; a GET returns 405 Method Not Allowed.
        mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/delete", TEST_OWNER_ID, TEST_PET_ID))
            .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void testGetDeleteUrlDoesNotDelete() throws Exception {
        // 8.5: GET request to delete URL does not perform deletion
        mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/delete", TEST_OWNER_ID, TEST_PET_ID))
            .andExpect(status().isMethodNotAllowed());

        verify(this.clinicService, never()).deletePet(any(Pet.class));
    }

}
