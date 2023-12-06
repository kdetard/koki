package io.github.kdetard.koki.openremote.models;

import java.util.Collections;
import java.util.List;

public class Realm {
    public String id = null;
    public String name = null;
    public String displayName = null;
    public boolean enabled = true;
    public double notBefore;
    public boolean resetPasswordAllowed = true;
    public boolean duplicateEmailsAllowed = false;
    public boolean rememberMe = true;
    public boolean registrationAllowed = true;
    public boolean registrationEmailAsUsername  = true;
    public boolean verifyEmail = false;
    public boolean loginWithEmail = true;
    public String loginTheme = null;
    public String accountTheme = null;
    public String adminTheme = null;
    public String emailTheme = null;
    public int accessTokenLifespan;
    public List<RealmRole> realmRoles = Collections.emptyList();
}
