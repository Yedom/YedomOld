package ru.mralexeimk.yedom.utils.services;

import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import ru.mralexeimk.yedom.database.entities.RoleEntity;
import ru.mralexeimk.yedom.database.entities.UserEntity;
import ru.mralexeimk.yedom.interfaces.repositories.RoleRepository;
import ru.mralexeimk.yedom.models.User;

import java.util.*;

@Service
public class RolesService {
    private final RoleRepository roleRepository;

    private final HashMap<String, Set<String>> permsOfRole = new HashMap<>();

    public RolesService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void init() {
        System.out.println("Loading permissions...");
        List<String> roles = roleRepository.findAllRoles();
        System.out.println("Found roles: " + roles);

        try {
            for (String role : roles) {
                RoleEntity roleEntity = roleRepository.findByRole(role).get(0);
                permsOfRole.put(role, new HashSet<>(Arrays.stream(roleEntity.getPermissions().split(",")).toList()));
            }

            for (String role : roles) {
                RoleEntity roleEntity = roleRepository.findByRole(role).get(0);
                String inherits = roleEntity.getInherits();
                if(inherits != null && !inherits.equals("")) {
                    for (String s : inherits.split(",")) {
                        permsOfRole.get(role).addAll(permsOfRole.get(s));
                    }
                }
                System.out.println(role + " -> " + permsOfRole.get(role));
            }
        } catch (Exception ex) {
            System.out.println("Error while loading permissions");
            ex.printStackTrace();
            return;
        }
        System.out.println("Permissions loaded!");
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
