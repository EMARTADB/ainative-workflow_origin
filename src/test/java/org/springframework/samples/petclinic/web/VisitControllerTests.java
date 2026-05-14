package org.springframework.samples.petclinic.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.util.AopTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for {@link VisitController}
 *
 * @author Colin But
 */
@SpringJUnitWebConfig(locations = {"classpath:spring/mvc-test-config.xml", "classpath:spring/mvc-core-config.xml" })
class VisitControllerTests {

    private static final int TEST_PET_ID = 1;

    @Autowired
    private VisitController visitController;

    @Autowired
    private ClinicService clinicService;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        // Reset mock state so that stubs from one test don't bleed into the next
        ClinicService mockTarget = AopTestUtils.getTargetObject(this.clinicService);
        reset(mockTarget);

        this.mockMvc = MockMvcBuilders.standaloneSetup(visitController).build();

        given(this.clinicService.findPetById(TEST_PET_ID)).willReturn(new Pet());
        given(this.clinicService.findVets()).willReturn(List.of());
    }

    @Test
    void testInitNewVisitForm() throws Exception {
        mockMvc.perform(get("/owners/*/pets/{petId}/visits/new", TEST_PET_ID))
            .andExpect(status().isOk())
            .andExpect(view().name("pets/createOrUpdateVisitForm"));
    }

    @Test
    void testProcessNewVisitFormSuccess() throws Exception {
        mockMvc.perform(post("/owners/*/pets/{petId}/visits/new", TEST_PET_ID)
            .param("name", "George")
            .param("description", "Visit Description")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/owners/{ownerId}"));
    }

    @Test
    void testProcessNewVisitFormHasErrors() throws Exception {
        mockMvc.perform(post("/owners/*/pets/{petId}/visits/new", TEST_PET_ID)
            .param("name", "George")
        )
            .andExpect(model().attributeHasErrors("visit"))
            .andExpect(status().isOk())
            .andExpect(view().name("pets/createOrUpdateVisitForm"));
    }

    @Test
    void testShowVisits() throws Exception {
        mockMvc.perform(get("/owners/*/pets/{petId}/visits", TEST_PET_ID))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("visits"))
            .andExpect(view().name("visitList"));
    }

    @Test
    void testInitNewVisitForm_populatesVetList() throws Exception {
        Vet vet1 = new Vet();
        vet1.setId(1);
        vet1.setFirstName("James");
        vet1.setLastName("Carter");
        Vet vet2 = new Vet();
        vet2.setId(2);
        vet2.setFirstName("Helen");
        vet2.setLastName("Leary");
        given(this.clinicService.findVets()).willReturn(List.of(vet1, vet2));

        mockMvc.perform(get("/owners/*/pets/{petId}/visits/new", TEST_PET_ID))
            .andExpect(status().isOk())
            .andExpect(model().attribute("vets", notNullValue()))
            .andExpect(model().attribute("vets", hasSize(2)))
            .andExpect(view().name("pets/createOrUpdateVisitForm"));
    }

    @Test
    void testProcessNewVisitForm_withVet() throws Exception {
        Vet vet = new Vet();
        vet.setId(1);
        vet.setFirstName("James");
        vet.setLastName("Carter");
        given(this.clinicService.findVets()).willReturn(List.of(vet));

        mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", 1, TEST_PET_ID)
            .param("description", "Visit with vet")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/owners/{ownerId}"));

        verify(this.clinicService).saveVisit(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void testProcessNewVisitForm_withoutVet() throws Exception {
        given(this.clinicService.findVets()).willReturn(List.of());

        mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", 1, TEST_PET_ID)
            .param("description", "Visit without vet")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/owners/{ownerId}"));

        verify(this.clinicService).saveVisit(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void testInitNewVisitForm_vetListFetchFailure() throws Exception {
        given(this.clinicService.findVets()).willThrow(new DataAccessException("DB error") {});

        // DataAccessException from @ModelAttribute propagates out of standalone MockMvc
        // as a NestedServletException — asserting it is thrown confirms propagation (not caught by controller)
        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () ->
            mockMvc.perform(get("/owners/*/pets/{petId}/visits/new", TEST_PET_ID))
        );
    }

}

