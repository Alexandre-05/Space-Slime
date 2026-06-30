package fr.alex96x2.admin.api.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "mutes")
public class MuteEntity extends SanctionEntity {}
