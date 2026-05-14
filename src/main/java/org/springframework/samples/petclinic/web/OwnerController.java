/*
 * Copyright 2002-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.web;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.samples.petclinic.service.PetPhotoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 */
@Controller
public class OwnerController {

    private static final String VIEWS_OWNER_CREATE_OR_UPDATE_FORM = "owners/createOrUpdateOwnerForm";
    private final ClinicService clinicService;
    private final PetPhotoService petPhotoService;

    public OwnerController(ClinicService clinicService, PetPhotoService petPhotoService) {
        this.clinicService = clinicService;
        this.petPhotoService = petPhotoService;
    }

    @InitBinder
    public void setAllowedFields(WebDataBinder dataBinder) {
        dataBinder.setDisallowedFields("id");
    }

    @GetMapping(value = "/owners/new")
    public String initCreationForm(Map<String, Object> model) {
        Owner owner = new Owner();
        model.put("owner", owner);
        return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
    }

    @PostMapping(value = "/owners/new")
    public String processCreationForm(@Valid Owner owner, BindingResult result) {
        if (result.hasErrors()) {
            return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
        }

        this.clinicService.saveOwner(owner);
        return "redirect:/owners/" + owner.getId();
    }

    @GetMapping(value = "/owners/find")
    public String initFindForm(Map<String, Object> model) {
        model.put("owner", new Owner());
        return "owners/findOwners";
    }

    @GetMapping(value = "/owners")
    public String processFindForm(Owner owner, BindingResult result, Map<String, Object> model) {

        // if a microchip ID is provided, search by microchip first
        String microchipId = owner.getMicrochipId();
        if (microchipId != null && !microchipId.isBlank()) {
            if (!microchipId.matches("^\\d{15}$")) {
                result.rejectValue("microchipId", "invalidFormat", "Microchip ID must be exactly 15 digits");
                return "owners/findOwners";
            }
            Owner found = this.clinicService.findOwnerByPetMicrochipId(microchipId);
            if (found == null) {
                result.rejectValue("microchipId", "notFound", "not found");
                return "owners/findOwners";
            }
            return "redirect:/owners/" + found.getId();
        }

        // allow parameterless GET request for /owners to return all records
        if (owner.getLastName() == null) {
            owner.setLastName(""); // empty string signifies broadest possible search
        }

        // find owners by last name
        Collection<Owner> results = this.clinicService.findOwnerByLastName(owner.getLastName());
        if (results.isEmpty()) {
            // no owners found
            result.rejectValue("lastName", "notFound", "not found");
            return "owners/findOwners";
        } else if (results.size() == 1) {
            // 1 owner found
            owner = results.iterator().next();
            return "redirect:/owners/" + owner.getId();
        } else {
            // multiple owners found
            model.put("selections", results);
            return "owners/ownersList";
        }
    }

    @GetMapping(value = "/owners/{ownerId}/edit")
    public String initUpdateOwnerForm(@PathVariable("ownerId") int ownerId, Model model) {
        Owner owner = this.clinicService.findOwnerById(ownerId);
        model.addAttribute(owner);
        return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
    }

    @PostMapping(value = "/owners/{ownerId}/edit")
    public String processUpdateOwnerForm(@Valid Owner owner, BindingResult result, @PathVariable("ownerId") int ownerId) {
        if (result.hasErrors()) {
            return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
        }

        owner.setId(ownerId);
        this.clinicService.saveOwner(owner);
        return "redirect:/owners/{ownerId}";
    }

    /**
     * Custom handler for displaying an owner.
     *
     * @param ownerId the ID of the owner to display
     * @return a ModelMap with the model attributes for the view
     */
    @GetMapping("/owners/{ownerId}")
    public ModelAndView showOwner(@PathVariable("ownerId") int ownerId) {
        Owner owner = this.clinicService.findOwnerById(ownerId);
        ModelAndView mav = new ModelAndView("owners/ownerDetails");
        mav.addObject(owner);
        // Collect the pet IDs that have a photo, so the view can conditionally render the image
        Set<Integer> petPhotoIds = owner.getPets().stream()
            .filter(pet -> petPhotoService.findByPetId(pet.getId()).isPresent())
            .map(pet -> pet.getId())
            .collect(Collectors.toSet());
        mav.addObject("petPhotoIds", petPhotoIds);
        return mav;
    }

}
