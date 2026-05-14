/*
 * Copyright 2002-2024 the original author or authors.
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
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.samples.petclinic.service.PetTransferException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

/**
 * Controller for pet ownership transfer flow.
 * Routes: GET/POST /pets/{petId}/transfer and POST /pets/{petId}/transfer/confirm
 */
@Controller
@RequestMapping("/pets/{petId}/transfer")
public class PetTransferController {

    private static final String VIEW_TRANSFER_FORM    = "pets/transferForm";
    private static final String VIEW_TRANSFER_CONFIRM = "pets/transferConfirm";

    private final ClinicService clinicService;

    public PetTransferController(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * GET — show the transfer form pre-populated with pet and current owner.
     */
    @GetMapping
    public String showTransferForm(@PathVariable("petId") int petId, Model model) {
        Pet pet = clinicService.findPetById(petId);
        model.addAttribute("pet", pet);
        model.addAttribute("currentOwner", pet.getOwner());
        model.addAttribute("newOwnerId", "");
        return VIEW_TRANSFER_FORM;
    }

    /**
     * POST — validate owner selection and render confirmation page.
     */
    @PostMapping
    public String processTransferForm(@PathVariable("petId") int petId,
                                      @RequestParam(value = "newOwnerId", required = false, defaultValue = "") String newOwnerIdParam,
                                      @RequestParam(value = "lastName", required = false, defaultValue = "") String lastName,
                                      Model model) {
        Pet pet = clinicService.findPetById(petId);
        model.addAttribute("pet", pet);
        model.addAttribute("currentOwner", pet.getOwner());

        // Owner search by last name
        if (!lastName.isBlank()) {
            Collection<Owner> results = clinicService.findOwnerByLastName(lastName);
            model.addAttribute("ownerResults", results);
            model.addAttribute("searchLastName", lastName);
            return VIEW_TRANSFER_FORM;
        }

        // Proceed to confirmation if owner selected
        if (newOwnerIdParam == null || newOwnerIdParam.isBlank()) {
            model.addAttribute("transferError", "Please search for and select a new owner.");
            return VIEW_TRANSFER_FORM;
        }

        int newOwnerId;
        try {
            newOwnerId = Integer.parseInt(newOwnerIdParam);
        } catch (NumberFormatException e) {
            model.addAttribute("transferError", "Invalid owner selection.");
            return VIEW_TRANSFER_FORM;
        }

        // Self-transfer guard at controller level (service will re-validate)
        if (newOwnerId == pet.getOwner().getId()) {
            model.addAttribute("transferError", "The new owner must be different from the current owner.");
            return VIEW_TRANSFER_FORM;
        }

        Owner newOwner = clinicService.findOwnerById(newOwnerId);
        if (newOwner == null) {
            model.addAttribute("transferError", "Selected owner not found.");
            return VIEW_TRANSFER_FORM;
        }

        model.addAttribute("newOwner", newOwner);
        model.addAttribute("newOwnerId", newOwnerId);
        return VIEW_TRANSFER_CONFIRM;
    }

    /**
     * POST /confirm — execute the transfer and redirect on success.
     */
    @PostMapping("/confirm")
    public String confirmTransfer(@PathVariable("petId") int petId,
                                  @RequestParam("newOwnerId") int newOwnerId,
                                  Model model) {
        Pet pet = clinicService.findPetById(petId);
        // performed_by: no auth mechanism in app — use placeholder
        String performedBy = "staff";

        try {
            clinicService.transferPet(petId, newOwnerId, performedBy);
        } catch (PetTransferException e) {
            model.addAttribute("pet", pet);
            model.addAttribute("currentOwner", pet.getOwner());
            if (e.getReason() == PetTransferException.Reason.SELF_TRANSFER) {
                model.addAttribute("transferError", "The new owner must be different from the current owner.");
            } else if (e.getReason() == PetTransferException.Reason.PENDING_VISITS) {
                model.addAttribute("transferError", "This pet has pending or scheduled visits. Please resolve them before transferring.");
            } else {
                model.addAttribute("transferError", "Transfer could not be completed: " + e.getMessage());
            }
            return VIEW_TRANSFER_FORM;
        }

        // After transfer, redirect to new owner's page
        Owner newOwner = clinicService.findOwnerById(newOwnerId);
        return "redirect:/owners/" + newOwner.getId();
    }

}
