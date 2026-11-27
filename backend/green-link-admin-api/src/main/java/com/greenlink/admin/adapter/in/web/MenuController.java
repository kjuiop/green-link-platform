package com.greenlink.admin.adapter.in.web;

import com.greenlink.admin.adapter.in.web.dto.CreateMenuRequest;
import com.greenlink.admin.adapter.in.web.dto.UpdateMenuRequest;
import com.greenlink.core.application.menu.dto.MenuResult;
import com.greenlink.core.application.menu.usecase.CreateMenuUseCase;
import com.greenlink.core.application.menu.usecase.DeleteMenuUseCase;
import com.greenlink.core.application.menu.usecase.GetAllMenusUseCase;
import com.greenlink.core.application.menu.usecase.GetMenusByRoleUseCase;
import com.greenlink.core.application.menu.usecase.UpdateMenuUseCase;
import com.greenlink.core.domain.admin.model.AdminRole;
import com.greenlink.core.domain.company.model.CompanyType;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.auth.AuthErrorCode;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/menus")
public class MenuController {

    private final GetAllMenusUseCase getAllMenusUseCase;
    private final GetMenusByRoleUseCase getMenusByRoleUseCase;
    private final CreateMenuUseCase createMenuUseCase;
    private final UpdateMenuUseCase updateMenuUseCase;
    private final DeleteMenuUseCase deleteMenuUseCase;

    public MenuController(GetAllMenusUseCase getAllMenusUseCase,
                          GetMenusByRoleUseCase getMenusByRoleUseCase,
                          CreateMenuUseCase createMenuUseCase,
                          UpdateMenuUseCase updateMenuUseCase,
                          DeleteMenuUseCase deleteMenuUseCase) {
        this.getAllMenusUseCase = getAllMenusUseCase;
        this.getMenusByRoleUseCase = getMenusByRoleUseCase;
        this.createMenuUseCase = createMenuUseCase;
        this.updateMenuUseCase = updateMenuUseCase;
        this.deleteMenuUseCase = deleteMenuUseCase;
    }

    @GetMapping
    public ResponseEntity<List<MenuResult>> getAll() {
        return ResponseEntity.ok(getAllMenusUseCase.getAllMenus());
    }

    @GetMapping("/by-role")
    public ResponseEntity<List<MenuResult>> getByRole(Authentication authentication) {
        Set<String> authorities = authentication.getAuthorities().stream()
            .map(a -> a.getAuthority().replace("ROLE_", ""))
            .collect(Collectors.toSet());
        AdminRole role = Arrays.stream(AdminRole.values())
            .filter(r -> authorities.contains(r.name()))
            .findFirst()
            .orElseThrow(() -> new BusinessException(AuthErrorCode.INVALID_ROLE));
        Set<CompanyType> capabilities = EnumSet.noneOf(CompanyType.class);
        if (authorities.contains(CompanyType.ORDERING.name())) capabilities.add(CompanyType.ORDERING);
        if (authorities.contains(CompanyType.VENDOR.name())) capabilities.add(CompanyType.VENDOR);
        return ResponseEntity.ok(getMenusByRoleUseCase.getMenusByRole(role, capabilities));
    }

    @PostMapping
    public ResponseEntity<MenuResult> create(@RequestBody @Valid CreateMenuRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createMenuUseCase.createMenu(req.toCommand()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MenuResult> update(@PathVariable Long id,
                                              @RequestBody @Valid UpdateMenuRequest req) {
        return ResponseEntity.ok(updateMenuUseCase.updateMenu(id, req.toCommand()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteMenuUseCase.deleteMenu(id);
        return ResponseEntity.noContent().build();
    }
}
