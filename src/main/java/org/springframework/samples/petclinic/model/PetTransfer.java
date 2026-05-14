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
package org.springframework.samples.petclinic.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Immutable audit record for a pet ownership transfer.
 * No delete path is intentionally exposed.
 */
@Entity
@Table(name = "pet_transfers")
public class PetTransfer extends BaseEntity {

    @Column(name = "pet_id", nullable = false)
    private int petId;

    @Column(name = "from_owner_id", nullable = false)
    private int fromOwnerId;

    @Column(name = "to_owner_id", nullable = false)
    private int toOwnerId;

    @Column(name = "transferred_at", nullable = false)
    private LocalDateTime transferredAt;

    @Column(name = "performed_by", nullable = false)
    private String performedBy;

    public int getPetId() {
        return petId;
    }

    public void setPetId(int petId) {
        this.petId = petId;
    }

    public int getFromOwnerId() {
        return fromOwnerId;
    }

    public void setFromOwnerId(int fromOwnerId) {
        this.fromOwnerId = fromOwnerId;
    }

    public int getToOwnerId() {
        return toOwnerId;
    }

    public void setToOwnerId(int toOwnerId) {
        this.toOwnerId = toOwnerId;
    }

    public LocalDateTime getTransferredAt() {
        return transferredAt;
    }

    public void setTransferredAt(LocalDateTime transferredAt) {
        this.transferredAt = transferredAt;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

}
