/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cl.usach.managedbeans;

import cl.usach.entities.RolUsuario;
import cl.usach.sessionbeans.RolUsuarioFacadeLocal;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

/**
 *
 * @author FGT
 */
@Named(value = "rolUsuarioManagedBean")
@RequestScoped
public class RolUsuarioManagedBean {
    @EJB
    private RolUsuarioFacadeLocal rolUsuarioFacade;
    
    private Integer idRolUsuario;
    private String nombreRolUsuario;
    private String permisosRolUsuario;
    private List<RolUsuario> rolesUsuario;
    
    /**
     * Creates a new instance of RolUsuarioManagedBean
     */
    public RolUsuarioManagedBean() {
    }
    
    @PostConstruct
    public void init(){
        rolesUsuario = rolUsuarioFacade.findAll();
    }

    public Integer getIdRolUsuario() {
        return idRolUsuario;
    }

    public void setIdRolUsuario(Integer idRolUsuario) {
        this.idRolUsuario = idRolUsuario;
    }

    public String getNombreRolUsuario() {
        return nombreRolUsuario;
    }

    public void setNombreRolUsuario(String nombreRolUsuario) {
        this.nombreRolUsuario = nombreRolUsuario;
    }

    public String getPermisosRolUsuario() {
        return permisosRolUsuario;
    }

    public void setPermisosRolUsuario(String permisosRolUsuario) {
        this.permisosRolUsuario = permisosRolUsuario;
    }

    public List<RolUsuario> getRolesUsuario() {
        return rolesUsuario;
    }

    public void setRolesUsuario(List<RolUsuario> rolesUsuario) {
        this.rolesUsuario = rolesUsuario;
    }
    
}
