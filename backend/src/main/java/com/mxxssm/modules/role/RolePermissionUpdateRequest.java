package com.mxxssm.modules.role;

import jakarta.validation.constraints.NotNull;
import java.util.Set;

public record RolePermissionUpdateRequest(@NotNull Set<Long> permissionIds) {
}
