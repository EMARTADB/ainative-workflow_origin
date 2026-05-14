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

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetPhoto;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.samples.petclinic.service.PetPhotoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.dao.DataIntegrityViolationException;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 */
@Controller
@RequestMapping("/owners/{ownerId}")
public class PetController {

    private static final String VIEWS_PETS_CREATE_OR_UPDATE_FORM = "pets/createOrUpdatePetForm";
    private static final long MAX_PHOTO_SIZE = 2L * 1024 * 1024; // 2 MB
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList("image/jpeg", "image/png");

    private final ClinicService clinicService;
    private final PetPhotoService petPhotoService;

    public PetController(ClinicService clinicService, PetPhotoService petPhotoService) {
        this.clinicService = clinicService;
        this.petPhotoService = petPhotoService;
    }

    @ModelAttribute("types")
    public Collection<PetType> populatePetTypes() {
        return this.clinicService.findPetTypes();
    }

    @ModelAttribute("owner")
    public Owner findOwner(@PathVariable("ownerId") int ownerId) {
        return this.clinicService.findOwnerById(ownerId);
    }

    @InitBinder("owner")
    public void initOwnerBinder(WebDataBinder dataBinder) {
        dataBinder.setDisallowedFields("id");
    }

    @InitBinder("pet")
    public void initPetBinder(WebDataBinder dataBinder) {
        dataBinder.setValidator(new PetValidator());
        dataBinder.registerCustomEditor(String.class, "microchipId", new StringTrimmerEditor(true));
    }

    @GetMapping(value = "/pets/new")
    public String initCreationForm(Owner owner, ModelMap model) {
        Pet pet = new Pet();
        owner.addPet(pet);
        model.put("pet", pet);
        return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
    }

    @PostMapping(value = "/pets/new")
    public String processCreationForm(Owner owner, @Valid Pet pet, BindingResult result, ModelMap model) {
        if (StringUtils.hasLength(pet.getName()) && pet.isNew() && owner.getPet(pet.getName(), true) != null){
            result.rejectValue("name", "duplicate", "already exists");
        }
        if (result.hasErrors()) {
            model.put("pet", pet);
            return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
        }

        owner.addPet(pet);
        try {
            this.clinicService.savePet(pet);
        } catch (DataIntegrityViolationException e) {
            result.rejectValue("microchipId", "duplicate", "This microchip ID is already registered to another pet");
            model.put("pet", pet);
            return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
        }
        return "redirect:/owners/{ownerId}";
    }

    @GetMapping(value = "/pets/{petId}/edit")
    public String initUpdateForm(@PathVariable("petId") int petId, ModelMap model) {
        Pet pet = this.clinicService.findPetById(petId);
        model.put("pet", pet);
        return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
    }

    @PostMapping(value = "/pets/{petId}/edit")
    public String processUpdateForm(@Valid Pet pet, BindingResult result, Owner owner, ModelMap model) {
        if (result.hasErrors()) {
            model.put("pet", pet);
            return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
        }

        owner.addPet(pet);
        try {
            this.clinicService.savePet(pet);
        } catch (DataIntegrityViolationException e) {
            result.rejectValue("microchipId", "duplicate", "This microchip ID is already registered to another pet");
            model.put("pet", pet);
            return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
        }
        return "redirect:/owners/{ownerId}";
    }

    @PostMapping(value = "/pets/{petId}/photo")
    public String uploadPhoto(@PathVariable("ownerId") int ownerId,
                              @PathVariable("petId") int petId,
                              @RequestParam("photo") MultipartFile photo,
                              ModelMap model) throws IOException {
        if (photo.isEmpty()) {
            model.put("photoError", "Please select a file to upload.");
            model.put("pet", clinicService.findPetById(petId));
            return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
        }
        String contentType = photo.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            model.put("photoError", "Only JPEG and PNG files are allowed.");
            model.put("pet", clinicService.findPetById(petId));
            return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
        }
        if (photo.getSize() > MAX_PHOTO_SIZE) {
            model.put("photoError", "File size must not exceed 2 MB.");
            model.put("pet", clinicService.findPetById(petId));
            return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
        }
        petPhotoService.save(petId, photo.getBytes(), contentType);
        return "redirect:/owners/{ownerId}";
    }

    @PostMapping(value = "/pets/{petId}/photo/delete")
    public String deletePhoto(@PathVariable("petId") int petId) {
        petPhotoService.deleteByPetId(petId);
        return "redirect:/owners/{ownerId}";
    }

    @GetMapping(value = "/pets/{petId}/photo")
    public void servePhoto(@PathVariable("petId") int petId,
                           HttpServletResponse response) throws IOException {
        Optional<PetPhoto> photo = petPhotoService.findByPetId(petId);
        if (photo.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        response.setContentType(photo.get().getContentType());
        response.setContentLength(photo.get().getContent().length);
        response.getOutputStream().write(photo.get().getContent());
    }

}
