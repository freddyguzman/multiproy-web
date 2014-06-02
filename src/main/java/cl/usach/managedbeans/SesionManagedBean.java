/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cl.usach.managedbeans;

import cl.usach.entities.Usuario;
import cl.usach.sessionbeans.UsuarioFacadeLocal;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author FGT
 */
@Named(value = "sesionManagedBean")
@ViewScoped
public class SesionManagedBean implements Serializable{
    @EJB
    private UsuarioFacadeLocal usuarioFacade;

    private String username;
    
    private static final Logger log = Logger.getLogger(SesionManagedBean.class.getName());
    
    public SesionManagedBean() {
    }
    
    @PostConstruct
    public void init(){
        username = getUser();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    
    public Boolean checkUserAdmin(){
        FacesContext context = FacesContext.getCurrentInstance();
        return context.getExternalContext().isUserInRole("admin");
    }
    
    public Boolean checkUserProfesor(){
        FacesContext context = FacesContext.getCurrentInstance();
        return context.getExternalContext().isUserInRole("profesor");
    }
    
    public Boolean checkUserAlumno(){
        FacesContext context = FacesContext.getCurrentInstance();
        return context.getExternalContext().isUserInRole("alumno");
    }
    
    public Boolean checkUserAdminAndProfesor(){
        FacesContext context = FacesContext.getCurrentInstance();        
        return context.getExternalContext().isUserInRole("admin") || context.getExternalContext().isUserInRole("profesor");
    }
    
    public Boolean checkUserAdminAndAlumno(){
        FacesContext context = FacesContext.getCurrentInstance();        
        return context.getExternalContext().isUserInRole("admin") || context.getExternalContext().isUserInRole("alumno");
    }
    
    public String logout(){
        String destino = "/global/index?faces-redirect=true";
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        
        try {
            request.logout();
        } catch (ServletException e) {
            log.log(Level.SEVERE, "Error en logout user", e);
            destino = "/errorLogin?faces-redirect=true";
        }
        
        return destino;
    }
    
    public String getUser(){
        String login = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
        Usuario usuario = usuarioFacade.buscarPorLogin(login);
        return usuario.getNombreUsuario();        
    }    
}
