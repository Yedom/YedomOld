package ru.mralexeimk.yedom.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import ru.mralexeimk.yedom.database.entities.RoleEntity;
import ru.mralexeimk.yedom.database.entities.UserEntity;
import ru.mralexeimk.yedom.database.repositories.RolesRepository;
import ru.mralexeimk.yedom.models.User;

import java.util.*;

/**
 * Service for roles (user, moderator, admin)
 */
@Service
public class RolesService {
    private final RolesRepository rolesRepository;
    private final LogsService logsService;

    private final HashMap<String, Set<String>> permsOfRole = new HashMap<>();

    @Autowired
    public RolesService(RolesRepository rolesRepository, LogsService logsService) {
        this.rolesRepository = rolesRepository;
        this.logsService = logsService;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void init() {
        logsService.info("Service started: " + this.getClass().getSimpleName());
        List<String> roles = rolesRepository.findAllRoles();

        try {
            for (String role : roles) {
                RoleEntity roleEntity = rolesRepository.findByRole(role).get(0);
                permsOfRole.put(role, new HashSet<>(Arrays.stream(roleEntity.getPermissions().split(",")).toList()));
            }

            for (String role : roles) {
                RoleEntity roleEntity = rolesRepository.findByRole(role).get(0);
                String inherits = roleEntity.getInherits();
                if(inherits != null && !inherits.equals("")) {
                    for (String s : inherits.split(",")) {
                        permsOfRole.get(role).addAll(permsOfRole.get(s));
                    }
                }
            }
        } catch (Exception ex) {
            logsService.trace(ex.getMessage());
        }
    }

    public Set<String> getPermsOfRole(String role) {
        return permsOfRole.get(role);
    }

    public boolean hasPermission(String role, String permission) {
        return permsOfRole.get(role).contains(permission);
    }

    public boolean hasPermission(User user, String permission) {
        return permsOfRole.get(user.getRole()).contains(permission);
    }

    public boolean hasPermission(UserEntity userEntity, String permission) {
        return permsOfRole.get(userEntity.getRole()).contains(permission);
    }
}